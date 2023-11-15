package rabbit.gateway.common;

public enum PluginName {

    authentication,         // 认证

    addHeaders,             // 添加header

    removeHeaders,          // 移除header

    requestMapping,         // 请求映射

    requestRateLimit;       // 限流
}
