package com.smartsparrow.graphql.wiring;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.servlet.ServletContainer;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.smartsparrow.iam.filter.AuthenticationContextFilter;
import com.smartsparrow.iam.filter.*;
import com.smartsparrow.sso.filter.IESAuthenticationContextFilter;
import com.smartsparrow.sso.filter.MyCloudAuthenticationContextFilter;

public class GraphQLServletModule extends ServletModule {

    public final static String ENDPOINT_URL_PATTERN = "/graphql";

    @Override
    protected void configureServlets() {
        super.configureServlets();
        bind(ServletContainer.class).in(Scopes.SINGLETON);
        Map<String, String> params = new HashMap<>();
        params.put("javax.ws.rs.Application", GuiceResourceConfig.class.getCanonicalName());
        params.put("jersey.config.server.provider.packages", "com.smartsparrow.graphql.resource");

        // This servlet depends on GraphQL services.
        install(new GraphQLModule());

        // require this exposed service to have auth
        filter(ENDPOINT_URL_PATTERN).through(AuthenticationContextFilter.class);
        filter(ENDPOINT_URL_PATTERN).through(IESAuthenticationContextFilter.class);
        filter(ENDPOINT_URL_PATTERN).through(MyCloudAuthenticationContextFilter.class);
        filter(ENDPOINT_URL_PATTERN).through(ValidateAccountContextFilter.class);

        // serve up the graphQL resource.
        serve(ENDPOINT_URL_PATTERN).with(GraphQLServletContainer.class, params);
    }

}
