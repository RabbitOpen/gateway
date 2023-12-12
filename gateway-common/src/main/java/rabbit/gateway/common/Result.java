package rabbit.gateway.common;

public class Result {

    private String message;

    public static Result failed(String message) {
        Result result = new Result();
        result.setMessage(message);
        return result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
