package rabbit.gateway.test.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import rabbit.discovery.api.rest.anno.RestClient;
import rabbit.gateway.common.entity.Route;
import reactor.core.publisher.Mono;

@RestClient(application = "gateway", contextPath = "/route")
public interface RouteApi {

    /**
     * 新增路由
     *
     * @param route
     * @return
     */
    @PostMapping("/add")
    Mono<Route> add(@RequestBody Route route);

    /**
     * 更新路由
     *
     * @param route
     * @return
     */
    @PostMapping("/update")
    Mono<Route> update(@RequestBody Route route);

    /**
     * 删除路由
     *
     * @param routeCode
     * @return
     */
    @PostMapping("/delete/{routeCode}")
    Mono<Integer> delete(@PathVariable("routeCode") String routeCode);

    /**
     * 查询路由
     *
     * @param routeCode
     * @return
     */
    @GetMapping("/query/{routeCode}")
    Mono<Route> query(@PathVariable("routeCode") String routeCode);
}
