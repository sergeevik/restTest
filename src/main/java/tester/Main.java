package tester;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import tester.model.Properties;
import tester.model.Request;
import tester.model.Scenarios;
import tester.service.ParserConfig;
import tester.service.RequestService;
import tester.service.ScenarioService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class Main {
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException {
//        String propertiesFile = "C:\\work\\test-rest\\properties.json";
//
//        Properties commonProperties = ParserConfig.getCommonProperties(propertiesFile);
//        PropertiesService propertiesService = new PropertiesService(commonProperties);
//        propertiesService.addLogAppender();
//
//        List<String> requestsFiles = getAllFile(commonProperties.getRequestsFolder());
//        requestsFiles.addAll(commonProperties.getRequestList());
//
//        List<String> scenarioFiles = getAllFile(commonProperties.getScenariosFolder());
//        scenarioFiles.addAll(commonProperties.getScenarioList());
//
//        for (String requestFile : requestsFiles) {
//            req(commonProperties, requestFile);
//        }
//        for (String scenarioFile : scenarioFiles) {
//            scenario(commonProperties, scenarioFile);
//        }
        Properties properties = new Properties();
        reflectionFill(properties);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(properties));
    }
    private static void reflectionFill(Object parent) throws IllegalAccessException, InstantiationException {
        Field[] declaredFields = parent.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(JsonPropertyDescription.class)){
                field.setAccessible(true);
                JsonPropertyDescription annotation = field.getAnnotation(JsonPropertyDescription.class);
                System.out.println(annotation.value() + ":");
                if (isSimple(field)){
                    String s = sc.nextLine();
                    Object value = castToSimpleType(s, field.getType());
                    field.set(parent, value);
                }else {
                    Object o = field.get(parent);
                    if (o == null){
                        o = field.getType().newInstance();
                        field.set(parent, o);
                    }
                    reflectionFill(o);
                }
            }
        }
    }

    private static Object castToSimpleType(String s, Class<?> dClass) {
        if (dClass.equals(Boolean.class) || dClass.equals(boolean.class)) {
            return Boolean.parseBoolean(s);
        }
        else if (dClass.equals(Integer.class) || dClass.equals(int.class)){
            return Integer.parseInt(s);
        }else {
            return s;
        }
    }

    private static boolean isSimple(Field field) {
        Class<?> declaringClass = field.getType();
        return declaringClass.equals(int.class) ||
                declaringClass.equals(boolean.class) ||
                declaringClass.equals(String.class);
    }

    private static List<String> getAllFile(String requestsFolder) throws IOException {
        if (requestsFolder == null || requestsFolder.isEmpty()){
            return new ArrayList<>();
        }
        try(Stream<Path> pathStream = Files.walk(Paths.get(requestsFolder))){
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(toList());
        }
    }

    private static void scenario(Properties properties, String requestFile) throws IOException {
        Scenarios scenario = ParserConfig.getScenariosSortByID(requestFile);
        ScenarioService scenarioService = new ScenarioService(scenario, properties);
        scenarioService.execute();
    }

    private static void req(Properties properties, String requestFile) throws IOException {
        Request request = ParserConfig.getRequest(requestFile);
        RequestService requestService = new RequestService(request, properties);
        requestService.execute();
    }
}
