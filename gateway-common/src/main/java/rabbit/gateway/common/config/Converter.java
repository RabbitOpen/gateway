package rabbit.gateway.common.config;

public interface Converter<T> {

    T read(String value);
}
