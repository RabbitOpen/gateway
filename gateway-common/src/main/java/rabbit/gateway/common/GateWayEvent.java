package rabbit.gateway.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.springframework.context.ApplicationContext;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "name")
@JsonTypeIdResolver(TypeResolver.class)
public interface GateWayEvent {

    void run(ApplicationContext context);
}
