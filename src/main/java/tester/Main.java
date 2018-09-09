package tester;

import tester.model.Properties;
import tester.model.Request;
import tester.model.Scenarios;
import tester.service.ParserConfig;
import tester.service.PropertiesService;
import tester.service.RequestService;
import tester.service.ScenarioService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class Main {
    public static void main(String[] args) throws IOException {
        String propertiesFile = "C:\\work\\test-rest\\properties.json";

        Properties commonProperties = ParserConfig.getCommonProperties(propertiesFile);
        PropertiesService propertiesService = new PropertiesService(commonProperties);
        propertiesService.addLogAppender();

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
}
