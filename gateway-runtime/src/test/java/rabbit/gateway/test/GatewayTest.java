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
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.gateway.common.Protocol;
import rabbit.gateway.common.bean.Target;
import rabbit.gateway.common.entity.*;
import rabbit.gateway.runtime.context.GateWayContext;
import rabbit.gateway.runtime.context.GatewayService;
import rabbit.gateway.test.rest.ServiceApi;

import java.util.ArrayList;
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
    protected GateWayContext context;

    /**
     * 为了简单直连db测试，适合功能回归
     */
    @Test
    public void gatewayTest() {
        cleanDb();
        // 添加服务用例
        addServiceCase();
    }

    private void addServiceCase() {
        Service service = new Service();
        service.setCode("SVR00001");
        service.setProtocol(Protocol.HTTP);
        ArrayList<Target> upstreams = new ArrayList<>();
        upstreams.add(new Target("127.0.0.1", 12800, 1));
        upstreams.add(new Target("localhost", 12800, 1));
        service.setUpstreams(upstreams);
        serviceApi.add(service).block();
        waitUntil(() -> context.getService(service.getCode()), "创建服务");
        GatewayService cacheService = context.getService(service.getCode());
        TestCase.assertEquals(service.getProtocol(), cacheService.getProtocol());
        TestCase.assertEquals(service.getUpstreams().size(), cacheService.getUpstreams().size());
    }

    private <T> void waitUntil(Supplier<T> supplier, long timeoutSeconds, String caseName) {
        long start = System.currentTimeMillis();
        while (null == supplier.get()) {
            LockSupport.parkNanos(20L * 1000 * 1000);
            if (System.currentTimeMillis() - start > timeoutSeconds) {
                throw new RuntimeException(String.format("用例 [%s] 验证超时", caseName));
            }
        }
        logger.info("用例 [{}] 验证成功", caseName);
    }

    private <T> void waitUntil(Supplier<T> supplier, String caseName) {
        waitUntil(supplier, 5, caseName);
    }

    /**
     * 清除db残留数据，杜绝干扰
     */
    private void cleanDb() {
        logger.info("clean service: {}", template.delete(Service.class).matching(Query.empty()).all().block());
        logger.info("clean route: {}", template.delete(Route.class).matching(Query.empty()).all().block());
        logger.info("clean plugin: {}", template.delete(Plugin.class).matching(Query.empty()).all().block());
        logger.info("clean privilege: {}", template.delete(Privilege.class).matching(Query.empty()).all().block());
        logger.info("clean event: {}", template.delete(Event.class).matching(Query.empty()).all().block());
    }
}
