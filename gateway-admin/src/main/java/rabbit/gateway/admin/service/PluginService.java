package rabbit.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.common.event.ReloadPluginEvent;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

@Service
public class PluginService {

    @Autowired
    protected R2dbcEntityTemplate template;

    @Autowired
    protected EventService eventService;

    /**
     * 新增/替换已经存在的插件
     *
     * @param plugin
     * @return
     */
    @Transactional
    public Mono<Plugin> replacePlugin(Plugin plugin) {
        return eventService.addEvent(new ReloadPluginEvent(plugin.getTarget()))
                .flatMap(e -> deletePlugin(plugin.getTarget(), plugin.getName()))   // 删除旧的
                .flatMap(c -> template.insert(plugin));                             // 插入新的
    }

    /**
     * 删除服务插件
     * @param pluginName
     * @param target
     * @return
     */
    @Transactional
    public Mono<Integer> removePlugin(String pluginName, String target) {
        return eventService.addEvent(new ReloadPluginEvent(target))
                .flatMap(e -> deletePlugin(target, pluginName));   // 删除旧的
    }

    private Mono<Integer> deletePlugin(String target, String name) {
        Query query = Query.query(where("target").is(target)
                .and("name").is(name));
        return template.delete(query, Plugin.class);
    }

}
