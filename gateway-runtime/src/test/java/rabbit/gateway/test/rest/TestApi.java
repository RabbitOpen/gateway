package rabbit.gateway.test.rest;

import org.springframework.web.bind.annotation.GetMapping;
import rabbit.discovery.api.rest.anno.RestClient;
import rabbit.discovery.api.rest.http.HttpResponse;
import reactor.core.publisher.Mono;

@RestClient(application = "gateway", contextPath = "/test")
public interface TestApi {

    @GetMapping("/void")
    void callVoidRequest();

    @GetMapping("/void")
    Mono<HttpResponse<Void>> callMonoVoidRequest();
}
