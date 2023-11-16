package rabbit.gateway.runtime.plugin.request;

import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.common.entity.Route;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求映射插件
 */
public class RequestMappingPlugin extends RuntimePlugin {

    public RequestMappingPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Integer getPriority() {
        return 4000;
    }

    @Override
    protected Mono<ResponseEntity<String>> executeInternal(HttpRequestContext requestContext) {
        Route route = requestContext.getRoute();
        if (ObjectUtils.isEmpty(route.getMappingUri())) {
            return Mono.empty();
        }
        String[] values = requestContext.getRequestPath().split("/");
        String[] keys = route.getPath().split("/");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        String mappingUri = route.getMappingUri();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            mappingUri = mappingUri.replace(entry.getKey(), entry.getValue());
        }
        requestContext.setRequestPath(mappingUri);
        return Mono.empty();
    }
}
