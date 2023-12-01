package rabbit.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rabbit.gateway.common.PluginName;
import rabbit.gateway.common.Schema;
import rabbit.gateway.common.bean.HeaderAddSchema;
import rabbit.gateway.common.bean.HeaderRemoveSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.common.event.ReloadPluginEvent;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.springframework.data.relational.core.query.Criteria.where;

@Service
public class PluginService {

    @Autowired
    protected R2dbcEntityTemplate template;

    @Autowired
    protected EventService eventService;

    Map<PluginName, Supplier<Schema>> defaultSchemaSupplier =new EnumMap<>(PluginName.class);

    public PluginService() {
        defaultSchemaSupplier.put(PluginName.ADD_REQUEST_HEADERS, HeaderAddSchema::new);
        defaultSchemaSupplier.put(PluginName.ADD_RESPONSE_HEADERS, HeaderAddSchema::new);
        defaultSchemaSupplier.put(PluginName.REMOVE_REQUEST_HEADERS, HeaderRemoveSchema::new);
    }
    /**
     * 新增/替换已经存在的插件
     *
     * @param plugin
     * @return
     */
    @Transactional
    public Mono<Plugin> replacePlugin(Plugin plugin) {
        if (null == plugin.getSchema() && defaultSchemaSupplier.containsKey(plugin.getName())) {
            plugin.setSchema(defaultSchemaSupplier.get(plugin.getName()).get());
        }
        return eventService.addEvent(new ReloadPluginEvent(plugin.getTarget()))
                .flatMap(e -> deletePlugin(plugin.getTarget(), plugin.getName().name()))   // 删除旧的
                .flatMap(c -> template.insert(plugin));                             // 插入新的
    }

    /**
     * 删除服务插件
     *
     * @param pluginName
     * @param target
     * @return
     */
    @Transactional
    public Mono<Integer> removePlugin(String pluginName, String target) {
        return deletePlugin(target, pluginName).flatMap(c -> {
            if (0 == c) {
                return Mono.just(c);
            } else {
                return eventService.addEvent(new ReloadPluginEvent(target)).map(e -> c);
            }
        });
    }

    private Mono<Integer> deletePlugin(String target, String name) {
        Query query = Query.query(where("target").is(target)
                .and("name").is(name));
        return template.delete(query, Plugin.class);
    }

}
