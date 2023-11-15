package rabbit.gateway.runtime.plugin;

import rabbit.gateway.common.PluginType;

/**
 * 响应插件
 * @param <T>
 */
public abstract class ResponsePlugin<T> extends RuntimePlugin<T> {

    @Override
    protected PluginType getPluginType() {
        return PluginType.RESPONSE;
    }

}
