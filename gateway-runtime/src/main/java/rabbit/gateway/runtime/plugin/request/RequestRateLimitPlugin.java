package rabbit.gateway.runtime.plugin.request;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.bean.RequestRateLimit;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.common.exception.GateWayException;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import rabbit.gateway.runtime.service.LimitService;
import rabbit.gateway.runtime.service.SpringContext;
import reactor.core.publisher.Mono;

import static rabbit.gateway.runtime.service.LimitService.Status.CLIENT_LIMIT;
import static rabbit.gateway.runtime.service.LimitService.Status.SERVER_LIMIT;

/**
 * 限流插件
 */
public class RequestRateLimitPlugin extends RuntimePlugin {

    public RequestRateLimitPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Integer getPriority() {
        return 1200;
    }

    @Override
    protected Mono<ResponseEntity<String>> executeInternal(HttpRequestContext requestContext) {
        // 获取服务侧限流设置
        long serverLimit = getServerLimit(requestContext);
        // 获取客户侧限流设置
        long clientLimit = getClientLimit(requestContext);
        LimitService limitService = SpringContext.getBean(LimitService.class);
        // key: apiCode@credential
        String key = requestContext.getApiCode().concat("@").concat(requestContext.getCredential());
        if (SERVER_LIMIT == limitService.limit(key, 1, clientLimit, serverLimit)) {
            throw new GateWayException(HttpStatus.TOO_MANY_REQUESTS.value(), String.format("beyond server limit setting[%s]", serverLimit));
        }
        if (CLIENT_LIMIT == limitService.limit(key, 1, clientLimit, serverLimit)) {
            throw new GateWayException(HttpStatus.TOO_MANY_REQUESTS.value(), String.format("beyond client limit setting[%s]", clientLimit));
        }
        return Mono.empty();
    }

    /**
     * 获取服务侧限流设置
     *
     * @param requestContext
     * @return
     */
    private long getServerLimit(HttpRequestContext requestContext) {
        RequestRateLimit globalLimit = getSchema();
        RequestRateLimit routeLimit = requestContext.getRoute().getRequestRateLimit();
        long globalServerLimit = globalLimit.getServerDefault();
        Long routeServerLimit = routeLimit.getServerDefault();
        if (null == routeServerLimit) {
            return globalServerLimit;
        } else {
            return Math.min(globalServerLimit, routeServerLimit);
        }
    }

    private long getClientLimit(HttpRequestContext requestContext) {
        RequestRateLimit globalLimit = getSchema();
        RequestRateLimit routeLimit = requestContext.getRoute().getRequestRateLimit();
        long globalClientLimit = globalLimit.getClientDefault();
        if (null != routeLimit) {
            String credential = requestContext.getCredential();
            if (routeLimit.getClients().containsKey(credential)) {
                return Math.min(globalClientLimit, routeLimit.getClients().get(credential));
            }
            return Math.min(globalClientLimit, routeLimit.getClientDefault());
        } else {
            return globalClientLimit;
        }
    }
}
