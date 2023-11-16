package rabbit.gateway.test;

import org.springframework.stereotype.Component;
import rabbit.discovery.api.common.ServerNode;
import rabbit.discovery.api.rest.LoadBalancer;
import rabbit.discovery.api.rest.http.HttpRequest;

/**
 * 指向本机
 */
@Component
public class LocalLoadBalancer implements LoadBalancer {

    @Override
    public ServerNode choose(HttpRequest httpRequest) {
        return new ServerNode("http://localhost:12800");
    }
}
