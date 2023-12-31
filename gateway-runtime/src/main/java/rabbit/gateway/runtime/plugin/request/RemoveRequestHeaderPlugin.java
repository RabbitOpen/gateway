package rabbit.gateway.runtime.plugin.request;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.bean.HeaderRemoveSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

import static rabbit.gateway.runtime.plugin.request.AuthenticationPlugin.SERVICE_ROUTE_HEADER;

public class RemoveRequestHeaderPlugin extends RuntimePlugin {

    public RemoveRequestHeaderPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Integer getPriority() {
        return 3000;
    }

    @Override
    protected Mono<ResponseEntity<String>> executeInternal(HttpRequestContext requestContext) {
        HeaderRemoveSchema schema = getSchema();
        requestContext.removeHeader(SERVICE_ROUTE_HEADER);
        if (null != schema) {
            schema.getHeaders().forEach(requestContext::removeHeader);
        }
        return Mono.empty();
    }
}
