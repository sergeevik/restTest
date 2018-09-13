package tester.service;

import io.restassured.RestAssured;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import tester.model.Auth;
import tester.model.Properties;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PropertiesService {
    private Properties properties;
    private String fullPathToLogFile;

    public PropertiesService(Properties properties) {
        this.properties = properties;
        setFullPathToLog();
    }

    public void setRestAssuredProperties(){
        RestAssured.baseURI = properties.getBaseUrl();
        if (properties.getPort() > 0) {
            RestAssured.port = properties.getPort();
        }
        Auth auth = properties.getAuth();
        if (auth != null) {
            RestAssured.authentication = RestAssured.preemptive().basic(auth.getLogin(), auth.getPassword());
        }
    }

    public void addLogAppender(){
        appendLogWithLevel(fullPathToLogFile, Level.INFO);
        appendLogWithLevel(fullPathToLogFile, Level.DEBUG);
    }

    private void appendLogWithLevel(String path, Level level) {
        FileAppender fileAppender = new FileAppender();
        fileAppender.setName("log");
        fileAppender.setFile(path + level.toString() + ".log");
        fileAppender.setThreshold(level);
        fileAppender.setAppend(true);
        fileAppender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));
        fileAppender.activateOptions();
        Logger.getRootLogger().addAppender(fileAppender);
    }

    private void setFullPathToLog(){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy/HH-mm-ss/");
        fullPathToLogFile = properties.getFullPathToLogFile();
        if (fullPathToLogFile == null || fullPathToLogFile.isEmpty()){
            fullPathToLogFile = System.getProperty("user.home") + "/rest-tester";
        }
        fullPathToLogFile = fullPathToLogFile + "/logs/" + format.format(new Date());
    }

    public String getFullPathToLogFile() {
        return fullPathToLogFile;
    }
}
