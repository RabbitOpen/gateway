package rabbit.gateway.runtime.plugin.response;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.bean.HeaderAddSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

/**
 * 添加响应头
 */
public class AddResponseHeaderPlugin extends RuntimePlugin {

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
        ResponseEntity<String> response = requestContext.getResponseEntity();
        HttpHeaders headers = new HttpHeaders();
        response.getHeaders().forEach(headers::put);
        schema.getHeaders().forEach(headers::set);
        return Mono.just(new ResponseEntity<>(response.getBody(), headers, response.getStatusCode()));
    }
}
