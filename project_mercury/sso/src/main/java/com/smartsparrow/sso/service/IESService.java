package com.smartsparrow.sso.service;

import static com.smartsparrow.dataevent.RouteUri.IES_BATCH_PROFILE_GET;
import static com.smartsparrow.dataevent.RouteUri.IES_PROFILE_GET;
import static com.smartsparrow.dataevent.RouteUri.IES_TOKEN_VALIDATE;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.exception.InvalidJWTException;
import com.smartsparrow.iam.service.AuthenticationType;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.iam.data.IESAccountTracking;
import com.smartsparrow.iam.data.IesAccountTrackingGateway;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.sso.event.IESBatchProfileGetEventMessage;
import com.smartsparrow.sso.event.IESBatchProfileGetParams;
import com.smartsparrow.sso.event.IESProfileGetEventMessage;
import com.smartsparrow.sso.event.IESTokenValidationEventMessage;
import com.smartsparrow.util.JWT;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class IESService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IESService.class);

    public static final String IES_EMAIL_FORMAT = "%s@ies.external";

    private final AccountService accountService;
    private final SubscriptionPermissionService subscriptionPermissionService;
    private final CamelReactiveStreamsService camelReactiveStreamsService;
    private final IesAccountTrackingGateway iesAccountTrackingGateway;
    private final CacheService cacheService;

    @Inject
    public IESService(final AccountService accountService,
                      final SubscriptionPermissionService subscriptionPermissionService,
                      final CamelReactiveStreamsService camelReactiveStreamsService,
                      final IesAccountTrackingGateway iesAccountTrackingGateway,
                      final CacheService cacheService) {
        this.accountService = accountService;
        this.subscriptionPermissionService = subscriptionPermissionService;
        this.camelReactiveStreamsService = camelReactiveStreamsService;
        this.iesAccountTrackingGateway = iesAccountTrackingGateway;
        this.cacheService = cacheService;
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#IES_TOKEN_VALIDATE} route which instructs camel
     * to perform an external http request to the IES service to validate the token.
     *
     * @param token the token to validate
     * @return <code>true</code> when the token is valid
     * @throws UnauthorizedFault when the token is not valid
     */
    @Trace(async = true)
    public Mono<Boolean> validateToken(final String token) {

        affirmArgumentNotNullOrEmpty(token, "token is required");
        try {

            // validate token with IES
            Mono<Boolean> isValidMono = Mono.just(new IESTokenValidationEventMessage(token)) //
                    .doOnEach(log.reactiveInfo("handling ies authorization"))
                    .map(event -> camelReactiveStreamsService.toStream(IES_TOKEN_VALIDATE, event, IESTokenValidationEventMessage.class)) //
                    .flatMap(Mono::from)
                    .doOnEach(log.reactiveInfo("ies authorization handling completed"))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .map(iesTokenValidationEventMessage -> {
                        Boolean isValid = iesTokenValidationEventMessage.getValid();
                        if (isValid) {
                            return true;
                        }
                        throw new UnauthorizedFault("Invalid token supplied");
                    });

            // transparently cache if token valid up to expiration
            long expirationSeconds = JWT.getSecondsExp(token);
            String cacheKey = "ies:valid:" + token;

            return cacheService.computeIfAbsent(cacheKey, Boolean.class, isValidMono, expirationSeconds, TimeUnit.SECONDS);

        } catch (InvalidJWTException e) {
            log.jsonError("failed to extract ExpDateTime from ies token", new HashMap<String, Object>() {
                {put("token", token);}
            }, e);
            throw new UnauthorizedFault("Invalid token supplied");
        }
    }

    /**
     * Find the Bronte account by pearsonUid.
     *
     * @param pearsonUid the ies user id to find the account for
     * @return a mono with the associated account or an empty mono when not found
     */
    @Trace(async = true)
    public Mono<Account> findAccount(@Nonnull final String pearsonUid) {
        // find the corresponding account id
        return iesAccountTrackingGateway.findAccountId(pearsonUid)
                // find the account by id
                .flatMap(iesAccountTracking -> accountService.findById(iesAccountTracking.getAccountId())
                        .singleOrEmpty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Provision an account for a Pearson external user. The first time a Pearson external user is going through the
     * Bronte authorization flow the Bronte account is provisioned with a {@link com.smartsparrow.iam.service.AccountRole#STUDENT}
     * role.
     *
     * @param pearsonUid the pearson id to provision the account for
     * @return a mono with the provisioned account
     * @throws IllegalStateFault when the account email is already taken by another account
     */
    public Mono<Account> provisionAccount(@Nonnull final String pearsonUid) {
        return provisionAccount(pearsonUid, AccountProvisionSource.OIDC, AuthenticationType.IES);
    }

    public Mono<Account> provisionAccount(@Nonnull final String pearsonUid, AccountProvisionSource provisionSource,
                                          AuthenticationType authenticationType) {
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");

        try {
            AccountAdapter accountAdapter = accountService.provision(provisionSource,
                    null, null, null,
                    null, String.format(IES_EMAIL_FORMAT, pearsonUid), null,
                    // the account is provision with a student role
                    null, null, false, authenticationType);

            Account acct = accountAdapter.getAccount();

            return subscriptionPermissionService
                    .saveAccountPermission(acct.getId(), acct.getSubscriptionId(), PermissionLevel.OWNER)
                    .singleOrEmpty()
                    // persist the accountId -> pearsonUid tracking
                    .then(iesAccountTrackingGateway.persist(new IESAccountTracking()
                            .setAccountId(acct.getId())
                            .setIesUserId(pearsonUid))
                            .singleOrEmpty())
                    .thenReturn(acct);
        } catch (ConflictException e) {
            // rethrow as a fault, we should never get here in the IES authorization flow
            // if we are here, the data is in an inconsistent state
            log.error(e.getMessage(), e);
            throw new IllegalStateFault(e.getMessage());
        }
    }

    /**
     * Track the account with its ies userId counter-part. This is useful when an account is provisioned with a
     * source that is not aware of an IES session, later on the account should be tracked using this method.
     *
     * The current implementation allows for multiple accounts in Bronte to be associated with a single ies account
     *
     * TODO Ideally there should be some sort of Account Consolidation where 2 accounts are merged
     *
     * @param account the account to associate
     * @param pearsonUid the pearson uid to link the account to
     * @return a mono with the linked account
     */
    public Mono<Account> trackIESAccount(final Account account, final String pearsonUid) {
        return iesAccountTrackingGateway.persist(new IESAccountTracking()
                .setAccountId(account.getId())
                .setIesUserId(pearsonUid))
                .then(Mono.just(account));
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#IES_PROFILE_GET} route which instructs camel
     * to perform an external http request to the IES service to fetch the user identity profile.
     *
     * @param pearsonUid  the userId to find the identity profile for
     * @param accessToken a valid access token
     * @return a mono with the identity profile when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    @Trace(async = true)
    public Mono<IdentityProfile> getProfile(@Nonnull final String pearsonUid,
                                            @Nonnull final String accessToken) {
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");
        affirmArgumentNotNullOrEmpty(accessToken, "accessToken is required");
        Mono<IdentityProfile> loadProfile = Mono.just(new IESProfileGetEventMessage(pearsonUid, accessToken)) //
                .doOnEach(log.reactiveInfo("handling ies profile get"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(event -> camelReactiveStreamsService.toStream(IES_PROFILE_GET, event, IESProfileGetEventMessage.class)) //
                .doOnEach(log.reactiveErrorThrowable("error while fetching IES user identity profile"))
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfo("ies profile get handling completed"))
                .map(IESProfileGetEventMessage -> {
                    final IdentityProfile identity = IESProfileGetEventMessage.getIdentityProfile();
                    if (identity != null) {
                        return identity;
                    }
                    throw new UnauthorizedFault("Invalid token supplied");
                });
        String cacheKey = "ies:profile/" + pearsonUid;
        return cacheService.computeIfAbsent(cacheKey, IdentityProfile.class, loadProfile, 7, TimeUnit.DAYS);
    }

    /**
     * Get account payload for Pearson Identity user
     *
     * @param pearsonUid   pearson user id
     * @param pearsonToken pearson user access token
     * @param account      user account metadata
     * @return a mono with the account payload object
     */
    public Mono<AccountPayload> getAccountPayload(@Nonnull final String pearsonUid,
                                                  @Nonnull final String pearsonToken,
                                                  @Nonnull Account account) {
        affirmNotNull(account, "Account should not be null");
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");
        affirmArgumentNotNullOrEmpty(pearsonToken, "pearsonToken is required");

        return getProfile(pearsonUid, pearsonToken)
                .map(identityProfile -> AccountPayload.from(account, new AccountIdentityAttributes()
                        .setAccountId(account.getId())
                        .setPrimaryEmail(identityProfile.getPrimaryEmail())
                        .setGivenName(identityProfile.getGivenName())
                        .setFamilyName(identityProfile.getFamilyName()),
                        new AccountAvatar(), AuthenticationType.IES));
    }

    /**
     * Fetch a list of identity profiles from the ies service
     *
     * @param ids the ids to fetch the profiles for
     * @return a flux of identity profile, when the identity was not found for a particular id, then an identity profile
     * object is returned where only the id is set
     */
    public Flux<IdentityProfile> getProfiles(final List<String> ids) {
        affirmArgument(ids != null, "ids is required");
        affirmArgument(!ids.isEmpty(), "ids must not be empty");

        // TODO: Cache this?
        // prepare the ies batch message for the camel route
        return Mono.just(new IESBatchProfileGetEventMessage(new IESBatchProfileGetParams(ids)))
                .doOnEach(log.reactiveInfo("preparing batch fetch of ies profiles"))
                // send the message to the camel route
                .map(event -> camelReactiveStreamsService.toStream(IES_BATCH_PROFILE_GET, event, IESBatchProfileGetEventMessage.class)) //
                .doOnEach(log.reactiveErrorThrowable("error while fetching IES user identity profile"))
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfo("batch fetch of ies profiles completed"))
                .map(message -> {
                    // for each not found create an identity profile where only the id is set
                    final List<IdentityProfile> notFound = message.getNotFound().stream()
                            .map(id -> new IdentityProfile()
                                    .setId(id))
                            .collect(Collectors.toList());
                    // get the profiles found and add all the not found to it
                    List<IdentityProfile> profiles = message.getIdentityProfile();
                    profiles.addAll(notFound);
                    return profiles;
                })
                .flux()
                // flat map the list of profiles into a flux
                .flatMap(identityProfiles -> Flux.just(identityProfiles.toArray(new IdentityProfile[0])));
    }

    /**
     * Get account summary payloads for a list of ies accounts. This function calls the external ies service and
     * performs a bulk fetch of all the identity profiles. Please not this call is time consuming.
     *
     * @param accounts the ies accounts to fetch the account summary payload for
     * @return a flux of account summary payloads
     */
    public Flux<AccountSummaryPayload> getAccountSummaryPayload(final List<IESAccountTracking> accounts) {
        // prepare the list of ids to fetch the profiles for
        final List<String> ids = accounts.stream()
                .map(IESAccountTracking::getIesUserId)
                .collect(Collectors.toList());

        // prepare a map so we can quickly access accountIds when building the payload
        final Map<String, UUID> accountsMap = accounts.stream()
                .collect(Collectors.toMap(IESAccountTracking::getIesUserId, IESAccountTracking::getAccountId));

        // fetch the profiles
        return getProfiles(ids)
                // return the account summary payload
                .map(identityProfile -> AccountSummaryPayload.from(
                        new AccountIdentityAttributes()
                                .setAccountId(accountsMap.get(identityProfile.getId()))
                                .setPrimaryEmail(identityProfile.getPrimaryEmail())
                                .setGivenName(identityProfile.getGivenName())
                                .setFamilyName(identityProfile.getFamilyName()),
                        // set the avatar as empty, we don't get that info from ies atm
                        new AccountAvatar()
                ));
    }

    /**
     * Find the pearsonUid for an account id
     *
     * @param accountId the account id to find the pearson id for
     * @return a mono with the pearsonUid or an empty stream when not found
     */
    @Trace(async = true)
    public Mono<IESAccountTracking> findIESId(final UUID accountId) {
        return iesAccountTrackingGateway.findIesUserId(accountId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
