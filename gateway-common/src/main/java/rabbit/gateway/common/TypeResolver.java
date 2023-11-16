package rabbit.gateway.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class TypeResolver extends TypeIdResolverBase {

    protected JavaType javaType;

    @Override
    public void init(JavaType javaType) {
        this.javaType = javaType;
    }

    /**
     * 对象转值
     *
     * @param value
     * @return
     */
    @Override
    public String idFromValue(Object value) {
        return value.getClass().getName();
    }

    @Override
    public String idFromValueAndType(Object o, Class<?> aClass) {
        return null;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        try {
            return context.constructSpecializedType(javaType, Class.forName(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }
}
