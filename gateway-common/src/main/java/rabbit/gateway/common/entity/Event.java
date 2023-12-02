package rabbit.gateway.common.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rabbit.gateway.common.GateWayEvent;

@Table("event")
public class Event  {

    /**
     * 事件id
     */
    @Id
    @Column("id")
    private Long id;

    /**
     * 创建时间, 清理数据时使用
     */
    @Column("created_time")
    private Long createdTime;


    /**
     * 事件内容
     */
    @Column("event")
    private GateWayEvent gateWayEvent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public GateWayEvent getGateWayEvent() {
        return gateWayEvent;
    }

    public void setGateWayEvent(GateWayEvent gateWayEvent) {
        this.gateWayEvent = gateWayEvent;
    }
}
