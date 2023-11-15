package rabbit.gateway.runtime.plugin;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.PluginType;
import rabbit.gateway.common.entity.Plugin;
import reactor.core.publisher.Mono;

/**
 * 运行时插件
 *
 * @param <T> schema
 */
public abstract class RuntimePlugin<T> extends Plugin {

    /**
     * 插件类型
     * @return
     */
    protected abstract PluginType getPluginType();

    /**
     * 优先级
     * @return
     */
    public abstract Integer getPriority();

    public Mono<ResponseEntity<String>> execute() {
        return Mono.empty();
    }
}
