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

    public PropertiesService(Properties properties) {
        this.properties = properties;
    }

    public void setRestAssuredProperties(){
        RestAssured.baseURI = properties.getBaseUrl();
        RestAssured.port = properties.getPort();
        Auth auth = properties.getAuth();
        if (auth != null) {
            RestAssured.authentication = RestAssured.preemptive().basic(auth.getLogin(), auth.getPassword());
        }
    }

    public void addLogAppender(){
        FileAppender fileAppender = new FileAppender();
        fileAppender.setName("log");
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy/hh.mm.ss");
        String fullPathToLogFile = properties.getFullPathToLogFile();
        if (fullPathToLogFile == null || fullPathToLogFile.isEmpty()){
            fullPathToLogFile = System.getProperty("user.home") + "/rest-tester";
        }

        String fileName = fullPathToLogFile + "/logs/" + format.format(new Date()) + ".log";
        fileAppender.setFile(fileName);
        fileAppender.setThreshold(Level.toLevel(properties.getLogLevel().name()));
        fileAppender.setAppend(true);
        fileAppender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));
        fileAppender.activateOptions();
        Logger.getRootLogger().addAppender(fileAppender);
    }
}
