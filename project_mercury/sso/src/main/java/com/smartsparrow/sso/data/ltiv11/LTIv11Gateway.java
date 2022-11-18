package com.smartsparrow.sso.data.ltiv11;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.sso.service.LTIConsumerKey;
import com.smartsparrow.sso.service.LTILaunchRequestEntry;
import com.smartsparrow.sso.service.LTILaunchRequestLogEvent;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Singleton
public class LTIv11Gateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LTIv11Gateway.class);

    private final Session session;

    private final LTICredentialByIdMutator ltiCredentialByIdMutator;
    private final LTICredentialByKeyMutator ltiCredentialByKeyMutator;
    private final LTICredentialByKeyMaterializer ltiCredentialByKeyMaterializer;
    private final LTILaunchRequestEntryMutator ltiLaunchRequestEntryMutator;
    private final LTILaunchRequestStaticFieldsMutator ltiLaunchRequestStaticFieldsMutator;
    private final LTILaunchRequestLogEventMutator ltiLaunchRequestLogEventMutator;
    private final AccountByLTILaunchRequestMutator accountByLTILaunchRequestMutator;
    private final LTILaunchRequestByAccountMutator ltiLaunchRequestByAccountMutator;
    private final LTI11ConsumerConfigurationMutator lti11ConsumerConfigurationMutator;
    private final LTI11ConsumerConfigurationByWorkspaceMutator lti11ConsumerConfigurationByWorkspaceMutator;
    private final LTI11ConsumerConfigurationMaterializer lti11ConsumerConfigurationMaterializer;
    private final LTI11ConsumerConfigurationByWorkspaceMaterializer lti11ConsumerConfigurationByWorkspaceMaterializer;
    private final LTI11ConsumerCredentialsMutator lti11ConsumerCredentialsMutator;
    private final LTI11ConsumerCredentialsMaterializer lti11ConsumerCredentialsMaterializer;
    private final LTI11ConsumerCredentialsByConfigurationMutator lti11ConsumerCredentialsByConfigurationMutator;
    private final LTI11ConsumerCredentialsByConfigurationMaterializer lti11ConsumerCredentialsByConfigurationMaterializer;
    private final LTI11ConsumerCredentialsByWorkspaceMutator lti11ConsumerCredentialsByWorkspaceMutator;
    private final LTI11ConsumerCredentialsByWorkspaceMaterializer lti11ConsumerCredentialsByWorkspaceMaterializer;
    private final AccountByLTI11ConfigurationUserMaterializer accountByLTI11ConfigurationUserMaterializer;
    private final AccountByLTI11ConfigurationUserMutator accountByLTI11ConfigurationUserMutator;
    private final LTI11LaunchBySessionHashMutator lti11LaunchBySessionHashMutator;
    private final LTI11LaunchBySessionHashMaterializer lti11LaunchBySessionHashMaterializer;

    @Inject
    public LTIv11Gateway(final Session session,
                         final LTICredentialByIdMutator ltiCredentialByIdMutator,
                         final LTICredentialByKeyMutator ltiCredentialByKeyMutator,
                         final LTICredentialByKeyMaterializer ltiCredentialByKeyMaterializer,
                         final LTILaunchRequestEntryMutator ltiLaunchRequestEntryMutator,
                         final LTILaunchRequestStaticFieldsMutator ltiLaunchRequestStaticFieldsMutator,
                         final LTILaunchRequestLogEventMutator ltiLaunchRequestLogEventMutator,
                         final AccountByLTILaunchRequestMutator accountByLTILaunchRequestMutator,
                         final LTILaunchRequestByAccountMutator ltiLaunchRequestByAccountMutator,
                         final LTI11ConsumerConfigurationMutator lti11ConsumerConfigurationMutator,
                         final LTI11ConsumerConfigurationByWorkspaceMutator lti11ConsumerConfigurationByWorkspaceMutator,
                         final LTI11ConsumerConfigurationMaterializer lti11ConsumerConfigurationMaterializer,
                         final LTI11ConsumerConfigurationByWorkspaceMaterializer lti11ConsumerConfigurationByWorkspaceMaterializer,
                         final LTI11ConsumerCredentialsMutator lti11ConsumerCredentialsMutator,
                         final LTI11ConsumerCredentialsMaterializer lti11ConsumerCredentialsMaterializer,
                         final LTI11ConsumerCredentialsByConfigurationMutator lti11ConsumerCredentialsByConfigurationMutator,
                         final LTI11ConsumerCredentialsByConfigurationMaterializer lti11ConsumerCredentialsByConfigurationMaterializer,
                         final LTI11ConsumerCredentialsByWorkspaceMutator lti11ConsumerCredentialsByWorkspaceMutator,
                         final LTI11ConsumerCredentialsByWorkspaceMaterializer lti11ConsumerCredentialsByWorkspaceMaterializer,
                         final AccountByLTI11ConfigurationUserMaterializer accountByLTI11ConfigurationUserMaterializer,
                         final AccountByLTI11ConfigurationUserMutator accountByLTI11ConfigurationUserMutator,
                         final LTI11LaunchBySessionHashMutator lti11LaunchBySessionHashMutator,
                         final LTI11LaunchBySessionHashMaterializer lti11LaunchBySessionHashMaterializer) {
        this.session = session;
        this.ltiCredentialByIdMutator = ltiCredentialByIdMutator;
        this.ltiCredentialByKeyMutator = ltiCredentialByKeyMutator;
        this.ltiCredentialByKeyMaterializer = ltiCredentialByKeyMaterializer;
        this.ltiLaunchRequestEntryMutator = ltiLaunchRequestEntryMutator;
        this.ltiLaunchRequestStaticFieldsMutator = ltiLaunchRequestStaticFieldsMutator;
        this.ltiLaunchRequestLogEventMutator = ltiLaunchRequestLogEventMutator;
        this.accountByLTILaunchRequestMutator = accountByLTILaunchRequestMutator;
        this.ltiLaunchRequestByAccountMutator = ltiLaunchRequestByAccountMutator;
        this.lti11ConsumerConfigurationMutator = lti11ConsumerConfigurationMutator;
        this.lti11ConsumerConfigurationByWorkspaceMutator = lti11ConsumerConfigurationByWorkspaceMutator;
        this.lti11ConsumerConfigurationMaterializer = lti11ConsumerConfigurationMaterializer;
        this.lti11ConsumerConfigurationByWorkspaceMaterializer = lti11ConsumerConfigurationByWorkspaceMaterializer;
        this.lti11ConsumerCredentialsMutator = lti11ConsumerCredentialsMutator;
        this.lti11ConsumerCredentialsMaterializer = lti11ConsumerCredentialsMaterializer;
        this.lti11ConsumerCredentialsByConfigurationMutator = lti11ConsumerCredentialsByConfigurationMutator;
        this.lti11ConsumerCredentialsByConfigurationMaterializer = lti11ConsumerCredentialsByConfigurationMaterializer;
        this.lti11ConsumerCredentialsByWorkspaceMutator = lti11ConsumerCredentialsByWorkspaceMutator;
        this.lti11ConsumerCredentialsByWorkspaceMaterializer = lti11ConsumerCredentialsByWorkspaceMaterializer;
        this.accountByLTI11ConfigurationUserMaterializer = accountByLTI11ConfigurationUserMaterializer;
        this.accountByLTI11ConfigurationUserMutator = accountByLTI11ConfigurationUserMutator;
        this.lti11LaunchBySessionHashMutator = lti11LaunchBySessionHashMutator;
        this.lti11LaunchBySessionHashMaterializer = lti11LaunchBySessionHashMaterializer;
    }

    /**
     * Persists Lti Consumer Key mutation
     * @param ltiConsumerKey LTI Consumer Key to persist
     */
    @Deprecated
    public Flux<Void> save(LTIConsumerKey ltiConsumerKey) {
        return Mutators.execute(session, Flux.just(ltiCredentialByIdMutator.upsert(ltiConsumerKey),
                                                   ltiCredentialByKeyMutator.upsert(ltiConsumerKey)));
    }

    /**
     * Persist a launch request entry. Does not update the request url.
     *
     * @param entry the entry to persist
     * @return nada
     */
    public Flux<Void> persist(LTILaunchRequestEntry entry) {
        return Mutators.execute(session, Flux.just(ltiLaunchRequestEntryMutator.upsert(entry)));
    }

    /**
     * Persist the static fields of a launch request, includes:
     *  - assoiation to the credential ID
     *  - request url.
     *
     *  Does not update the other fields, only the request url.
     *
     * @param entry the entry containing the request url.
     * @return nada
     */
    public Flux<Void> persistLaunchRequestStaticFields(LTILaunchRequestEntry entry) {
        return Mutators.execute(session, Flux.just(ltiLaunchRequestStaticFieldsMutator.upsert(entry)));
    }

    /**
     * Persist a launch request status.
     *
     * @param requestStatus the status message
     * @return nada
     */
    public Flux<Void> persist(LTILaunchRequestLogEvent requestStatus) {
        return Mutators.execute(session, Flux.just(ltiLaunchRequestLogEventMutator.upsert(requestStatus)));
    }

    /**
     * Record the association of the launch request to a user.
     *
     * @param launchRequestId the launch request
     * @param accountId the account id
     * @return nada
     */
    public Flux<Void> associateLaunchRequestToAccount(final UUID launchRequestId, final UUID accountId) {
        return Flux.just(Tuples.of(launchRequestId, accountId))
                .flatMap(tuple -> Mutators.execute(session,
                                                   Flux.just(accountByLTILaunchRequestMutator.upsert(tuple),
                                                             ltiLaunchRequestByAccountMutator.upsert(tuple))));
    }

    /**
     * Finds a LTIConsumerKey
     * @param key a consumer key value
     * @return empty Mono if LTIConsumerKey is not found
     */
    @Deprecated
    public Mono<LTIConsumerKey> findLTIConsumerKey(String key) {
        return ResultSets.query(session, ltiCredentialByKeyMaterializer.fetchByKey(key))
                .flatMapIterable(row -> row)
                .map(ltiCredentialByKeyMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist LTI consumer key credentials
     *
     * @param ltIv11ConsumerKey the object to persist
     * @return a mono with the persisted object
     */
    public Mono<LTIv11ConsumerKey> persist(final LTIv11ConsumerKey ltIv11ConsumerKey) {
        return Mutators.execute(session, Flux.just(
                lti11ConsumerCredentialsMutator.upsert(ltIv11ConsumerKey),
                lti11ConsumerCredentialsByConfigurationMutator.upsert(ltIv11ConsumerKey),
                lti11ConsumerCredentialsByWorkspaceMutator.upsert(ltIv11ConsumerKey)))
                .then(Mono.just(ltIv11ConsumerKey))
                .doOnEach(log.reactiveErrorThrowable("error persisting the LTI consumer credentials", throwable -> new HashMap<String, Object>() {
                    {put("workspaceId", ltIv11ConsumerKey.getWorkspaceId());}
                    {put("cohortId", ltIv11ConsumerKey.getCohortId());}
                    {put("consumerConfigurationId", ltIv11ConsumerKey.getConsumerConfigurationId());}
                }));
    }

    /**
     * Find the LTI consumer configuration id for a workspace
     *
     * @param workspaceId the workspace to find the LTI configuration for
     * @return a mono with the lti consumer configuration object or an empty mono when not found
     */
    public Mono<LTIv11ConsumerConfiguration> findConfigurationByWorkspace(final UUID workspaceId) {
        return ResultSets.query(session, lti11ConsumerConfigurationByWorkspaceMaterializer.findByWorkspace(workspaceId))
                .flatMapIterable(row -> row)
                .map(lti11ConsumerConfigurationByWorkspaceMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error fetching the LTI consumer configurations", throwable -> new HashMap<String, Object>() {
                    {put("workspaceId", workspaceId);}
                }));
    }

    /**
     * Persist LTI Consumer configuration agains a workspace
     *
     * @param ltIv11ConsumerConfiguration the consumer configuration to persist
     * @return a mono with the persisted consumer configuration
     */
    public Mono<LTIv11ConsumerConfiguration> persist(final LTIv11ConsumerConfiguration ltIv11ConsumerConfiguration) {
        return Mutators.execute(session, Flux.just(
                lti11ConsumerConfigurationMutator.upsert(ltIv11ConsumerConfiguration),
                lti11ConsumerConfigurationByWorkspaceMutator.upsert(ltIv11ConsumerConfiguration)))
                .then(Mono.just(ltIv11ConsumerConfiguration))
                .doOnEach(log.reactiveErrorThrowable("error persisting the LTI consumer configurations", throwable -> new HashMap<String, Object>() {
                    {put("workspaceId", ltIv11ConsumerConfiguration.getWorkspaceId());}
                }));
    }

    /**
     * Fetch an account id for an LTI consumer configuration id and userId
     *
     * @param configurationId the lti consumer configuration id
     * @param userId the user id
     * @return a mono of uuid representing the accountId
     */
    public Mono<UUID> fetchAccountId(final UUID configurationId, final String userId) {
        return ResultSets.query(session, accountByLTI11ConfigurationUserMaterializer.findByConfigurationUser(configurationId, userId))
                .flatMapIterable(row -> row)
                .map(accountByLTI11ConfigurationUserMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error fetching the account id", throwable -> new HashMap<String, Object>() {
                    {put("consumerConfigurationId", configurationId);}
                    {put("userId", userId);}
                }));
    }

    /**
     * Persist the association of an accountId to an LTI consumer configuration id and userId
     *
     * @param configurationId the LTI consumer configuration id
     * @param userId the user id
     * @param accountId the account id to associate
     * @return a flux of void
     */
    public Flux<Void> persistAccountIdByLTI(final UUID configurationId, final String userId, final UUID accountId) {
        return Mutators.execute(session, Flux.just(
                accountByLTI11ConfigurationUserMutator.upsert(configurationId, userId, accountId)))
                .doOnEach(log.reactiveErrorThrowable("error persisting the account id", throwable -> new HashMap<String, Object>() {
                    {put("consumerConfigurationId", configurationId);}
                    {put("userId", userId);}
                    {put("accountId", accountId);}
                }));
    }

    /**
     * Find the consumer key associated to a workspace and cohort
     *
     * @param workspaceId the workspace id
     * @param cohortId the cohort id
     * @param oauthConsumerKey the consumer key to find
     * @return a mono with the consumer key object or an empty mono when not found
     */
    public Mono<LTIv11ConsumerKey> findConsumerKey(final UUID workspaceId, final UUID cohortId, final String oauthConsumerKey) {
        return ResultSets.query(session, lti11ConsumerCredentialsByWorkspaceMaterializer
                .findByWorkspaceCohortKey(workspaceId, cohortId, oauthConsumerKey))
                .flatMapIterable(row -> row)
                .map(lti11ConsumerCredentialsByWorkspaceMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error fetching the consumer key", throwable -> new HashMap<String, Object>() {
                    {put("workspaceId", workspaceId);}
                    {put("cohortId", cohortId);}
                    {put("oauthConsumerKey", oauthConsumerKey);}
                }));
    }

    /**
     * Persist an LTI launch session hash (to track a session.js init)
     *
     * @param hash the hash object to persist
     * @return a mono with the persisted object
     */
    public Mono<LTI11LaunchSessionHash> persist(final LTI11LaunchSessionHash hash) {
        return Mutators.execute(session, Flux.just(
                lti11LaunchBySessionHashMutator.upsert(hash)))
                .doOnEach(log.reactiveErrorThrowable("error persisting the LTI session hash", throwable -> new HashMap<String, Object>() {
                    {put("consumerConfigurationId", hash.getConfigurationId());}
                    {put("userId", hash.getUserId());}
                    {put("launchRequestId", hash.getLaunchRequestId());}
                }))
                .then(Mono.just(hash));
    }

    /**
     * Fetch the lti session hash (to continue the LTI flow after a session.js init)
     *
     * @param hash the hash to find
     * @param launchRequestId the launch request id associated
     * @return a mono with the found launch session hash or an empty mono when not found
     */
    public Mono<LTI11LaunchSessionHash> findSessionHash(final String hash, final UUID launchRequestId) {
        return ResultSets.query(session, lti11LaunchBySessionHashMaterializer.findBy(hash, launchRequestId))
                .flatMapIterable(row -> row)
                .map(lti11LaunchBySessionHashMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error fetching the LTI session hash", throwable -> new HashMap<String, Object>() {
                    {put("hash", hash);}
                    {put("launchRequestId", launchRequestId);}
                }));
    }

    /**
     * Fetch all the LTI consumer key configured to a cohort
     *
     * @param workspaceId the workspace id the cohort belongs to
     * @param cohortId the cohort to find the consumer key for
     * @return a flux containing all the keys or empty when none found
     */
    public Flux<LTIv11ConsumerKey> findConsumerKey(final UUID workspaceId, final UUID cohortId) {
        return ResultSets.query(session, lti11ConsumerCredentialsByWorkspaceMaterializer.findByWorkspaceCohort(workspaceId, cohortId))
                .flatMapIterable(row -> row)
                .map(lti11ConsumerCredentialsByWorkspaceMaterializer::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error fetching the LTI consumer key", throwable -> new HashMap<String, Object>() {
                    {put("workspaceId", workspaceId);}
                    {put("cohortId", cohortId);}
                }));
    }

}
