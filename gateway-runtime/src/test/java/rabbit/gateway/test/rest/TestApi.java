package rabbit.gateway.test.rest;

import org.springframework.web.bind.annotation.GetMapping;
import rabbit.discovery.api.rest.anno.RestClient;
import rabbit.discovery.api.rest.http.HttpResponse;
import reactor.core.publisher.Mono;

@RestClient(application = "gateway", contextPath = "/test")
public interface TestApi {

    @GetMapping("/void")
    void callVoidRequest();

    /**
     * 模拟异常
     */
    @GetMapping("/error")
    void fakeException();

    @GetMapping("/void")
    Mono<HttpResponse<Void>> callMonoVoidRequest();
}
