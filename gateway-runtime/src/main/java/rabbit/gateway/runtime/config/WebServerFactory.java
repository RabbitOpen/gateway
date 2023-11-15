package rabbit.gateway.runtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.stereotype.Component;

/**
 * 双端口服务启动
 */
@Component
public class WebServerFactory extends NettyReactiveWebServerFactory {

    @Value("${server.port}")
    int port;

    @Override
    public WebServer getWebServer(HttpHandler httpHandler) {
        WebServer adminServer = super.getWebServer(httpHandler);
        WebServer bizServer = super.getWebServer(httpHandler);
        return new WebServer() {
            @Override
            public void start() throws WebServerException {
                adminServer.start();
                setPort(port + 1);
                bizServer.start();
            }

            @Override
            public void stop() throws WebServerException {
                adminServer.stop();
                bizServer.stop();
            }

            @Override
            public int getPort() {
                return port;
            }
        };
    }
}
