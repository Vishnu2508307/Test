package com.smartsparrow.iam.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.BearerToken;
import com.smartsparrow.iam.service.CredentialTemporary;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CredentialGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CredentialGateway.class);

    //
    @Inject
    private Session session;

    //
    @Inject
    private CredentialTemporaryMutator credentialTemporaryMutator;
    @Inject
    private CredentialTemporaryMaterializer credentialTemporaryMaterializer;
    @Inject
    private CredentialTemporaryByAccountMutator credentialTemporaryByAccountMutator;
    @Inject
    private CredentialTemporaryByAccountMaterializer credentialTemporaryByAccountMaterializer;
    @Inject
    private BearerTokenMutator bearerTokenMutator;
    @Inject
    private BearerTokenMaterializer bearerTokenMaterializer;
    @Inject
    private WebSessionTokenMutator webSessionTokenMutator;
    @Inject
    private WebSessionTokenMaterializer webSessionTokenMaterializer;
    @Inject
    private CredentialByHashMaterializer credentialByHashMaterializer;
    @Inject
    private CredentialByHashMutator credentialByHashMutator;
    @Inject
    private CredentialsByAccountMutator credentialByAccountMutator;
    @Inject
    private CredentialsByAccountMaterializer credentialByAccountMaterializer;

    /**
     * Create a temporary credential
     *
     * @param temporaryCredential the temporary credential to persist
     * @deprecated use non-blocking {@link CredentialGateway#create(CredentialTemporary)} instead
     */
    @Deprecated
    public void createBlocking(final CredentialTemporary temporaryCredential) {
        Mutators.executeBlocking(session, credentialTemporaryMutator.upsert(temporaryCredential),
                credentialTemporaryByAccountMutator.upsert(temporaryCredential));
    }

    /**
     * Create a temporary credential
     *
     * @param temporaryCredential the temporary credential to persist
     * @return Flux of {@link Void}
     */
    public Flux<Void> create(final CredentialTemporary temporaryCredential) {
        return Mutators.execute(session,
                Flux.just(credentialTemporaryMutator.upsert(temporaryCredential),
                        credentialTemporaryByAccountMutator.upsert(temporaryCredential)))
                .doOnError(throwable -> {
                    log.error(String.format("error while creating temporary credential for account %s",
                            temporaryCredential.getAccountId()), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a temporary credential by authorization code.
     *
     * @param authorizationCode the authorization code
     * @return a {@link Flux} of the temporary credential
     */
    public Flux<CredentialTemporary> findTemporaryByCode(final String authorizationCode) {
        //
        return ResultSets.query(session, credentialTemporaryMaterializer.fetchByAuthorizationCode(authorizationCode))
                //
                .flatMapIterable(row -> row)
                //
                .map(this::mapToTemporary)
                //
                .doOnError(throwable -> {
                    log.error("error while fetching temporary credentials by authorization code", throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the temporary credentials for an account.
     *
     * @param accountId the account id
     * @return a {@link Flux} of temporary credentials
     */
    public Flux<CredentialTemporary> findTemporaryByAccount(final UUID accountId) {
        return ResultSets.query(session, credentialTemporaryByAccountMaterializer.findByAccountId(accountId))
                .flatMapIterable(row -> row)
                //
                .map(this::mapToTemporary)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching all temporary credentials for account %s",
                            accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete the provided temporary credential.
     *
     * @param credentialTemporary
     * @deprecated use non-blocking {@link CredentialGateway#delete(CredentialTemporary)} instead
     */
    public void deleteBlocking(final CredentialTemporary credentialTemporary) {
        Mutators.executeBlocking(session, credentialTemporaryMutator.delete(credentialTemporary),
                credentialTemporaryByAccountMutator.delete(credentialTemporary));
    }

    /**
     * Delete the provided temporary credential.
     *
     * @param credentialTemporary
     * @return Flux of {@link Void}
     */
    public Flux<Void> delete(final CredentialTemporary credentialTemporary) {
        return Mutators.execute(session, Flux.just(credentialTemporaryMutator.delete(credentialTemporary),
                credentialTemporaryByAccountMutator.delete(credentialTemporary)))
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting temporary credential for account %s",
                            credentialTemporary.getAccountId()), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    private CredentialTemporary mapToTemporary(Row row) {
        return new CredentialTemporary()
                //
                .setAuthorizationCode(row.getString("authorization_code"))
                .setType(Enums.of(CredentialTemporary.Type.class, row.getString("type")))
                .setAccountId(row.getUUID("account_id"));
    }

    /* Bearer Token */

    /**
     * Find a bearer token.
     * @param token a bearer token
     * @return BearerToken; returns empty Mono if token is expired
     */
    @Trace(async = true)
    public Mono<BearerToken> findBearerToken(final String token) {
        return ResultSets.query(session, bearerTokenMaterializer.fetchByToken(token))
                .flatMapIterable(row -> row)
                .map(this::mapToBearerToken)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching bearer token with token %s", token), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    private BearerToken mapToBearerToken(Row row) {
        return new BearerToken()
                .setAccountId(row.getUUID("account_id"))
                .setToken(row.getString("key"));
    }

    /* Web Session Token */

    /**
     * Find a web session token
     * @param token token value
     * @return web session token details; empty Mono if token doesn't exist
     */
    @Trace(async = true)
    public Mono<WebSessionToken> findWebSessionToken(final String token) {
        return ResultSets.query(session, webSessionTokenMaterializer.fetchByToken(token))
                .flatMapIterable(row -> row)
                .map(this::mapToWebSessionToken)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching a web session token with token %s", token), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save web session token and save token as bearer token with ttl
     * @param token a token
     * @param ttl time-to-live in seconds
     * @return Flux of {@link Void}
     */
    @Trace(async = true)
    public Flux<Void> save(final WebSessionToken token, final int ttl) {
        return Mutators.execute(session, Flux.just(webSessionTokenMutator.upsert(token),
                bearerTokenMutator.upsert(token, ttl)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error("error while saving web session token", throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Invalidate web session token: delete bearer token and update web session token
     * @param token a token
     * @return Flux of {@link Void}
     */
    public Flux<Void> invalidate(final WebSessionToken token) {
        return Mutators.execute(session, Flux.just(webSessionTokenMutator.upsert(token),
                bearerTokenMutator.delete(token)))
                .doOnError(throwable -> {
                    log.error("error while invalidating web session token", throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    private WebSessionToken mapToWebSessionToken(Row row) {
        return new WebSessionToken()
                .setAccountId(row.getUUID("account_id"))
                .setToken(row.getString("key"))
                .setCreatedTs(row.getLong("created_ts"))
                .setValidUntilTs(row.getLong("expired_ts"))
                .setAuthoritySubscriptionId(row.getUUID("authority_subscription_id"))
                .setAuthorityRelyingPartyId(row.getUUID("authority_relying_party_id"));
    }

    /**
     * Persist credential type info
     *
     * @param credentialsType, the credential type object
     */
    @Trace(async = true)
    public Mono<Void> persistCredentialType(final CredentialsType credentialsType) {
        Flux<? extends Statement> iter = Mutators.upsert(credentialByHashMutator,
                                                         credentialsType);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving credential type by hash",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("accountId", credentialsType.getAccountId());
                                                             put("authenticationType", credentialsType.getAuthenticationType());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch credential type by email hash
     *
     * @param hash, the email hash
     * @return mono of credential type by hash
     */
    @Trace(async = true)
    public Flux<CredentialsType> fetchCredentialTypeByHash(final String hash) {
        return ResultSets.query(session, credentialByHashMaterializer.fetchCredential(hash))
                .flatMapIterable(row -> row)
                .map(credentialByHashMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching credential by hash %s"));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist credentials type info by account
     *
     * @param credentialsType, the credential type object
     */
    @Trace(async = true)
    public Mono<Void> persistCredentialsTypeByAccount(final CredentialsType credentialsType) {
        Flux<? extends Statement> iter = Mutators.upsert(credentialByAccountMutator,
                                                         credentialsType);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving credential type by account",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("accountId", credentialsType.getAccountId());
                                                             put("authenticationType", credentialsType.getAuthenticationType());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch credentials type by account
     *
     * @param accountId, the account id
     * @return mono of credentials type by account
     */
    @Trace(async = true)
    public Flux<CredentialsType> fetchCredentialsTypeByAccount(final UUID accountId) {
        return ResultSets.query(session, credentialByAccountMaterializer.fetchCredentials(accountId))
                .flatMapIterable(row -> row)
                .map(credentialByAccountMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching credential by account %s"));
                    throw Exceptions.propagate(throwable);
                });
    }

}
