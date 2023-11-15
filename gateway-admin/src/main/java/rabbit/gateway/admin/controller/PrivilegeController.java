package rabbit.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rabbit.gateway.admin.service.PrivilegeService;
import rabbit.gateway.common.entity.Privilege;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/privilege")
public class PrivilegeController {

    @Autowired
    protected PrivilegeService privilegeService;

    /**
     * 授权
     *
     * @param privilege
     * @return
     */
    @PostMapping("/authorize")
    public Mono<Void> authorize(@RequestBody Privilege privilege) {
        return privilegeService.authorize(privilege);
    }

    /**
     * 取消授权
     * @param privilege
     * @return
     */
    @PostMapping("/unAuthorize")
    public Mono<Void> unAuthorize(@RequestBody Privilege privilege) {
        return privilegeService.unAuthorize(privilege);
    }

    /**
     * 查询授权
     * @param credential
     * @return
     */
    @GetMapping("/query/{credential}")
    public Mono<Privilege> query(@PathVariable("credential") String credential) {
        return privilegeService.getPrivilege(credential);
    }
}
