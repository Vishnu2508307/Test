package com.smartsparrow.ext_http.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class ExternalHttpOperationBinding {

    private final ConfigurationBindingOperations binder;

    public ExternalHttpOperationBinding(final ConfigurationBindingOperations binder) {
        this.binder = binder;
    }

    public void bind() {
        binder.bind("ext_http.infra")
                .toConfigType(ExternalHttpInfraConfiguration.class);
    }
}
