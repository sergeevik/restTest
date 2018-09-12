package tester.exeption;

public class ExecuteFail extends RuntimeException{
    private String requestRelativeUrl;
    public ExecuteFail(String relativeUrl) {
        requestRelativeUrl = relativeUrl;
    }

    public String getRequestRelativeUrl() {
        return requestRelativeUrl;
    }
}
