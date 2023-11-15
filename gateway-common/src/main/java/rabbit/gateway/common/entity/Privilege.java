package rabbit.gateway.common.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rabbit.gateway.common.BaseEntity;
import rabbit.gateway.common.bean.ApiDesc;

import java.util.Map;

/**
 * 权限
 */
@Table("privilege")
public class Privilege extends BaseEntity {

    /**
     * 凭据
     */
    @Column("credential")
    private String credential;

    /**
     * 公钥
     */
    @Column("public_key")
    private String publicKey;

    /**
     * 私钥
     */
    @Column("private_key")
    private String privateKey;

    /**
     * 授权，key是apiCode，等同route code
     */
    @Column("privileges")
    private Map<String, ApiDesc> privileges;

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Map<String, ApiDesc> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Map<String, ApiDesc> privileges) {
        this.privileges = privileges;
    }
}
