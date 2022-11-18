package com.smartsparrow.export.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class ExportOperationBinding {

    private final ConfigurationBindingOperations binder;

    public ExportOperationBinding(final ConfigurationBindingOperations binder) {
        this.binder = binder;
    }

    public void bind() {
        binder.bind("courseware_export.infra")
                .toConfigType(ExportInfraConfiguration.class);
    }
}
