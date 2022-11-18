package com.smartsparrow.sso.wiring;

import javax.annotation.Nullable;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.sso.route.SSORoutes;
import com.smartsparrow.sso.service.AbstractLTIAuthenticationService;
import com.smartsparrow.sso.service.IESAuthenticationService;
import com.smartsparrow.sso.service.IESCredentials;
import com.smartsparrow.sso.service.IESWebSession;
import com.smartsparrow.sso.service.LTI11AuthenticationService;
import com.smartsparrow.sso.service.LTIConsumerCredentials;
import com.smartsparrow.sso.service.LTIWebSession;
import com.smartsparrow.sso.service.MyCloudAuthenticationService;
import com.smartsparrow.sso.service.MyCloudCredentials;
import com.smartsparrow.sso.service.MyCloudWebSession;

public class SSOModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SSORoutes.class);

        //
        // bind of authentication implementations
        //
        // raw types
        // LTI
        bind(AuthenticationService.class).annotatedWith(LTI11ConsumerAuthentication.class).to(LTI11AuthenticationService.class);
        bind(AuthenticationService.class).annotatedWith(LTIConsumerAuthentication.class).to(AbstractLTIAuthenticationService.class);
        // IES
        bind(AuthenticationService.class).annotatedWith(IESAuthentication.class).to(IESAuthenticationService.class);
        //MyCloud
        bind(AuthenticationService.class).annotatedWith(MyCloudAuthentication.class).to(MyCloudAuthenticationService.class);
        // type literal binding for the abstract implementation
        // LTI
        bind(new TypeLiteral<AuthenticationService<LTIConsumerCredentials, LTIWebSession>>(){})
                .annotatedWith(LTIConsumerAuthentication.class)
                .to(AbstractLTIAuthenticationService.class);
        // IES
        bind(new TypeLiteral<AuthenticationService<IESCredentials, IESWebSession>>(){})
                .annotatedWith(IESAuthentication.class)
                .to(IESAuthenticationService.class);

        // MyCloud
        bind(new TypeLiteral<AuthenticationService<MyCloudCredentials, MyCloudWebSession>>(){})
                .annotatedWith(MyCloudAuthentication.class)
                .to(MyCloudAuthenticationService.class);

    }

    @Provides
    @Nullable
    public OpenIDConnectConfig getOpenIdConnectConfig(ConfigurationService configurationService) {
        return configurationService.get(OpenIDConnectConfig.class, "sso.oidc");
    }

    @Provides
    @Nullable
    public IESConfig getIESConfig(ConfigurationService configurationService) {
        return configurationService.get(IESConfig.class, "sso.ies");
    }

    @Provides
    @Nullable
    public MyCloudConfig getMyCloudConfig(ConfigurationService configurationService) {
        return configurationService.get(MyCloudConfig.class, "sso.mycloud");
    }

    @Provides
    @Nullable
    public RegistrarConfig getRegistrarConfig(ConfigurationService configurationService) {
        return configurationService.get(RegistrarConfig.class, "sso.registrar");
    }
}
