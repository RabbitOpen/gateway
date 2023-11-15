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
import rabbit.gateway.common.BaseEntity;
import rabbit.gateway.common.GateWayEvent;
import rabbit.gateway.common.bean.Target;
import rabbit.gateway.common.utils.JsonUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
        // java.util.Date和数据库对象互转
        converterList.add(DateReader.INST);
        converterList.add(DateWriter.INST);
        return converterList;
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

    @Override
    public ConnectionFactory connectionFactory() {
        return null;
    }
}
