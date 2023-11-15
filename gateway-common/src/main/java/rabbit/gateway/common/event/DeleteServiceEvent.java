package rabbit.gateway.common.event;

import org.springframework.context.ApplicationContext;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.context.ServiceContext;

/**
 * 删除服务事件
 */
public class DeleteServiceEvent implements GateWayEvent {

    /**
     * 服务编码
     */
    private String serviceCode;

    public DeleteServiceEvent() {
    }

    public DeleteServiceEvent(String serviceCode) {
        this();
        setServiceCode(serviceCode);
    }

    @Override
    public void run(ApplicationContext context) {
        context.getBean(ServiceContext.class).deleteService(getServiceCode());
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }
}
