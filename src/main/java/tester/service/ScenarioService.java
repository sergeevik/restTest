package tester.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.log4j.Logger;
import tester.exception.ExecuteFailException;
import tester.model.*;

import java.io.IOException;
import java.util.HashMap;

public class ScenarioService {
    private static final Logger log = Logger.getLogger(ScenarioService.class);
    private Scenarios scenarios;
    private Properties properties;
    private HashMap<Integer, HashMap<String, Object>> values;
    private ElParser elParser;
    private ExpectedResultService expectedResultService;

    public ScenarioService(Scenarios scenarios, Properties properties) {
        this.scenarios = scenarios;
        this.properties = properties;
        this.expectedResultService = new ExpectedResultService();
        values = new HashMap<>();
        elParser = new ElParser();
    }

    public void execute() throws IOException {
        log.info("============= SCENARIO START =============");
        for (Scenario scenario : scenarios.getScenarios()) {
            Request request = scenario.getRequest();
            Response response = executeWithRepeatable(scenario);
            saveReqAndRespMap(scenario, request, response);
        }

        log.info("============= SCENARIO END =============");
    }

    private void saveReqAndRespMap(Scenario scenario, Request request, Response response) throws IOException {
        HashMap<String, Object> stepMap = values.getOrDefault(scenario.getStepId(), new HashMap<>());
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap hashMap = mapper.readValue(request.getBody(), HashMap.class);
            stepMap.put("req", hashMap);
        }
        stepMap.put( "resp", response.jsonPath().get());
        values.put(scenario.getStepId(), stepMap);
    }

    private Response executeWithRepeatable(Scenario scenario) {
        Request request = scenario.getRequest();
        Response response;
        if (scenario.getRepeatableWhile() == null){
            response = executeOnce(request);
        }else {
            RepeatableWhile repeatable = scenario.getRepeatableWhile();
            int countRepeat = 1;
            do {
                response = executeOnce(request);
                if (++countRepeat > repeatable.getMaxRepeatCount()){
                    break;
                }
                sleep(repeatable.getSleep());
            } while (!expectedResultService.check(repeatable.getExpectedResult(), response));
            checkResult(scenario, response, countRepeat);
        }
        return response;
    }

    private void checkResult(Scenario scenario, Response response, int countRepeat) {
        RepeatableWhile repeatable = scenario.getRepeatableWhile();
        Request request = scenario.getRequest();
        if (!expectedResultService.check(repeatable.getExpectedResult(), response)){
            String actualValue = expectedResultService.getValue(repeatable.getExpectedResult(), response);
            log.info("Запрос " + request.getRelativeUrl() + " повторился " + countRepeat + " раз. Но" +
                    " ожидаемый результат не получен. Ожидался результат\n" +
                    "--- " + actualValue + " будет равно(" + repeatable.getExpectedResult().isEqual() + ") "
                    + repeatable.getExpectedResult().getValue());
            throw new ExecuteFailException(request.getRelativeUrl());
        }
    }

    private void sleep(int ms) {
        try{
            log.info("sleep: " + ms + "ms.");
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.info("sleep interrupted.");
            log.warn(e.getMessage(), e);
        }
    }

    Response executeOnce(Request request) {
        replaceEl(request);
        RequestService requestService = new RequestService(request, properties);
        return requestService.execute();
    }

    private void replaceEl(Request request) {
        if (!values.isEmpty()){
            request.setRelativeUrl(elParser.parseELByStepId(request.getRelativeUrl(), values) );
            request.setBody( elParser.parseELByStepId(request.getBody(), values) );
            for (QueryParam param : request.getQueryParams()) {
                param.setValue( elParser.parseELByStepId(param.getValue(), values) );
            }
        }
    }


}
