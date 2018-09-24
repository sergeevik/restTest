package tester.service;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;

public class ElParser {
    private static final Logger log = Logger.getLogger(ElParser.class);
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
    String parseELByStepId(String text, HashMap<Integer, HashMap<String, Object>> values) {
        log.debug("начинаем парсить строку: " + text);
        if (text == null || text.isEmpty()){
            return text;
        }
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

    @SuppressWarnings("unchecked")
    private String parseEl(String[] text, HashMap<String, Object> map){
        for (String key : text) {
            Object value = map.get(key);
            if (value instanceof HashMap){
                map = (HashMap<String, Object>) value;
            }else if (value != null){
                return value.toString();
            }
        }
        throw new IllegalArgumentException("Ошибка парсинга выражения: " + Arrays.toString(text));
    }



    String parseELInAnswer(String text, HashMap<String, Object> answer) {
        log.debug("начинаем парсить строку: " + text);
        String[] strings = StringUtils.substringsBetween(text, "#", "#");
        if (strings == null){
            return text;
        }
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
