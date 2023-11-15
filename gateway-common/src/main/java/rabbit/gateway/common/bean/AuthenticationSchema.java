package rabbit.gateway.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import rabbit.gateway.common.Schema;

import java.security.PublicKey;

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
    @JsonIgnore
    private PublicKey innerPublicKey;

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

    public PublicKey getInnerPublicKey() {
        return innerPublicKey;
    }

    public void setInnerPublicKey(PublicKey innerPublicKey) {
        this.innerPublicKey = innerPublicKey;
    }

    public String getInnerCredential() {
        return innerCredential;
    }

    public void setInnerCredential(String innerCredential) {
        this.innerCredential = innerCredential;
    }
}
