package rabbit.gateway.runtime.context;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import rabbit.gateway.common.context.PluginContext;
import rabbit.gateway.common.context.PrivilegeContext;
import rabbit.gateway.common.context.RouteContext;
import rabbit.gateway.common.context.ServiceContext;
import rabbit.gateway.common.entity.Privilege;
import rabbit.gateway.common.entity.Route;
import rabbit.gateway.common.entity.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.data.relational.core.query.Criteria.where;

@org.springframework.stereotype.Service
public class GateWayContext implements ServiceContext, RouteContext, PrivilegeContext, PluginContext {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 服务缓存
     */
    private Map<String, GatewayService> serviceCache = new ConcurrentHashMap<>(1024);

    /**
     * 路由缓存
     */
    private Map<String, Route> routeCache = new ConcurrentHashMap<>(4096);

    /**
     * 全新缓存
     */
    private Map<String, Privilege> privilegeCache = new ConcurrentHashMap<>(1024);

    @Autowired
    protected R2dbcEntityTemplate template;

    @Override
    public void reloadService(String serviceCode) {
        logger.info("service[{}] is loaded!", serviceCode);
        template.selectOne(Query.query(where("code").is(serviceCode)), Service.class)
                .map(service -> {
                    serviceCache.put(serviceCode, new GatewayService(service));
                    return Mono.empty();
                }).subscribe();
    }

    @Override
    public void deleteService(String serviceCode) {
        logger.info("service[{}] is deleted!", serviceCode);
        serviceCache.remove(serviceCode);
    }

    @Override
    public GatewayService getService(String serviceCode) {
        return serviceCache.get(serviceCode);
    }

    @Override
    public void reloadPrivileges(String credential) {
        logger.info("privileges[{}] is loaded!", credential);
        template.selectOne(Query.query(where("credential").is(credential)), Privilege.class)
                .map(privilege -> {
                    privilegeCache.put(credential, privilege);
                    return Mono.empty();
                }).subscribe();
    }

    @Override
    public Privilege getPrivilege(String credential) {
        return privilegeCache.get(credential);
    }

    @Override
    public void reloadRoute(String routeCode) {
        logger.info("route[{}] is loaded!", routeCode);
        template.selectOne(Query.query(where("code").is(routeCode)), Route.class)
                .map(route -> {
                    routeCache.put(routeCode, route);
                    return Mono.empty();
                }).subscribe();
    }

    @Override
    public void deleteRoute(String routeCode) {
        logger.info("route[{}] is deleted!", routeCode);
        routeCache.remove(routeCode);
    }

    @Override
    public Route getRoute(String routeCode) {
        return routeCache.get(routeCode);
    }

    /**
     * 加载缓存
     */
    @PostConstruct
    public void cacheData() {
        template.select(Route.class).all().collectList().flatMap(list -> Mono.create(ctx -> {
            list.forEach(r -> routeCache.put(r.getCode(), r));
            long start = ctx.contextView().get("start");
            logger.info("加载[{}]条路由数据，耗时：{}ms", list.size(), System.currentTimeMillis() - start);
            ctx.success(list);
        })).contextWrite(context -> context.put("start", System.currentTimeMillis())).block();

        template.select(Service.class).all().collectList().flatMap(list -> Mono.create(ctx -> {
            list.forEach(s -> serviceCache.put(s.getCode(), new GatewayService(s)));
            long start = ctx.contextView().get("start");
            logger.info("加载[{}]条服务数据，耗时：{}ms", list.size(), System.currentTimeMillis() - start);
            ctx.success(list);
        })).contextWrite(context -> context.put("start", System.currentTimeMillis())).block();

        template.select(Privilege.class).all().collectList().flatMap(list -> Mono.create(ctx -> {
            list.forEach(p -> privilegeCache.put(p.getCredential(), p));
            long start = ctx.contextView().get("start");
            logger.info("加载[{}]条授权数据，耗时：{}ms", list.size(), System.currentTimeMillis() - start);
            ctx.success(list);
        })).contextWrite(context -> context.put("start", System.currentTimeMillis())).block();
    }

    @Override
    public void reloadPlugins(String serviceCode) {

    }
}
