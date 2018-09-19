package tester.service;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

import java.util.HashMap;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

class MockTemplate {
    static Response mockResponse(){
        Response respMock = mock(Response.class);
        JsonPath jsonPathMock = mock(JsonPath.class);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("key", "value");
        when(jsonPathMock.get()).thenReturn(map);
        when(respMock.jsonPath()).thenReturn(jsonPathMock);
        return respMock;
    }

    static ValidatableResponse mockValidateResponse() {
        ValidatableResponse validateRespMock = mock(ValidatableResponse.class);
        when(validateRespMock.assertThat()).thenReturn(validateRespMock);
        return validateRespMock;
    }
}
