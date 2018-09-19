package tester.service;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import tester.model.ExpectedResult;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static tester.service.MockTemplate.mockResponse;

@SuppressWarnings("unchecked")
public class ExpectedResultServiceTest {
    private static final String KEY = "#key#";
    private static final String VALUE = "value";
    private ExpectedResultService service;
    private ElParser mock;

    @Before
    public void setUp() throws Exception {
        service = new ExpectedResultService();
        mock = mock(ElParser.class);
        FieldUtils.writeDeclaredField(service, "parser", mock, true);
    }

    @Test
    public void checkEqualTrueReturnTrueWhenValueEqual() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey(KEY)
                .withEqual(true)
                .withValue(VALUE);
        when(mock.parseELInAnswer(eq(KEY), any(HashMap.class)))
                .thenReturn(VALUE);
        Response respMock = mockResponse();
        boolean check = service.check(expectedResult, respMock);
        assertTrue(check);
    }

    @Test
    public void checkEqualFalseReturnTrueWhenValueNotEqual() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey(KEY)
                .withEqual(false)
                .withValue(VALUE);
        when(mock.parseELInAnswer(eq(KEY), any(HashMap.class)))
                .thenReturn("not value");
        Response respMock = mockResponse();
        boolean check = service.check(expectedResult, respMock);
        assertTrue(check);
    }

    @Test
    public void checkEqualTrueReturnFalseWhenValueNotEqual() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey(KEY)
                .withEqual(true)
                .withValue(VALUE);
        when(mock.parseELInAnswer(eq(KEY), any(HashMap.class)))
                .thenReturn("not Equal");
        Response respMock = mockResponse();
        boolean check = service.check(expectedResult, respMock);
        assertFalse(check);
    }

    @Test
    public void checkEqualFalseReturnFalseWhenValueEqual() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey(KEY)
                .withEqual(false)
                .withValue(VALUE);
        when(mock.parseELInAnswer(eq(KEY), any(HashMap.class)))
                .thenReturn(VALUE);
        Response respMock = mockResponse();
        boolean check = service.check(expectedResult, respMock);
        assertFalse(check);
    }

    @Test
    public void getValueEqual() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey(KEY)
                .withEqual(false)
                .withValue(VALUE);

        when(mock.parseELInAnswer(eq(KEY), any(HashMap.class)))
                .thenReturn(VALUE);
        Response respMock = mockResponse();

        String value = service.getValue(expectedResult, respMock);
        assertEquals(VALUE, value);
    }
    @Test
    public void getValueNotEqual() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey(KEY)
                .withEqual(false)
                .withValue(VALUE);

        when(mock.parseELInAnswer(eq(KEY), any(HashMap.class)))
                .thenReturn("notEqualValue");
        Response respMock = mockResponse();

        String value = service.getValue(expectedResult, respMock);
        assertNotEquals(VALUE, value);
    }

    @Test
    public void containsInAnswer() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey(KEY)
                .withEqual(true)
                .withValue(VALUE);
        Response response = mockResponse();
        when(mock.parseELInAnswer(anyString(), any())).thenCallRealMethod();
        boolean contains = service.containsInAnswer(expectedResult, response);
        assertTrue(contains);
    }

    @Test
    public void noContainsInAnswer() {
        ExpectedResult expectedResult = new ExpectedResult()
                .withKey("#NotKey#")
                .withEqual(true)
                .withValue(VALUE);
        Response response = mockResponse();
        when(mock.parseELInAnswer(anyString(), any())).thenCallRealMethod();
        boolean contains = service.containsInAnswer(expectedResult, response);
        assertFalse(contains);
    }
}