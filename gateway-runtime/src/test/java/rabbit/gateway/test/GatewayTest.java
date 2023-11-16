package rabbit.gateway.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.gateway.common.Protocol;
import rabbit.gateway.common.bean.Target;
import rabbit.gateway.common.entity.*;
import rabbit.gateway.common.exception.GateWayException;
import rabbit.gateway.runtime.context.GateWayContext;
import rabbit.gateway.runtime.context.GatewayService;
import rabbit.gateway.test.rest.RouteApi;
import rabbit.gateway.test.rest.ServiceApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootTestEntry.class, webEnvironment = DEFINED_PORT)
public class GatewayTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected R2dbcEntityTemplate template;

    @Autowired
    protected ServiceApi serviceApi;

    @Autowired
    protected RouteApi routeApi;

    @Autowired
    protected GateWayContext context;

    private String serviceCode = "SVR00001";

    private String routeCode = "RES00001";

    /**
     * 为了简单直连db测试，适合功能回归
     */
    @Test
    public void gatewayTest() {
        try {
            cleanDb();
            runCases();
        } finally {
            cleanDb();
        }
    }

    private void runCases() {
        // 添加服务用例
        addServiceCase();

        // 添加路由
        addRouteCase();
    }

    /**
     * 添加路由
     */
    private void addRouteCase() {
        Route route = new Route();
        route.setPath("/route/add");
        route.setMappingUri("/route/add1");
        route.setMethod(HttpMethod.POST);
        route.setCode(routeCode);
        route.setServiceCode(serviceCode);
        routeApi.add(route).block();
        waitUntilFound(() -> context.getRoute(route.getCode()), "创建路由");
        verifyRouteCache(route);

        // 删除缓存
        context.deleteRoute(route.getCode());
        route.setMappingUri("/route/add");
        routeApi.update(route).block();
        waitUntilFound(() -> routeApi.query(route.getCode()).block(), "更新路由");
        verifyRouteCache(route);

        routeApi.delete(route.getCode()).block();
        waitUntilNotFound(() -> context.getRoute(route.getCode()), "删除路由");
    }

    /**
     * 验证缓存是否一致
     * @param route
     */
    private void verifyRouteCache(Route route) {
        Route r = context.getRoute(route.getCode());
        TestCase.assertNotNull(r.getRequestRateLimit());
        TestCase.assertEquals(route.getCode(), r.getCode());
        TestCase.assertEquals(route.getMethod(), r.getMethod());
        TestCase.assertEquals(route.getPath(), r.getPath());
        TestCase.assertEquals(route.getServiceCode(), r.getServiceCode());
        TestCase.assertEquals(route.getMappingUri(), r.getMappingUri());
    }

    /**
     * 添加服务
     */
    private void addServiceCase() {
        Service service = new Service();
        service.setCode(serviceCode);
        service.setProtocol(Protocol.HTTPS);
        List<Target> upstreams = new ArrayList<>();
        Target t1 = new Target("127.0.0.1", 12800, 1);
        t1.setCaCertificate("caCertificate");
        t1.setCertificate("certificate");
        upstreams.add(t1);
        Target t2 = new Target("localhost", 12800, 2);
        t2.setCaCertificate("caCertificate");
        t2.setCertificate("certificate");
        upstreams.add(t2);
        service.setUpstreams(upstreams);
        serviceApi.add(service).block();
        waitUntilFound(() -> context.getService(service.getCode()), "创建服务");
        verifyServiceCache(service);

        // 清除缓存
        context.deleteService(serviceCode);
        service.setProtocol(Protocol.HTTP);
        upstreams = new ArrayList<>();
        t1 = new Target("127.0.0.1", 12800, 1);
        t1.setCaCertificate("caCertificate");
        t1.setCertificate("certificate");
        upstreams.add(t1);
        service.setUpstreams(upstreams);
        serviceApi.update(service).block();
        waitUntilFound(() -> serviceApi.query(service.getCode()).block(), "更新服务");
        verifyServiceCache(service);

        serviceApi.delete(serviceCode).block();
        waitUntilNotFound(() -> context.getService(serviceCode), "删除服务");
    }

    /**
     * 验证缓存是否一致
     * @param service
     */
    private void verifyServiceCache(Service service) {
        GatewayService cacheService = context.getService(service.getCode());
        TestCase.assertEquals(service.getProtocol(), cacheService.getProtocol());
        TestCase.assertEquals(service.getUpstreams().size(), cacheService.getUpstreams().size());
        for (int i = 0; i < service.getUpstreams().size(); i++) {
            TestCase.assertEquals(service.getUpstreams().get(i).getPort(), cacheService.getUpstreams().get(i).getPort());
            TestCase.assertEquals(service.getUpstreams().get(i).getHost(), cacheService.getUpstreams().get(i).getHost());
            TestCase.assertEquals(service.getUpstreams().get(i).getWeight(), cacheService.getUpstreams().get(i).getWeight());
            TestCase.assertEquals(service.getUpstreams().get(i).getCertificate(), cacheService.getUpstreams().get(i).getCertificate());
            TestCase.assertEquals(service.getUpstreams().get(i).getCaCertificate(), cacheService.getUpstreams().get(i).getCaCertificate());
        }
    }

    private <T> void waitUntil(Supplier<T> supplier, long timeoutSeconds, String caseName, boolean found) {
        long start = System.currentTimeMillis();
        while (found ? (null == supplier.get()) : (null != supplier.get())) {
            LockSupport.parkNanos(20L * 1000 * 1000);
            if (System.currentTimeMillis() - start > timeoutSeconds * 1000) {
                throw new GateWayException(String.format("用例 [%s] 验证超时", caseName));
            }
        }
        logger.info("用例 [{}] 验证成功", caseName);
    }

    /**
     * 一直等到supplier返回值不为空
     *
     * @param supplier
     * @param caseName
     * @param <T>
     */
    private <T> void waitUntilFound(Supplier<T> supplier, String caseName) {
        waitUntil(supplier, 5, caseName, true);
    }

    /**
     * 一直等到supplier返回值为空
     *
     * @param supplier
     * @param caseName
     * @param <T>
     */
    private <T> void waitUntilNotFound(Supplier<T> supplier, String caseName) {
        waitUntil(supplier, 5, caseName, false);
    }

    /**
     * 清除db残留数据，杜绝干扰
     */
    private void cleanDb() {
        logger.debug("clean service: {}", template.delete(Service.class).matching(Query.empty()).all().block());
        logger.debug("clean route: {}", template.delete(Route.class).matching(Query.empty()).all().block());
        logger.debug("clean plugin: {}", template.delete(Plugin.class).matching(Query.empty()).all().block());
        logger.debug("clean privilege: {}", template.delete(Privilege.class).matching(Query.empty()).all().block());
        logger.debug("clean event: {}", template.delete(Event.class).matching(Query.empty()).all().block());
    }
}
