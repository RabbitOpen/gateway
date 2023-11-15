package rabbit.gateway.runtime.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import rabbit.gateway.common.Result;
import rabbit.gateway.common.utils.JsonUtils;
import reactor.core.publisher.Mono;

/**
 * 核心dispatcher
 */
@Component
public class RequestDispatcher implements WebFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 管理端口
     */
    @Value("${server.port}")
    private int managePort;

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
            return dispatchOpenApiRequest(exchange);
        }
    }

    /**
     * 开放接口路由
     *
     * @param exchange
     * @return
     */
    private Mono<Void> dispatchOpenApiRequest(ServerWebExchange exchange) {
        return Mono.empty();
    }
}
