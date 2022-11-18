package com.smartsparrow.plugin.wiring;

import org.slf4j.Logger;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class PluginOperationsBinding {

    private final ConfigurationBindingOperations binder;

    public PluginOperationsBinding(ConfigurationBindingOperations configurationBindingOperations) {
        this.binder = configurationBindingOperations;
    }

    public void bind() {
        binder.bind("schemas.infra")
                .toConfigType(SchemaInfraConfiguration.class);
        binder.bind("plugin.infra")
                .toConfigType(PluginInfraConfiguration.class);
        binder.bind("plugin.feature")
                .toConfigType(PluginFeatureConfiguration.class);
        binder.bind("plugin_log.infra")
                .toConfigType(PluginLogInfraConfiguration.class);
    }
}
