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
        RequestRateLimit globalLimit = getSchema();
        RequestRateLimit routeLimit = requestContext.getRoute().getRequestRateLimit();
        return Mono.empty();
    }
}
