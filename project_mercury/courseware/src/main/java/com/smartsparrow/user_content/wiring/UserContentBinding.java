package com.smartsparrow.user_content.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class UserContentBinding {

    private final ConfigurationBindingOperations binder;

    public UserContentBinding(final ConfigurationBindingOperations binder) {
        this.binder = binder;
    }

    public void bind() {
        binder.bind("user_content.infra")
                .toConfigType(UserContentInfraConfiguration.class);

    }
}
