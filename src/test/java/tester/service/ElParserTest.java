package tester.service;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ElParserTest {
    private ElParser elParser;

    @Before
    public void setUp() {
        elParser = new ElParser();
    }

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

        String easy = elParser.parseELByStepId("#1.operation.valueT# parse operation id = #1.operation.id#", values);
        assertEquals(EXPECTED_EASY, easy);

        String hard = elParser.parseELByStepId("#1.operation.valueT##1.operation.id#", values);
        assertEquals(EXPECTED_HARD, hard);
    }

    @Test
    public void whenEmptyTextPassToParseELInAnswerThenReturnEmptyText() {
        String emptyText = "";
        String actualReturn = elParser.parseELInAnswer(emptyText, new HashMap<>());
        assertThat(actualReturn)
                .isNotNull()
                .isEqualTo(emptyText);
    }

    @Test
    public void whenNullTextPassToParseELInAnswerThenReturnNull() {
        String actualReturn = elParser.parseELInAnswer(null, new HashMap<>());
        assertThat(actualReturn)
                .isNull();
    }

    @Test
    public void whenEmptyTextPassToParseELByStepIdThenReturnEmptyText() {
        String emptyText = "";
        String actualReturn = elParser.parseELByStepId(emptyText, new HashMap<>());
        assertThat(actualReturn)
                .isNotNull()
                .isEqualTo(emptyText);
    }

    @Test
    public void whenNullTextPassToParseELByStepIdThenReturnNull() {
        String actualReturn = elParser.parseELByStepId(null, new HashMap<>());
        assertThat(actualReturn)
                .isNull();
    }

    @Test
    public void whenTextWithoutElPassToParseELInAnswerThenReturnOriginText() {
        String textWithoutEl = "test without el (#)";
        String actualReturn = elParser.parseELInAnswer(textWithoutEl, new HashMap<>());
        assertThat(actualReturn)
                .isNotNull()
                .isEqualTo(textWithoutEl);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTextWithElNotContainInMapPassToParseELInAnswerThenThrowException() {
        String textWithEl = "test with el (#exception#)";
        elParser.parseELInAnswer(textWithEl, new HashMap<>());
    }

    @Test
    public void whenTextWithElContainInMapPassToParseELInAnswerThenSuccessReplace() {

        String replaceKey = "success";
        String replaceValue = "success Replace";

        String textWithEl = "test with el (#" + replaceKey + "#)";
        String textWithElReplace = "test with el (" + replaceValue + ")";
        HashMap<String, Object> answer = new HashMap<>();
        answer.put(replaceKey, replaceValue);

        String actualReturn = elParser.parseELInAnswer(textWithEl, answer);
        assertThat(actualReturn)
                .isNotNull()
                .isEqualTo(textWithElReplace);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTextWithElContainInMapAndValueNotStringPassToParseELInAnswerThenThrowException() {
        String textWithEl = "test with el (#exception#)";
        HashMap<String, Object> answer = new HashMap<>();
        answer.put("exception", new HashMap<>());
        elParser.parseELInAnswer(textWithEl, answer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTextWithElContainInMapAndValueNotStringPassToParseELByStepIdThenThrowException() {
        String textWithEl = "test with el (#1.exception#)";
        HashMap<String, Object> answer = new HashMap<>();
        answer.put("exception", new HashMap<>());
        HashMap<Integer, HashMap<String, Object>> stepMap = new HashMap<>();
        stepMap.put(1, answer);
        elParser.parseELByStepId(textWithEl, stepMap);
    }

}