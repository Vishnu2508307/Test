package com.smartsparrow.sso.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class SSOOperationBinding {

    private final ConfigurationBindingOperations binder;

    public SSOOperationBinding(final ConfigurationBindingOperations binder) {
        this.binder = binder;
    }

    public void bind() {
        binder.bind("sso_oidc.infra")
                .toConfigType(OIDCInfraConfiguration.class);
        binder.bind("sso_mycloud.infra")
                .toConfigType(MyCloudInfraConfiguration.class);
        binder.bind("sso_registrar.infra")
                .toConfigType(RegistrarInfraConfiguration.class);
        binder.bind("sso_ies.infra")
                .toConfigType(IESInfraConfiguration.class);
    }
}
