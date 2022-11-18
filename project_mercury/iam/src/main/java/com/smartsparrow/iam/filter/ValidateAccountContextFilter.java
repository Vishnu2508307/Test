package com.smartsparrow.iam.filter;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.MutableAuthenticationContextProvider;

@Singleton
public class ValidateAccountContextFilter extends AuthenticationContextFilter {

    private static final Logger log = LoggerFactory.getLogger(ValidateAccountContextFilter.class);

    @Inject
    public ValidateAccountContextFilter(final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider,
                                          final AccountService accountService,
                                          final CredentialService credentialService) {
        super(mutableAuthenticationContextProvider, accountService, credentialService);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // assume this is safe cast.
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (httpServletRequest.getMethod().equals(HttpMethod.GET) || httpServletRequest.getMethod().equals(HttpMethod.POST)) {

            // Parse all aspects of request looking for the token
            String token = findToken(httpServletRequest);

            try {

                if (isNullOrEmpty(token)) {
                    throw new NotAuthorizedException("Bearer token not supplied");
                }

                // check for an account
                MutableAuthenticationContext context = mutableAuthenticationContextProvider.get();
                if (context.getAccount() == null) {
                    throw new NotAuthorizedException();
                }

                httpServletRequest.setAttribute("mutableAuthenticationContext", context);

                chain.doFilter(httpServletRequest, response);

            } catch (NotAuthorizedException e) {
                handleAuthFailure(response, token, e);
            } finally {
                MutableAuthenticationContextProvider.cleanup();
            }
        } else {
            chain.doFilter(httpServletRequest, response);
        }
    }
}
