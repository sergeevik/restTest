package tester.service;

import io.restassured.response.Response;
import tester.model.ExpectedResult;

import java.util.HashMap;

public class ExpectedResultService {
    private ElParser parser = new ElParser();

    public boolean check(ExpectedResult result, Response response){
        HashMap<String, Object> respMap = response.jsonPath().get();
        String key = result.getKey();
        String actualValue = parser.parseELInAnswer(key, respMap);
        return result.getValue().equals(actualValue) == result.isEqual();
    }

    public String getValue(ExpectedResult result, Response response){
        HashMap<String, Object> respMap = response.jsonPath().get();
        String key = result.getKey();
        return parser.parseELInAnswer(key, respMap);
    }


}
