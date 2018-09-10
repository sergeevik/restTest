package tester.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
            Response response = executeWithRepeatable(scenario);

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

    private Response executeWithRepeatable(Scenario scenario) throws IOException {
        Request request = scenario.getRequest();
        Response response;
        if (scenario.getRepeatableWhile() == null){
            response = executeRequest(request);
        }else {
            RepeatableWhile repeatable = scenario.getRepeatableWhile();
            String actualValue;
            int countRepeat = 0;
            do {
                response = executeRequest(request);
                actualValue = parseELInAnswer(repeatable.getKey(), response.jsonPath().get());
                if (countRepeat++ > repeatable.getMaxRepeatCount()){
                    break;
                }
                try{
                    log.info("sleep: " + repeatable.getSleep() + "ms.");
                    Thread.sleep(repeatable.getSleep());
                } catch (InterruptedException e) {
                    log.info("sleep interrupted.");
                    log.warn(e.getMessage(), e);
                }
            } while (actualValue.equals(repeatable.getValue()) != repeatable.isEqual());
            if (countRepeat >= repeatable.getMaxRepeatCount() &&
                    actualValue.equals(repeatable.getValue()) != repeatable.isEqual()){
                log.info("Запрос " + request.getRelativeUrl() + " повторился " + countRepeat + " раз. Но" +
                        " ожидаемый результат не получен. Ожидался результат\n" +
                        "--- " + actualValue + " будет равно(" + repeatable.isEqual() + ") " + repeatable.getValue());
            }
        }
        return response;
    }

    private Response executeRequest(Request request) throws IOException {
        if (values != null && !values.isEmpty()){
            request.setRelativeUrl( parseELByStepId(request.getRelativeUrl()) );
            request.setBody( parseELByStepId(request.getBody()) );
            for (QueryParam param : request.getQueryParams()) {
                param.setValue( parseELByStepId(param.getValue()) );
            }
        }
        RequestService requestService = new RequestService(request, properties);
        return requestService.execute();
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
    String parseELByStepId(String text) {
        if (text == null || text.isEmpty()){
            return text;
        }
        log.debug("начинаем парсить строку: " + text);
        String[] strings = StringUtils.substringsBetween(text, "#", "#");
        for (String string : strings) {
            String[] split = string.split("\\.");
            int stepId = Integer.parseInt(split[0]);
            HashMap<String, Object> tempMap = values.get(stepId);
            String[] subarray = ArrayUtils.subarray(split, 1, split.length);
            try {
                String value = parseEl(subarray, tempMap);
                 text = text.replace("#"+string+"#", value);
                log.info("Успешно заменено: '" + string + "' на '" + value + "'" );
            }catch (IllegalArgumentException ex){
                log.warn("Не удалось распарсить выражение: '" + string + "', в тексте: '"+ text + "'");
                throw ex;
            }
        }
        return text;
    }

    private String parseEl(String[] text, HashMap<String, Object> map){
        for (String key : text) {
            Object value = map.get(key);
            if (value instanceof String){
                return (String) value;
            }else if (value instanceof HashMap){
                map = (HashMap<String, Object>) value;
            }else {
                throw new IllegalArgumentException("Ошибка парсинга выражения: " + text);
            }
        }
        throw new IllegalArgumentException("Ошибка парсинга выражения: " + text);
    }



    String parseELInAnswer(String text, HashMap<String, Object> answer) {
        log.debug("начинаем парсить строку: " + text);
        String[] strings = StringUtils.substringsBetween(text, "#", "#");
        for (String string : strings) {
            try {
                String[] split = string.split("\\.");
                String value = parseEl(split, answer);
                text = text.replace("#"+string+"#", value);
                log.debug("замена: '" + string + "' прошла успешно. Новое значение: " + value);
            }catch (IllegalArgumentException ex){
                log.warn("замена: '" + string + "' не увенчалась успехом, а жаль. Попробуй посмотреть запрос/ответ " +
                        "из которого надо было получить это значение. Пришло что-то не то.");
                throw ex;
            }
        }
        return text;
    }



}
