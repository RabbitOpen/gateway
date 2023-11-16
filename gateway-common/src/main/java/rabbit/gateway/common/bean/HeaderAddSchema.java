package rabbit.gateway.common.bean;

import rabbit.gateway.common.Schema;

import java.util.HashMap;
import java.util.Map;

/**
 * 添加header
 */
public class HeaderAddSchema implements Schema {

    private Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
