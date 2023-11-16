package rabbit.gateway.common;

public enum PluginName {

    authentication,         // 认证

    addRequestHeaders,      // 添加header

    removeRequestHeaders,   // 移除header

    requestMapping,         // 请求映射

    requestRateLimit,       // 限流

    addResponseHeaders      // 添加response header
}
