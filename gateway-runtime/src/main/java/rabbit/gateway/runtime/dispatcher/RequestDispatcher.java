package rabbit.gateway.runtime.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import rabbit.gateway.common.Result;
import rabbit.gateway.common.utils.JsonUtils;
import rabbit.gateway.runtime.context.GateWayContext;
import rabbit.gateway.runtime.context.HttpClientFactory;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.context.PluginManager;
import reactor.core.publisher.Mono;

/**
 * 核心dispatcher
 */
@Component
public class RequestDispatcher implements WebFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected GateWayContext gateWayContext;

    @Autowired
    protected HttpClientFactory clientFactory;

    /**
     * 管理端口
     */
    @Value("${server.port}")
    protected int managePort;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getLocalAddress().getPort() == managePort) {
            return chain.filter(exchange).onErrorResume(e -> {
                logger.warn(e.getMessage(), e);
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                byte[] errorBytes = JsonUtils.writeObject(Result.failed(e.getMessage())).getBytes();
                DataBuffer wrap = response.bufferFactory().wrap(errorBytes);
                return response.writeWith(Mono.just(wrap));
            });
        } else {
            return dispatchOpenApiRequest(new HttpRequestContext(exchange, gateWayContext));
        }
    }

    /**
     * 开放接口路由
     *
     * @param context
     * @return
     */
    private Mono<Void> dispatchOpenApiRequest(HttpRequestContext context) {
        PluginManager pluginManager = gateWayContext.getPluginManager(context.getService().getCode());
        return pluginManager.handleRequest(context)
                .flatMap(r -> responseData(context, r))     // 如果插件有输出则直接输出响应
                .switchIfEmpty(Mono.defer(() -> clientFactory.execute(context))
                        .flatMap(r -> {
                            context.setResponseEntity(r);
                            // 响应插件
                            return pluginManager.handleResponse(context).flatMap(resp -> responseData(context, resp));
                        }));
    }

    /**
     * 输出响应
     *
     * @param context
     * @param responseEntity
     * @return
     */
    private Mono<Void> responseData(HttpRequestContext context, ResponseEntity<String> responseEntity) {
        ServerHttpResponse response = context.getResponse();
        // 填充状态码
        response.setRawStatusCode(responseEntity.getStatusCodeValue());
        // 填充响应头
        responseEntity.getHeaders().forEach((key, value) -> {
            if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(key)) {
                return;
            }
            response.getHeaders().set(key, value.get(0));
        });
        byte[] bytes = getResponseBodyBytes(responseEntity);
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * 读取 response body
     *
     * @param responseEntity
     * @return
     */
    private byte[] getResponseBodyBytes(ResponseEntity<String> responseEntity) {
        String body = responseEntity.getBody();
        byte[] bytes;
        if (ObjectUtils.isEmpty(body)) {
            bytes = new byte[0];
        } else {
            bytes = body.getBytes();
        }
        return bytes;
    }
}
