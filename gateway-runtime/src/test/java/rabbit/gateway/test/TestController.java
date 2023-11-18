package rabbit.gateway.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import rabbit.gateway.common.exception.GateWayException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class TestController {

    private Logger logger = LoggerFactory.getLogger(getClass());

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

    @PostMapping("/post")
    public void post(ServerHttpRequest request, ServerHttpResponse response,
                     @RequestBody() String body) {
        addResponseHeaders(request, response);
        logger.info("request body: {}", body);
        response.getHeaders().set("request-body", body);
    }
}
