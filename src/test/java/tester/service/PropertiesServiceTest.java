package tester.service;

import io.restassured.RestAssured;
import io.restassured.authentication.AuthenticationScheme;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import tester.exception.PropertiesConfigException;
import tester.model.Auth;
import tester.model.Properties;

import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesServiceTest {

    private static final String BASE_URL = "baseUrl";
    private static final int PORT = 111;
    private static final Auth AUTH = new Auth("login", "pass");
    private static final int ZERO_PORT = 0;
    private static final String FULL_PATH_TO_LOG_FILE = "full_path_to_log_file";

    @Test
    public void testSetAllProperties() {
        Properties properties = new Properties()
                .withBaseUrl(BASE_URL)
                .withPort(PORT)
                .withAuth(AUTH);
        PropertiesService propertiesService = new PropertiesService(properties);
        propertiesService.setRestAssuredProperties();

        assertThat(RestAssured.baseURI). isEqualTo(BASE_URL);
        assertThat(RestAssured.port). isEqualTo(PORT);
        assertThat(RestAssured.authentication)
                .isExactlyInstanceOf(PreemptiveBasicAuthScheme.class);

        PreemptiveBasicAuthScheme authentication = (PreemptiveBasicAuthScheme)RestAssured.authentication;
        assertThat(authentication.getUserName())
                .isEqualTo(AUTH.getLogin());
        assertThat(authentication.getPassword())
                .isEqualTo(AUTH.getPassword());
    }
    @Test(expected = PropertiesConfigException.class)
    public void testBaseUrlIsRequired() {
        Properties properties = new Properties()
                .withPort(PORT)
                .withAuth(AUTH);
        PropertiesService propertiesService = new PropertiesService(properties);
        propertiesService.setRestAssuredProperties();
    }


    @Test
    public void testPortZeroNotSetInRestAssured() {
        int portBeforeTest = RestAssured.port;
        Properties properties = new Properties()
                .withBaseUrl(BASE_URL)
                .withPort(ZERO_PORT)
                .withAuth(AUTH);
        PropertiesService propertiesService = new PropertiesService(properties);
        propertiesService.setRestAssuredProperties();

        assertThat(RestAssured.port).isEqualTo(portBeforeTest);
    }
    @Test
    public void testAuthNull() {
        AuthenticationScheme authBeforeTest = RestAssured.authentication;
        Properties properties = new Properties()
                .withBaseUrl(BASE_URL)
                .withPort(PORT);
        PropertiesService propertiesService = new PropertiesService(properties);
        propertiesService.setRestAssuredProperties();

        assertThat(RestAssured.authentication).isEqualTo(authBeforeTest);
    }

    @Test
    public void testFullPathToLogInProperties() {
        Properties withLogPath = new Properties()
                .withFullPathToLogFile(FULL_PATH_TO_LOG_FILE);
        PropertiesService propertiesService = new PropertiesService(withLogPath);
        String fullPathToLogFile = propertiesService.getFullPathToLogFile();
        assertThat(fullPathToLogFile).startsWith(FULL_PATH_TO_LOG_FILE);
    }

    @Test
    public void testLogsSaveInUserHomeWhenFullPathNotSet() {
        Properties withLogPath = new Properties();
        PropertiesService propertiesService = new PropertiesService(withLogPath);
        String fullPathToLogFile = propertiesService.getFullPathToLogFile();
        assertThat(fullPathToLogFile).startsWith(System.getProperty("user.home"));
    }

    @Test
    public void appendLoggers() {
        Logger rootLogger = Logger.getRootLogger();
        int countBeforeSet = countAppender(rootLogger.getAllAppenders());
        PropertiesService propertiesService = new PropertiesService(new Properties());
        propertiesService.addLogAppender();

        int countAfterSet = countAppender(rootLogger.getAllAppenders());
        assertThat(countAfterSet).isGreaterThan(countBeforeSet);

        int fileAppenderLoggerCount = 2;
        assertThat(countAfterSet - countBeforeSet).isEqualTo(fileAppenderLoggerCount);
        checkFileLogger(Level.INFO, rootLogger);
        checkFileLogger(Level.DEBUG, rootLogger);
    }

    private int countAppender(Enumeration allAppenders) {
        int count = 0;
        while (allAppenders.hasMoreElements()){
            count++;
            allAppenders.nextElement();
        }
        return count;
    }

    private void checkFileLogger(Level level, Logger rootLogger){
        Appender appender = rootLogger.getAppender(level + ".log");
        assertThat(appender).isNotNull()
                    .isInstanceOf(FileAppender.class);
        FileAppender fileAppender = (FileAppender) appender;
        assertThat(fileAppender.getThreshold().toString())
                .isEqualTo(level.toString());
        assertThat(fileAppender.getFile())
                .endsWith(level + ".log");
    }
}