package tester.service;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import tester.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScenarioService {
    private Scenarios scenarios;
    private Properties properties;
    private HashMap<Integer, HashMap<String, Object>> values;

    public ScenarioService(Scenarios scenarios, Properties properties) {
        this.scenarios = scenarios;
        this.properties = properties;
        values = new HashMap<>();
    }

    public void execute() throws IOException {
        for (Scenario scenario : scenarios.getScenarios()) {
            Request request = scenario.getRequest();
            if (values != null && !values.isEmpty()){
                request.setRelativeUrl( parseEL(request.getRelativeUrl()) );
                request.setBody( parseEL(request.getBody()) );
                for (QueryParam param : request.getQueryParams()) {
                    param.setValue( parseEL(param.getValue()) );
                }
            }
            RequestService requestService = RequestService.RequestServiceBuilder.builder()
                                .withRequest(request)
                                .withProperties(properties)
                                .build();
            Response response = requestService.execute();
            values.put( scenario.getStepId(), response.jsonPath().get());
        }
    }

    /**
     * метод ищет все слова окруженные # и заменяет их на ведружимое мапы
     *
     * пример:
     *
     * operation/status?id=#operaton.id#
     *
     * В этом случае мы находим #operaton.id#, достаем operaton.id делим по точке получаем 2 слова operaton и id
     * по порядку идем по мапе прошлого ответа:
     *  - ищем объект по ключу operaton
     *  - у него пытаемся получить объект по ключу id
     *
     *  Если все хорошо и мы нашли подходящее значение - заменяем его, если нет, ничего не делаем
     *  TODO: стоит подумать над exception
     * @param text
     * @return
     */
    String parseEL(String text) {
        int start = text.indexOf("#");
        int end = text.indexOf("#", start+1);
        while (end > start){
            String substring = text.substring(start, end)
                    .replaceAll("#","");
            String[] split = substring.split("\\.");
            int stepId = Integer.parseInt(split[0]);
            HashMap<String, Object> tempMap = values.get(stepId);
            for (int i = 1; i < split.length; i++) {
                Object value = tempMap.get(split[i]);
                if (value instanceof HashMap) {
                    tempMap = (HashMap<String, Object>) value;
                }else if (value instanceof String && split[i].equals(split[split.length-1])){
                    text = text.replace("#"+substring+"#", value.toString());
                }
            }
            start = text.indexOf("#");
            end = text.indexOf("#", start+1);
        }
        return text;
    }

}
