package rabbit.gateway.test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rabbit.gateway.common.exception.GateWayException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/echo")
    public Mono<String> echoRequest(ServerHttpRequest request, ServerHttpResponse response) {
        return Mono.create(s -> {
            addResponseHeaders(request, response);
            s.success("hello");
        });
    }

    private void addResponseHeaders(ServerHttpRequest request, ServerHttpResponse response) {
        request.getHeaders().forEach((k, v) -> {
            if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(k)) {
                return;
            }
            response.getHeaders().put(k, v);
        });
    }

    @GetMapping("/void")
    public void voidRequest(ServerHttpRequest request, ServerHttpResponse response) {
        addResponseHeaders(request, response);
    }

    @GetMapping("/error")
    public void error() {
        throw new GateWayException("模拟异常");
    }
}
