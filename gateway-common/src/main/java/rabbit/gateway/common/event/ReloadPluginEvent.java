package rabbit.gateway.common.event;

import org.springframework.context.ApplicationContext;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.context.PluginContext;

/**
 * 重载插件
 */
public class ReloadPluginEvent implements GateWayEvent {

    private String serviceCode;

    public ReloadPluginEvent() {
    }

    public ReloadPluginEvent(String serviceCode) {
        this();
        this.setServiceCode(serviceCode);
    }

    @Override
    public void run(ApplicationContext context) {
        context.getBean(PluginContext.class).reloadPlugins(getServiceCode());
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }
}
