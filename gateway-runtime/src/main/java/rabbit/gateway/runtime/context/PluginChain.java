package rabbit.gateway.runtime.context;

import rabbit.gateway.common.PluginName;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.plugin.RequestPlugin;
import rabbit.gateway.runtime.plugin.ResponsePlugin;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import rabbit.gateway.runtime.plugin.impl.AuthenticationPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static rabbit.gateway.common.PluginType.REQUEST;

public class PluginChain {

    /**
     * 请求插件
     */
    private List<RequestPlugin> requestPlugins = new ArrayList<>();

    /**
     * 响应插件
     */
    private List<ResponsePlugin> responsePlugins = new ArrayList<>();

    /**
     * 插件转换器
     */
    private static final Map<PluginName, Function<Plugin, Plugin>> pluginConverter = new ConcurrentHashMap<>();

    /**
     * 添加插件
     *
     * @param plugin
     */
    public void addPlugin(Plugin plugin) {
        if (REQUEST == plugin.getType()) {
            List<RequestPlugin> plugins = requestPlugins.stream().filter(f -> !f.getName().equals(plugin.getName())).collect(Collectors.toList());
            plugins.add((RequestPlugin) pluginConverter.get(plugin.getName()).apply(plugin));
            Collections.sort(plugins, Comparator.comparing(RuntimePlugin::getPriority));
            this.requestPlugins = plugins;
        } else {
            List<ResponsePlugin> plugins = responsePlugins.stream().filter(f -> !f.getName().equals(plugin.getName())).collect(Collectors.toList());
            plugins.add((ResponsePlugin) pluginConverter.get(plugin.getName()).apply(plugin));
            Collections.sort(plugins, Comparator.comparing(RuntimePlugin::getPriority));
            this.responsePlugins = plugins;
        }
    }

    static {
        pluginConverter.put(PluginName.authentication, p -> new AuthenticationPlugin(p));
    }

}
