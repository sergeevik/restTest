package tester.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import tester.model.Properties;
import tester.model.Request;
import tester.model.Scenario;
import tester.model.Scenarios;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class ParserConfig {
    public static Properties getCommonProperties(String fileUrl) throws IOException {
        return readValue(fileUrl, Properties.class);
    }

    public static Request getRequest(String fileUrl) throws IOException {
        return readValue(fileUrl, Request.class);
    }

    public static Scenarios getScenariosSortByID(String fileUrl) throws IOException {
        Scenarios scenarios = readValue(fileUrl, Scenarios.class);
        scenarios.getScenarios().sort(Comparator.comparingInt(Scenario::getStepId));
        return scenarios;
    }

    private static  <T> T readValue(String fileUrl, Class<T> tClass) throws IOException {
        File propertiesFile = new File(fileUrl);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(propertiesFile, tClass);
    }
}
