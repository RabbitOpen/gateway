package rabbit.gateway.runtime.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import rabbit.discovery.api.common.utils.JsonUtils;
import rabbit.gateway.common.Result;
import rabbit.gateway.common.exception.GateWayException;
import rabbit.gateway.runtime.context.GateWayContext;
import rabbit.gateway.runtime.context.HttpClientFactory;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.context.PluginManager;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

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
                if (e instanceof GateWayException) {
                    logger.error(e.getMessage());
                } else {
                    logger.error(e.getMessage(), e);
                }
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                byte[] errorBytes = JsonUtils.writeObject(Result.failed(e.getMessage())).getBytes(Charset.forName("UTF8"));
                DataBuffer buffer = response.bufferFactory().wrap(errorBytes);
                return response.writeWith(Mono.just(buffer)).then(Mono.defer(() -> {
                    DataBufferUtils.release(buffer);
                    return Mono.empty();
                }));
            });
        } else {
            HttpRequestContext context = new HttpRequestContext(exchange, gateWayContext);
            return Mono.defer(() -> dispatchOpenApiRequest(context)).onErrorResume(e -> {
                String result = e.getMessage();
                int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
                if (e instanceof GateWayException) {
                    statusCode = ((GateWayException) e).getStatusCode();
                }
                logger.warn(e.getMessage());
                context.setResponseEntity(new ResponseEntity<>(result, null, statusCode));
                return responseData(context);
            });
        }
    }

    /**
     * 开放接口路由
     *
     * @param context
     * @return
     */
    private Mono<Void> dispatchOpenApiRequest(HttpRequestContext context) {
        context.loadRoute();
        context.loadService();
        PluginManager pluginManager = gateWayContext.getPluginManager(context.getService().getCode());
        return pluginManager.handleRequest(context)
                .flatMap(r -> responseData(context))                            // 如果插件有输出则直接输出响应
                .switchIfEmpty(Mono.defer(() -> clientFactory.execute(context)) // 调用服务
                        .map(context::setResponseEntity)                        // 设置response
                        // 执行response 插件 然后输出结果
                        .flatMap(ctx -> pluginManager.handleResponse(ctx).flatMap(r -> responseData(context))));
    }

    /**
     * 输出响应
     *
     * @param context
     */
    private Mono<Void> responseData(HttpRequestContext context) {
        return Mono.defer(() -> {
            ServerHttpResponse response = context.getResponse();
            ResponseEntity<String> responseEntity = context.getResponseEntity();
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
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer)).then(Mono.defer(() -> {
                DataBufferUtils.release(buffer);
                return Mono.empty();
            }));
        });
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
            bytes = body.getBytes(Charset.forName("UTF8"));
        }
        return bytes;
    }
}
