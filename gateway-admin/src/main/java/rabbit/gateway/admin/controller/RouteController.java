package rabbit.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rabbit.gateway.admin.service.RouteService;
import rabbit.gateway.common.entity.Route;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    /**
     * 新增路由
     * @param route
     * @return
     */
    @PostMapping("/add")
    public Mono<Route> add(@RequestBody Route route) {
        return routeService.add(route);
    }

    /**
     * 更新路由
     * @param route
     * @return
     */
    @PostMapping("/update")
    public Mono<Route> update(@RequestBody Route route) {
        return routeService.update(route);
    }

    /**
     * 删除路由
     * @param routeCode
     * @return
     */
    @PostMapping("/delete/{routeCode}")
    public Mono<Integer> delete(@PathVariable("routeCode") String routeCode) {
        return routeService.delete(routeCode);
    }

    /**
     * 查询路由
     * @param routeCode
     * @return
     */
    @GetMapping("/query/{routeCode}")
    public Mono<Route> query(@PathVariable("routeCode") String routeCode) {
        return routeService.query(routeCode);
    }
}
