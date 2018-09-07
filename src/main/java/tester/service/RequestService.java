package tester.service;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import tester.model.Header;
import tester.model.Properties;
import tester.model.QueryParam;
import tester.model.Request;

import java.io.File;
import java.io.IOException;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

public class RequestService {
    private static final Logger log = Logger.getLogger(RequestService.class);
    private Request request;
    private Properties properties;


    public RequestService(Request request, Properties properties) {
        this.request = request;
        this.properties = properties;
    }

    public Response execute() throws IOException {
        try {

            log.info("====== START REQUEST: " + request.getRelativeUrl() + " ======");
            PropertiesService propertiesService = new PropertiesService(properties);
            propertiesService.setRestAssuredProperties();

            RequestSpecification requestSpecification = RestAssured.given()
                    .log().all()
                    .contentType(request.getContentType())
                    .body(request.getBody());

            fillQueryParam(requestSpecification);
            fillHeaders(requestSpecification);
            fillContentType(requestSpecification);

            Response response = executeRequest(requestSpecification);
            System.out.println("Response:");
            response.prettyPrint();

            String schemaFullPath = getSchemaPath();
            checkResposeEquasSceme(response, schemaFullPath);
            checkResponseCode(response);

            return response;
        }catch (AssertionError error){
            log.warn("====== FAIL REQUEST: " + request.getRelativeUrl() + " ======");
            System.out.println(error.getMessage());
            throw error;
        }finally {
            log.info("====== END REQUEST: " + request.getRelativeUrl() + " ======");
        }

    }

    private void checkResponseCode(Response response) {
        response.then().assertThat()
                .statusCode(request.getAnswerCode());
    }

    private void checkResposeEquasSceme(Response response, String schemaFullPath) {
        if (request.isValidateSchema() && response != null){
            response.then().assertThat()
                    .body(matchesJsonSchema(new File(schemaFullPath).toURI()));
        }
    }

    private String getSchemaPath() {
        if (!request.getSchemaFullPath().isEmpty()){
            return request.getSchemaFullPath();
        }else {
            return properties.getSchemaBaseUrl() + request.getSchemaRelativePath();
        }
    }

    private Response executeRequest(RequestSpecification requestSpecification) {
        if (request.getRequestType() == Request.RequestType.GET){
            return requestSpecification.get(request.getRelativeUrl());
        }else{
            return requestSpecification.post(request.getRelativeUrl());
        }
    }

    private void fillContentType(RequestSpecification requestSpecification) {
        if (!request.getContentType().isEmpty()){
            requestSpecification.contentType(request.getContentType());
        }
    }

    private void fillHeaders(RequestSpecification requestSpecification) {
        if (request.getHeaders() != null){
            for (Header header : request.getHeaders()) {
                requestSpecification.header(header.getKey(), header.getValue());
            }
        }
    }

    private void fillQueryParam(RequestSpecification requestSpecification) {
        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()){
            for (QueryParam param : request.getQueryParams()) {
                requestSpecification.queryParam(param.getKey(), param.getValue());
            }
        }
    }

}
