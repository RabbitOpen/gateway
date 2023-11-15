package rabbit.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rabbit.gateway.common.context.PrivilegeContext;
import rabbit.gateway.common.entity.Privilege;
import rabbit.gateway.common.event.ReloadPrivilegeEvent;
import reactor.core.publisher.Mono;

@Service
public class PrivilegeService {

    @Autowired
    protected R2dbcEntityTemplate template;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected PrivilegeContext privilegeContext;

    /**
     * 授权
     *
     * @param privilege
     * @return
     */
    @Transactional
    public Mono<Void> authorize(Privilege privilege) {
        Privilege exist = privilegeContext.getPrivilege(privilege.getCredential());
        return eventService.addEvent(new ReloadPrivilegeEvent(privilege.getCredential()))
                .flatMap(e -> {
                    if (null == exist) {
                        return template.insert(privilege);
                    } else {
                        exist.getPrivileges().putAll(privilege.getPrivileges());
                        return template.update(exist);
                    }
                }).then();
    }

    /**
     * 取消授权
     *
     * @param privilege
     * @return
     */
    @Transactional
    public Mono<Void> unAuthorize(Privilege privilege) {
        Privilege exist = privilegeContext.getPrivilege(privilege.getCredential());
        if (null == exist) {
            return Mono.empty();
        }
        return eventService.addEvent(new ReloadPrivilegeEvent(privilege.getCredential()))
                .flatMap(e -> {
                    privilege.getPrivileges().forEach((key, value) -> exist.getPrivileges().remove(key));
                    return template.update(exist);
                }).then();
    }

    /**
     * 查询授权
     * @param credential
     * @return
     */
    public Mono<Privilege> getPrivilege(String credential) {
        Privilege privilege = privilegeContext.getPrivilege(credential);
        return null == privilege ? Mono.empty() : Mono.just(privilege);
    }
}
