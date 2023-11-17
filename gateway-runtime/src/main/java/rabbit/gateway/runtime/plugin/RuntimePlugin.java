package rabbit.gateway.runtime.plugin;

import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import reactor.core.publisher.Mono;

import static rabbit.gateway.common.PluginType.RESPONSE;

/**
 * 运行时插件
 */
public abstract class RuntimePlugin extends Plugin {

    public RuntimePlugin(Plugin plugin) {
        BeanUtils.copyProperties(plugin, this);
    }

    /**
     * 优先级
     *
     * @return
     */
    public abstract Integer getPriority();

    /**
     * 运行插件
     *
     * @param requestContext
     * @param chain
     * @return
     */
    public final Mono<ResponseEntity<String>> execute(HttpRequestContext requestContext, PluginChain chain) {
        return executeInternal(requestContext).map(resp -> {
            if (RESPONSE == getType()) {
                // 用响应插件返回的response entity替换当前的entity
                requestContext.setResponseEntity(resp);
            }
            return resp;
        }).switchIfEmpty(chain.doChain(requestContext));
    }

    /**
     * 执行插件逻辑
     *
     * @param requestContext
     * @return
     */
    protected abstract Mono<ResponseEntity<String>> executeInternal(HttpRequestContext requestContext);
}
