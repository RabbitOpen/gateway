package rabbit.gateway.test.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import rabbit.discovery.api.rest.anno.RestClient;
import rabbit.gateway.common.entity.Privilege;
import reactor.core.publisher.Mono;

@RestClient(application = "gateway", contextPath = "/privilege")
public interface PrivilegeApi {

    /**
     * 授权
     *
     * @param privilege
     * @return
     */
    @PostMapping("/authorize")
    Mono<Void> authorize(@RequestBody Privilege privilege);

    /**
     * 取消授权
     * @param privilege
     * @return
     */
    @PostMapping("/unAuthorize")
    Mono<Void> unAuthorize(@RequestBody Privilege privilege);

    /**
     * 查询授权
     * @param credential
     * @return
     */
    @GetMapping("/query/{credential}")
    Mono<Privilege> query(@PathVariable("credential") String credential);
}
