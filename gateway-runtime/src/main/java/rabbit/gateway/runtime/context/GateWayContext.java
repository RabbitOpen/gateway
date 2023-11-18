package rabbit.gateway.runtime.context;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.pattern.PathPatternParser;
import rabbit.gateway.admin.service.EventService;
import rabbit.gateway.common.context.PluginContext;
import rabbit.gateway.common.context.PrivilegeContext;
import rabbit.gateway.common.context.RouteContext;
import rabbit.gateway.common.context.ServiceContext;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.common.entity.Privilege;
import rabbit.gateway.common.entity.Route;
import rabbit.gateway.common.entity.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.data.relational.core.query.Criteria.where;

@org.springframework.stereotype.Service
public class GateWayContext implements ServiceContext, RouteContext, PrivilegeContext, PluginContext,
        ApplicationListener<ContextRefreshedEvent> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 服务缓存
     */
    private Map<String, GatewayService> serviceCache;

    /**
     * 路由缓存
     */
    private Map<String, Route> routeCache;

    /**
     * 全新缓存
     */
    private Map<String, PrivilegeDesc> privilegeCache;

    /**
     * 插件缓存
     */
    private Map<String, PluginManager> pluginManagerCache;

    @Autowired
    protected R2dbcEntityTemplate template;

    @Override
    public void reloadService(String serviceCode) {
        template.selectOne(Query.query(where("code").is(serviceCode)), Service.class)
                .map(service -> {
                    serviceCache.put(serviceCode, new GatewayService(service));
                    logger.info("service[{}] is loaded!", serviceCode);
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
        template.selectOne(Query.query(where("credential").is(credential)), Privilege.class)
                .map(privilege -> {
                    cachePrivilege(privilege);
                    logger.info("privileges[{}] is loaded!", credential);
                    return Mono.empty();
                }).subscribe();
    }

    @Override
    public PrivilegeDesc getPrivilege(String credential) {
        return privilegeCache.get(credential);
    }

    @Override
    public void reloadRoute(String routeCode) {
        template.selectOne(Query.query(where("code").is(routeCode)), Route.class)
                .map(route -> {
                    routeCache.put(routeCode, route);
                    logger.info("route[{}] is loaded!", routeCode);
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
    public void loadData4Cache() {
        this.initCache();
        String keyName = "start";
        template.select(Route.class).all().collectList().flatMap(list -> Mono.create(ctx -> {
            list.forEach(r -> routeCache.put(r.getCode(), r));
            long start = ctx.contextView().get(keyName);
            logger.info("加载[{}]条路由数据，耗时：{}ms", list.size(), System.currentTimeMillis() - start);
            ctx.success(list);
        })).contextWrite(context -> context.put(keyName, System.currentTimeMillis())).block();

        template.select(Service.class).all().collectList().flatMap(list -> Mono.create(ctx -> {
            list.forEach(s -> serviceCache.put(s.getCode(), new GatewayService(s)));
            long start = ctx.contextView().get(keyName);
            logger.info("加载[{}]条服务数据，耗时：{}ms", list.size(), System.currentTimeMillis() - start);
            ctx.success(list);
        })).contextWrite(context -> context.put(keyName, System.currentTimeMillis())).block();

        template.select(Privilege.class).all().collectList().flatMap(list -> Mono.create(ctx -> {
            list.forEach(this::cachePrivilege);
            long start = ctx.contextView().get(keyName);
            logger.info("加载[{}]条授权数据，耗时：{}ms", list.size(), System.currentTimeMillis() - start);
            ctx.success(list);
        })).contextWrite(context -> context.put(keyName, System.currentTimeMillis())).block();

        template.select(Plugin.class).all().collectList().flatMap(list -> Mono.create(ctx -> {
            list.forEach(p -> pluginManagerCache.computeIfAbsent(p.getTarget(), k -> new PluginManager())
                    .addPlugin(p));
            long start = ctx.contextView().get(keyName);
            logger.info("加载[{}]条插件数据，耗时：{}ms", list.size(), System.currentTimeMillis() - start);
            ctx.success(list);
        })).contextWrite(context -> context.put(keyName, System.currentTimeMillis())).block();
    }

    private void cachePrivilege(Privilege privilege) {
        if (!CollectionUtils.isEmpty(privilege.getPrivileges())) {
            PathPatternParser parser = new PathPatternParser();
            privilege.getPrivileges().forEach((c, api) -> api.setPattern(parser.parse(api.getPath())));
        }
        privilegeCache.put(privilege.getCredential(), new PrivilegeDesc(privilege));
    }

    /**
     * 初始化cache
     */
    private void initCache() {
        serviceCache = new ConcurrentHashMap<>(1024);
        routeCache = new ConcurrentHashMap<>(4096);
        privilegeCache = new ConcurrentHashMap<>(1024);
        pluginManagerCache = new ConcurrentHashMap<>(1024);
    }

    @Override
    public void reloadPlugins(String serviceCode) {
        Query query = Query.query(where("target").is(serviceCode));
        template.select(Plugin.class).matching(query).all().collectList().map(list -> {
            PluginManager pluginManager = new PluginManager();
            list.forEach(pluginManager::addPlugin);
            pluginManagerCache.put(serviceCode, pluginManager);
            logger.info("service[{}] plugin is reloaded", serviceCode);
            return list;
        }).subscribe();
    }

    /**
     * 获取服务对应的插件管理器
     *
     * @param serviceCode
     * @return
     */
    public PluginManager getPluginManager(String serviceCode) {
        return pluginManagerCache.get(serviceCode);
    }

    @Autowired
    private EventService eventService;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("prepared------------------");
        if (null == this.serviceCache) {
            this.loadData4Cache();
            eventService.init();
        }
    }
}
