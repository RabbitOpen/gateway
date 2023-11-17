package rabbit.gateway.runtime.context;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import rabbit.gateway.common.bean.Target;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;

@Component
public class HttpClientFactory {

    protected WebClient webClient;

    protected ReactorResourceFactory resourceFactory = new ReactorResourceFactory();

    // 2M
    @Value("${spring.codec.max-in-memory-size:2097152}")
    protected int maxBodySize;

    protected ExchangeStrategies strategies;

    @PostConstruct
    public void init() {
        strategies = ExchangeStrategies.builder().codecs(codec -> codec.defaultCodecs().maxInMemorySize(maxBodySize)).build();
        resourceFactory.setLoopResources(LoopResources.create("webclient-pool-", 1,
                Runtime.getRuntime().availableProcessors() * 4, true));
        resourceFactory.setConnectionProvider(ConnectionProvider.builder("httpClient")
                .maxConnections(2048)
                .pendingAcquireMaxCount(4096)
                .maxLifeTime(Duration.ofMinutes(30))
                .maxIdleTime(Duration.ofMinutes(1))
                .build());
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(resourceFactory,
                httpClient -> httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000));
        webClient = WebClient.builder().exchangeStrategies(strategies).clientConnector(connector).build();
    }

    @PreDestroy
    public void close() {
        resourceFactory.getConnectionProvider().dispose();
        resourceFactory.destroy();
    }

    /**
     * 执行请求
     *
     * @param context
     * @return
     */
    public Mono<ResponseEntity<String>> execute(HttpRequestContext context) {
        GatewayService service = context.getService();
        Target target = service.getTarget();
        WebClient.RequestBodyUriSpec webRequest = getCachedWebClient(service, target).method(context.getRequest().getMethod());
        addRequestHeaders(context, webRequest, target);
        webRequest.uri(uriBuilder -> uriBuilder.scheme(service.getProtocol().name().toLowerCase())
                .host(target.getHost()).port(target.getPort())
                .path(context.getRequestPath())
                .queryParams(context.getRequest().getQueryParams())
                .build());
        return DataBufferUtils.join(context.getRequest().getBody()).map(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            return bytes;
        }).flatMap(bodyBytes -> {
            webRequest.bodyValue(bodyBytes);        // 有body
            return getResponse(webRequest);
        }).switchIfEmpty(getResponse(webRequest));  // 没有body
    }

    private Mono<ResponseEntity<String>> getResponse(WebClient.RequestBodyUriSpec webRequest) {
        return Mono.defer(() -> webRequest.exchangeToMono(this::readResponse));
    }

    /**
     * 读取响应
     * @param r
     * @return
     */
    private Mono<ResponseEntity<String>> readResponse(ClientResponse r) {
        return r.bodyToMono(ByteArrayResource.class).map(bytes -> new ResponseEntity<>(new String(bytes.getByteArray()), r.headers().asHttpHeaders(), r.statusCode()))
                .switchIfEmpty(Mono.just(new ResponseEntity<>("", r.headers().asHttpHeaders(), r.statusCode())));
    }

    /**
     * 添加请求头
     *
     * @param context
     * @param webRequest
     * @param target
     */
    private void addRequestHeaders(HttpRequestContext context, WebClient.RequestBodyUriSpec webRequest, Target target) {
        context.getRequest().getHeaders().forEach((name, values) -> {
            if (context.isRemovedHeader(name) || HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                return;
            }
            webRequest.headers(httpHeaders -> httpHeaders.put(name, values));
        });
        context.getHeaders2Add().forEach((name, value) -> {
            if (context.isRemovedHeader(name)) {
                return;
            }
            webRequest.headers(httpHeaders -> httpHeaders.set(name, value));
        });
        webRequest.headers(httpHeaders -> httpHeaders.set("host", target.getHost().concat(":")
                .concat(Integer.toString(target.getPort()))));
    }

    private WebClient getCachedWebClient(GatewayService service, Target target) {
        return webClient;
    }
}
