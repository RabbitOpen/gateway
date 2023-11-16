package rabbit.gateway.runtime.plugin.request;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.bean.HeaderAddSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

/**
 * 添加请求头插件
 */
public class AddRequestHeaderPlugin extends RuntimePlugin<HeaderAddSchema> {

    public AddRequestHeaderPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Integer getPriority() {
        return 2000;
    }

    @Override
    protected Mono<ResponseEntity<String>> executeInternal(HttpRequestContext requestContext) {
        HeaderAddSchema schema = getSchema();
        if (null != schema) {
            schema.getHeaders().forEach(requestContext::addHeader);
        }
        return Mono.empty();
    }
}
