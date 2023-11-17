package rabbit.gateway.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.entity.Event;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.relational.core.query.Criteria.where;

@Service
public class EventService implements ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected R2dbcEntityTemplate template;

    protected ApplicationContext context;

    private Thread thread;

    private Semaphore semaphore = new Semaphore(0);

    /**
     * 添加事件
     *
     * @param e
     * @return
     */
    public Mono<Event> addEvent(GateWayEvent e) {
        Event event = new Event();
        event.setGateWayEvent(e);
        event.setCreatedTime(System.currentTimeMillis());
        event.setName(e.getClass().getName());
        return template.insert(event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    @PostConstruct
    public void init() {
        thread = new Thread(() -> {
            Query query = Query.empty().sort(Sort.by(Sort.Direction.DESC, "id")).limit(1).offset(0);
            long minEventId = template.select(Event.class).matching(query).first().map(Event::getId)
                    .switchIfEmpty(Mono.just(0L)).block();
            while (true) {
                try {
                    query = Query.query(where("id").greaterThan(minEventId))
                            .sort(Sort.by(Sort.Direction.ASC, "id")).limit(10).offset(0);
                    List<Event> list = template.select(Event.class).matching(query).all().collectList().block();
                    list.forEach(event -> event.getGateWayEvent().run(context));
                    if (!list.isEmpty()) {
                        minEventId = list.get(list.size() - 1).getId();
                    }
                    if (semaphore.tryAcquire(1, 200, TimeUnit.MILLISECONDS)) {
                        return;
                    }
                } catch (Exception e) {
                    logger.info(e.getMessage(), e);
                }
            }
        });
        thread.setDaemon(false);
        thread.start();
    }

    @PreDestroy
    public void close() throws InterruptedException {
        semaphore.release();
        thread.join();
        logger.info("event service is destroyed!");
    }
}
