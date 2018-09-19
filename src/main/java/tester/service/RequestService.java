package tester.service;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import tester.exception.ExecuteFailException;
import tester.exception.RequestFillException;
import tester.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

public class RequestService {
    private static final Logger log = Logger.getLogger(RequestService.class);
    private Request request;
    private ResponseValidator responseValidator;
    private Properties properties;
    private PrintStream printStream;
    private ByteArrayOutputStream out;
    private ExpectedResultService expectedResultService;


    public RequestService(Request request, Properties properties) {
        this.request = request;
        this.responseValidator = request.getResponseValidator();
        this.properties = properties;
        this.expectedResultService = new ExpectedResultService();
        out = new ByteArrayOutputStream();
        printStream = new PrintStream(out);
    }

    public Response execute() {
        try {
            log.info("====== START REQUEST: " + request.getRelativeUrl() + " ======");
            RequestSpecification requestSpecification = RestAssured.given()
                    .log()
                    .all()
                    .filter(RequestLoggingFilter.logRequestTo(printStream));

            fillBody(requestSpecification);
            fillQueryParam(requestSpecification);
            fillHeaders(requestSpecification);
            fillContentType(requestSpecification);

            Response response = executeRequest(requestSpecification);
            log.info(out.toString());
            log.info("Response: \n" + response.prettyPrint());

            String schemaFullPath = getSchemaPath();
            checkResponseEqualsScheme(response, schemaFullPath);
            checkResponseCode(response);
            checkResponseHeaders(response);
            checkExpectedResult(response);
            return response;
        }catch (AssertionError error){
            log.warn("====== FAIL REQUEST: " + request.getRelativeUrl() + " ======");
            log.warn(error.getMessage());
            throw new ExecuteFailException(request.getRelativeUrl());
        }finally {
            log.info("====== END REQUEST: " + request.getRelativeUrl() + " ======");
        }

    }

    void fillBody(RequestSpecification requestSpecification) {
        if (request.getBody() != null){
            requestSpecification.body(request.getBody());
        }
    }

    void checkExpectedResult(Response response) {
        ExpectedResult expectedResult = request.getResponseValidator().getExpectedResult();
        if (expectedResult == null){
            return;
        }

        if (!expectedResultService.containsInAnswer(expectedResult, response)){
            throw new AssertionError("expected result key "+ expectedResult.getKey() + " not contains in response.");
        }
        if (!expectedResultService.check(expectedResult, response)){
            throw new AssertionError("actual result not equal expected. \n" +
                    "-- Expected: " + expectedResult.getValue() +"\n" +
                    "-- Actual  : " + expectedResultService.getValue(expectedResult, response));
        }
    }

    void checkResponseCode(Response response) {
        response.then().assertThat()
                .statusCode(request.getAnswerCode());
    }
    void checkResponseHeaders(Response response) {
        ValidatableResponse validatableResponse = response.then().assertThat();
        for (Header header : responseValidator.getHeaders()) {
            validatableResponse.header(header.getKey(), header.getValue());
        }
    }

    void checkResponseEqualsScheme(Response response, String schemaFullPath) {
        if (responseValidator.isValidateSchema()){
            response.then().assertThat()
                    .body(matchesJsonSchema(new File(schemaFullPath).toURI()));
        }
    }

    String getSchemaPath() {
        if (responseValidator == null){
            return "";
        }
        if (!responseValidator.getSchemaFullPath().isEmpty()){
            return responseValidator.getSchemaFullPath();
        }else if (!properties.getSchemaBaseUrl().isEmpty()){
            return properties.getSchemaBaseUrl() + responseValidator.getSchemaRelativePath();
        }else {
            return "";
        }
    }

    Response executeRequest(RequestSpecification requestSpecification) {
        if (request.getRequestType() == Request.RequestType.GET){
            return requestSpecification.get(request.getRelativeUrl());
        }else{
            return requestSpecification.post(request.getRelativeUrl());
        }
    }

    void fillContentType(RequestSpecification requestSpecification) {
        if (!request.getContentType().isEmpty()){
            requestSpecification.contentType(request.getContentType());
        }else if(!properties.getContentType().isEmpty()){
            requestSpecification.contentType(properties.getContentType());
        }else {
            throw new RequestFillException("content-type must be not empty in request or property file");
        }
    }

    void fillHeaders(RequestSpecification requestSpecification) {
        for (Header header : request.getHeaders()) {
            requestSpecification.header(header.getKey(), header.getValue());
        }
    }

    void fillQueryParam(RequestSpecification requestSpecification) {
        for (QueryParam param : request.getQueryParams()) {
            requestSpecification.queryParam(param.getKey(), param.getValue());
        }
    }

}
