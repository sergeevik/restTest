package tester.exception;

public class ExecuteFailException extends RuntimeException{
    private String requestRelativeUrl;
    public ExecuteFailException(String relativeUrl) {
        requestRelativeUrl = relativeUrl;
    }

    public String getRequestRelativeUrl() {
        return requestRelativeUrl;
    }
}
