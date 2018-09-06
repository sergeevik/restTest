package tester;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class Main {
    public static void main(String[] args) throws IOException {
        String propertiesFile = "C:\\work\\test-rest\\properties.json";

        Properties commonProperties = ParserConfig.getCommonProperties(propertiesFile);
        List<String> requestsFiles = getAllFile(commonProperties.getRequestsFolder());
        requestsFiles.addAll(commonProperties.getRequestList());

        List<String> scenarioFiles = getAllFile(commonProperties.getScenariosFolder());
        scenarioFiles.addAll(commonProperties.getScenarioList());

        for (String requestFile : requestsFiles) {
            req(propertiesFile, requestFile);
        }
        for (String scenarioFile : scenarioFiles) {
            scenario(propertiesFile, scenarioFile);
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

    private static void scenario(String propertiesFile, String requestFile) throws IOException {
        Scenarios scenario = ParserConfig.getScenariosSortByID(requestFile);
        Properties commonProperties = ParserConfig.getCommonProperties(propertiesFile);
        ScenarioService scenarioService = new ScenarioService(scenario, commonProperties);
        scenarioService.execute();
    }

    private static void req(String propertiesFile, String requestFile) throws IOException {
        Request request = ParserConfig.getRequest(requestFile);
        Properties commonProperties = ParserConfig.getCommonProperties(propertiesFile);
        RequestService requestService = RequestService.RequestServiceBuilder.builder()
                .withRequest(request)
                .withProperties(commonProperties)
                .build();
        requestService.execute();
    }
}
