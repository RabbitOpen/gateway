package rabbit.gateway.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.gateway.common.entity.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootTestEntry.class, webEnvironment = DEFINED_PORT)
public class GatewayTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected R2dbcEntityTemplate template;

    /**
     * 为了简单直连db测试，适合功能回归
     */
    @Test
    public void gatewayTest() {
        cleanDb();
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
