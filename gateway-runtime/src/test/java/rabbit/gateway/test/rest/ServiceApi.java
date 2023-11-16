package rabbit.gateway.test.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import rabbit.discovery.api.rest.anno.RestClient;
import rabbit.gateway.common.entity.Service;
import reactor.core.publisher.Mono;

@RestClient(application = "gateway", contextPath = "/service")
public interface ServiceApi {

    /**
     * 新增服务
     *
     * @param service
     * @return
     */
    @PostMapping("/add")
    Mono<Service> add(@RequestBody Service service);

    /**
     * 更新服务
     *
     * @param service
     * @return
     */
    @PostMapping("/update")
    Mono<Service> update(@RequestBody Service service);

    /**
     * 查询服务
     *
     * @param serviceCode
     * @return
     */
    @GetMapping("/query/{serviceCode}")
    Mono<Service> query(@PathVariable("serviceCode") String serviceCode);

    /**
     * 删除服务
     *
     * @param serviceCode
     * @return
     */
    @PostMapping("/delete/{serviceCode}")
    Mono<Integer> delete(@PathVariable("serviceCode") String serviceCode);
}
