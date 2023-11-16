package rabbit.gateway.common;

public enum PluginName {

    AUTHENTICATION,                     // 认证

    ADD_REQUEST_HEADERS,                // 添加header

    REMOVE_REQUEST_HEADERS,             // 移除header

    REQUEST_MAPPING,                    // 请求映射

    REQUEST_RATE_LIMIT,                 // 限流

    ADD_RESPONSE_HEADERS;               // 添加response header

}
