package rabbit.gateway.runtime.plugin.request;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import rabbit.gateway.common.Headers;
import rabbit.gateway.common.bean.ApiDesc;
import rabbit.gateway.common.bean.AuthenticationSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.common.exception.GateWayException;
import rabbit.gateway.runtime.context.HttpRequestContext;
import rabbit.gateway.runtime.context.PrivilegeDesc;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import reactor.core.publisher.Mono;

import static rabbit.gateway.common.utils.HexUtils.toBytes;
import static rabbit.gateway.common.utils.RsaUtils.verifyWithPublicKey;

/**
 * 认证插件
 */
public class AuthenticationPlugin extends RuntimePlugin<AuthenticationSchema> {

    /**
     * 最大请求体长度
     */
    private static final long MAX_CONTENT_LENGTH = 2L * 1024 * 1024;

    public AuthenticationPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Integer getPriority() {
        return 1000;
    }

    @Override
    protected Mono<ResponseEntity<String>> executeInternal(HttpRequestContext requestContext) {
        if (requestContext.getRequest().getHeaders().getContentLength() > MAX_CONTENT_LENGTH) {
            throw new GateWayException(HttpStatus.FORBIDDEN.value(), "请求body过大！");
        }
        assertEmpty(requestContext.getApiCode(), "接口编码不能为空");
        assertEmpty(requestContext.getCredential(), "消费凭据不能为空");
        assertEmpty(requestContext.getPrivilege(), "访问受限，没有对应接口的访问权限!");
        assertEmpty(requestContext.getRequestTimeSignature(), "请求时间签名信息不能为空");
        // 验签
        verifySignature(requestContext);
        // 鉴权
        verifyAuthorization(requestContext);
        // 检查访问路径是否和api code 匹配，防止越权访问
        verifyRequestPath(requestContext);
        // 添加路由增强头
        addServiceRouteHeader(requestContext);
        return Mono.empty();
    }

    /**
     * 添加路由增强头
     * @param requestContext
     */
    private void addServiceRouteHeader(HttpRequestContext requestContext) {
        AuthenticationSchema schema = getSchema();
        requestContext.getHeaders2Add().put(Headers.serviceRouteHeader.name(), schema.getRouteHeaderValue());
    }

    /**
     * 检查访问路径是否和api code 匹配
     *
     * @param context
     */
    private void verifyRequestPath(HttpRequestContext context) {
        PrivilegeDesc privilege = context.getPrivilege();
        ApiDesc apiDesc = privilege.getPrivileges().get(context.getApiCode());
        if (!apiDesc.getPattern().matches(PathContainer.parsePath(context.getRequestPath()))) {
            String message = String.format("越权访问，路径信息不匹配！required: [%s], actual: [%s]", apiDesc.getPath(), context.getRequestPath());
            throw new GateWayException(HttpStatus.FORBIDDEN.value(), message);
        }
    }

    /**
     * 检查是否有权限
     *
     * @param context
     */
    private void verifyAuthorization(HttpRequestContext context) {
        String apiCode = assertEmpty(context.getApiCode(), "接口编码不能为空");
        String credential = assertEmpty(context.getCredential(), "消费凭据不能为空");
        PrivilegeDesc privilege = assertEmpty(context.getPrivilege(), "访问受限，没有对应接口的访问权限!");
        if (CollectionUtils.isEmpty(privilege.getPrivileges()) || !privilege.getPrivileges().containsKey(apiCode)) {
            String message = String.format("credential[%s] is not allowed to access api[%s]", credential, apiCode);
            throw new GateWayException(HttpStatus.FORBIDDEN.value(), message);
        }
    }

    /**
     * 验签
     *
     * @param context
     */
    private void verifySignature(HttpRequestContext context) {
        String requestTime = context.getRequestTime();
        String signature = context.getRequestTimeSignature();
        try {
            if (!verifyWithPublicKey(toBytes(signature), requestTime, context.getPrivilege().getRsaPublicKey())) {
                throw new GateWayException("验签失败");
            }
        } catch (Exception e) {
            throw new GateWayException(e.getMessage());
        }
        AuthenticationSchema schema = getSchema();
        long expectedOffset = schema.getOffsetSeconds() * 1000;
        long offset = Math.abs(System.currentTimeMillis() - Long.parseLong(requestTime));
        if (offset > expectedOffset) {
            String message = String.format("访问时间偏差过大! expected: %d ms, actual: %d ms", expectedOffset, offset);
            throw new GateWayException(message);
        }
    }

    private <T> T assertEmpty(T value, String message) {
        if (ObjectUtils.isEmpty(value)) {
            throw new GateWayException(message);
        }
        return value;
    }
}
