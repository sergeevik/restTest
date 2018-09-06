package tester.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.log4j.Logger;
import tester.model.*;

import java.io.IOException;
import java.util.HashMap;

public class ScenarioService {
    private static final Logger log = Logger.getLogger(ScenarioService.class);
    private Scenarios scenarios;
    private Properties properties;
    private HashMap<Integer, HashMap<String, Object>> values;

    public ScenarioService(Scenarios scenarios, Properties properties) {
        this.scenarios = scenarios;
        this.properties = properties;
        values = new HashMap<>();
    }

    public void execute() throws IOException {
        log.info("============= SCENARIO START =============");
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

            HashMap<String, Object> stepMap = values.getOrDefault(scenario.getStepId(), new HashMap<>());
            if (request.getBody() != null && !request.getBody().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                HashMap hashMap = mapper.readValue(request.getBody(), HashMap.class);
                stepMap.put("req", hashMap);
            }
            stepMap.put( "resp", response.jsonPath().get());
            values.put(scenario.getStepId(), stepMap);
        }

        log.info("============= SCENARIO END =============");
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
        log.debug("начинаем парсить строку: " + text);
        int start = text.indexOf("#");
        int end = text.indexOf("#", start+1);
        while (end > start){
            String substring = text.substring(start, end)
                    .replaceAll("#","");
            log.debug("заменяем: " + substring);
            String[] split = substring.split("\\.");
            int stepId = Integer.parseInt(split[0]);
            HashMap<String, Object> tempMap = values.get(stepId);
            for (int i = 1; i < split.length; i++) {
                Object value = tempMap.get(split[i]);
                if (value instanceof HashMap) {
                    tempMap = (HashMap<String, Object>) value;
                }else if (value instanceof String && split[i].equals(split[split.length-1])){
                    text = text.replace("#"+substring+"#", value.toString());
                    log.debug("замена: '" + substring + "' прошла успешно. Новое значение: " + value.toString());
                }else if (value == null){
                    log.warn("замена: '" + substring + "' не увенчалась успехом, а жаль. Попробуй посмотреть запрос/ответ" +
                            "из которого надо было получить это значение. Пришло что-то не то.");
                    throw new RuntimeException("Text: " +substring + ", not found in cache request/resp. Try find it.");
                }
            }
            start = text.indexOf("#");
            end = text.indexOf("#", start+1);
        }
        return text;
    }

}
