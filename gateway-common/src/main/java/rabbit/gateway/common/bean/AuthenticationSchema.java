package rabbit.gateway.common.bean;

import rabbit.gateway.common.Schema;

public class AuthenticationSchema implements Schema {

    /**
     * 允许的时间偏移
     */
    private long offsetSeconds = 30;

    /**
     * 内置凭据公钥
     */
    private String publicKey;

    /**
     * 内置凭据
     */
    private String innerCredential;

    /**
     * 内置凭据公钥
     */
    private String innerPublicKey;

    /**
     * 路由header的值
     */
    private String routeHeaderValue;

    public long getOffsetSeconds() {
        return offsetSeconds;
    }

    public void setOffsetSeconds(long offsetSeconds) {
        this.offsetSeconds = offsetSeconds;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getInnerPublicKey() {
        return innerPublicKey;
    }

    public void setInnerPublicKey(String innerPublicKey) {
        this.innerPublicKey = innerPublicKey;
    }

    public String getInnerCredential() {
        return innerCredential;
    }

    public void setInnerCredential(String innerCredential) {
        this.innerCredential = innerCredential;
    }

    public String getRouteHeaderValue() {
        return routeHeaderValue;
    }

    public void setRouteHeaderValue(String routeHeaderValue) {
        this.routeHeaderValue = routeHeaderValue;
    }
}
