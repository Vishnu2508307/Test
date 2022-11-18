package com.smartsparrow.publication.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class PublicationOperationsBinding {

    private final ConfigurationBindingOperations binder;

    public PublicationOperationsBinding(final ConfigurationBindingOperations binder) {
        this.binder = binder;
    }

    public void bind() {
        binder.bind("oculus.infra")
                .toConfigType(PublicationInfraConfiguration.class);

    }
}
