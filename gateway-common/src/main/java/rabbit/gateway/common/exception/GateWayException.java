package rabbit.gateway.common.exception;

import org.springframework.http.HttpStatus;

public class GateWayException extends RuntimeException {

    private int statusCode;

    public GateWayException(String message) {
        this(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    public GateWayException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public GateWayException(Throwable cause) {
        super(cause);
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    public int getStatusCode() {
        return statusCode;
    }
}
