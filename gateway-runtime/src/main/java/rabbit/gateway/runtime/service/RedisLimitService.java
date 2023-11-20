package rabbit.gateway.runtime.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
@ConditionalOnMissingBean(MemoryLimitService.class)
public class RedisLimitService extends RedisTemplate<String, String> implements LimitService {

    @Autowired
    protected RedisConnectionFactory connectionFactory;

    protected RedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        StringBuilder script = new StringBuilder();
        script.append("local client = redis.call('hincrby', KEYS[1], 'client', 1) \n");
        script.append("local server = redis.call('hincrby', KEYS[1], 'server', 1) \n");
        script.append("redis.call('expire', KEYS[1], tonumber(ARGV[1])) \n");
        script.append("if client > tonumber(ARGV[2]) then \n");
        script.append("   redis.call('hincrby', KEYS[1], 'client', -1)\n");
        script.append("   redis.call('hincrby', KEYS[1], 'server', -1)\n");
        // 超过客户端限流设置
        script.append("   return 1 \n");
        script.append("end \n");
        script.append("if server > tonumber(ARGV[3]) then \n");
        script.append("   redis.call('hincrby', KEYS[1], 'client', -1)\n");
        script.append("   redis.call('hincrby', KEYS[1], 'server', -1)\n");
        // 超过服务端限流设置
        script.append("   return 2 \n");
        script.append("end \n");
        script.append("return 0");
        redisScript = new DefaultRedisScript<>(script.toString(), Long.class);
    }

    @Override
    public void afterPropertiesSet() {
        setConnectionFactory(connectionFactory);
        setKeySerializer(StringRedisSerializer.UTF_8);
        setValueSerializer(StringRedisSerializer.UTF_8);
        setHashKeySerializer(StringRedisSerializer.UTF_8);
        setHashValueSerializer(StringRedisSerializer.UTF_8);
        super.afterPropertiesSet();
    }

    @Override
    public Status limit(String keyName, int expireSeconds, long clientLimit, long serverLimit) {
        return Status.valueOf(opsForHash().getOperations().execute(redisScript, Arrays.asList(keyName),
                Integer.toString(expireSeconds), Long.toString(clientLimit), Long.toString(serverLimit)));
    }
}
