package rabbit.gateway.runtime.plugin;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.runtime.context.HttpRequestContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class PluginChain {

    private List<RuntimePlugin> plugins;

    private int index = 0;

    public PluginChain(List<RuntimePlugin> plugins) {
        this.plugins = new ArrayList<>(plugins);
    }

    /**
     * 执行插件链
     * @param context
     * @return
     */
    public Mono<ResponseEntity<String>> doChain(HttpRequestContext context) {
        if (index == plugins.size()) {
            return Mono.empty();
        }
        return nextPlugin().execute(context, this);
    }

    private RuntimePlugin nextPlugin() {
        RuntimePlugin plugin = plugins.get(index);
        index++;
        return plugin;
    }

}
