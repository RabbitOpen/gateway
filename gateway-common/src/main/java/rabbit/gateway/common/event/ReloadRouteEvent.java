package rabbit.gateway.common.event;

import org.springframework.context.ApplicationContext;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.context.RouteContext;

/**
 * 刷新路由事件
 */
public class ReloadRouteEvent implements GateWayEvent {

    /**
     * 路由编码
     */
    private String routeCode;

    public ReloadRouteEvent() {
    }

    public ReloadRouteEvent(String routeCode) {
        this();
        setRouteCode(routeCode);
    }

    @Override
    public void run(ApplicationContext context) {
        context.getBean(RouteContext.class).reloadRoute(getRouteCode());
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
}
