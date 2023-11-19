package rabbit.gateway.common.bean;

import rabbit.gateway.common.Schema;

import java.util.HashMap;
import java.util.Map;

public class RequestRateLimit implements Schema {

    /**
     * 服务侧限流
     */
    private Long serverDefault = 1000L;

    /**
     * 客户侧限流，key为 credential
     */
    private Map<String, Long> clients = new HashMap<>();

    public Long getServerDefault() {
        return serverDefault;
    }

    public void setServerDefault(Long serverDefault) {
        this.serverDefault = serverDefault;
    }

    public Map<String, Long> getClients() {
        return clients;
    }

    public void setClients(Map<String, Long> clients) {
        this.clients = clients;
    }
}
