package rabbit.gateway.runtime.context;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import rabbit.discovery.api.common.Headers;
import rabbit.gateway.common.entity.Route;
import rabbit.gateway.common.exception.GateWayException;
import rabbit.gateway.common.exception.UnKnowApiCodeException;
import rabbit.gateway.runtime.exception.EmptyApiCodeException;

import java.util.*;

public class HttpRequestContext {

    /**
     * 要添加的头
     */
    private Map<String, String> headers2Add = new HashMap<>();

    /**
     * 要删除的头
     */
    private Set<String> headers2Remove = new HashSet<>();

    private ServerWebExchange exchange;

    private GateWayContext gateWayContext;

    /**
     * 响应数据
     */
    private ResponseEntity<String> responseEntity;

    /**
     * 当前请求的路由信息
     */
    private Route route;

    /**
     * 当前请求对应的服务
     */
    private GatewayService service;

    /**
     * 请求路径
     */
    private String requestPath;


    public HttpRequestContext(ServerWebExchange exchange, GateWayContext context) {
        this.exchange = exchange;
        this.gateWayContext = context;
        this.requestPath = getRequest().getPath().value();
        this.route = loadRoute(context);
        this.service = loadService(context);
    }

    private GatewayService loadService(GateWayContext context) {
        GatewayService currentService = context.getService(route.getServiceCode());
        if (null == currentService) {
            throw new GateWayException(String.format("服务[%s]信息不存在", route.getServiceCode()));
        }
        return currentService;
    }

    /**
     * 加载路由
     *
     * @param context
     * @return
     */
    private Route loadRoute(GateWayContext context) {
        String apiCode = getApiCode();
        if (ObjectUtils.isEmpty(apiCode)) {
            throw new EmptyApiCodeException(getRequestPath());
        }
        Route currentRoute = context.getRoute(apiCode);
        if (null == currentRoute) {
            throw new UnKnowApiCodeException(apiCode);
        }
        if (getRequest().getMethod() != currentRoute.getMethod()) {
            throw new GateWayException("request method type is not matched!");
        }
        if (!CollectionUtils.isEmpty(currentRoute.getRules())) {
            currentRoute.getRules().forEach((name, value) -> {
                if (!Objects.equals(value, getRouteHeaderValue(name))) {
                    throw new GateWayException(String.format("route rule is not matched for header[%s]!", name));
                }
            });
        }
        return currentRoute;
    }

    /**
     * 获取路由规则头的值
     *
     * @param name
     * @return
     */
    private String getRouteHeaderValue(String name) {
        String routeValue = headers2Add.get(name);
        if (!ObjectUtils.isEmpty(routeValue)) {
            return routeValue;
        }
        return getRequest().getHeaders().getFirst(name);
    }

    /**
     * 获取请求对象
     *
     * @return
     */
    public ServerHttpRequest getRequest() {
        return exchange.getRequest();
    }

    public Map<String, String> getHeaders2Add() {
        return headers2Add;
    }

    /**
     * 添加header
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headers2Add.put(name, value);
    }

    /**
     * 移除header
     *
     * @param name
     */
    public void removeHeader(String name) {
        headers2Remove.add(name);
    }

    /**
     * 获取响应对象
     *
     * @return
     */
    public ServerHttpResponse getResponse() {
        return exchange.getResponse();
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    /**
     * 获取api code
     *
     * @return
     */
    public String getApiCode() {
        return getRequest().getHeaders().getFirst(Headers.OPEN_API_CODE);
    }

    /**
     * 获取请求凭据
     *
     * @return
     */
    public String getCredential() {
        return getRequest().getHeaders().getFirst(Headers.OPEN_API_CREDENTIAL);
    }

    /**
     * 获取请求时间
     *
     * @return
     */
    public String getRequestTime() {
        return getRequest().getHeaders().getFirst(Headers.OPEN_API_REQUEST_TIME);
    }

    /**
     * 获取请求时间签名
     *
     * @return
     */
    public String getRequestTimeSignature() {
        return getRequest().getHeaders().getFirst(Headers.OPEN_API_REQUEST_TIME_SIGNATURE);
    }

    /**
     * 获取当前路由
     *
     * @return
     */
    public Route getRoute() {
        return route;
    }

    /**
     * 获取当前服务
     *
     * @return
     */
    public GatewayService getService() {
        return service;
    }

    /**
     * 是否要移除该header
     *
     * @param name
     * @return
     */
    public boolean isRemovedHeader(String name) {
        return headers2Remove.contains(name);
    }

    public ResponseEntity<String> getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(ResponseEntity<String> responseEntity) {
        this.responseEntity = responseEntity;
    }

    /**
     * 获取当前消费方的权限
     *
     * @return
     */
    public PrivilegeDesc getPrivilege() {
        return gateWayContext.getPrivilege(getCredential());
    }


}
