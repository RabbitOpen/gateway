package rabbit.gateway.runtime.context;

import org.springframework.beans.BeanUtils;
import rabbit.gateway.common.entity.Privilege;
import rabbit.gateway.common.utils.RsaUtils;

import java.security.PublicKey;

public class PrivilegeDesc extends Privilege {

    private PublicKey rsaPublicKey;

    public PrivilegeDesc(Privilege privilege) {
        BeanUtils.copyProperties(privilege, this);
        rsaPublicKey = RsaUtils.loadPublicKeyFromString(getPublicKey());
    }

    public PublicKey getRsaPublicKey() {
        return rsaPublicKey;
    }


}
