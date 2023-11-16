package rabbit.gateway.runtime.context;

import org.springframework.http.ResponseEntity;
import rabbit.gateway.common.PluginName;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.plugin.PluginChain;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import rabbit.gateway.runtime.plugin.request.AuthenticationPlugin;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static rabbit.gateway.common.PluginType.REQUEST;

public class PluginManager {

    /**
     * 请求插件
     */
    private List<RuntimePlugin> requestPlugins = new ArrayList<>();

    /**
     * 响应插件
     */
    private List<RuntimePlugin> responsePlugins = new ArrayList<>();

    /**
     * 插件转换器
     */
    private static final Map<PluginName, Function<Plugin, RuntimePlugin>> pluginConverter = new ConcurrentHashMap<>();

    /**
     * 添加插件
     *
     * @param plugin
     */
    public void addPlugin(Plugin plugin) {
        if (REQUEST == plugin.getType()) {
            List<RuntimePlugin> plugins = requestPlugins.stream().filter(f -> !f.getName().equals(plugin.getName())).collect(Collectors.toList());
            plugins.add(pluginConverter.get(plugin.getName()).apply(plugin));
            Collections.sort(plugins, Comparator.comparing(RuntimePlugin::getPriority));
            this.requestPlugins = plugins;
        } else {
            List<RuntimePlugin> plugins = responsePlugins.stream().filter(f -> !f.getName().equals(plugin.getName())).collect(Collectors.toList());
            plugins.add( pluginConverter.get(plugin.getName()).apply(plugin));
            Collections.sort(plugins, Comparator.comparing(RuntimePlugin::getPriority));
            this.responsePlugins = plugins;
        }
    }

    /**
     * 处理请求
     * @param context
     * @return
     */
    public Mono<ResponseEntity<String>> handleRequest(HttpRequestContext context) {
        return new PluginChain(requestPlugins).doChain(context);
    }

    /**
     * 处理response
     * @param context
     * @return
     */
    public Mono<ResponseEntity<String>> handleResponse(HttpRequestContext context) {
        return new PluginChain(responsePlugins).doChain(context);
    }

    static {
        pluginConverter.put(PluginName.authentication, p -> new AuthenticationPlugin(p));
    }

}
