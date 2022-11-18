package com.smartsparrow.sso.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.smartsparrow.sso.service.LTILaunchRequestLogEvent.Action.ERROR;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HttpHeaders;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountLogEntry;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.sso.data.ltiv11.LTI11LaunchSessionHash;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerConfiguration;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerKey;
import com.smartsparrow.sso.data.ltiv11.LTIv11Gateway;
import com.smartsparrow.sso.service.LTILaunchRequestLogEvent.Action;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.Tokens;
import com.smartsparrow.util.UUIDs;

import io.reactivex.functions.Function3;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * LTI v1.1 functionality.
 */
@Singleton
public class LTIv11Service {

    private static final long LOG_SENSITIVE_TTL = TimeUnit.DAYS.toSeconds(3);

    private static final Logger log = LoggerFactory.getLogger(LTIv11Service.class);

    private final AccountService accountService;
    private final LTIv11Gateway ltIv11Gateway;
    private final LTIMessageSignatures ltiMessageSignatures;
    private final IESService iesService;
    private final RedissonReactiveClient redissonReactiveClient;

    @Inject
    public LTIv11Service(final AccountService accountService,
                         final LTIv11Gateway ltIv11Gateway,
                         final LTIMessageSignatures ltiMessageSignatures,
                         final IESService iesService,
                         final RedissonReactiveClient redissonReactiveClient) {
        this.accountService = accountService;
        this.ltIv11Gateway = ltIv11Gateway;
        this.ltiMessageSignatures = ltiMessageSignatures;
        this.iesService = iesService;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    /**
     * Generates LTI Consumer Key for the given subscription
     *
     * @param subscriptionId subscription id
     * @param comment any additional information related to this key, e.g. the LMS hostname
     *
     * @return created LTI Consumer Key
     * @throws IllegalStateFault if a generated key has a collision
     */
    @Deprecated
    public LTIConsumerKey createLTIConsumerKey(final UUID subscriptionId, @Nullable final String comment) {
        affirmNotNull(subscriptionId, "missing subscription id");

        LTIConsumerKey credential = new LTIConsumerKey()
                .setId(UUIDs.timeBased())
                .setKey(Tokens.generate(24))
                .setSecret(Tokens.generate(36))
                .setSubscriptionId(subscriptionId)
                .setComment(comment);

        // the probability of a collision is small. really really really small.
        Boolean duplicate = ltIv11Gateway.findLTIConsumerKey(credential.getKey()).hasElement().block();
        if (Boolean.TRUE.equals(duplicate)) {
            log.error("a duplicate lti consumer key was generated. wow! {}", credential.getKey());
            throw new IllegalStateFault("there was a collision, please try again");
        }

        ltIv11Gateway.save(credential).blockLast();

        return credential;
    }

    /**
     * Lookup an account given LTI consumer configurations and user_id.
     *
     * @param configurationId the consumer configuration id to find the account in
     * @param userId the user id to lookup the account for
     * @return a mono with the associated account or an empty mono when not found
     * @throws IllegalArgumentFault when the required arguments are null
     */
    public Mono<Account> findAccountByLTIConfiguration(final UUID configurationId, final String userId) {
        affirmArgument(configurationId != null, "configurationId is required");
        affirmArgument(userId != null, "userId is required");
        return ltIv11Gateway.fetchAccountId(configurationId, userId)
                .flatMap(accountId -> accountService.findById(accountId)
                        .singleOrEmpty());
    }

    /**
     * Find the LTI consumer configuration for a workspace
     *
     * @param workspaceId the workspace id to find the LTI consumer configuration for
     * @return a mono with the consumer configuration or an empty mono when not found
     */
    public Mono<LTIv11ConsumerConfiguration> findLTIConsumerConfiguration(final UUID workspaceId) {
        affirmArgument(workspaceId != null, "workspaceId is required");
        return ltIv11Gateway.findConfigurationByWorkspace(workspaceId);
    }

    /**
     * Creates an association between the LTI consumer configuration id, a user id and the corresponding bronte account id
     *
     * @param configurationId the lti consumer configuration id
     * @param userId the user id (generally from the lti param user_id)
     * @param accountId the account id
     * @return a mono with the account
     */
    public Flux<Void> associateAccountIdToLTI(final UUID configurationId, final String userId, final UUID accountId) {
        affirmArgument(configurationId != null, "configurationId is required");
        affirmArgument(userId != null, "userId is required");
        affirmArgument(accountId != null, "accountId is required");
        return ltIv11Gateway.persistAccountIdByLTI(configurationId, userId, accountId);
    }

    /**
     * Find the LTI consumer key for a cohort
     *
     * @param workspaceId the workspace id the cohort belongs to
     * @param cohortId the cohort id
     * @param key the consumer key to find
     * @return a mono with the lti consumer key or empty when not found
     */
    public Mono<LTIv11ConsumerKey> findConsumerKey(final UUID workspaceId, final UUID cohortId, final String key) {
        return ltIv11Gateway.findConsumerKey(workspaceId, cohortId, key);
    }

    /**
     * Create an lti launch session hash. To track the session.js init flow
     *
     * @param consumerConfigurationId the consumer configuration id
     * @param cohortId the cohort id the launch is related to
     * @param continueTo the launch url
     * @param userId the lti user id
     * @param launchRequestId the lti launch request id
     * @return a mono with an lti launch session hash obj
     */
    public Mono<LTI11LaunchSessionHash> createSessionHash(final UUID consumerConfigurationId, final UUID cohortId,
                                               final String continueTo, final String userId, final UUID launchRequestId) {
        return ltIv11Gateway.persist(new LTI11LaunchSessionHash()
                .setHash(Hashing.string(String.format("%s:%s", userId, launchRequestId.toString())))
                .setStatus(LTI11LaunchSessionHash.Status.VALID)
                .setConfigurationId(consumerConfigurationId)
                .setCohortId(cohortId)
                .setContinueTo(continueTo)
                .setUserId(userId)
                .setLaunchRequestId(launchRequestId));
    }

    /**
     * Mark an lti launch session hash object as expired, meaning the session.js flow has completed
     *
     * @param launchSessionHash the launch session hash to expire
     * @return a mono with the expired lti launch session hash obj
     */
    public Mono<LTI11LaunchSessionHash> expireSessionHash(final LTI11LaunchSessionHash launchSessionHash) {
        return ltIv11Gateway.persist(new LTI11LaunchSessionHash()
                .setHash(launchSessionHash.getHash())
                .setStatus(LTI11LaunchSessionHash.Status.EXPIRED)
                .setConfigurationId(launchSessionHash.getConfigurationId())
                .setCohortId(launchSessionHash.getCohortId())
                .setContinueTo(launchSessionHash.getContinueTo())
                .setUserId(launchSessionHash.getUserId())
                .setLaunchRequestId(launchSessionHash.getLaunchRequestId()));
    }

    /**
     * Find a valid lti launch session hash (to continue the LTI launch after a session.js init). Filters out
     * any lti launch session hash that has a status of
     * {@link com.smartsparrow.sso.data.ltiv11.LTI11LaunchSessionHash.Status#EXPIRED}
     *
     * @param hash the hash to find
     * @param launchRequestId the launch request id
     * @return a mono with the found object or an empty stream when not found
     */
    public Mono<LTI11LaunchSessionHash> findValidSessionHash(final String hash, final UUID launchRequestId) {
        return ltIv11Gateway.findSessionHash(hash, launchRequestId)
                // filter out any expired hash
                .filter(launchSessionHash -> !launchSessionHash.isExpired());
    }

    /**
     * Provision a Bronte account for a new user_id launching via LTI after session.js has been initialized
     *
     * @param hash the hash to find the launch session for
     * @param launchRequestId the launch request id
     * @param pearsonUid the pi user id
     * @param enrollAccount the function that enrols the account
     * @return a mono with the LTI11LaunchSessionHash object
     */
    public Mono<LTI11LaunchSessionHash> provisionAccount(final String hash,
                                         final UUID launchRequestId, final String pearsonUid,
                                         Function3<UUID, UUID, String, Mono<UUID>> enrollAccount) {
        // lookup the session hash launch
        return findValidSessionHash(hash, launchRequestId)
                .single()
                // throw a fault when that is not found
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new UnauthorizedFault("invalid session hash launch");
                })
                // try finding an account for this user
                .flatMap(sessionHash -> iesService.findAccount(pearsonUid)
                        .switchIfEmpty(Mono.defer(() -> {
                            // provision the account with source LTI when no account is found
                            return iesService.provisionAccount(pearsonUid, AccountProvisionSource.LTI,
                                                               AuthenticationType.LTI);
                        }))
                        // persist the lti user id relationship with the account so we can find it next time
                        .flatMap(account -> ltIv11Gateway.persistAccountIdByLTI(sessionHash.getConfigurationId(),
                                sessionHash.getUserId(), account.getId())
                                .then(Mono.just(account)))
                        // update the launch request status to account provisioned
                        .flatMap(account -> updateLaunchRequestStatus(launchRequestId, Action.ACCOUNT_PROVISIONED, null)
                                .then(Mono.just(account)))
                        // expire the session hash since it is single use
                        .flatMap(account -> expireSessionHash(sessionHash)
                                .then(Mono.just(account)))
                        // try enrolling the account
                        // take the enrollment function as a callback argument for now
                        // there is a circular dependency and I am running out of time
                        // this is not super nice
                        // TODO this can be refactored better
                        .flatMap(account -> {
                            try {
                                return enrollAccount.apply(account.getId(), sessionHash.getCohortId(), pearsonUid)
                                        .then(Mono.just(account));
                            } catch (Exception e) {
                                return Mono.error(new IllegalStateFault("error enrolling provisioned account via LTI"));
                            }
                        })
                        // return the launch session hash
                        .flatMap(account -> {
                            try {
                                //get redis ltiparams by sessionHash
                                String cacheNameToRetrieve = String.format("ltiParams:sessionHash:/%s", sessionHash.getHash());

                                RBucketReactive<Map<String, String>> retrieveBucket = redissonReactiveClient.getBucket(cacheNameToRetrieve);
                                Map<String, String> ltiParams = retrieveBucket.get().block();

                                // Store ltiparams by account ID and deploymentId
                                String labId = sessionHash.getContinueTo();
                                String deploymentId = labId.substring(labId.lastIndexOf('/') + 1);
                                String cacheNameToStore = String.format("ltiParams:account:/%s:deploymentId:/%s", account.getId(), deploymentId);

                                RBucketReactive<Map<String, String>> storeBucket = redissonReactiveClient.getBucket(cacheNameToStore);
                                storeBucket.set(ltiParams, 1l, TimeUnit.DAYS).block();
                            } catch (Exception e) {
                                log.info("Error occurred while storing/retrieving of ltiParams from cache");
                            }
                            return Mono.just(sessionHash);
                        }));
    }

    /**
     * Record the supplied launch request along with HTTP headers.
     *
     * @param credentials the lti consumer credentials
     * @return a Mono of the Launch Request ID
     */
    Mono<UUID> recordLaunchRequest(final LTI11ConsumerCredentials credentials) {
        //
        final UUID launchRequestId = UUIDs.timeBased();

        // create a flux out of the HTTP headers; convert the Map<String, List<String>> to single key/value pairs mostly.
        Flux<LTILaunchRequestEntry> headerEntries = Flux.fromIterable(credentials.getHttpHeaders().entrySet())
                .map(entry -> {
                    // lowercase the request header.
                    String name = entry.getKey().toLowerCase();

                    // merge the headers values.
                    List<String> values = entry.getValue();
                    String value;
                    // can we shortcut to avoid non-necessary logic in the else below?
                    if (values.size() <= 1) {
                        value = values.get(0);
                    } else {
                        // idea from: https://github.com/bnoordhuis/mozilla-central/blob/master/netwerk/protocol/http/nsHttpHeaderArray.h#L185
                        if (HttpHeaders.SET_COOKIE.equalsIgnoreCase(name) ||
                                HttpHeaders.COOKIE.equalsIgnoreCase(name) ||
                                HttpHeaders.WWW_AUTHENTICATE.equalsIgnoreCase(name) ||
                                HttpHeaders.PROXY_AUTHENTICATE.equalsIgnoreCase(name)) {
                            // Special case these headers and use a newline delimiter to
                            // delimit the values from one another as commas may appear
                            // in the values of these headers contrary to what the spec says.
                            value = String.join("\n", values);
                        } else {
                            // Delimit each value from the others using a comma (per HTTP spec)
                            value = String.join(", ", values);
                        }
                    }

                    return new LTILaunchRequestEntry() //
                            .setLaunchRequestId(launchRequestId)
                            .setPart(LTILaunchRequestEntry.Part.HEADER)
                            .setName(name)
                            .setValue(value);
                });

        // if log debug, create a flux of entries out of the LTI parameters
        Flux<LTILaunchRequestEntry> paramEntries = Flux.empty();
        if (credentials.isLogDebug()) {
            paramEntries = Flux.fromIterable(credentials.getLtiMessage().getParams().entrySet())
                    .map(entry -> new LTILaunchRequestEntry() //
                            .setLaunchRequestId(launchRequestId)
                            .setPart(LTILaunchRequestEntry.Part.PARAM)
                            .setName(entry.getKey())
                            .setValue(entry.getValue())
                            .setTtl((int) LOG_SENSITIVE_TTL));
        }

        // join them together & persist!
        return Flux.concat(headerEntries, paramEntries)
                // write the entries.
                //.doOnNext(entry -> log.info("persisting: {}", entry))
                .doOnNext(entry -> ltIv11Gateway.persist(entry).subscribe())
                // update the static fields
                .then(Mono.just(new LTILaunchRequestEntry() //
                        .setLaunchRequestId(launchRequestId) //
                        .setRequestUrl(credentials.getUrl())))
                .doOnNext(entry -> ltIv11Gateway.persistLaunchRequestStaticFields(entry).subscribe())
                //.doOnNext(entry -> log.info("persisting: {}", entry))
                // finish by returning the launch request id.
                .then(Mono.just(launchRequestId));
    }

    /**
     * Update a launch request status.
     *
     * @param launchRequestId the launch request id
     * @param action the status
     * @param message any relevant message
     * @return the created status.
     */
    public Mono<LTILaunchRequestLogEvent> updateLaunchRequestStatus(final UUID launchRequestId,
                                                             final Action action,
                                                             @Nullable final String message) {
        //
        return Mono.just(new LTILaunchRequestLogEvent() //
                                 .setLaunchRequestId(launchRequestId) //
                                 .setId(UUIDs.timeBased()) // new id for the entry itself.
                                 .setAction(action)
                                 .setMessage(message))
                .doOnNext(s -> ltIv11Gateway.persist(s).subscribe());
    }

    /**
     * Assert that a require parameter exists in the supplied LTI launch parameters.
     *
     * @param ltiMessage the LTI message
     * @param param the parameter to check from the parameters
     * @param launchRequestId the launch request id (for logging)
     * @param account the account (for logging)
     */
    void assertRequiredParameter(final LTIMessage ltiMessage,
                                 final LTIParam param,
                                 final UUID launchRequestId,
                                 @Nullable final Account account) {
        //
        String value = ltiMessage.get(param);
        if (isNullOrEmpty(value)) {
            String msg = String.format("missing required field %s", param.getValue());
            // log the message against the user.
            if (account != null) {
                accountService.addLogEntry(account.getId(), AccountLogEntry.Action.ERROR, null, msg);
            }
            // log an error against the launch request
            if (launchRequestId != null) {
                updateLaunchRequestStatus(launchRequestId, ERROR, msg).block();
            }

            throw new IllegalArgumentFault(msg);
        }
    }

    /**
     * Assert that a required parameter exists in the supplied LTI launch parameters
     *
     * @param message the lti message to check the params for
     * @param param the param to check
     * @throws IllegalArgumentFault when the param is missing from the message
     */
    private void assertRequiredParam(LTIMessage message, LTIParam param) {
        final String requiredParam = message.get(param);

        if (requiredParam == null || requiredParam.isEmpty()) {
            throw new IllegalArgumentFault(String.format("missing required field %s", param.getValue()));
        }
    }

    /**
     * Assert that the signature of the message is valid and perform necessary failure logging.
     *
     * @param credentials the lti credentials to validate the signature for
     */
    void assertValidSignature(final LTI11ConsumerCredentials credentials) {
        // validate the message signature.
        OAuthMessage oAuthMessage = new OAuthMessage("POST", credentials.getUrl(), credentials.getLtiMessage().entrySet());
        try {
            ltiMessageSignatures.validate(oAuthMessage, credentials.getKey(), credentials.getSecret());
        } catch (OAuthProblemException ope) {
            throw new IllegalArgumentFault(ope.getProblem());
        }
    }
}
