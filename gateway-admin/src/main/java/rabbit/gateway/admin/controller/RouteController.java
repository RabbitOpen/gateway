package rabbit.gateway.admin.controller;

import org.springframework.web.bind.annotation.*;
import rabbit.gateway.common.entity.Route;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/route")
public class RouteController {

    /**
     * 新增路由
     * @param route
     * @return
     */
    @PostMapping("/add")
    public Mono<Route> add(@RequestBody Route route) {
        return Mono.empty();
    }

    /**
     * 更新路由
     * @param route
     * @return
     */
    @PostMapping("/update")
    public Mono<Route> update(@RequestBody Route route) {
        return Mono.empty();
    }

    /**
     * 删除路由
     * @param routeCode
     * @return
     */
    @PostMapping("/delete/{routeCode}")
    public Mono<Route> delete(@PathVariable("routeCode") String routeCode) {
        return Mono.empty();
    }

    /**
     * 查询路由
     * @param routeCode
     * @return
     */
    @PostMapping("/query/{routeCode}")
    public Mono<Route> query(@PathVariable("routeCode") String routeCode) {
        return Mono.empty();
    }
}
