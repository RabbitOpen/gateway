package rabbit.gateway.common.context;

public interface PluginContext {

    /**
     * 重新加载服务插件
     * @param serviceCode
     */
    void reloadPlugins(String serviceCode);

}
