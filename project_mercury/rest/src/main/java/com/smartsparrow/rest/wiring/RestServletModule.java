package com.smartsparrow.rest.wiring;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.servlet.ServletContainer;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.smartsparrow.ext_http.wiring.ExternalHttpModule;
import com.smartsparrow.iam.filter.AuthenticationContextFilter;
import com.smartsparrow.iam.filter.ValidateAccountContextFilter;
import com.smartsparrow.sso.filter.IESAuthenticationContextFilter;
import com.smartsparrow.sso.filter.MyCloudAuthenticationContextFilter;
import com.smartsparrow.sso.wiring.SSOModule;

/**
 * Configures servlets and bindings required for REST module.
 */
public class RestServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        super.configureServlets();

        //
        bind(ServletContainer.class).in(Scopes.SINGLETON);

        configure_r();
        configure_sns();
        configure_sso();
        configure_to();
    }

    private void configure_r() {
        Map<String, String> params = new HashMap<>();
        params.put("javax.ws.rs.Application", GuiceResourceConfig.class.getCanonicalName());
        params.put("jersey.config.server.provider.packages", "com.smartsparrow.rest.resource.r");

        // Explicit mapping of endpoints to be filtered by AuthenticationContextFilter
        // TODO: figure out a Jersey2 equivalent of PackagesResourceConfig so it can be done by annotations like in AELP
        filter("/r/asset/*").through(AuthenticationContextFilter.class);
        filter("/r/asset/*").through(IESAuthenticationContextFilter.class);
        filter("/r/asset/*").through(MyCloudAuthenticationContextFilter.class);
        filter("/r/asset/*").through(ValidateAccountContextFilter.class);

        filter("/r/mastering/*").through(AuthenticationContextFilter.class);
        filter("/r/mastering/*").through(MyCloudAuthenticationContextFilter.class);

        // serve up the resources
        serve("/r/*").with(RestServletContainer.class, params);
    }

    private void configure_sns() {
        //
        install(new ExternalHttpModule());

        //
        Map<String, String> params = new HashMap<>();
        params.put("javax.ws.rs.Application", GuiceResourceConfig.class.getCanonicalName());
        params.put("jersey.config.server.provider.packages", "com.smartsparrow.rest.resource.sns");

        serve("/sns/*").with(SNSServletContainer.class, params);
    }

    private void configure_sso() {
        //
        install(new SSOModule());

        //
        Map<String, String> params = new HashMap<>();
        params.put("javax.ws.rs.Application", GuiceResourceConfig.class.getCanonicalName());
        params.put("jersey.config.server.provider.packages", "com.smartsparrow.rest.resource.sso");

        serve("/sso/*").with(SSOServletContainer.class, params);
    }

    private void configure_to() {
        Map<String, String> params = new HashMap<>();
        params.put("javax.ws.rs.Application", GuiceResourceConfig.class.getCanonicalName());
        params.put("jersey.config.server.provider.packages", "com.smartsparrow.rest.resource.to");

        serve("/to/*").with(ToServletContainer.class, params);
    }
}
