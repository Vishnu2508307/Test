package com.smartsparrow.sso.service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smartsparrow.iam.lang.AuthenticationNotSupportedFault;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.sso.wiring.LTI11ConsumerAuthentication;

import reactor.core.publisher.Mono;

@SuppressWarnings("rawtypes")
public class AbstractLTIAuthenticationService implements AuthenticationService<LTIConsumerCredentials, LTIWebSession> {

    private final AuthenticationService lti11ConsumerAuthenticationService;

    @Inject
    public AbstractLTIAuthenticationService(@LTI11ConsumerAuthentication AuthenticationService lti11ConsumerAuthenticationService) {
        this.lti11ConsumerAuthenticationService = lti11ConsumerAuthenticationService;
    }

    /**
     * Based on the LTI Credentials type routes to the proper lti consumer authentication implementation
     *
     * @param credentials the credentials to validate and exchange
     * @return the exchanged LTIWebSession
     */
    @SuppressWarnings("unchecked")
    @Override
    public Mono<LTIWebSession> authenticate(LTIConsumerCredentials credentials) {
        if (credentials.getLTIVersion().equals(LTIVersion.VERSION_1_1)) {
            return lti11ConsumerAuthenticationService.authenticate(credentials);
        }

        throw new AuthenticationNotSupportedFault(String.format("LTI authentication not supported for %s", credentials.getLTIVersion()));
    }

    @Override
    public Mono<LTIWebSession> authenticate(final String token) {
        throw new AuthenticationNotSupportedFault("LTI bearer token authentication not supported.");
    }

    @Override
    public void authenticate(LTIConsumerCredentials credentials, HttpServletRequest req, HttpServletResponse res) {
        throw new AuthenticationNotSupportedFault(String.format("LTI servlet authentication not supported for %s", credentials.getLTIVersion()));
    }
}
