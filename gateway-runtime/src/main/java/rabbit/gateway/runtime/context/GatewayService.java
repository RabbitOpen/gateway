package rabbit.gateway.runtime.context;

import org.springframework.beans.BeanUtils;
import rabbit.gateway.common.bean.Target;
import rabbit.gateway.common.bean.WeightList;
import rabbit.gateway.common.entity.Service;

public class GatewayService extends Service {

    private WeightList<Target> targets;

    public GatewayService(Service service) {
        BeanUtils.copyProperties(service, this);
        this.targets = new WeightList<>(service.getUpstreams());
    }

    /**
     * 获取服务对象
     * @return
     */
    public Target getTarget() {
        return targets.next();
    }
}
