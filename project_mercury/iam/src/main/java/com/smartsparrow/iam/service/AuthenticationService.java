package com.smartsparrow.iam.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import reactor.core.publisher.Mono;

/**
 * This class is responsible for handling the authentication logic by exchanging valid {@link Credentials} for
 * a {@link WebSession}
 *
 * @param <C> the type of credentials
 * @param <T> the type of webSession
 */
public interface AuthenticationService<C extends Credentials, T extends WebSession<? extends WebToken>> {

    /**
     * Authenticate by exchanging valid {@link Credentials} for a {@link WebSession}
     *
     * @param credentials the credentials to validate and exchange
     * @return a web session
     */
    Mono<T> authenticate(C credentials);

    /**
     * Authenticate by exchanging valid {@link BearerToken} for a {@link WebSession}
     *
     * @param token the bearer token
     * @return a web session
     */
    Mono<T> authenticate(String token);

    /**
     * Authenticate by exchanging valid {@link Credentials} for a {@link WebSession} in a servlet fashion
     *
     * @param credentials the credentials to exchange
     * @param req the incoming request
     * @param res the response
     */
    void authenticate(C credentials, HttpServletRequest req, HttpServletResponse res);
}
