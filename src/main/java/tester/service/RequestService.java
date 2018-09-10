package tester.service;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import tester.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

public class RequestService {
    private static final Logger log = Logger.getLogger(RequestService.class);
    private Request request;
    private ResponseValidator responseValidator;
    private Properties properties;
    private PrintStream printStream;
    private ByteArrayOutputStream out;


    public RequestService(Request request, Properties properties) {
        this.request = request;
        this.responseValidator = request.getResponseValidator();
        this.properties = properties;
        out = new ByteArrayOutputStream();
        printStream = new PrintStream(out);
    }

    public Response execute() {
        try {

            log.info("====== START REQUEST: " + request.getRelativeUrl() + " ======");
            PropertiesService propertiesService = new PropertiesService(properties);
            propertiesService.setRestAssuredProperties();

            RequestSpecification requestSpecification = RestAssured.given()
                    .log().all().filter(RequestLoggingFilter.logRequestTo(printStream))
                    .contentType(request.getContentType());

            if (request.getBody() != null){
                requestSpecification.body(request.getBody());
            }

            fillQueryParam(requestSpecification);
            fillHeaders(requestSpecification);
            fillContentType(requestSpecification);

            Response response = executeRequest(requestSpecification);
            log.info(out.toString());
            log.info("Response: \n" + response.prettyPrint());

            String schemaFullPath = getSchemaPath();
            checkResposeEquasSceme(response, schemaFullPath);
            checkResponseCode(response);
            checkResponseHeaders(response);
            return response;
        }catch (AssertionError error){
            log.warn("====== FAIL REQUEST: " + request.getRelativeUrl() + " ======");
            log.warn(error.getMessage());
            throw error;
        }finally {
            log.info("====== END REQUEST: " + request.getRelativeUrl() + " ======");
        }

    }

    private void checkResponseCode(Response response) {
        response.then().assertThat()
                .statusCode(request.getAnswerCode());
    }
    private void checkResponseHeaders(Response response) {
        ValidatableResponse validatableResponse = response.then().assertThat();
        for (Header header : responseValidator.getHeaders()) {
            validatableResponse.header(header.getKey(), header.getValue());
        }
    }

    private void checkResposeEquasSceme(Response response, String schemaFullPath) {
        if (responseValidator.isValidateSchema() && response != null){
            response.then().assertThat()
                    .body(matchesJsonSchema(new File(schemaFullPath).toURI()));
        }
    }

    private String getSchemaPath() {
        if (!responseValidator.getSchemaFullPath().isEmpty()){
            return responseValidator.getSchemaFullPath();
        }else {
            return properties.getSchemaBaseUrl() + responseValidator.getSchemaRelativePath();
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
        }else if(!properties.getContentType().isEmpty()){
            requestSpecification.contentType(properties.getContentType());
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
