package rabbit.gateway.common.event;

import org.springframework.context.ApplicationContext;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.ServiceContext;

/**
 * 重载服务事件
 */
public class ReloadServiceEvent implements GateWayEvent {

    /**
     * 服务编码
     */
    private String serviceCode;

    public ReloadServiceEvent() {
    }

    public ReloadServiceEvent(String serviceCode) {
        this();
        setServiceCode(serviceCode);
    }

    @Override
    public void run(ApplicationContext context) {
        context.getBean(ServiceContext.class).reloadService(getServiceCode());
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }
}
