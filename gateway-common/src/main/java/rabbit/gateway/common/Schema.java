package rabbit.gateway.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "name")
@JsonTypeIdResolver(TypeResolver.class)
public interface Schema {
}
