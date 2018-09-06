package tester;

import tester.model.Properties;
import tester.model.Request;
import tester.model.Scenarios;
import tester.service.ParserConfig;
import tester.service.RequestService;
import tester.service.ScenarioService;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        //"C:\work\test-rest\properties.json" "C:\work\test-rest\wiki.get.request.json"
//        String propertiesFile = args[0];
//        String requestFile = args[1];

        String propertiesFile = "C:\\work\\test-rest\\properties.json";
        String requestFile = "C:\\work\\test-rest\\wiki.get.request.json";
        String scenarionFile = "C:\\work\\test-rest\\download.scenario.json";

//        req(propertiesFile, requestFile);
        scenario(propertiesFile, scenarionFile);


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
