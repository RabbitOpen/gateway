package rabbit.gateway.runtime.plugin;

import rabbit.gateway.common.PluginType;

/**
 * 请求插件
 * @param <T>
 */
public abstract class RequestPlugin<T> extends RuntimePlugin<T> {

    @Override
    protected PluginType getPluginType() {
        return PluginType.REQUEST;
    }
}
