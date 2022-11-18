package com.smartsparrow.sso.filter;

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

import com.google.inject.Provider;
import com.smartsparrow.exception.InvalidJWTException;
import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.iam.filter.AuthenticationContextFilter;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.MutableAuthenticationContextProvider;
import com.smartsparrow.sso.service.IESService;
import com.smartsparrow.sso.service.IESWebToken;
import com.smartsparrow.util.JWT;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class IESAuthenticationContextFilter extends AuthenticationContextFilter {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IESAuthenticationContextFilter.class);

    private final IESService iesService;

    @Inject
    public IESAuthenticationContextFilter(final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider,
                                          final AccountService accountService,
                                          final CredentialService credentialService,
                                          final IESService iesService) {
        super(mutableAuthenticationContextProvider, accountService, credentialService);
        this.iesService = iesService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // assume this is safe cast.
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (httpServletRequest.getMethod().equals(HttpMethod.GET) || httpServletRequest.getMethod().equals(HttpMethod.POST)) {
            MutableAuthenticationContext context = mutableAuthenticationContextProvider.get();

            // if the account is not in the context try IES authorization
            if (context.getAccount() == null) {
                // Parse all aspects of request looking for the token
                String token = findToken(httpServletRequest);

                try {

                    if (isNullOrEmpty(token)) {
                        throw new NotAuthorizedException("Bearer token not supplied");
                    }

                    // we are expecting this bearer token to be the ies JWT, so get the userId
                    final String pUid = extractPIUserId(token);

                    if (pUid == null) {
                        throw new NotAuthorizedException("Token informed invalid");
                    }

                    // check if the token is still valid before continuing
                    iesService.validateToken(token)
                            .doOnError(UnauthorizedFault.class, ex -> {
                                throw new NotAuthorizedException();
                            })
                            .block();

                    // find the account associated to this userId
                    final Account account = iesService.findAccount(pUid)
                            // provision the account if not there already
                            .switchIfEmpty(Mono.defer(() -> iesService.provisionAccount(pUid)))
                            .block();

                    if (account == null) {
                        throw new NotAuthorizedException("Failed to provision account");
                    }

                    // store it into the RequestContext.
                    context.setPearsonToken(token);
                    context.setPearsonUid(pUid);
                    context.setAccount(account);
                    context.setAuthenticationType(AuthenticationType.IES);
                    context.setWebSessionToken(credentialService.createWebSessionToken(account.getId(), token)
                                                       .block());
                    context.setWebToken(new IESWebToken(token));

                    chain.doFilter(httpServletRequest, response);

                } catch (NotAuthorizedException e) {
                    chain.doFilter(httpServletRequest, response);
                } finally {
                    MutableAuthenticationContextProvider.cleanup();
                }
            } else {
                chain.doFilter(httpServletRequest, response);
            }

        } else {
            chain.doFilter(httpServletRequest, response);
        }
    }

    /**
     * Extract the userId from the ies JWT
     *
     * @param piToken the jwt to extract the userId from
     * @return the userId or null when not found
     */
    protected String extractPIUserId(String piToken) {
        try {
            return JWT.getUserId(piToken);
        } catch (InvalidJWTException e) {
            log.debug("invalid jwt", e);
            // return null when jwt is invalid
            return null;
        }
    }

}
