package rabbit.gateway.runtime.plugin;

import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.context.HttpRequestContext;
import reactor.core.publisher.Mono;

/**
 * 运行时插件
 *
 * @param <T> schema
 */
public abstract class RuntimePlugin<T> extends Plugin {

    public RuntimePlugin(Plugin plugin) {
        BeanUtils.copyProperties(plugin, this);
    }

    /**
     * 优先级
     * @return
     */
    public abstract Integer getPriority();

    /**
     * 运行插件
     * @param context
     * @param chain
     * @return
     */
    public final Mono<ResponseEntity<String>> execute(HttpRequestContext context, PluginChain chain) {
        return executeInternal(context).switchIfEmpty(chain.doChain(context));
    }

    /**
     * 执行插件逻辑
     * @param context
     * @return
     */
    protected abstract Mono<ResponseEntity<String>> executeInternal(HttpRequestContext context);
}
