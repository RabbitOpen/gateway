package rabbit.gateway.common.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rabbit.gateway.common.BaseEntity;
import rabbit.gateway.common.Protocol;
import rabbit.gateway.common.bean.Target;

import java.util.List;

/**
 * 服务
 */
@Table("service")
public class Service extends BaseEntity {

    /**
     * 服务code
     */
    @Column("code")
    private String code;

    /**
     * 协议
     */
    @Column("protocol")
    private Protocol protocol;

    /**
     * 服务节点信息
     */
    @Column("upstreams")
    private List<Target> upstreams;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public List<Target> getUpstreams() {
        return upstreams;
    }

    public void setUpstreams(List<Target> upstreams) {
        this.upstreams = upstreams;
    }
}
