package rabbit.gateway.common.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.util.ObjectUtils;
import rabbit.discovery.api.common.utils.JsonUtils;
import rabbit.gateway.common.BaseEntity;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.Schema;
import rabbit.gateway.common.bean.ApiDesc;
import rabbit.gateway.common.bean.RequestRateLimit;
import rabbit.gateway.common.bean.Target;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Configuration
public class R2dbcConfig extends AbstractR2dbcConfiguration {


    /**
     * insert时自动填充id等字段
     *
     * @return
     */
    @Bean
    public BeforeConvertCallback defaultValueGenerator() {
        return (entity, table) -> {
            if (entity instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) entity;
                if (ObjectUtils.isEmpty(baseEntity.getId())) {
                    baseEntity.setId(UUID.randomUUID().toString());
                }
            }
            return Mono.just(entity);
        };
    }

    /**
     * 自定义类型转换器
     *
     * @return
     */
    @Override
    protected List<Object> getCustomConverters() {
        List<Object> converterList = new ArrayList<>();
        converterList.add(TargetReader.INST);
        converterList.add(TargetWriter.INST);
        converterList.add(EventReader.INST);
        converterList.add(EventWriter.INST);
        converterList.add(RequestRateLimitReader.INST);
        converterList.add(RequestRateLimitWriter.INST);
        converterList.add(ApiMapReader.INST);
        converterList.add(ApiMapWriter.INST);
        converterList.add(RuleMapReader.INST);
        converterList.add(RuleMapWriter.INST);
        converterList.add(SchemaReader.INST);
        converterList.add(SchemaWriter.INST);
        converterList.add(SetReader.INST);
        converterList.add(SetWriter.INST);
        // java.util.Date和数据库对象互转
        converterList.add(DateReader.INST);
        converterList.add(DateWriter.INST);
        return converterList;
    }

    /**
     * set reader
     */
    @ReadingConverter
    enum SetReader implements Converter<String, Set<String>> {
        INST;

        @Override
        public Set<String> convert(String json) {
            return JsonUtils.readValue(json, JsonUtils.constructListType(HashSet.class, String.class));
        }
    }

    /**
     * set writer
     */
    @WritingConverter
    enum SetWriter implements Converter<Set<String>, String> {
        INST;

        @Override
        public String convert(Set<String> targets) {
            return JsonUtils.writeObject(targets);
        }
    }

    /**
     * Map reader
     */
    @ReadingConverter
    enum ApiMapReader implements Converter<String, Map<String, ApiDesc>> {
        INST;

        @Override
        public Map<String, ApiDesc> convert(String json) {
            return JsonUtils.readValue(json, JsonUtils.constructMapType(HashMap.class, String.class, ApiDesc.class));
        }
    }

    /**
     * Map writer
     */
    @WritingConverter
    enum ApiMapWriter implements Converter<Map<String, ApiDesc>, String> {
        INST;

        @Override
        public String convert(Map<String, ApiDesc> targets) {
            return JsonUtils.writeObject(targets);
        }
    }

    /**
     * Map reader
     */
    @ReadingConverter
    enum RuleMapReader implements Converter<String, Map<String, String>> {
        INST;

        @Override
        public Map<String, String> convert(String json) {
            return JsonUtils.readValue(json, JsonUtils.constructMapType(HashMap.class, String.class, String.class));
        }
    }

    /**
     * Map writer
     */
    @WritingConverter
    enum RuleMapWriter implements Converter<Map<String, String>, String> {
        INST;

        @Override
        public String convert(Map<String, String> targets) {
            return JsonUtils.writeObject(targets);
        }
    }

    /**
     * List<Target> reader
     */
    @ReadingConverter
    enum TargetReader implements Converter<String, List<Target>> {
        INST;

        @Override
        public List<Target> convert(String json) {
            return JsonUtils.readValue(json, JsonUtils.constructListType(ArrayList.class, Target.class));
        }
    }

    /**
     * List<Target> writer
     */
    @WritingConverter
    enum TargetWriter implements Converter<List<Target>, String> {
        INST;

        @Override
        public String convert(List<Target> targets) {
            return JsonUtils.writeObject(targets);
        }
    }

    /**
     * RequestRateLimit reader
     */
    @ReadingConverter
    enum RequestRateLimitReader implements Converter<String, RequestRateLimit> {
        INST;

        @Override
        public RequestRateLimit convert(String json) {
            return JsonUtils.readValue(json, RequestRateLimit.class);
        }
    }

    /**
     * RequestRateLimit writer
     */
    @WritingConverter
    enum RequestRateLimitWriter implements Converter<RequestRateLimit, String> {
        INST;

        @Override
        public String convert(RequestRateLimit rateLimit) {
            return JsonUtils.writeObject(rateLimit);
        }
    }

    /**
     * GateWayEvent reader
     */
    @ReadingConverter
    enum EventReader implements Converter<String, GateWayEvent> {
        INST;

        @Override
        public GateWayEvent convert(String json) {
            return JsonUtils.readValue(json, GateWayEvent.class);
        }
    }

    /**
     * GateWayEvent writer
     */
    @WritingConverter
    enum EventWriter implements Converter<GateWayEvent, String> {
        INST;

        @Override
        public String convert(GateWayEvent event) {
            return JsonUtils.writeObject(event);
        }
    }

    /**
     * 日期reader
     */
    @ReadingConverter
    enum DateReader implements Converter<LocalDateTime, Date> {
        INST;

        @Override
        public Date convert(LocalDateTime dateTime) {
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    /**
     * 日期writer
     */
    @WritingConverter
    enum DateWriter implements Converter<Date, LocalDateTime> {
        INST;

        @Override
        public LocalDateTime convert(Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * Schema reader
     */
    @ReadingConverter
    enum SchemaReader implements Converter<String, Schema> {
        INST;

        @Override
        public Schema convert(String json) {
            return JsonUtils.readValue(json, Schema.class);
        }
    }

    /**
     * Schema writer
     */
    @WritingConverter
    enum SchemaWriter implements Converter<Schema, String> {
        INST;

        @Override
        public String convert(Schema schema) {
            return JsonUtils.writeObject(schema);
        }
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return null;
    }
}
