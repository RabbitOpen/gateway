package rabbit.gateway.common.context;

import rabbit.gateway.common.entity.Privilege;

public interface PrivilegeContext {

    /**
     * 重载权限
     * @param credential
     */
    void reloadPrivileges(String credential);

    /**
     * 查询权限
     * @param credential
     * @return
     */
    Privilege getPrivilege(String credential);
}
