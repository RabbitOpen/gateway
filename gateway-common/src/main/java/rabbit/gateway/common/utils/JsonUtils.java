package rabbit.gateway.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import rabbit.gateway.common.exception.GateWayException;

import java.lang.reflect.Type;
import java.util.Collection;

public class JsonUtils {
    
    private ObjectMapper mapper = new ObjectMapper();

    private static final JsonUtils inst = new JsonUtils();

    private JsonUtils() {
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T readValue(String json, Type type) {
        try {
            return inst.getMapper().readValue(json, inst.getMapper().getTypeFactory().constructType(type));
        } catch (JsonProcessingException e) {
            throw new GateWayException(e);
        }
    }

    public static <T> T readValue(String json, JavaType type) {
        try {
            return inst.getMapper().readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new GateWayException(e);
        }
    }

    public static String writeObject(Object data) {
        try {
            return inst.getMapper().writeValueAsString(data);
        } catch (JsonProcessingException var2) {
            throw new GateWayException(var2);
        }
    }

    public static JavaType constructListType(Class<? extends Collection> collectionClz, Class<?> elementType) {
        return inst.getMapper().getTypeFactory().constructCollectionType(collectionClz, elementType);
    }

    private ObjectMapper getMapper() {
        return this.mapper;
    }
}
