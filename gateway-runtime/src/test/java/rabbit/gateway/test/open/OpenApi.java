package rabbit.gateway.test.open;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rabbit.discovery.api.rest.anno.OpenApiClient;
import rabbit.discovery.api.rest.anno.OpenApiCode;
import rabbit.discovery.api.rest.http.HttpResponse;
import rabbit.gateway.common.entity.Route;
import reactor.core.publisher.Mono;

@OpenApiClient(credential = "CRE0001", baseUri = "http://localhost:12801",
        privateKey = "${discovery.application.security.key}")
public interface OpenApi {

    @OpenApiCode("RES00001")
    @GetMapping("/route/query/{routeCode}")
    Mono<HttpResponse<Route>> queryRoute(@PathVariable("routeCode") String routeCode);

    /**
     * 未定义的路由
     * @param routeCode
     * @return
     */
    @OpenApiCode("UNDEFINED-ROUTE")
    @GetMapping("/route/query/{routeCode}")
    Mono<HttpResponse<Route>> undefinedRoute(@PathVariable("routeCode") String routeCode);
}
