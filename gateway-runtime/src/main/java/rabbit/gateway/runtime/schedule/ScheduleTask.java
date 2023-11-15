package rabbit.gateway.runtime.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rabbit.gateway.common.entity.Event;

import static org.springframework.data.relational.core.query.Criteria.where;

@Component
public class ScheduleTask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected R2dbcEntityTemplate template;

    /**
     * 移除过期的事件
     */
    @Scheduled(cron = "0/15 * * * * ?")
    public void removeExpiredEvents() {
        // 删除5分钟之前的事件
        Query query = Query.query(where("createdTime")
                .lessThan(System.currentTimeMillis() - 5L * 60 * 1000));
        Integer count = template.delete(Event.class).matching(query).all().block();
        if (0 != count) {
            logger.info("remove expired event data, count: {}", count);
        }
    }
}
