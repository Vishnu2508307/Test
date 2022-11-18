package com.smartsparrow.sso.service;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smartsparrow.exception.InvalidJWTException;
import com.smartsparrow.iam.lang.AuthenticationNotSupportedFault;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.util.JWT;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class IESAuthenticationService implements AuthenticationService<IESCredentials, IESWebSession> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IESAuthenticationService.class);

    private final IESService iesService;
    private final CredentialService credentialService;

    @Inject
    public IESAuthenticationService(final IESService iesService,
                                    final CredentialService credentialService) {
        this.iesService = iesService;
        this.credentialService = credentialService;
    }

    @Override
    public Mono<IESWebSession> authenticate(IESCredentials credentials) {
        affirmArgumentNotNullOrEmpty(credentials.getToken(), "token is required");
        affirmArgumentNotNullOrEmpty(credentials.getPearsonUid(), "token is pearsonUid");
        // validate the token
        return iesService.validateToken(credentials.getToken())
                .flatMap(isValid -> {
                    // find the account by pearson uid
                    return iesService.findAccount(credentials.getPearsonUid())
                            // if the account was not found provision a new one
                            .switchIfEmpty(Mono.defer(() -> iesService.provisionAccount(credentials.getPearsonUid())))
                            // create a web session token
                            .flatMap(account -> credentialService.createWebSessionToken(account.getId(), credentials.getToken())
                                    // initialise an IESWebSession
                                    .map(webSessionToken -> new IESWebSession(account)
                                            .setIesWebToken(new IESWebToken(credentials.getToken())
                                                    .setPearsonUid(credentials.getPearsonUid())
                                                    .setValidUntilTs(webSessionToken.getValidUntilTs()))))
                            .doOnEach(log.reactiveErrorThrowable("IES authentication error", throwable -> new HashMap<String, Object>() {
                                {
                                    put("credentials", credentials);
                                }
                            }));
                });
    }

    @Override
    public Mono<IESWebSession> authenticate(final String token) {
        affirmArgumentNotNullOrEmpty(token, "token is required");
        try {
            // try extracting the pearsonUid from the token
            final String pearsonUid = JWT.getUserId(token);
            // validate the provided token
            return authenticate(new IESCredentials().setPearsonUid(pearsonUid).setToken(token));
        } catch (InvalidJWTException e) {
            // if that was not successful then return no credentials
            return Mono.empty();
        }
    }

    @Override
    public void authenticate(IESCredentials credentials, HttpServletRequest req, HttpServletResponse res) {
        throw new AuthenticationNotSupportedFault("IES servlet authentication not supported");
    }
}
