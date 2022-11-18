package com.smartsparrow.iam.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class IAMOperationsBinding {

    private final ConfigurationBindingOperations binder;

    public IAMOperationsBinding(ConfigurationBindingOperations configurationBindingOperations) {
        this.binder = configurationBindingOperations;
    }

    public void bind() {
        binder.bind("system_credentials.infra")
                .toConfigType(SystemCredentialsInfraConfiguration.class);
    }
}
