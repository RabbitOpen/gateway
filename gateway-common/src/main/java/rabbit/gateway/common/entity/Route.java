package rabbit.gateway.common.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.http.HttpMethod;
import rabbit.gateway.common.BaseEntity;
import rabbit.gateway.common.bean.RequestRateLimit;

import java.util.Map;

/**
 * 路由
 */
@Table("route")
public class Route extends BaseEntity {

    /**
     * code
     */
    @Column("code")
    private String code;

    /**
     * 服务code
     */
    @Column("service_code")
    private String serviceCode;

    /**
     * 方法类型
     */
    @Column("method")
    private HttpMethod method;

    /**
     * 请求路径
     */
    @Column("path")
    private String path;

    /**
     * 转发路径
     */
    @Column("mapping_uri")
    private String mappingUri;

    /**
     * 路由规则
     */
    @Column("rules")
    private Map<String, String> rules;

    /**
     * 限流设置
     */
    @Column("request_rate_limit")
    private RequestRateLimit requestRateLimit;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMappingUri() {
        return mappingUri;
    }

    public void setMappingUri(String mappingUri) {
        this.mappingUri = mappingUri;
    }

    public RequestRateLimit getRequestRateLimit() {
        return requestRateLimit;
    }

    public void setRequestRateLimit(RequestRateLimit requestRateLimit) {
        this.requestRateLimit = requestRateLimit;
    }

    public Map<String, String> getRules() {
        return rules;
    }

    public void setRules(Map<String, String> rules) {
        this.rules = rules;
    }
}
