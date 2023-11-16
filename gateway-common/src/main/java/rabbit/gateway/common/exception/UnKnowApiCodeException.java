package rabbit.gateway.common.exception;

public class UnKnowApiCodeException extends GateWayException {

    public UnKnowApiCodeException(String apiCode) {
        super(String.format("unknown api code [%s]", apiCode));
    }
}
