package rabbit.gateway.common;

public class Result {

    private String message;

    private ErrorType errorType;

    public static Result failed(String message, ErrorType errorType) {
        Result result = new Result();
        result.setMessage(message);
        result.setErrorType(errorType);
        return result;
    }

    public static Result failed(String message) {
        return failed(message, ErrorType.GATEWAY);
    }

    public String getMessage() {
        return message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }
}
