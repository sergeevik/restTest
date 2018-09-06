package tester.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ScenarioServiceTest {
    @Test
    public void parseEL() throws Exception {
        final String EXPECTED_EASY = "test parse operation id = 12345";
        final String EXPECTED_HARD = "test12345";

        ScenarioService scenarioService = new ScenarioService(null, null);
        HashMap<String, Object> operation = new HashMap<>();
        operation.put("id", "12345");
        operation.put("valueT", "test");
        HashMap<String, Object> firstStep = new HashMap<>();
        firstStep.put("operation", operation);
        firstStep.put("test", "test");

        HashMap<Integer, HashMap<String, Object>> values = new HashMap<>();
        values.put(1, firstStep);
        FieldUtils.writeDeclaredField(scenarioService, "values", values, true);


        String easy = scenarioService.parseEL("#1.operation.valueT# parse operation id = #1.operation.id#");
        assertEquals(EXPECTED_EASY, easy);

        String hard = scenarioService.parseEL("#1.operation.valueT##1.operation.id#");
        assertEquals(EXPECTED_HARD, hard);


    }

}