package com.smartsparrow.ingestion.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class IngestionOperationsBinding {

    private final ConfigurationBindingOperations binder;

    public IngestionOperationsBinding(ConfigurationBindingOperations configurationBindingOperations) {
        this.binder = configurationBindingOperations;
    }

    public void bind() {
        binder.bind("self_ingestion.infra")
                .toConfigType(IngestionInfraConfiguration.class);
    }
}
