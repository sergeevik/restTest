package tester.service;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import tester.exception.ExecuteFailException;
import tester.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static tester.service.MockTemplate.mockResponse;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ScenarioService.class, RequestService.class})
public class ScenarioServiceTest {

    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String KEY_EL = "#key#";
    private RequestService requestServiceMock;
    private Response responseMock;

    @Before
    public void setUp() throws Exception {
        requestServiceMock = mock(RequestService.class);
        PowerMockito.whenNew(RequestService.class)
                .withAnyArguments()
                .thenReturn(requestServiceMock);
        responseMock = mockResponse();
        when(requestServiceMock.execute()).thenReturn(responseMock);
    }

    @Test
    public void executeOnce() throws Exception {

        RequestService requestService = mock(RequestService.class);
        PowerMockito.whenNew(RequestService.class)
                .withAnyArguments()
                .thenReturn(requestService);

        ScenarioService scenarioService = new ScenarioService(new Scenarios(), new Properties());
        Request request = mock(Request.class);
        Response response = mockResponse();
        when(requestService.execute()).thenReturn(response);
        Response actualResponse = scenarioService.executeOnce(request);
        assertThat(actualResponse)
                .isNotNull()
                .isEqualTo(response)
                .isSameAs(response);

    }

    @Test
    public void executeOneRequestInScenarioWithoutRepeat() throws Exception {
        Request request = new Request();
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Collections.singletonList(
                        new Scenario().withStepId(1).withRequest(request)
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        verifyNew(RequestService.class).withArguments(eq(request), any(Properties.class));
        verify(requestServiceMock, times(1)).execute();
    }

    /**
     * тест сенария, когда запрос надо повторить и по итогу ExpectedResult выполнился
     * @throws Exception
     */
    @Test
    public void executeOneRequestInScenarioWithRepeat() throws Exception {
        Response responseFail = mockResponse();
        JsonPath jsonPath = mock(JsonPath.class);
        when(responseFail.jsonPath()).thenReturn(jsonPath);
        HashMap<Object, Object> map = new HashMap<>();
        map.put(KEY, "failValue");
        when(jsonPath.get()).thenReturn(map);
        int repeatCount = 10;
        when(requestServiceMock.execute()).thenAnswer(goodAndFailAnswer(responseMock, responseFail, repeatCount));
        Request request = new Request();
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Collections.singletonList(
                        new Scenario()
                                .withStepId(1)
                                .withRequest(request)
                                .withRepeatableWhile(
                                        new RepeatableWhile()
                                                .withExpectedResult(
                                                        new ExpectedResult()
                                                                .withKey(KEY_EL)
                                                                .withValue(VALUE)
                                                                .withEqual(true)
                                                )
                                                .withMaxRepeatCount(repeatCount)
                                )
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        verifyNew(RequestService.class, times(repeatCount)).withArguments(eq(request), any(Properties.class));
        verify(requestServiceMock, times(repeatCount)).execute();
    }

    /**
     * тестируем что инкремент счетчика не изменили на декремент
     * @throws Exception
     */
    @Test(expected = ExecuteFailException.class)
    public void testIncrement() throws Exception {
        Response responseFail = mockResponse();
        JsonPath jsonPath = mock(JsonPath.class);
        when(responseFail.jsonPath()).thenReturn(jsonPath);
        HashMap<Object, Object> map = new HashMap<>();
        map.put(KEY, "failValue");
        when(jsonPath.get()).thenReturn(map);
        int repeatCount = 10;
        when(requestServiceMock.execute()).thenAnswer(goodAndFailAnswer(responseMock, responseFail, repeatCount));
        Request request = new Request();
        int realRepeatCount = repeatCount / 2;
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Collections.singletonList(
                        new Scenario()
                                .withStepId(1)
                                .withRequest(request)
                                .withRepeatableWhile(
                                        new RepeatableWhile()
                                                .withExpectedResult(
                                                        new ExpectedResult()
                                                                .withKey(KEY_EL)
                                                                .withValue(VALUE)
                                                                .withEqual(true)
                                                )
                                                .withMaxRepeatCount(realRepeatCount)
                                )
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        verifyNew(RequestService.class, times(realRepeatCount)).withArguments(eq(request), any(Properties.class));
        verify(requestServiceMock, times(realRepeatCount)).execute();
    }


    /**
     * тест сенария, когда запрос надо повторить и по итогу ExpectedResult не выполнился
     * @throws Exception
     */
    @Test(expected = ExecuteFailException.class)
    public void executeOneRequestInScenarioWithRepeatWhenExpectedResultNotEqual() throws Exception {
        Response responseFail = mockResponse();
        JsonPath jsonPath = mock(JsonPath.class);
        when(responseFail.jsonPath()).thenReturn(jsonPath);
        HashMap<Object, Object> map = new HashMap<>();
        map.put(KEY, "failValue");
        when(jsonPath.get()).thenReturn(map);
        int repeatCount = 2;
        when(requestServiceMock.execute()).thenReturn(responseFail);
        Request request = new Request();
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Collections.singletonList(
                        new Scenario()
                                .withStepId(1)
                                .withRequest(request)
                                .withRepeatableWhile(
                                        new RepeatableWhile()
                                                .withExpectedResult(
                                                        new ExpectedResult()
                                                                .withKey(KEY_EL)
                                                                .withValue(VALUE)
                                                                .withEqual(true)
                                                )
                                                .withMaxRepeatCount(repeatCount)
                                )
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        verifyNew(RequestService.class, times(repeatCount)).withArguments(eq(request), any(Properties.class));
        verify(requestServiceMock, times(repeatCount)).execute();
    }


    /**
     * тест сенария, когда key из ExpectedResult не содержится в ответе.
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void executeOneRequestInScenarioWithRepeatFailWhenElNotExist() throws Exception {
        Response responseFail = mockResponse();
        JsonPath jsonPath = mock(JsonPath.class);
        when(responseFail.jsonPath()).thenReturn(jsonPath);
        HashMap<Object, Object> map = new HashMap<>();
        map.put(KEY, "failValue");
        when(jsonPath.get()).thenReturn(map);
        int repeatCount = 10;
        Request request = new Request();
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Collections.singletonList(
                        new Scenario()
                                .withStepId(1)
                                .withRequest(request)
                                .withRepeatableWhile(
                                        new RepeatableWhile()
                                                .withExpectedResult(
                                                        new ExpectedResult()
                                                                .withKey("#notKey#")
                                                                .withValue(VALUE)
                                                                .withEqual(true)
                                                )
                                                .withMaxRepeatCount(repeatCount)
                                )
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();
    }


    /**
     * тест сенария, замены el в relativeUrl второго запроса.
     * @throws Exception
     */
    @Test
    public void testReplaceElInRelativeUrlSecondRequest() throws Exception {
        Request request = new Request();
        Request secondRequest = new Request()
                .withRelativeUrl("#1.resp.key#");
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Arrays.asList(
                        new Scenario()
                            .withStepId(1)
                            .withRequest(request),
                        new Scenario()
                            .withStepId(2)
                            .withRequest(secondRequest)
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        assertThat(secondRequest.getRelativeUrl())
                .isNotNull()
                .isEqualTo(VALUE);
    }
    /**
     * тест сенария, замены el в body второго запроса.
     * @throws Exception
     */
    @Test
    public void testReplaceElInBodySecondRequest() throws Exception {
        Request request = new Request();
        Request secondRequest = new Request()
                .withBody("{\"body\": \"#1.resp.key#\"}");
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Arrays.asList(
                        new Scenario()
                            .withStepId(1)
                            .withRequest(request),
                        new Scenario()
                            .withStepId(2)
                            .withRequest(secondRequest)
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        assertThat(secondRequest.getBody())
                .isNotNull()
                .isEqualTo("{\"body\": \"" + VALUE + "\"}");
    }
    /**
     * тест сенария, замены el в body второго запроса. Должен упасть, потому что тело запроса null
     * и нет возможности заменить el
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceElInBodySecondRequestFailWhenRequestBodyNull() throws Exception {
        Request request = new Request();
        Request secondRequest = new Request()
                .withBody("{\"body\": \"#1.req.key#\"}");
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Arrays.asList(
                        new Scenario()
                            .withStepId(1)
                            .withRequest(request),
                        new Scenario()
                            .withStepId(2)
                            .withRequest(secondRequest)
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();
    }
    /**
     * тест сенария, замены el в body второго запроса. Должен упасть, потому что тело запроса пустое
     * и нет возможности заменить el
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceElInBodySecondRequestFailWhenRequestBodyEmpty() throws Exception {
        Request request = new Request()
                .withBody("");
        Request secondRequest = new Request()
                .withBody("{\"body\": \"#1.req.key#\"}");
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Arrays.asList(
                        new Scenario()
                            .withStepId(1)
                            .withRequest(request),
                        new Scenario()
                            .withStepId(2)
                            .withRequest(secondRequest)
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();
    }
    /**
     * тест сенария, замены el в body второго запроса из тела первого запроса.
     * @throws Exception
     */
    @Test
    public void testReplaceElInBodySecondRequestFromBodyFirstRequest() throws Exception {
        Request request = new Request()
                .withBody("{\"key\": \""+VALUE+"\"}");
        Request secondRequest = new Request()
                .withBody("{\"body\": \"#1.req.key#\"}");
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Arrays.asList(
                        new Scenario()
                            .withStepId(1)
                            .withRequest(request),
                        new Scenario()
                            .withStepId(2)
                            .withRequest(secondRequest)
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        assertThat(secondRequest.getBody())
                .isNotNull()
                .isEqualTo("{\"body\": \"" + VALUE + "\"}");
    }
    /**
     * тест сенария, замены el в queryParams второго запроса из тела первого запроса.
     * @throws Exception
     */
    @Test
    public void testReplaceElInQueryParamSecondRequest() throws Exception {
        Request request = new Request();
        Request secondRequest = new Request()
                .withQueryParams(
                        Collections.singletonList(
                                new QueryParam("set", "#1.resp.key#")
                        )
                );
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Arrays.asList(
                        new Scenario()
                            .withStepId(1)
                            .withRequest(request),
                        new Scenario()
                            .withStepId(2)
                            .withRequest(secondRequest)
                        )
                );

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        scenarioService.execute();

        assertThat(secondRequest.getQueryParams())
                .isNotNull()
                .isNotEmpty();
        QueryParam queryParam = secondRequest.getQueryParams().get(0);
        assertThat(queryParam).isNotNull();
        assertThat(queryParam.getKey()).isNotNull()
                .isEqualTo("set");
        assertThat(queryParam.getValue()).isNotNull()
                .isEqualTo(VALUE);
    }

    @Test
    public void testSleep() throws IOException {
        int sleepMs = 1_000;
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Collections.singletonList(
                        new Scenario()
                                .withStepId(1)
                                .withRequest(new Request())
                                .withRepeatableWhile(
                                        new RepeatableWhile()
                                        .withSleep(sleepMs)
                                        .withMaxRepeatCount(2)
                                        .withExpectedResult(
                                                new ExpectedResult()
                                                .withKey(KEY_EL)
                                                .withValue(VALUE)
                                                .withEqual(true)
                                        )
                                )
                ));

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        StopWatch timer = new StopWatch();
        timer.start();
        scenarioService.execute();
        timer.stop();
        long sleepActual = timer.getTime();
        assertThat(sleepActual).isGreaterThanOrEqualTo(sleepMs);
    }

    @Test
    public void testSleepInterrupt() throws Exception {
        int sleepMs = 10_000;
        int timeInterrupt = sleepMs / 4;
        Scenarios scenarios = new Scenarios()
                .withName("test")
                .withScenarios(Collections.singletonList(
                        new Scenario()
                                .withStepId(1)
                                .withRequest(new Request())
                                .withRepeatableWhile(
                                        new RepeatableWhile()
                                        .withSleep(sleepMs)
                                        .withMaxRepeatCount(2)
                                        .withExpectedResult(
                                                new ExpectedResult()
                                                .withKey(KEY_EL)
                                                .withValue(VALUE)
                                                .withEqual(true)
                                        )
                                )
                ));

        ScenarioService scenarioService = new ScenarioService(scenarios, new Properties());
        StopWatch timer = new StopWatch();
        Thread thread = new Thread(() -> {
            try {
                timer.start();
                scenarioService.execute();
                timer.stop();
            } catch (IOException ignore) {
            }
        });
        thread.start();
        Thread.sleep(timeInterrupt);
        thread.interrupt();
        thread.join();
        assertThat(timer.getTime())
                .isNotNull()
                .isGreaterThanOrEqualTo(timeInterrupt)
                .isLessThanOrEqualTo(sleepMs);
    }

    public Answer goodAndFailAnswer(Response good, Response fail, int repeatCount){
        final AtomicInteger count = new AtomicInteger(0);
        return invocationOnMock -> {
            int andIncrement = count.getAndIncrement();
            if (andIncrement >= repeatCount-1){
                return good;
            }else {
                return fail;
            }
        };
    }
}