package rabbit.gateway.common.exception;

public class NoRouteException extends GateWayException {

    public NoRouteException(String apiCode) {
        super(String.format("未定义的路由[%s]", apiCode));
    }
}
