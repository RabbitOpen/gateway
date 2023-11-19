package rabbit.gateway.common.config;

import org.springframework.data.util.TypeInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rabbit.discovery.api.common.utils.JsonUtils.constructMapType;
import static rabbit.discovery.api.common.utils.JsonUtils.readValue;

public class ConverterManager {

    private static final ConverterManager inst = new ConverterManager();

    private ConverterManager() {

    }

    /**
     * map数据类型转换器
     */
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Converter<?>>>> mapClzConverters = new HashMap();


    /**
     * 添加map类型的转换器
     *
     * @param keyType
     * @param valueType
     * @param converter
     * @param <T>
     */
    public static <T> void addMapConverter(Class<? extends Map> mapClz, Class<?> keyType, Class<?> valueType,
                                           Converter<T> converter) {
        inst.mapClzConverters.computeIfAbsent(mapClz, k -> new HashMap<>())
                .computeIfAbsent(keyType, k -> new HashMap<>()).put(valueType, converter);

    }

    /**
     * 添加默认的map类型的转换器
     *
     * @param keyType
     * @param valueType
     * @param <T>
     */
    public static <T> void addMapConverter(Class<?> keyType, Class<?> valueType) {
        Converter<T> converter = value -> readValue(value, constructMapType(Map.class, keyType, valueType));
        addMapConverter(Map.class, keyType, valueType, converter);
    }


    /**
     * 是否包含指定类型
     *
     * @param type
     * @return
     */
    public static boolean contains(TypeInformation<?> type) {
        if (type.isMap()) {
            List<TypeInformation<?>> arguments = type.getTypeArguments();
            return inst.mapClzConverters.containsKey(Map.class) &&
                    inst.mapClzConverters.get(Map.class).containsKey(arguments.get(0).getType()) &&
                    inst.mapClzConverters.get(Map.class).get(arguments.get(0).getType()).containsKey(arguments.get(1).getType());
        }
        return false;
    }

    public static Converter getConverter(TypeInformation<?> type) {
        List<TypeInformation<?>> arguments = type.getTypeArguments();
        if (type.isMap()) {
            return inst.mapClzConverters.get(Map.class).get(arguments.get(0).getType()).get(arguments.get(1).getType());
        }
        return inst.mapClzConverters.get(Map.class).get(arguments.get(0).getType()).get(arguments.get(1).getType());
    }
}
