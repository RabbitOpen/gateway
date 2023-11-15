package rabbit.gateway.runtime.plugin.impl;

import rabbit.gateway.common.bean.AuthenticationSchema;
import rabbit.gateway.common.entity.Plugin;
import rabbit.gateway.runtime.plugin.RequestPlugin;

public class AuthenticationPlugin extends RequestPlugin<AuthenticationSchema> {

    public AuthenticationPlugin(Plugin plugin) {
    }

    @Override
    public Integer getPriority() {
        return 1000;
    }
}
