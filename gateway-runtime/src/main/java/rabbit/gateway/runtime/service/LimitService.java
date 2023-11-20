package rabbit.gateway.runtime.service;

import java.util.HashMap;
import java.util.Map;

public interface LimitService {

    /**
     * 计算限流值
     *
     * @param keyName
     * @param expireSeconds key超时时间
     * @param clientLimit
     * @param serverLimit
     * @return
     */
    Status limit(String keyName, int expireSeconds, long clientLimit, long serverLimit);

    enum Status {
        OK(0),
        CLIENT_LIMIT(1),
        SERVER_LIMIT(2);

        int value;

        Status(int value) {
            this.value = value;
        }

        static Status valueOf(Long value) {
            Map<Integer, Status> map = new HashMap<>();
            for (Status status : Status.values()) {
                map.put(status.value, status);
            }
            return map.get(value.intValue());
        }
    }
}
