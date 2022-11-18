package com.smartsparrow.iam.wiring;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.pearson.autobahn.common.domain.Environment;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.BronteAuthenticationService;
import com.smartsparrow.iam.service.BronteCredentials;
import com.smartsparrow.iam.service.BronteWebSession;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.util.Enums;

public class IamModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();

        // bind the AuthenticationContexts to Providers
        bind(MutableAuthenticationContext.class).toProvider(MutableAuthenticationContextProvider.class);
        bind(AuthenticationContext.class).toProvider(AuthenticationContextProvider.class);
        // bind of authentication implementations
        bind(AuthenticationService.class).annotatedWith(BronteAuthentication.class).to(BronteAuthenticationService.class);
        // type literal binding for the abstract implementation
        bind(new TypeLiteral<AuthenticationService<BronteCredentials, BronteWebSession>>(){})
                .annotatedWith(BronteAuthentication.class)
                .to(BronteAuthenticationService.class);
    }

    @Provides
    @Singleton
    public SystemCredentialsConfig provideSystemCredentialsConfig(ConfigurationService configurationService) {
        SystemCredentialsConfig config = configurationService.get(SystemCredentialsConfig.class, "system.credentials");
        if (config != null && config.getPassword() == null) {
            config.setPassword(System.getProperty("system.credentials.password", config.getPassword()));
        }
        return config;
    }

    @Provides
    @Singleton
    public IesSystemToSystemIdentityProvider provideIesSystemToSystemIdentityProvider(SystemCredentialsConfig
                                                                                                  systemCredentialsConfig) {
        return new IesSystemToSystemIdentityProvider(
                systemCredentialsConfig.getUsername(), // username
                systemCredentialsConfig.getPassword(), // password
                Enums.of(Environment.class, systemCredentialsConfig.getEnvironment()) // environment
        );
    }
}
