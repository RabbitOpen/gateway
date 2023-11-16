package rabbit.gateway.test.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import rabbit.discovery.api.rest.anno.RestClient;
import rabbit.gateway.common.entity.Plugin;
import reactor.core.publisher.Mono;

@RestClient(application = "gateway", contextPath = "/plugin")
public interface PluginApi {

    /**
     * add or update
     * @param plugin
     * @return
     */
    @PostMapping("/replace")
    Mono<Plugin> replace(@RequestBody Plugin plugin);

    /**
     * 删除插件
     * @param target
     * @param pluginName
     * @return
     */
    @PostMapping("/delete/{target}/{pluginName}")
    Mono<Integer> delete(@PathVariable("target") String target, @PathVariable("pluginName") String pluginName);
}
