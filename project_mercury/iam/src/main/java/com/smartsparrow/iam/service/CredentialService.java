package com.smartsparrow.iam.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static java.time.Duration.ofSeconds;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.InvalidJWTException;
import com.smartsparrow.iam.data.CredentialGateway;
import com.smartsparrow.iam.data.CredentialsType;
import com.smartsparrow.iam.lang.WebTokenNotFoundFault;
import com.smartsparrow.util.Emails;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.JWT;
import com.smartsparrow.util.Tokens;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service to manage user credentials.
 */
@Singleton
public class CredentialService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CredentialService.class);

    private static final int WEB_SESSION_TOKEN_DEFAULT_TTL = (int) TimeUnit.DAYS.toSeconds(5);

    //
    @Inject
    private AccountService accountService;

    @Inject
    private CredentialGateway credentialGateway;

    @Inject
    public CredentialService() {
    }

    /**
     * Create a temporary credential for the user.
     *
     * @param accountId the account id
     * @param type the type of temporary credential
     * @return a temporary credential for the given user.
     */
    public CredentialTemporary createTemporary(UUID accountId, CredentialTemporary.Type type) {
        Preconditions.checkArgument(accountId != null, "missing account id");
        Preconditions.checkArgument(type != null, "missing credential type");

        final Account account = accountService.verifyValidAccount(accountId);

        // generate a authorization code.
        final String authorizationCode = UUID.randomUUID().toString().replaceAll("-", "");

        CredentialTemporary temporaryCredential = new CredentialTemporary() //
                .setAccountId(account.getId()) //
                .setType(type) //
                .setAuthorizationCode(authorizationCode);

        try {
            credentialGateway.createBlocking(temporaryCredential);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }
        return temporaryCredential;
    }

    /**
     * Verify and use/expire this one-use temporary credential.
     *
     * @param authorizationCode the authorization code to use
     * @return the account associated to the temporary credential.
     * @throws IllegalArgumentException if the authorization code is invalid or expired.
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "FindBugs don't see that Precondition.checkArgument prevents NPE")
    public Account verifyTemporaryCredential(String authorizationCode) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(authorizationCode), "missing authorization code");

        // find the supplied credential.
        CredentialTemporary cred = credentialGateway.findTemporaryByCode(authorizationCode).blockFirst();

        // error if not found.
        // FIXME: replace IllegalArgumentException with something more "business" like
        Preconditions.checkArgument(cred != null, "invalid authorization code");

        // fetch the associated account.
        Account account = accountService.verifyValidAccount(cred.getAccountId());

        // delete all credentials of this type (i.e. all password reset codes)
        credentialGateway.findTemporaryByAccount(account.getId()) //
                .filter(t -> t.getType().equals(cred.getType())) // only operate on the provided type.
                .toIterable() //
                .forEach(t -> credentialGateway.deleteBlocking(t));

        return account;
    }

    /**
     * Generate and save web session token for a given account in a reactive fashion, with the default web session TTL
     *
     * @param accountId the account id to generate the web session token for
     * @return a mono of the generated web session token
     */
    @Trace(async = true)
    public Mono<WebSessionToken> createWebSessionToken(@Nonnull final UUID accountId) {
        //
        return createWebSessionToken(accountId, ofSeconds(WEB_SESSION_TOKEN_DEFAULT_TTL), null, null)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Generate and save web session token for a given account in a reactive fashion, with the ies jwt expiration time
     *
     * @param accountId the account id to generate the web session token for
     * @param token the jwt to extract expiration time from
     * @return a mono of the generated web session token
     */
    @Trace(async = true)
    public Mono<WebSessionToken> createWebSessionToken(@Nonnull final UUID accountId, @Nonnull final String token) {
        try {
            // extract expiration time from jwt
            return createWebSessionToken(accountId, ofSeconds(JWT.getSecondsExp(token)),
                    null, null)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } catch (InvalidJWTException ex) {
            log.error("error occurred while extracting expiration time from jwt", ex);
            // create the token with the default ttl
            return createWebSessionToken(accountId, ofSeconds(WEB_SESSION_TOKEN_DEFAULT_TTL),
                    null, null)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        }
    }

    /**
     * Generate and save web sessiont oken for a given account, using the default websession TTL.
     *
     * @param accountId the account id
     * @param authoritySubscriptionId the subscription which performed the authentication
     * @param authorityRelyingPartyId the relying party id which performed the authentication
     * @return a mono of the generated web session token
     */
    public Mono<WebSessionToken> createWebSessionToken(@Nonnull final UUID accountId,
                                                       @Nullable final UUID authoritySubscriptionId,
                                                       @Nullable final UUID authorityRelyingPartyId) {
        //
        return createWebSessionToken(accountId, ofSeconds(WEB_SESSION_TOKEN_DEFAULT_TTL),
                                     authoritySubscriptionId, authorityRelyingPartyId);
    }

    /**
     * Generate and save web session token for a given account in a reactive fashion
     *
     * @param accountId the account id to generate the web session token for
     * @param duration the duration for which this token should live for
     * @return a mono of the generated web session token
     */
    @Trace(async = true)
    public Mono<WebSessionToken> createWebSessionToken(@Nonnull final UUID accountId,
                                                       @Nonnull final TemporalAmount duration,
                                                       @Nullable final UUID authoritySubscriptionId,
                                                       @Nullable final UUID authorityRelyingPartyId) {

        final Instant now = Instant.now();

        final WebSessionToken token = new WebSessionToken()
                .setToken(Tokens.generate())
                .setAccountId(accountId)
                .setCreatedTs(now.toEpochMilli())
                .setValidUntilTs(now.plus(duration).toEpochMilli())
                .setAuthoritySubscriptionId(authoritySubscriptionId)
                .setAuthorityRelyingPartyId(authorityRelyingPartyId);

        return credentialGateway.save(token, (int) duration.get(ChronoUnit.SECONDS))
                .singleOrEmpty()
                .thenReturn(token)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find web session token {@link WebSessionToken} by token string
     *
     * @param token a token string
     * @return WebSessionToken if exists, otherwise null
     */
    public WebSessionToken findWebSessionToken(String token) {
        checkNotNull(token);

        return credentialGateway.findWebSessionToken(token).block();
    }

    /**
     * Find web session token by token
     *
     * @param token the token to find the web session for
     * @return a mono of web session token
     * @throws WebTokenNotFoundFault when the token is not found
     */
    @Trace(async = true)
    public Mono<WebSessionToken> findWebSessionTokenReactive(@Nonnull String token) {
        return credentialGateway.findWebSessionToken(token)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new WebTokenNotFoundFault("web token not found");
                });
    }

    /**
     * Find bearer token {@link BearerToken} by token string
     *
     * @param token a token string
     * @return BearerToken if exists, otherwise null
     */
    public BearerToken findBearerToken(String token) {
        checkNotNull(token);
        return credentialGateway.findBearerToken(token).block();
    }

    /**
     * Revoke a Token across all possible auth mechanisms.
     *
     * @param token the token to revoke
     */
    public void invalidate(final String token) {
        affirmArgumentNotNullOrEmpty(token, "missing required token");

        // attempt to invalidate any WebSessionToken
        WebSessionToken webSessionToken = findWebSessionToken(token);
        if (webSessionToken != null) {
            invalidate(webSessionToken);
        }
    }

    /**
     * Invalidate bearer token.
     *
     * @param token a token
     */
    public void invalidate(BearerToken token) {
        checkNotNull(token);

        Instant now = Instant.now();

        if (token instanceof WebSessionToken) {
            WebSessionToken webToken = ((WebSessionToken) token).setValidUntilTs(now.toEpochMilli());
            credentialGateway.invalidate(webToken).blockLast();
        } else {
            throw new UnsupportedOperationException("Only WebSessionToken is currently supported");
        }
    }

    /**
     * Fetch credentials type by email hash
     * @param email the email id
     */
    @Trace(async = true)
    public Flux<CredentialsType> fetchCredentialTypeByHash(String email){
        affirmArgument(email != null, "email is required");
        affirmArgument(Emails.isEmailValid(email), "Invalid email address");

        return Flux.just(email)
                .map(Emails::normalize)
                .map(Hashing::email)
                .flatMap(credentialGateway::fetchCredentialTypeByHash)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch credentials type by account
     * @param accountId the accountId
     */
    @Trace(async = true)
    public Flux<CredentialsType> fetchCredentialTypeByAccount(UUID accountId){
        affirmArgument(accountId != null, "accountId is required");

        return credentialGateway.fetchCredentialsTypeByAccount(accountId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
