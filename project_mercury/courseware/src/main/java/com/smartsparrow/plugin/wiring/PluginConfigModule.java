package com.smartsparrow.plugin.wiring;

import com.google.inject.Provides;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.config.wiring.AbstractConfigurationModule;

public class PluginConfigModule extends AbstractConfigurationModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    public void decorate() {
        // config binding
        new PluginOperationsBinding(binder)
                .bind();
    }

    @Provides
    PluginInfraResponse providePluginInfraConfig(ConfigurationService configurationService) {
        return configurationService.load("plugin.infra", PluginInfraResponse.class);
    }

    @Provides
    PluginFeatureResponse providePluginFeatureConfig(ConfigurationService configurationService) {
        return configurationService.load("plugin.feature", PluginFeatureResponse.class);
    }

    @Provides
    PluginLogInfraResponse providePluginLogInfraConfig(ConfigurationService configurationService) {
        return configurationService.load("plugin_log.infra", PluginLogInfraResponse.class);
    }
}
