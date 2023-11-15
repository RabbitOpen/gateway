package rabbit.gateway.runtime;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.gateway.common.ServiceContext;

@org.springframework.stereotype.Service
public class GateWayContext implements ServiceContext {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void reloadService(String serviceCode) {
        logger.info("service[{}] is loaded!", serviceCode);
    }

    @Override
    public void deleteService(String serviceCode) {
        logger.info("service[{}] is deleted!", serviceCode);
    }
}
