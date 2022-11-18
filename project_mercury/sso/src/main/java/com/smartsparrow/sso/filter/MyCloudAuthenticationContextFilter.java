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
import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.iam.filter.AuthenticationContextFilter;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.MutableAuthenticationContextProvider;
import com.smartsparrow.sso.service.IdentityProfile;
import com.smartsparrow.sso.service.MyCloudService;
import com.smartsparrow.sso.service.MyCloudWebToken;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Singleton
public class MyCloudAuthenticationContextFilter extends AuthenticationContextFilter {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MyCloudAuthenticationContextFilter.class);

    private final MyCloudService myCloudService;

    @Inject
    public MyCloudAuthenticationContextFilter(final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider,
                                          final AccountService accountService,
                                          final CredentialService credentialService,
                                          final MyCloudService myCloudService) {
        super(mutableAuthenticationContextProvider, accountService, credentialService);
        this.myCloudService = myCloudService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // assume this is safe cast.
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (httpServletRequest.getMethod().equals(HttpMethod.GET) || httpServletRequest.getMethod().equals(HttpMethod.POST)) {
            MutableAuthenticationContext context = mutableAuthenticationContextProvider.get();

            // if the account is not in the context try myCloud authorization
            if (context.getAccount() == null) {
                // Parse all aspects of request looking for the token
                String token = findToken(httpServletRequest);

                try {

                    if (isNullOrEmpty(token)) {
                        throw new NotAuthorizedException("Bearer token not supplied");
                    }

                    // check if the token is still valid before continuing
                    final String pUid = myCloudService.validateToken(token)
                            .doOnError(UnauthorizedFault.class, ex -> {
                                throw new NotAuthorizedException();
                            })
                            .block();

                    if (pUid == null) {
                        throw new NotAuthorizedException("Token informed invalid");
                    }

                    // get profile info
                    final IdentityProfile profile = myCloudService.getProfile(pUid, token)
                            .doOnError(UnauthorizedFault.class, ex -> {
                                throw new NotAuthorizedException();
                            })
                            .block();

                    if (profile == null) {
                        throw new NotAuthorizedException("Failed to find profile");
                    }

                    // find the account
                    final Account account = accountService.findAccountByEmail(profile.getPrimaryEmail())
                            .doOnError(UnauthorizedFault.class, ex -> {
                                throw new NotAuthorizedException();
                            })
                            .block();

                    if (account == null) {
                        throw new NotAuthorizedException("Failed to find account");
                    }

                    // store it into the RequestContext.
                    context.setPearsonToken(token);
                    context.setPearsonUid(pUid);
                    context.setAccount(account);
                    context.setAuthenticationType(AuthenticationType.MYCLOUD);
                    context.setWebSessionToken(credentialService.createWebSessionToken(account.getId(), token)
                            .block());
                    context.setWebToken(new MyCloudWebToken(token));

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
}
