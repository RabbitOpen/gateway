package rabbit.gateway.common.exception;

public class GateWayException extends RuntimeException {

    public GateWayException() {
    }

    public GateWayException(String message) {
        super(message);
    }

    public GateWayException(Throwable cause) {
        super(cause);
    }
}
