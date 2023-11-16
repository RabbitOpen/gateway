package rabbit.gateway.runtime.plugin.response;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.bean.HeaderAddSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

/**
 * 添加响应头
 */
public class AddResponseHeaderPlugin extends RuntimePlugin<HeaderAddSchema> {

    public AddResponseHeaderPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Integer getPriority() {
        return 1000;
    }

    @Override
    protected Mono<ResponseEntity<String>> executeInternal(HttpRequestContext requestContext) {
        HeaderAddSchema schema = getSchema();
        if (null != schema) {
            schema.getHeaders().forEach(requestContext.getResponseEntity().getHeaders()::set);
        }
        return Mono.empty();
    }
}
