package rabbit.gateway.common.event;

import org.springframework.context.ApplicationContext;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.RouteContext;

/**
 * 删除路由事件
 */
public class DeleteRouteEvent implements GateWayEvent {

    /**
     * 路由编码
     */
    private String routeCode;

    public DeleteRouteEvent() {
    }

    public DeleteRouteEvent(String routeCode) {
        this();
        setRouteCode(routeCode);
    }

    @Override
    public void run(ApplicationContext context) {
        context.getBean(RouteContext.class).deleteRoute(getRouteCode());
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
}
