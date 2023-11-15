package rabbit.gateway.common.context;

import rabbit.gateway.common.entity.Route;

public interface RouteContext {

    /**
     * 重新加载路由
     * @param routeCode
     */
    void reloadRoute(String routeCode);

    /**
     * 删除路由
     * @param routeCode
     */
    void deleteRoute(String routeCode);

    /**
     * 查询路由
     * @param routeCode
     * @return
     */
    Route getRoute(String routeCode);
}
