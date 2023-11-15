package rabbit.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rabbit.gateway.admin.service.RuntimeServiceService;
import rabbit.gateway.common.entity.Service;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/service")
public class ServiceController {

    @Autowired
    private RuntimeServiceService rss;

    /**
     * 新增服务
     * @param service
     * @return
     */
    @PostMapping("/add")
    public Mono<Service> add(@RequestBody Service service) {
        return rss.addService(service);
    }

    /**
     * 更新服务
     * @param service
     * @return
     */
    @PostMapping("/update")
    public Mono<Service> update(@RequestBody Service service) {
        return rss.updateService(service);
    }

    /**
     * 查询服务
     * @param serviceCode
     * @return
     */
    @GetMapping("/query/{serviceCode}")
    public Mono<Service> query(@PathVariable("serviceCode") String serviceCode) {
        return rss.queryService(serviceCode);
    }

    /**
     * 删除服务
     * @param serviceCode
     * @return
     */
    @PostMapping("/delete/{serviceCode}")
    public Mono<Integer> delete(@PathVariable("serviceCode") String serviceCode) {
        return rss.deleteService(serviceCode);
    }
}
