package rabbit.gateway.runtime.exception;

import rabbit.gateway.common.exception.GateWayException;

public class EmptyApiCodeException extends GateWayException {

    public EmptyApiCodeException(String uri) {
        super(String.format("非法请求[%s], api code不能为空!", uri));
    }
}
