package tester.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ElParserTest {
    @Test
    public void parseELByStepId() {
        final String EXPECTED_EASY = "test parse operation id = 12345";
        final String EXPECTED_HARD = "test12345";

        HashMap<String, Object> operation = new HashMap<>();
        operation.put("id", "12345");
        operation.put("valueT", "test");
        HashMap<String, Object> firstStep = new HashMap<>();
        firstStep.put("operation", operation);
        firstStep.put("test", "test");

        HashMap<Integer, HashMap<String, Object>> values = new HashMap<>();
        values.put(1, firstStep);

        ElParser elParser = new ElParser();
        String easy = elParser.parseELByStepId("#1.operation.valueT# parse operation id = #1.operation.id#", values);
        assertEquals(EXPECTED_EASY, easy);

        String hard = elParser.parseELByStepId("#1.operation.valueT##1.operation.id#", values);
        assertEquals(EXPECTED_HARD, hard);


    }

}