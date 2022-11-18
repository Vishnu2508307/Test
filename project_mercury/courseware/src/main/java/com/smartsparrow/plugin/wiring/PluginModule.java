package com.smartsparrow.plugin.wiring;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class PluginModule extends AbstractModule {

    private final static Logger log = MercuryLoggerFactory.getLogger(PluginModule.class);

    @Override
    protected void configure() {

    }

    /**
     * Plugin configuration can be undefined, in this case it should be injected using
     * <code>com.google.inject.Provider&lt;PluginConfig&gt;</code>
     *
     * @param configurationService configuration service
     * @return plugin configuration
     */
    @Provides
    @Nullable
    public PluginConfig getPluginConfig(ConfigurationService configurationService) {
        return configurationService.get(PluginConfig.class, "plugin");
    }

    /**
     * Get the schema configuration
     *
     * @param configurationService configuration service
     * @return schema configuration
     */
    @Provides
    @Nullable
    public SchemaConfig getSchemaConfig(ConfigurationService configurationService) {
        return configurationService.get(SchemaConfig.class, "schemas");
    }

    /**
     * Plugin Log configuration can be undefined, in this case it should be injected using
     * <code>com.google.inject.Provider&lt;PluginLogConfig&gt;</code>
     *
     * @param configurationService configuration service
     * @return pluginLog configuration
     */
    @Singleton
    @Provides
    @Nullable
    public PluginLogConfig getPluginLogConfig(ConfigurationService configurationService) {
        PluginLogConfig pluginLogConfig = configurationService.get(PluginLogConfig.class, "plugin_log");
        log.info("Plugin Log Config is {}", pluginLogConfig);
        return pluginLogConfig;
    }
}
