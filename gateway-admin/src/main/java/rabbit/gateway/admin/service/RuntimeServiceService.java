package rabbit.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import rabbit.gateway.common.context.ServiceContext;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.common.entity.Service;
import rabbit.gateway.common.event.DeleteServiceEvent;
import rabbit.gateway.common.event.ReloadServiceEvent;
import rabbit.gateway.common.exception.GateWayException;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

@org.springframework.stereotype.Service
public class RuntimeServiceService {

    @Autowired
    protected R2dbcEntityTemplate template;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected ServiceContext serviceContext;

    /**
     * 新增
     *
     * @param service
     * @return
     */
    @Transactional
    public Mono<Service> addService(Service service) {
        return eventService.addEvent(new ReloadServiceEvent(service.getCode()))
                .flatMap(e -> template.insert(service));
    }

    /**
     * 更新
     *
     * @param service
     * @return
     */
    @Transactional
    public Mono<Service> updateService(Service service) {
        return template.selectOne(Query.query(where("code").is(service.getCode())), Service.class)
                .flatMap(s -> {
                    s.setProtocol(service.getProtocol());
                    s.setUpstreams(service.getUpstreams());
                    return eventService.addEvent(new ReloadServiceEvent(service.getCode()))
                            .flatMap(e -> template.update(s));
                }).switchIfEmpty(Mono.defer(() -> Mono.error(new GateWayException("服务不存在"))));
    }

    /**
     * 删除服务
     * @param serviceCode
     * @return
     */
    @Transactional
    public Mono<Integer> deleteService(String serviceCode) {
        return eventService.addEvent(new DeleteServiceEvent(serviceCode))
                // 删除服务
                .flatMap(e -> template.delete(Query.query(where("code").is(serviceCode)), Service.class))
                // 删除插件
                .flatMap(integer -> template.delete(Query.query(where("target").is(serviceCode)), Plugin.class));
    }

    /**
     * 查询服务
     *
     * @param serviceCode
     * @return
     */
    public Mono<Service> queryService(String serviceCode) {
        Service service = serviceContext.getService(serviceCode);
        return null == service ? Mono.empty() : Mono.just(service);
    }
}
