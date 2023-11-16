package rabbit.gateway.runtime.exception;

import rabbit.gateway.common.exception.GateWayException;

public class EmptyApiCodeException extends GateWayException {

    public EmptyApiCodeException(String uri) {
        super(String.format("invalid request[%s], api code is not found!", uri));
    }
}
