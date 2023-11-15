package rabbit.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import rabbit.gateway.common.entity.Privilege;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class PrivilegeService {

    @Autowired
    protected R2dbcEntityTemplate template;

    @Autowired
    protected EventService eventService;

    /**
     * 授权
     * @param credential
     * @param privileges
     * @return
     */
    public Mono<Void> authorize(String credential, List<Privilege> privileges) {
        return Mono.empty();
    }
}
