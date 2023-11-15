package rabbit.gateway.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import rabbit.gateway.common.resolver.SchemaResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "name")
@JsonTypeIdResolver(SchemaResolver.class)
public interface Schema {
}
