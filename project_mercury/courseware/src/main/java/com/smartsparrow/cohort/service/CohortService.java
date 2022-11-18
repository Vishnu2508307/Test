package com.smartsparrow.cohort.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.data.AccountCohortCollaborator;
import com.smartsparrow.cohort.data.CohortGateway;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSettingsGateway;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.data.LtiConsumerCredentialGateway;
import com.smartsparrow.cohort.data.TeamCohortCollaborator;
import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.payload.CohortSettingsPayload;
import com.smartsparrow.cohort.payload.CohortSummaryPayload;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.exception.NotFoundException;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerConfiguration;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerKey;
import com.smartsparrow.sso.data.ltiv11.LTIv11Gateway;
import com.smartsparrow.util.ClockProvider;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import org.apache.commons.lang.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CohortService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CohortService.class);

    private final CohortGateway cohortGateway;
    private final CohortPermissionService cohortPermissionService;
    private final CohortSettingsGateway cohortSettingsGateway;
    private final CohortEnrollmentService cohortEnrollmentService;
    private final AccountService accountService;
    private final TeamService teamService;
    private final DeploymentGateway deploymentGateway;
    private final LtiConsumerCredentialGateway ltiConsumerCredentialGateway;
    private final ClockProvider clockProvider;
    private final LTIv11Gateway ltIv11Gateway;

    @Inject
    public CohortService(final CohortGateway cohortGateway,
                         final CohortPermissionService cohortPermissionService,
                         final CohortSettingsGateway cohortSettingsGateway,
                         final CohortEnrollmentService cohortEnrollmentService,
                         final AccountService accountService,
                         final TeamService teamService,
                         final DeploymentGateway deploymentGateway,
                         final LtiConsumerCredentialGateway ltiConsumerCredentialGateway,
                         final ClockProvider clockProvider,
                         final LTIv11Gateway ltIv11Gateway) {
        this.cohortGateway = cohortGateway;
        this.cohortPermissionService = cohortPermissionService;
        this.cohortSettingsGateway = cohortSettingsGateway;
        this.cohortEnrollmentService = cohortEnrollmentService;
        this.accountService = accountService;
        this.teamService = teamService;
        this.deploymentGateway = deploymentGateway;
        this.ltiConsumerCredentialGateway = ltiConsumerCredentialGateway;
        this.clockProvider = clockProvider;
        this.ltIv11Gateway = ltIv11Gateway;
    }

    /**
     * Fetch cohort summary by id
     *
     * @param cohortId the id of the cohort to fetch
     * @return a {@link Mono} of {@link CohortSummary}
     */
    @Trace(async = true)
    public Mono<CohortSummary> fetchCohortSummary(final UUID cohortId) {
        return cohortGateway.findCohortSummary(cohortId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch cohort settings by id
     *
     * @param cohortId the id of the cohort which settings should be fetched
     */
    @Trace(async = true)
    public Mono<CohortSettings> fetchCohortSettings(final UUID cohortId) {
        return cohortSettingsGateway.findCohortSettings(cohortId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch the account collaborators for a cohort
     *
     * @param cohortId the cohort id to search the collaborators for
     * @return a {@link Flux} of {@link AccountCohortCollaborator}
     */
    public Flux<AccountCohortCollaborator> fetchAccountCollaborators(final UUID cohortId) {
        return cohortGateway.findAccountCollaborators(cohortId);
    }

    /**
     * Fetch the team collaborators for a cohort
     *
     * @param cohortId the cohort id to search the collaborators for
     * @return a {@link Flux} of {@link TeamCohortCollaborator}
     */
    public Flux<TeamCohortCollaborator> fetchTeamCollaborators(final UUID cohortId) {
        return cohortGateway.findTeamCollaborators(cohortId);
    }

    /**
     * Create a cohort summary and saves the {@param creatorId} as the {@link PermissionLevel#OWNER} of the newly
     * created entity.
     *
     * @param creatorId the id of the account creating the cohort
     * @param workspaceId the workspace id the cohort belongs to
     * @param name the cohort name
     * @param type the enrllment type
     * @param startDate the start date
     * @param endDate the end date
     * @return a mono of cohort summary
     * @throws IllegalArgumentException when a required argument is <code>null</code>
     */
    public Mono<CohortSummary> createCohort(final UUID creatorId, final UUID workspaceId, final String name, final EnrollmentType type,
                                            final Long startDate, final Long endDate, final UUID subscriptionId) {

        return createCohort(UUIDs.timeBased(), creatorId, workspaceId, name, type, startDate, endDate, subscriptionId);
    }

    /**
     * Create a cohort summary and saves the {@param creatorId} as the {@link PermissionLevel#OWNER} of the newly
     * created entity.
     *
     * @param cohortId the cohort id to use when creating the cohort
     * @param creatorId the id of the account creating the cohort
     * @param workspaceId the workspace id the cohort belongs to
     * @param name the cohort name
     * @param type the enrllment type
     * @param startDate the start date
     * @param endDate the end date
     * @return a mono of cohort summary
     * @throws IllegalArgumentException when a required argument is <code>null</code>
     */
    public Mono<CohortSummary> createCohort(UUID cohortId, UUID creatorId, UUID workspaceId, String name,
                                            EnrollmentType type, Long startDate, Long endDate, UUID subscriptionId) {

        checkArgument(cohortId != null, "cohortId is required");
        checkArgument(creatorId != null, "creatorId is required");
        checkArgument(workspaceId != null, "workspaceId is required");
        checkArgument(name != null, "name is required");
        checkArgument(type != null, "enrollmentType is required");
        checkArgument(subscriptionId != null, "subscriptionId is required");

        CohortSummary cohort = new CohortSummary()
                .setId(cohortId)
                .setCreatorId(creatorId)
                .setName(name)
                .setType(type)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setWorkspaceId(workspaceId)
                .setSubscriptionId(subscriptionId);

        return cohortGateway.persist(cohort)
                .concatWith(cohortPermissionService.saveAccountPermissions(creatorId, cohort.getId(), PermissionLevel.OWNER))
                .singleOrEmpty()
                .thenReturn(cohort);
    }

    /**
     * Create cohort settings
     *
     * @param cohortId the cohort id the settings belong to
     * @param bannerPattern the banner pattern
     * @param color the color
     * @param bannerImage the banner image
     * @param productId the Pearson product identifier, aka PDZ ID, PPID
     *
     * @return a mono of cohort setting or empty if none of the settings value is valid
     * @throws IllegalArgumentException when a {@param cohortId} is <code>null</code>
     */
    public Mono<CohortSettings> createSettings(final UUID cohortId,
                                               @Nullable final String bannerPattern,
                                               @Nullable final String color,
                                               @Nullable final String bannerImage,
                                               @Nullable final String productId) {
        //
        affirmNotNull(cohortId, "cohortId is required");

        CohortSettings cohortSettings = new CohortSettings()
                .setCohortId(cohortId)
                .setBannerPattern(bannerPattern)
                .setColor(color)
                .setBannerImage(bannerImage)
                .setProductId(productId);

        return cohortSettingsGateway.persist(cohortSettings)
                .singleOrEmpty()
                .thenReturn(cohortSettings);
    }

    /**
     * Set the learner redirect id for a cohort settings entry
     *
     * @param cohortId the cohort id to set the redirect id for
     * @param learnerRedirectId a reference to {@link com.smartsparrow.learner.redirect.LearnerRedirect} object
     * @return a flux of void
     * @throws com.smartsparrow.exception.IllegalArgumentFault when the cohort is null
     */
    public Flux<Void> updateLearnerRedirectId(final UUID cohortId, final UUID learnerRedirectId) {
        affirmArgument(cohortId != null, "cohortId is required");
        return cohortSettingsGateway.updateLearnerRedirectId(cohortId, learnerRedirectId);
    }

    /**
     * Creates CohortPayload for provided cohortId.
     * It fetches CohortSummary and CohortSettings and builds CohortPayload. The Lti consumer credential will only be
     * added to the CohortSettings if enrollment type is LTI
     *
     * @param cohortId cohort unique identifier
     * @return empty mono if cohort is not found
     */
    public Mono<CohortPayload> getCohortPayload(@Nonnull UUID cohortId) {
        checkNotNull(cohortId);

        Mono<CohortSummaryPayload> summaryMono = getCohortSummaryPayload(cohortId);
        Mono<CohortSettingsPayload> settingsMono = getCohortSettingsPayload(cohortId).defaultIfEmpty(new CohortSettingsPayload());

        return Mono.zip(summaryMono, settingsMono).flatMap(tuple2 -> {
            final CohortSummaryPayload summary = tuple2.getT1();
            final CohortSettingsPayload settings = tuple2.getT2();

            // if the type is LTI add the keys to the settings
            if (summary.getEnrollmentType() == EnrollmentType.LTI) {
                return fetchLtiConsumerKeys(summary)
                        .flatMap(keys -> {
                            settings.setLtiConsumerCredentials(keys);
                            return Mono.just(CohortPayload.from(summary, settings));
                        });
            }

            return Mono.just(CohortPayload.from(summary, settings));
        });
    }

    /**
     * Find all the cohorts an account has access to (direct access or as a team member)
     *
     * @param accountId the account to search the cohorts for
     * @return a flux of cohort ids
     */
    public Flux<UUID> fetchCohorts(final UUID accountId) {
        return cohortGateway.findCohortsByAccount(accountId)
                .mergeWith(teamService.findTeamsForAccount(accountId)
                        .flatMap(team -> cohortGateway.findCohortsByTeam(team.getTeamId())))
                .distinct();
    }

    /**
     * Build a cohort summary payload. The encapsulated {@link AccountPayload} object is never <code>null</code><br>
     * When the account payload is not found an empty object is created with the accountId set as the creatorId which
     * is available from the {@link CohortSummary}
     *
     * @param cohortId the cohort id to build the summary payload for
     * @return a mono of cohort summary payload
     */
    public Mono<CohortSummaryPayload> getCohortSummaryPayload(final UUID cohortId) {
        final Mono<CohortSummary> summaryMono = fetchCohortSummary(cohortId);
        final Mono<Long> enrollments = cohortEnrollmentService.fetchEnrollments(cohortId).count();

        return Mono.zip(summaryMono, enrollments)
                .flatMap(tuple2 -> {

                    final CohortSummary cohortSummary = tuple2.getT1();

                    return Mono.zip(summaryMono, enrollments, accountService.getAccountPayload(cohortSummary.getCreatorId())
                            .switchIfEmpty(Mono.just(new AccountPayload().setAccountId(cohortSummary.getCreatorId()))))
                            .map(tuple3 -> CohortSummaryPayload.from(tuple3.getT1(), tuple3.getT2(), tuple3.getT3()));
                })
                .doOnError(throwable -> log.reactiveErrorThrowable(String
                        .format("error while building the cohort summary payload for cohort %s",
                                cohortId)));
    }

    /**
     * Builds a cohort settings payload from CohortSettings fetched from DB.
     *
     * @param cohortId the cohort id to build the cohort settings payload for
     * @return empty Mono if no cohort settings found
     */
    public Mono<CohortSettingsPayload> getCohortSettingsPayload(final UUID cohortId) {

        return fetchCohortSettings(cohortId).map(CohortSettingsPayload::from);
    }

    /**
     * Update cohort summary and cohort settings
     *
     * @param cohortSummary  new summary to save
     * @param cohortSettings new settings to save
     */
    public Flux<Void> updateCohort(final CohortSummary cohortSummary, final CohortSettings cohortSettings) {
        checkNotNull(cohortSummary);
        checkNotNull(cohortSettings);

        cohortSettings.setCohortId(cohortSummary.getId());
        return Flux.merge(cohortGateway.update(cohortSummary), cohortSettingsGateway.update(cohortSettings));
    }

    /**
     * Archive a cohort.
     *
     * @param cohortId the cohort id to archive
     * @return a timebased UUID representing the set finishedDate
     */
    public Mono<UUID> archive(final UUID cohortId) {
        final UUID finishedDate = UUIDs.timeBased();
        return cohortGateway.setFinish(cohortId, finishedDate)
                .then(Mono.just(finishedDate));
    }

    /**
     * Unarchive a cohort. Call the setFinish gateway with a <code>null</code> finished date
     *
     * @param cohortId the cohort to unarchive
     */
    public Flux<Void> unarchive(UUID cohortId) {
        return cohortGateway.setFinish(cohortId, null);
    }

    /**
     * Find the id of all the activities in a cohort
     *
     * @param cohortId the cohort to find the activity ids for
     * @return a flux of activity ids
     */
    public Flux<UUID> findCohortActivities(final UUID cohortId) {
        Flux<UUID> deploymentIds = deploymentGateway.findByCohort(cohortId);

        return deploymentIds
                .flatMap(deploymentId -> deploymentGateway.findLatest(deploymentId)
                        .map(DeployedActivity::getActivityId));
    }

    /**
     * Fetch cohort product id from cohort settings
     *
     * @param cohortId the id of the cohort which settings should be fetched
     */
    public Mono<String> fetchCohortProductId(final UUID cohortId) {
        return cohortSettingsGateway.findCohortSettings(cohortId)
                .map(getProductId());
    }

    /**
     * Create LTI consumer key for this cohort. The method will try to find a global LTI configuration tied to the
     * workspace to associate the consumer key to. When a configuration is not found a new one is provisioned
     *
     * @param cohortSummary the cohort summary to configure the lti consumer key for
     * @param oauthConsumerKey the consumer key
     * @param oauthConsumerSecret the consumer secret
     * @return a mono of cohort summary
     */
    public Mono<CohortSummary>  saveLTIConsumerKey(final CohortSummary cohortSummary, final String oauthConsumerKey,
                                                  final String oauthConsumerSecret) {
        affirmNotNull(cohortSummary, "cohortSummary is required");
        affirmArgumentNotNullOrEmpty(oauthConsumerKey, "consumer key is required");
        affirmArgumentNotNullOrEmpty(oauthConsumerSecret, "consumer secret is required");

        // try finding the LTI configuration on this workspace
        return ltIv11Gateway.findConfigurationByWorkspace(cohortSummary.getWorkspaceId())
                // when the LTI configuration is not found then, provision one
                .switchIfEmpty(Mono.defer(() -> ltIv11Gateway.persist(new LTIv11ConsumerConfiguration()
                        .setWorkspaceId(cohortSummary.getWorkspaceId())
                        .setId(UUIDs.timeBased())
                        .setComment(null))))
                // create the consumer key object and persist it
                .flatMap(ltIv11ConsumerConfiguration -> ltIv11Gateway.persist(new LTIv11ConsumerKey()
                        .setId(UUIDs.timeBased())
                        .setOauthConsumerKey(oauthConsumerKey)
                        .setOauthConsumerSecret(oauthConsumerSecret)
                        .setCohortId(cohortSummary.getId())
                        .setWorkspaceId(cohortSummary.getWorkspaceId())
                        .setConsumerConfigurationId(ltIv11ConsumerConfiguration.getId())
                        .setLogDebug(false)))
                // return the original cohort summary
                .then(Mono.just(cohortSummary));
    }

    /**
     * Find all the LTI key configured to a cohort. Keeps the backward compatibility with the frontend by
     * mapping each object to a {@link LtiConsumerCredential}
     *
     * @param summaryPayload the cohort summary to find the configured keys for
     * @return a mono with a list of consumer key or a mono with an empty list when none found
     */
    public Mono<List<LtiConsumerCredential>> fetchLtiConsumerKeys(final CohortSummaryPayload summaryPayload) {
        return fetchLtiConsumerKeys(summaryPayload.getWorkspaceId(), summaryPayload.getCohortId());
    }

    /**
     * Find all the LTI key configured by workspace id and cohort id
     *
     * @param workspaceId the workspace id
     * @param cohortId    the cohort id
     * @return a mono with a list of consumer key or a mono with an empty list when none found
     */
    public Mono<List<LtiConsumerCredential>> fetchLtiConsumerKeys(final UUID workspaceId, final UUID cohortId) {
        return ltIv11Gateway.findConsumerKey(workspaceId, cohortId)
                .map(ltIv11ConsumerKey -> new LtiConsumerCredential()
                        .setSecret(ltIv11ConsumerKey.getOauthConsumerSecret())
                        .setKey(ltIv11ConsumerKey.getOauthConsumerKey()))
                .collectList();
    }

    /**
     * Associate a cohort id to a product id
     *
     * @param productId the product id
     * @param cohortId the cohort id
     * @return void mono or empty
     */
    @Trace(async = true)
    public Mono<Void> saveProductCohortId(String productId, UUID cohortId) {
        return cohortGateway.persistIdByProduct(productId, cohortId)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the cohort id for a given product id
     *
     * @param productId the product id to find the cohort id for
     * @return cohort id mono, or empty
     */
    @Trace(async = true)
    public Mono<UUID> findIdByProduct(String productId) {
        return cohortGateway.findIdByProduct(productId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Associate a cohort instance id to a cohort template id
     *
     * @param templateCohortId the cohort template id
     * @param instanceCohortId the cohort instance id
     * @return void mono or empty
     */
    @Trace(async = true)
    public Mono<Void> saveCohortInstanceId(UUID templateCohortId, UUID instanceCohortId) {
        return cohortGateway.persistCohortInstanceByTemplate(templateCohortId, instanceCohortId)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the cohort isntance ids for a given cohort template id
     *
     * @param templateCohortId the cohort template id
     * @return flux of cohort instance ids, or empty
     */
    @Trace(async = true)
    public Flux<UUID> findCohortInstanceIds(UUID templateCohortId) {
        return cohortGateway.findCohortInstancesByTemplate(templateCohortId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Associate a cohort id to a LMS course id
     *
     * @param lmsCourseId the LMS course id
     * @param cohortId the cohort id
     * @return void mono or empty
     */
    @Trace(async = true)
    public Mono<Void> saveLmsCourseCohortId(String lmsCourseId, UUID cohortId) {
        return cohortGateway.persistIdByLmsCourse(lmsCourseId, cohortId)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the cohort id for a given product id
     *
     * @param lmsCourseId the LMS course id to find the cohort id for
     * @return cohort id mono, or empty
     */
    @Trace(async = true)
    public Mono<UUID> findIdByLmsCourse(String lmsCourseId) {
        return cohortGateway.findIdByLmsCourse(lmsCourseId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * @return the ProductId or an empty string if the ProductId is null
     */
    private Function<CohortSettings, String> getProductId() {
        return cohortSettings -> {
            if (cohortSettings.getProductId() != null) {
                return cohortSettings.getProductId();
            }
            return StringUtils.EMPTY;
        };
}
}
