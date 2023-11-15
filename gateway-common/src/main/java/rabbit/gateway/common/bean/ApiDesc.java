package rabbit.gateway.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.pattern.PathPattern;

public class ApiDesc {

    /**
     * 路径
     */
    private String path;

    /**
     * 方法类型
     */
    private HttpMethod method;

    /**
     * 所属服务
     */
    private String serviceCode;

    /**
     * 失效日期
     */
    private long invalidDate;

    @JsonIgnore
    private PathPattern pattern;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public long getInvalidDate() {
        return invalidDate;
    }

    public void setInvalidDate(long invalidDate) {
        this.invalidDate = invalidDate;
    }

    public PathPattern getPattern() {
        return pattern;
    }

    public void setPattern(PathPattern pattern) {
        this.pattern = pattern;
    }
}
