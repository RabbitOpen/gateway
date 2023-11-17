package rabbit.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import rabbit.gateway.common.bean.ApiDesc;
import rabbit.gateway.common.context.PrivilegeContext;
import rabbit.gateway.common.entity.Privilege;
import rabbit.gateway.common.event.ReloadPrivilegeEvent;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.data.relational.core.query.Criteria.where;

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
        return eventService.addEvent(new ReloadPrivilegeEvent(privilege.getCredential())).flatMap(e -> {
            Query query = Query.query(where("credential").is(privilege.getCredential()));
            return template.selectOne(query, Privilege.class).flatMap(exist -> {
                // 存在就更新
                Map<String, ApiDesc> privileges = exist.getPrivileges();
                privileges.putAll(privilege.getPrivileges());
                privilege.setPrivileges(privileges);
                privilege.setId(exist.getId());
                // 更新非空字段
                return template.update(privilege);
            }).switchIfEmpty(template.insert(privilege));
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
        if (CollectionUtils.isEmpty(privilege.getPrivileges())) {
            return Mono.empty();
        }
        return eventService.addEvent(new ReloadPrivilegeEvent(privilege.getCredential())).flatMap(e -> {
            Query query = Query.query(where("credential").is(privilege.getCredential()));
            return template.selectOne(query, Privilege.class).flatMap(exist -> {
                Map<String, ApiDesc> privileges = exist.getPrivileges();
                privilege.getPrivileges().forEach((k, v) -> privileges.remove(k));
                exist.setPrivileges(privileges);
                exist.setId(exist.getId());
                return template.update(exist);
            });
        }).then();
    }

    /**
     * 查询授权
     *
     * @param credential
     * @return
     */
    public Mono<Privilege> getPrivilege(String credential) {
        Privilege privilege = privilegeContext.getPrivilege(credential);
        return null == privilege ? Mono.empty() : Mono.just(privilege);
    }
}
