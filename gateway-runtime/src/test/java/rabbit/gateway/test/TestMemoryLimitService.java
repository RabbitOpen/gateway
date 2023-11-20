package rabbit.gateway.test;

import org.springframework.stereotype.Component;
import rabbit.gateway.runtime.service.MemoryLimitService;

/**
 * 单元测试专用
 */
@Component
public class TestMemoryLimitService implements MemoryLimitService {

    @Override
    public Status limit(String keyName, int expireSeconds, long clientLimit, long serverLimit) {
        return Status.OK;
    }
}
