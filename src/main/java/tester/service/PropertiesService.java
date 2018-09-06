package tester.service;

import io.restassured.RestAssured;
import tester.model.Auth;
import tester.model.Properties;

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
}
