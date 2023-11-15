package rabbit.gateway.common;

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
}
