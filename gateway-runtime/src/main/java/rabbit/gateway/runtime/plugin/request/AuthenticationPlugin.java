package rabbit.gateway.runtime.plugin.request;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.bean.AuthenticationSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

/**
 * 认证插件
 */
public class AuthenticationPlugin extends RuntimePlugin<AuthenticationSchema> {

    /**
     * 最大请求体长度
     */
    private static final long MAX_CONTENT_LENGTH = 2L * 1024 * 1024;

    public AuthenticationPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Integer getPriority() {
        return 1000;
    }

    @Override
    protected Mono<ResponseEntity<String>> executeInternal(HttpRequestContext context) {
        return null;
    }
}
