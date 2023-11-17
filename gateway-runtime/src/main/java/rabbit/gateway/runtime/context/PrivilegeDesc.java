package rabbit.gateway.runtime.context;

import org.springframework.beans.BeanUtils;
import rabbit.gateway.common.entity.Privilege;

import java.security.PublicKey;

import static rabbit.discovery.api.common.utils.RsaUtils.loadPublicKeyFromString;

public class PrivilegeDesc extends Privilege {

    private PublicKey rsaPublicKey;

    public PrivilegeDesc(Privilege privilege) {
        BeanUtils.copyProperties(privilege, this);
        rsaPublicKey = loadPublicKeyFromString(getPublicKey());
    }

    public PublicKey getRsaPublicKey() {
        return rsaPublicKey;
    }


}
