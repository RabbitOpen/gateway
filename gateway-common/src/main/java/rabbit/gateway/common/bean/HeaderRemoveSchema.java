package rabbit.gateway.common.bean;

import rabbit.gateway.common.Schema;

import java.util.HashSet;
import java.util.Set;

public class HeaderRemoveSchema implements Schema {

    private Set<String> headers = new HashSet<>();

    public Set<String> getHeaders() {
        return headers;
    }

    public void setHeaders(Set<String> headers) {
        this.headers = headers;
    }
}
