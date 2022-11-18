package com.smartsparrow.wiring;

import org.slf4j.Logger;

import com.smartsparrow.config.AssetInfraConfiguration;
import com.smartsparrow.config.wiring.ConfigurationBindingOperations;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class AssetOperationsBinding {

    private final static Logger log = MercuryLoggerFactory.getLogger(AssetOperationsBinding.class);
    private final ConfigurationBindingOperations binder;

    public AssetOperationsBinding(ConfigurationBindingOperations configurationBindingOperations) {
        this.binder = configurationBindingOperations;
    }

    public void bind() {
        binder.bind("assets.infra")
                .toConfigType(AssetInfraConfiguration.class);
    }
}
