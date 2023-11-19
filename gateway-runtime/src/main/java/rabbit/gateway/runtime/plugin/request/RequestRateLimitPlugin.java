package rabbit.gateway.runtime.plugin.request;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.bean.RequestRateLimit;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

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
        // 获取服务侧限流
        long serverLimit = getServerLimit(requestContext);
        return Mono.empty();
    }

    /**
     * 获取服务侧限流
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
}
