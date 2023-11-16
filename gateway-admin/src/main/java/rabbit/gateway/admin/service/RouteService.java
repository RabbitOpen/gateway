package rabbit.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rabbit.gateway.common.bean.RequestRateLimit;
import rabbit.gateway.common.context.RouteContext;
import rabbit.gateway.common.entity.Route;
import rabbit.gateway.common.event.DeleteRouteEvent;
import rabbit.gateway.common.event.ReloadRouteEvent;
import rabbit.gateway.common.exception.GateWayException;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

@Service
public class RouteService {

    @Autowired
    protected R2dbcEntityTemplate template;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected RouteContext routeContext;

    /**
     * 新增路由
     *
     * @param route
     * @return
     */
    @Transactional
    public Mono<Route> add(Route route) {
        if (null == route.getRequestRateLimit()) {
            route.setRequestRateLimit(new RequestRateLimit());
        }
        return eventService.addEvent(new ReloadRouteEvent(route.getCode()))
                .flatMap(e -> template.insert(route));
    }

    /**
     * 更新路由
     *
     * @param route
     * @return
     */
    @Transactional
    public Mono<Route> update(Route route) {
        return template.selectOne(Query.query(where("code").is(route.getCode())), Route.class)
                .flatMap(r -> {
                    r.setMappingUri(route.getMappingUri());
                    r.setRequestRateLimit(null == route.getRequestRateLimit() ? new RequestRateLimit() : route.getRequestRateLimit());
                    r.setMethod(route.getMethod());
                    r.setPath(route.getPath());
                    return eventService.addEvent(new ReloadRouteEvent(route.getCode()))
                            .flatMap(e -> template.update(r));
                }).switchIfEmpty(Mono.defer(() -> Mono.error(new GateWayException("路由不存在"))));
    }

    /**
     * 删除路由
     *
     * @param routeCode
     * @return
     */
    @Transactional
    public Mono<Integer> delete(String routeCode) {
        return eventService.addEvent(new DeleteRouteEvent(routeCode))
                .flatMap(e -> template.delete(Query.query(where("code").is(routeCode)), Route.class));
    }

    /**
     * 查询路由
     *
     * @param routeCode
     * @return
     */
    public Mono<Route> query(String routeCode) {
        Route route = routeContext.getRoute(routeCode);
        return null == route ? Mono.empty() : Mono.just(route);
    }
}
