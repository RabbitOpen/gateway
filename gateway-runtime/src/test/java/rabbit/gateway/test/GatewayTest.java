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
import rabbit.discovery.api.common.Headers;
import rabbit.discovery.api.rest.http.HttpResponse;
import rabbit.flt.common.utils.ReflectUtils;
import rabbit.gateway.admin.service.EventService;
import rabbit.gateway.common.PluginName;
import rabbit.gateway.common.PluginType;
import rabbit.gateway.common.Protocol;
import rabbit.gateway.common.Result;
import rabbit.gateway.common.bean.*;
import rabbit.gateway.common.entity.*;
import rabbit.gateway.common.exception.GateWayException;
import rabbit.gateway.common.utils.JsonUtils;
import rabbit.gateway.runtime.context.GateWayContext;
import rabbit.gateway.runtime.context.GatewayService;
import rabbit.gateway.runtime.context.PluginManager;
import rabbit.gateway.runtime.context.PrivilegeDesc;
import rabbit.gateway.runtime.plugin.RuntimePlugin;
import rabbit.gateway.runtime.plugin.request.AddRequestHeaderPlugin;
import rabbit.gateway.runtime.plugin.request.AuthenticationPlugin;
import rabbit.gateway.runtime.plugin.request.RemoveRequestHeaderPlugin;
import rabbit.gateway.runtime.plugin.request.RequestMappingPlugin;
import rabbit.gateway.test.open.OpenApi;
import rabbit.gateway.test.rest.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpMethod.GET;
import static rabbit.gateway.common.ErrorType.GATEWAY;

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
    protected PluginApi pluginApi;

    @Autowired
    protected PrivilegeApi privilegeApi;

    @Autowired
    protected GateWayContext context;

    @Autowired
    protected OpenApi openApi;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected TestApi testApi;

    /**
     * 测试 服务编码
     */
    protected String serviceCode = "SVR00001";

    /**
     * 测试 路由编码
     */
    protected String routeCode = "RES00001";

    protected String mappingApiCode = "MAPPING";

    protected String undefinedRouteCode = "UNDEFINED-ROUTE";

    /**
     * 添加header插件添加的header头
     */
    protected String addedRequestHeader = "added-request-header";

    /**
     * 测试 凭据
     */
    protected String credential = "CRE0001";

    /**
     * 测试 内置凭据
     */
    protected String innerCredential = "INC0001";

    /**
     * 测试 公钥
     */
    private final String publicKey = "305C300D06092A864886F70D0101010500034B003048024100C5B76A3974FEED9144066469D95D3A0297288F626A54A3624901552353DFBDA20FA4156CE11C6048FC3F9DB79101DB047933E031074719C10D552E05658D16290203010001";

    /**
     * 测试 私钥
     */
    private final String privateKey = "30820155020100300D06092A864886F70D01010105000482013F3082013B020100024100C5B76A3974FEED9144066469D95D3A0297288F626A54A3624901552353DFBDA20FA4156CE11C6048FC3F9DB79101DB047933E031074719C10D552E05658D1629020301000102410086104630CB8A086055B5D7E48604D6CEE6DC13CD71B80E4918A394AD2DB42A8A453ADFC30F8C0FA52587E94EC41372615E440EA9112DD66529F28CDE5AAE50E1022100F035866A0144672E0AF7C4C67F624ADB48E3CD6B5CCAA9ADD374D91AB8A38505022100D2B6C84345E276628AFC33ECA5E7ED98D186FDE6607D1BC6DBC4D90AC11E15D502210094F58C5A3658F5E73E93F1A9EA9AC8B2FD9B8EEA924B7737BD56CBBF5F5AC005022060C7CD2180F6ABF344ECE3987CF7129D0F1796847AAADBD83156AF6D8E179865022022004279B1F834908EA24CC813E810E16D9E72CE74F246EDF4A066152034FAAF";

    /**
     * 响应头中添加的header名
     */
    private String addedResponseHeader = "added-response-header";

    /**
     * 为了简单直连db测试，适合功能回归
     */
    @Test
    public void gatewayTest() throws InterruptedException {
        try {
            adminCases();
            runtimeCases();
        } finally {
            eventService.close();
            cleanDb();
        }
    }

    /**
     * 运行态用例
     */
    private void runtimeCases() {
        cleanDb();
        // 重置缓存
        resetCache();
        // 添加运行时服务
        addRuntimeService();
        // 添加运行时插件
        addRuntimePlugins();
        // 添加运行时路由
        addRuntimeRoute();
        // 未授权访问(自身没任何权限)
        unAuthorizedAccessCase();
        // 授权访问case
        authorizedAccessCase();
        // 未定义的路由
        unDefinedRouteCase();
        // 下发未定义的路由, 再调
        addUndefinedRoute();
        // 自己有权限，但是没有该接口的权限
        authorizedAccessCase2();
        // 越权访问
        pathCodeNotMatchCase();
        // 接口映射插件
        callMappingPathCase();

        // 无响应的管理端接口测试
        noResponseAdminApiCase();

        // 验证无返回值的open api case
        noResponseOpenApiTest();
    }

    /**
     * 无返回值的openApi case
     */
    private void noResponseOpenApiTest() {
        openApi.callVoidRequest();
        HttpResponse<Void> response = openApi.callMonoVoidRequest().block();
        TestCase.assertNull(response.getData());
        TestCase.assertTrue(response.getHeaders().containsKey(Headers.OPEN_API_CODE));
        TestCase.assertTrue(response.getHeaders().containsKey(addedResponseHeader));
        TestCase.assertTrue(response.getHeaders().containsKey(addedRequestHeader));
        response = openApi.callHttpResponseVoidRequest();
        TestCase.assertNull(response.getData());
        TestCase.assertTrue(response.getHeaders().containsKey(Headers.OPEN_API_CODE));
        TestCase.assertTrue(response.getHeaders().containsKey(addedResponseHeader));
        TestCase.assertTrue(response.getHeaders().containsKey(addedRequestHeader));
        logger.info("用例 [无返回值open api访问] 验证成功");
    }

    private void noResponseAdminApiCase() {
        testApi.callVoidRequest();
        HttpResponse<Void> block = testApi.callMonoVoidRequest().block();
        TestCase.assertTrue(block.getHeaders().containsKey(Headers.API_VERSION));
        try {
            testApi.fakeException();
            throw new RuntimeException("");
        } catch (Exception e) {
            Result err = JsonUtils.readValue(e.getMessage(), Result.class);
            TestCase.assertEquals(GATEWAY, err.getErrorType());
            TestCase.assertTrue(err.getMessage().contains("模拟异常"));
            logger.info("用例 [模拟异常] 验证成功");
        }
    }

    private void callMappingPathCase() {
        HttpResponse<String> response = openApi.accessMappingPath().block();
        TestCase.assertTrue(response.getHeaders().containsKey(addedRequestHeader));
        TestCase.assertTrue("hello".equals(response.getData()));
        logger.info("用例 [接口映射] 验证成功");
    }

    private void pathCodeNotMatchCase() {
        try {
            openApi.wrongPath().block();
            throw new RuntimeException("");
        } catch (Exception e) {
            Result err = JsonUtils.readValue(e.getMessage(), Result.class);
            TestCase.assertEquals(GATEWAY, err.getErrorType());
            TestCase.assertTrue(err.getMessage().contains("越权访问"));
            logger.info("用例 [越权访问] 验证成功");
        }
    }

    private void authorizedAccessCase2() {
        try {
            openApi.undefinedRoute().block();
            throw new RuntimeException("");
        } catch (Exception e) {
            Result err = JsonUtils.readValue(e.getMessage(), Result.class);
            TestCase.assertEquals(GATEWAY, err.getErrorType());
            TestCase.assertTrue(err.getMessage().contains("没有对应接口的访问权限"));
            logger.info("用例 [访问没权限的接口] 验证成功");
        }
    }

    private void addUndefinedRoute() {
        Route route = new Route();
        route.setPath("/route/query/{routeCode}");
        route.setMappingUri("/route/query/{routeCode}");
        route.setMethod(GET);
        route.setCode(undefinedRouteCode);
        route.setServiceCode(serviceCode);
        routeApi.add(route).block();
        waitUntilFound(() -> context.getRoute(undefinedRouteCode), "添加运行时路由");
    }

    private void unDefinedRouteCase() {
        try {
            openApi.undefinedRoute().block();
            throw new RuntimeException("");
        } catch (Exception e) {
            Result err = JsonUtils.readValue(e.getMessage(), Result.class);
            TestCase.assertEquals(GATEWAY, err.getErrorType());
            TestCase.assertTrue(err.getMessage().contains("未定义的路由"));
            logger.info("用例 [访问不存在的路由] 验证成功");
        }
    }

    private void authorizedAccessCase() {
        Privilege privilege = new Privilege();
        privilege.setCredential(credential);
        privilege.setPrivateKey(privateKey);
        privilege.setPublicKey(publicKey);
        Map<String, ApiDesc> privilegesMap = new HashMap<>();
        privilegesMap.put(routeCode, new ApiDesc("/route/query/{routeCode}", GET, serviceCode, 10000));
        privilegesMap.put(mappingApiCode, new ApiDesc("/test/echo/mapping", GET, serviceCode, 10000));
        privilegesMap.put("VOID-RESPONSE", new ApiDesc("/test/void", GET, serviceCode, 10000));
        privilege.setPrivileges(privilegesMap);
        TestCase.assertNull(context.getPrivilege(credential));
        privilegeApi.authorize(privilege).block();
        waitUntilFound(() -> context.getPrivilege(credential), "新增授限");
        HttpResponse<Route> response = openApi.queryRoute(routeCode).block();
        TestCase.assertTrue(response.getHeaders().containsKey(addedResponseHeader));
        TestCase.assertEquals(context.getRoute(routeCode).getPath(), response.getData().getPath());
        logger.info("用例 [授权访问] 验证成功");
    }

    private void unAuthorizedAccessCase() {
        try {
            openApi.queryRoute(routeCode).block();
            throw new RuntimeException("");
        } catch (Exception e) {
            Result err = JsonUtils.readValue(e.getMessage(), Result.class);
            TestCase.assertEquals(GATEWAY, err.getErrorType());
            TestCase.assertTrue(err.getMessage().contains("没有对应接口的访问权限"));
            logger.info("用例 [访问受限] 验证成功");
        }
    }

    private void resetCache() {
        try {
            Method initCache = context.getClass().getDeclaredMethod("initCache");
            initCache.setAccessible(true);
            initCache.invoke(context);
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }

    private void addRuntimePlugins() {
        // 添加授权插件
        createAuthenticationPlugin();
        createAddRequestHeadersPlugin();
        createRemoveRequestHeadersPlugin();
        createRequestMappingPlugin();
        createAddResponseHeadersPlugin();
        waitUntilFinished(() -> {
            PluginManager pluginManager = context.getPluginManager(serviceCode);
            if (null == pluginManager) {
                return false;
            }
            Field field = getClassField(PluginManager.class, "requestPlugins");
            List<RuntimePlugin> list = ReflectUtils.getValue(pluginManager, field);
            if (4 == list.size()) {
                TestCase.assertTrue(list.get(0) instanceof AuthenticationPlugin);
                TestCase.assertTrue(list.get(1) instanceof AddRequestHeaderPlugin);
                TestCase.assertTrue(list.get(2) instanceof RemoveRequestHeaderPlugin);
                TestCase.assertTrue(list.get(3) instanceof RequestMappingPlugin);
            }
            return list.size() == 4;
        }, "添加服务插件");
    }

    private void createAddResponseHeadersPlugin() {
        Plugin plugin = new Plugin();
        plugin.setName(PluginName.ADD_RESPONSE_HEADERS);
        HeaderAddSchema schema = new HeaderAddSchema();
        schema.setHeaders(new HashMap<>());
        schema.getHeaders().put(addedResponseHeader, PluginName.ADD_RESPONSE_HEADERS.name());
        plugin.setSchema(schema);
        plugin.setTarget(serviceCode);
        plugin.setType(PluginType.RESPONSE);
        pluginApi.replace(plugin).block();
    }

    private void createRequestMappingPlugin() {
        Plugin plugin = new Plugin();
        plugin.setName(PluginName.REQUEST_MAPPING);
        plugin.setTarget(serviceCode);
        plugin.setType(PluginType.REQUEST);
        pluginApi.replace(plugin).block();
    }

    private void createRemoveRequestHeadersPlugin() {
        Plugin plugin = new Plugin();
        plugin.setName(PluginName.REMOVE_REQUEST_HEADERS);
        plugin.setTarget(serviceCode);
        plugin.setType(PluginType.REQUEST);
        pluginApi.replace(plugin).block();
    }

    private void createAddRequestHeadersPlugin() {
        Plugin plugin = new Plugin();
        plugin.setName(PluginName.ADD_REQUEST_HEADERS);
        HeaderAddSchema schema = new HeaderAddSchema();
        schema.getHeaders().put(addedRequestHeader, "test-case");
        plugin.setSchema(schema);
        plugin.setTarget(serviceCode);
        plugin.setType(PluginType.REQUEST);
        pluginApi.replace(plugin).block();
    }

    /**
     * 添加授权插件
     *
     * @return
     */
    private Plugin createAuthenticationPlugin() {
        Plugin plugin = new Plugin();
        plugin.setName(PluginName.AUTHENTICATION);
        AuthenticationSchema schema = new AuthenticationSchema();
        schema.setInnerCredential(innerCredential);
        schema.setOffsetSeconds(300);
        schema.setPublicKey(publicKey);
        schema.setInnerPublicKey(publicKey);
        schema.setRouteHeaderValue(serviceCode);
        plugin.setSchema(schema);
        plugin.setTarget(serviceCode);
        plugin.setType(PluginType.REQUEST);
        pluginApi.replace(plugin).block();
        return plugin;
    }

    private void addRuntimeRoute() {
        Route route = new Route();
        route.setPath("/route/query/{routeCode}");
        route.setMappingUri("/route/query/{routeCode}");
        route.setMethod(GET);
        route.setCode(routeCode);
        route.setServiceCode(serviceCode);
        route.setRequestRateLimit(new RequestRateLimit());
        routeApi.add(route).block();
        waitUntilFound(() -> context.getRoute(routeCode), "添加运行时路由");

        route = new Route();
        route.setPath("/test/echo/mapping");
        route.setMappingUri("/test/echo");
        route.setMethod(GET);
        route.setCode(mappingApiCode);
        route.setServiceCode(serviceCode);
        routeApi.add(route).block();
        waitUntilFound(() -> context.getRoute(mappingApiCode), "添加映射路由");

        route = new Route();
        route.setPath("/test/void");
        route.setMethod(GET);
        route.setCode("VOID-RESPONSE");
        route.setServiceCode(serviceCode);
        routeApi.add(route).block();
        waitUntilFound(() -> context.getRoute("VOID-RESPONSE"), "添加无返回值路由");
    }

    private void addRuntimeService() {
        Service service = new Service();
        service.setCode(serviceCode);
        service.setProtocol(Protocol.HTTP);
        List<Target> upstreams = createTargets();
        service.setUpstreams(upstreams);
        serviceApi.add(service).block();
    }

    private List<Target> createTargets() {
        List<Target> upstreams = new ArrayList<>();
        Target t1 = new Target("127.0.0.1", 12800, 1);
        t1.setCaCertificate("caCertificate");
        t1.setCertificate("certificate");
        upstreams.add(t1);
        Target t2 = new Target("localhost", 12800, 2);
        t2.setCaCertificate("caCertificate");
        t2.setCertificate("certificate");
        upstreams.add(t2);
        return upstreams;
    }

    /**
     * 管理态case
     */
    private void adminCases() {
        cleanDb();

        // 服务用例用例
        serviceCase();

        // 路由用例
        routeCase();

        // 插件用例
        pluginCase();

        // 授权用例
        privilegeCase();
    }

    /**
     * 授权case
     */
    private void privilegeCase() {
        Privilege privilege = new Privilege();
        privilege.setCredential(credential);
        privilege.setPrivateKey(privateKey);
        privilege.setPublicKey(publicKey);
        Map<String, ApiDesc> privilegesMap = new HashMap<>();
        privilegesMap.put("API001", new ApiDesc("/p1", GET, serviceCode, 10000));
        privilegesMap.put("API002", new ApiDesc("/p2", GET, serviceCode, 10000));
        privilege.setPrivileges(privilegesMap);
        privilegeApi.authorize(privilege).block();
        waitUntilFound(() -> context.getPrivilege(credential), "新增授限");
        assertPrivileges(privilege);

        Map<String, ApiDesc> map2 = new HashMap<>();
        map2.put("API002", new ApiDesc("/p2", GET, serviceCode, 12000));
        map2.put("API003", new ApiDesc("/p3", GET, serviceCode, 10000));
        privilege.setPrivileges(map2);
        TestCase.assertEquals(2, context.getPrivilege(credential).getPrivileges().size());
        privilegeApi.authorize(privilege).block();
        waitUntilFinished(() -> context.getPrivilege(credential).getPrivileges().size() == 3, "增量授权");
        privilegesMap.putAll(map2);
        privilege.setPrivileges(privilegesMap);
        assertPrivileges(privilege);

        Map<String, ApiDesc> map3 = new HashMap<>();
        map3.put("API002", new ApiDesc());
        map3.put("API003", new ApiDesc());
        privilege.setPrivileges(map3);
        privilegeApi.unAuthorize(privilege).block();
        waitUntilFinished(() -> privilegeApi.query(credential).block().getPrivileges().size() == 1, "取消授权");
        TestCase.assertTrue(context.getPrivilege(credential).getPrivileges().containsKey("API001"));

        TestCase.assertNull(privilegeApi.query("not-existed-credential").block());
    }

    /**
     * 断言权限数据
     *
     * @param privilege
     */
    private void assertPrivileges(Privilege privilege) {
        PrivilegeDesc desc = context.getPrivilege(credential);
        TestCase.assertEquals(privilege.getCredential(), desc.getCredential());
        TestCase.assertEquals(privilege.getPublicKey(), desc.getPublicKey());
        TestCase.assertEquals(privilege.getPrivateKey(), desc.getPrivateKey());
        TestCase.assertEquals(privilege.getPrivileges().size(), desc.getPrivileges().size());
        desc.getPrivileges().forEach((code, api) -> {
            ApiDesc apiDesc = privilege.getPrivileges().get(code);
            TestCase.assertEquals(api.getMethod(), apiDesc.getMethod());
            TestCase.assertEquals(api.getPath(), apiDesc.getPath());
            TestCase.assertEquals(api.getServiceCode(), apiDesc.getServiceCode());
            TestCase.assertEquals(api.getInvalidDate(), apiDesc.getInvalidDate());
            TestCase.assertNotNull(api.getPattern());
        });
    }

    /**
     * 插件用例
     */
    private void pluginCase() {
        Plugin plugin = createAuthenticationPlugin();
        waitUntilFound(() -> context.getPluginManager(serviceCode), "添加插件");
        PluginManager manager = context.getPluginManager(serviceCode);
        Field field = getClassField(PluginManager.class, "requestPlugins");
        List<RuntimePlugin> requestPlugins = ReflectUtils.getValue(manager, field);
        TestCase.assertEquals(1, requestPlugins.size());
        TestCase.assertEquals(plugin.getTarget(), requestPlugins.get(0).getTarget());
        TestCase.assertEquals(plugin.getName(), requestPlugins.get(0).getName());
        TestCase.assertEquals(plugin.getType(), requestPlugins.get(0).getType());
        assertAuthenticationSchema(plugin.getSchema(), requestPlugins.get(0).getSchema());

        // 删除插件
        pluginApi.delete(serviceCode, PluginName.AUTHENTICATION.name()).block();
        waitUntilFinished(() -> {
            PluginManager pluginManager = context.getPluginManager(serviceCode);
            List<RuntimePlugin> list = ReflectUtils.getValue(pluginManager, field);
            return list.isEmpty();
        }, "删除插件");
        TestCase.assertEquals(0, pluginApi.delete(serviceCode, PluginName.AUTHENTICATION.name()).block().intValue());

    }

    private <T> Field getClassField(Class<T> clz, String fieldName) {
        try {
            return clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new GateWayException(e);
        }
    }

    private void assertAuthenticationSchema(AuthenticationSchema pluginSchema, AuthenticationSchema cachedSchema) {
        TestCase.assertEquals(pluginSchema.getInnerCredential(), cachedSchema.getInnerCredential());
        TestCase.assertEquals(pluginSchema.getInnerPublicKey(), cachedSchema.getInnerPublicKey());
        TestCase.assertEquals(pluginSchema.getPublicKey(), cachedSchema.getPublicKey());
        TestCase.assertEquals(pluginSchema.getOffsetSeconds(), cachedSchema.getOffsetSeconds());
        TestCase.assertEquals(pluginSchema.getRouteHeaderValue(), cachedSchema.getRouteHeaderValue());
    }

    /**
     * 路由用例
     */
    private void routeCase() {
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
     *
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
     * 服务用例
     */
    private void serviceCase() {
        Service service = new Service();
        service.setCode(serviceCode);
        service.setProtocol(Protocol.HTTPS);
        service.setUpstreams(createTargets());
        serviceApi.add(service).block();
        waitUntilFound(() -> context.getService(service.getCode()), "创建服务");
        verifyServiceCache(service);

        // 清除缓存
        context.deleteService(serviceCode);
        service.setProtocol(Protocol.HTTP);
        List<Target> upstreams = new ArrayList<>();
        Target t1 = new Target("127.0.0.1", 12800, 1);
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
     *
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

    private <T> void waitUntilFinished(Callable<Boolean> condition, long timeoutSeconds, String caseName) {
        long start = System.currentTimeMillis();
        try {
            while (!condition.call()) {
                LockSupport.parkNanos(20L * 1000 * 1000);
                if (System.currentTimeMillis() - start > timeoutSeconds * 1000) {
                    throw new GateWayException(String.format("用例 [%s] 验证超时", caseName));
                }
            }
        } catch (Exception e) {
            throw new GateWayException(e);
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
     * 等到条件为真
     *
     * @param condition
     * @param caseName
     */
    private void waitUntilFinished(Callable<Boolean> condition, String caseName) {
        waitUntilFinished(condition, 5, caseName);
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
