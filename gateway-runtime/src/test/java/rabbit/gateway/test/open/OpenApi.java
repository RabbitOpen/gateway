package rabbit.gateway.test.open;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import rabbit.discovery.api.common.http.anno.Body;
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
     * @return
     */
    @OpenApiCode("UNDEFINED-ROUTE")
    @GetMapping("/test/echo")
    Mono<HttpResponse<Route>> undefinedRoute();

    /**
     * 越权访问, 有apiCode的权限，但是路径写的是别的路径
     * @return
     */
    @OpenApiCode("RES00001")
    @GetMapping("/test/echo")
    Mono<HttpResponse<Route>> wrongPath();

    /**
     * 通过映射路径访问
     * @return
     */
    @OpenApiCode("MAPPING")
    @GetMapping("/test/echo/mapping")
    Mono<HttpResponse<String>> accessMappingPath();

    @OpenApiCode("VOID-RESPONSE")
    @GetMapping("/test/void")
    void callVoidRequest();

    /**
     * get 方法，body会被忽略
     * @param body
     * @return
     */
    @OpenApiCode("VOID-RESPONSE")
    @GetMapping("/test/void")
    Mono<HttpResponse<Void>> callMonoVoidRequest(@Body String body);

    @OpenApiCode("VOID-RESPONSE")
    @GetMapping("/test/void")
    HttpResponse<Void> callHttpResponseVoidRequest();

    @OpenApiCode("POST")
    @PostMapping("/test/post")
    HttpResponse<Void> post(@Body String body);
}
