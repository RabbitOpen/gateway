package rabbit.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rabbit.gateway.admin.service.PluginService;
import rabbit.gateway.common.entity.Plugin;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/plugin")
public class PluginController {

    @Autowired
    protected PluginService pluginService;

    /**
     * add or update
     * @param plugin
     * @return
     */
    @PostMapping("/replace")
    public Mono<Plugin> replace(@RequestBody Plugin plugin) {
        return pluginService.replacePlugin(plugin);
    }

    /**
     * 删除插件
     * @param target
     * @param pluginName
     * @return
     */
    @PostMapping("/delete/{target}/{pluginName}")
    public Mono<Integer> delete(@PathVariable("target") String target,
                               @PathVariable("pluginName") String pluginName) {
        return pluginService.removePlugin(pluginName, target);
    }
}
