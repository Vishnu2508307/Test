package com.smartsparrow.iam.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.lang.AuthenticationNotSupportedFault;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.util.Passwords;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class BronteAuthenticationService implements AuthenticationService<BronteCredentials, BronteWebSession> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(BronteAuthenticationService.class);

    final private AccountService accountService;
    final private CredentialService credentialService;

    @Inject
    public BronteAuthenticationService(final AccountService accountService,
                                       final CredentialService credentialService) {
        this.accountService = accountService;
        this.credentialService = credentialService;
    }

    @Trace(async = true)
    @Override
    public Mono<BronteWebSession> authenticate(final BronteCredentials credentials) {
        boolean hasToken = credentials.getBearerToken() != null;
        boolean hasEmailAndPassword = credentials.getEmail() != null && credentials.getPassword() != null;

        affirmArgument(hasToken || hasEmailAndPassword, "either bearerToken or email and password are required");

        if (!Strings.isNullOrEmpty(credentials.getBearerToken())) {
            return authenticate(credentials.getBearerToken());
        } else {
            return accountService.findAccountByEmail(credentials.getEmail())
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(log.reactiveDebugSignal("Account info of the user", account -> new HashMap<String, Object>() {
                        {
                            put("status", account.getStatus());
                            put("accountId", account.getId());
                            put("subscriptionId", account.getSubscriptionId());
                            put("region", account.getIamRegion());
                        }
                    }))
                    .map(account -> {
                        if (!Passwords.verify(credentials.getPassword(), account.getPasswordHash())) {
                            throw new UnauthorizedFault("invalid credentials");
                        }
                        return account;
                    })
                    .flatMap(account -> credentialService.createWebSessionToken(account.getId())
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            //initialise BronteWebSession
                            .map(webSessionToken -> new BronteWebSession(account)
                                    .setBronteWebToken(new BronteWebToken(webSessionToken.getToken())
                                                               .setCreatedTs(webSessionToken.getCreatedTs())
                                                               .setValidUntilTs(webSessionToken.getValidUntilTs())
                                                               .setAuthoritySubscriptionId(webSessionToken.getAuthoritySubscriptionId())
                                                               .setAuthorityRelyingPartyId(webSessionToken.getAuthorityRelyingPartyId()))));

        }
    }

    @Trace(async = true)
    @Override
    public Mono<BronteWebSession> authenticate(final String token) {
        affirmArgumentNotNullOrEmpty(token, "token is required");
        return accountService.findAccountByToken(token)
                .onErrorResume(NotFoundFault.class, notFoundFault -> Mono.empty())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveDebugSignal("Account info of the user", account -> new HashMap<String, Object>() {
                    {
                        put("status", account.getStatus());
                        put("accountId", account.getId());
                        put("subscriptionId", account.getSubscriptionId());
                        put("region", account.getIamRegion());
                    }
                }))
                .flatMap(account -> credentialService.findWebSessionTokenReactive(token)
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        //initialise BronteWebSession
                        .map(webSessionToken -> new BronteWebSession(account)
                                .setBronteWebToken(new BronteWebToken(webSessionToken.getToken())
                                                           .setCreatedTs(webSessionToken.getCreatedTs())
                                                           .setValidUntilTs(webSessionToken.getValidUntilTs())
                                                           .setAuthoritySubscriptionId(webSessionToken.getAuthoritySubscriptionId())
                                                           .setAuthorityRelyingPartyId(webSessionToken.getAuthorityRelyingPartyId()))));
    }

    @Override
    public void authenticate(final BronteCredentials credentials,
                             final HttpServletRequest req,
                             final HttpServletResponse res) {
        throw new AuthenticationNotSupportedFault("BRONTE servlet authentication not supported");
    }
}
