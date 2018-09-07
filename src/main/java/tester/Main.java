package tester;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import tester.model.Properties;
import tester.model.Request;
import tester.model.Scenarios;
import tester.service.ParserConfig;
import tester.service.RequestService;
import tester.service.ScenarioService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class Main {
    public static void main(String[] args) throws IOException {
        String propertiesFile = "C:\\work\\test-rest\\properties.json";
        logAppender();

        Properties commonProperties = ParserConfig.getCommonProperties(propertiesFile);
        List<String> requestsFiles = getAllFile(commonProperties.getRequestsFolder());
        requestsFiles.addAll(commonProperties.getRequestList());

        List<String> scenarioFiles = getAllFile(commonProperties.getScenariosFolder());
        scenarioFiles.addAll(commonProperties.getScenarioList());

        for (String requestFile : requestsFiles) {
            req(commonProperties, requestFile);
        }
        for (String scenarioFile : scenarioFiles) {
            scenario(commonProperties, scenarioFile);
        }


    }

    private static List<String> getAllFile(String requestsFolder) throws IOException {
        if (requestsFolder == null || requestsFolder.isEmpty()){
            return new ArrayList<>();
        }
        try(Stream<Path> pathStream = Files.walk(Paths.get(requestsFolder))){
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(toList());
        }
    }

    private static void scenario(Properties properties, String requestFile) throws IOException {
        Scenarios scenario = ParserConfig.getScenariosSortByID(requestFile);
        ScenarioService scenarioService = new ScenarioService(scenario, properties);
        scenarioService.execute();
    }

    private static void req(Properties properties, String requestFile) throws IOException {
        Request request = ParserConfig.getRequest(requestFile);
        RequestService requestService = new RequestService(request, properties);
        requestService.execute();
    }

    private static void logAppender(){
        FileAppender fileAppender = new FileAppender();
        fileAppender.setName("log");
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy/hh.mm.ss");
        String fileName = "./logs/" + format.format(new Date()) + ".log";
        fileAppender.setFile(fileName);
        fileAppender.setThreshold(Level.INFO);
        fileAppender.setAppend(true);
        fileAppender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));
        fileAppender.activateOptions();
        Logger.getRootLogger().addAppender(fileAppender);
    }
}
