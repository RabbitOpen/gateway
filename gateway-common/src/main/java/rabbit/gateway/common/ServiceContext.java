package rabbit.gateway.common;

import rabbit.gateway.common.entity.Service;

public interface ServiceContext {

    /**
     * 重新加载服务
     * @param serviceCode
     */
    void reloadService(String serviceCode);

    /**
     * 删除服务
     * @param serviceCode
     */
    void deleteService(String serviceCode);

    /**
     * 查询缓存的服务
     * @param serviceCode
     * @return
     */
    Service getService(String serviceCode);
}
