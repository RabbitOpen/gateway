package rabbit.gateway.admin.controller;

import org.springframework.web.bind.annotation.*;
import rabbit.gateway.common.entity.Privilege;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/privilege")
public class PrivilegeController {

    /**
     * 授权
     *
     * @param credential
     * @param privileges
     * @return
     */
    @PostMapping("/authorize/{credential}")
    public Mono<Void> add(@PathVariable("credential") String credential, @RequestBody List<Privilege> privileges) {

        return Mono.empty();
    }

}
