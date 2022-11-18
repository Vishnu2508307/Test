package com.smartsparrow.sso.service;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.InvalidAzureException;
import com.smartsparrow.iam.lang.AuthenticationNotSupportedFault;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.util.Azure;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class MyCloudAuthenticationService implements AuthenticationService<MyCloudCredentials, MyCloudWebSession> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MyCloudAuthenticationService.class);

    private final MyCloudService myCloudService;
    private final CredentialService credentialService;
    private final AccountService accountService;

    @Inject
    public MyCloudAuthenticationService(final MyCloudService myCloudService,
                                        final CredentialService credentialService,
                                        final AccountService accountService) {
        this.myCloudService = myCloudService;
        this.credentialService = credentialService;
        this.accountService = accountService;
    }

    @Trace(async = true)
    @Override
    public Mono<MyCloudWebSession> authenticate(MyCloudCredentials credentials) {
        affirmArgumentNotNullOrEmpty(credentials.getToken(), "token is required");

        // validate the provided token
        return myCloudService.validateToken(credentials.getToken())
                .flatMap(pearsonUid ->
                                 // grab myCloud user's profile
                                 myCloudService.getProfile(pearsonUid, credentials.getToken())
                                         // check if myCloud user already has a Bronte account (using myCloud user's email)
                                         .flatMap(identityProfile -> accountService.findAccountByEmail(identityProfile.getPrimaryEmail())
                                                 .doOnEach(ReactiveTransaction.linkOnNext()))
                                         // create a web session token
                                         .flatMap(account -> credentialService.createWebSessionToken(account.getId(),
                                                                                                     credentials.getToken())
                                                 // initialise an MyCloudWebSession
                                                 .map(webSessionToken -> new MyCloudWebSession(account)
                                                         .setMyCloudWebToken(new MyCloudWebToken(credentials.getToken())
                                                                                     .setPearsonUid(pearsonUid)
                                                                                     .setValidUntilTs(webSessionToken.getValidUntilTs()))))
                                         .doOnEach(log.reactiveErrorThrowable("My cloud authentication error",
                                                                              throwable -> new HashMap<String, Object>() {
                                                                                  {
                                                                                      put("credentials", credentials);
                                                                                  }
                                                                              })))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Override
    public Mono<MyCloudWebSession> authenticate(final String token) {
        affirmArgumentNotNullOrEmpty(token, "token is required");
        try {
            // check if this is an Azure session token
            Azure.validateFormatting(token);
            // validate the provided token
            return authenticate(new MyCloudCredentials().setToken(token));
        } catch (InvalidAzureException e) {
            // if that was not successful then return no credentials
            return Mono.empty();
        }
    }

    @Override
    public void authenticate(final MyCloudCredentials credentials,
                             final HttpServletRequest req,
                             final HttpServletResponse res) {
        throw new AuthenticationNotSupportedFault("Mycloud servlet authentication not supported");
    }
}
