package rabbit.gateway.runtime.context;

import org.springframework.beans.BeanUtils;
import rabbit.gateway.common.entity.Privilege;
import rabbit.gateway.common.utils.RsaUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

public class PrivilegeDesc extends Privilege {

    private PrivateKey rsaPrivateKey;

    private PublicKey rsaPublicKey;

    public PrivilegeDesc(Privilege privilege) {
        BeanUtils.copyProperties(privilege, this);
        rsaPrivateKey = RsaUtils.loadPrivateKeyFromString(getPrivateKey());
        rsaPublicKey = RsaUtils.loadPublicKeyFromString(getPublicKey());
    }

    public PrivateKey getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public PublicKey getRsaPublicKey() {
        return rsaPublicKey;
    }
}
