package rabbit.gateway.common.event;

import org.springframework.context.ApplicationContext;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.context.PrivilegeContext;

public class ReloadPrivilegeEvent implements GateWayEvent {

    /**
     * 凭据
     */
    private String credential;

    public ReloadPrivilegeEvent() {
    }

    public ReloadPrivilegeEvent(String credential) {
        this();
        setCredential(credential);
    }

    @Override
    public void run(ApplicationContext context) {
        context.getBean(PrivilegeContext.class).reloadPrivileges(getCredential());
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
