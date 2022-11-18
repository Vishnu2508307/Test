package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerActivityGateway;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.LearnerPathwayGateway;
import com.smartsparrow.learner.data.LearnerWalkablePathwayChildren;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.ThemeConfig;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;
import com.smartsparrow.learner.lang.PublishActivityException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerActivityService {

    private final LearnerActivityGateway learnerActivityGateway;
    private final LearnerPathwayGateway learnerPathwayGateway;
    private final ActivityService activityService;
    private final LearnerAssetService learnerAssetService;
    private final LearnerService learnerService;
    private final PublishCompetencyDocumentService publishCompetencyDocumentService;
    private final DeploymentLogService deploymentLogService;
    private final ManualGradeService manualGradeService;
    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;
    private final LatestDeploymentChangeIdCache changeIdCache;
    private final CacheService cacheService;
    private final PluginService pluginService;
    private final LearnerSearchableDocumentService learnerSearchableDocumentService;
    private final AnnotationService annotationService;
    private final CoursewareService coursewareService;
    private final ThemeService themeService;
    private final LearnerThemeService learnerThemeService;

    @Inject
    public LearnerActivityService(final LearnerActivityGateway learnerActivityGateway,
                                  final LearnerPathwayGateway learnerPathwayGateway,
                                  final ActivityService activityService,
                                  final LearnerAssetService learnerAssetService,
                                  final LearnerService learnerService,
                                  final PublishCompetencyDocumentService publishCompetencyDocumentService,
                                  final DeploymentLogService deploymentLogService,
                                  final ManualGradeService manualGradeService,
                                  final CoursewareElementMetaInformationService coursewareElementMetaInformationService,
                                  final LatestDeploymentChangeIdCache changeIdCache,
                                  final CacheService cacheService,
                                  final PluginService pluginService,
                                  final LearnerSearchableDocumentService learnerSearchableDocumentService,
                                  final AnnotationService annotationService,
                                  final CoursewareService coursewareService,
                                  final ThemeService themeService,
                                  final LearnerThemeService learnerThemeService) {
        this.learnerActivityGateway = learnerActivityGateway;
        this.learnerPathwayGateway = learnerPathwayGateway;
        this.activityService = activityService;
        this.learnerAssetService = learnerAssetService;
        this.learnerService = learnerService;
        this.publishCompetencyDocumentService = publishCompetencyDocumentService;
        this.deploymentLogService = deploymentLogService;
        this.manualGradeService = manualGradeService;
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
        this.changeIdCache = changeIdCache;
        this.cacheService = cacheService;
        this.pluginService = pluginService;
        this.learnerSearchableDocumentService = learnerSearchableDocumentService;
        this.annotationService = annotationService;
        this.coursewareService = coursewareService;
        this.themeService = themeService;
        this.learnerThemeService = learnerThemeService;
    }

    /**
     * Find all the activity objects that are necessary to build a learner activity. The learner activity is then build
     * and published. If the parent pathway argument supplied is not <code>null</code>, the method persist the
     * parent/child relationship between the activity and its parent pathway.
     *
     * @param activityId the activity id to deploy
     * @param deployment the deployment to deploy the learner activity to
     * @param parentPathwayId the activity parent pathway id. Argument can be <code>null</code> for top level activities
     * @return a mono of learner activity
     * @throws PublishActivityException when either:
     * <br> activityId is <code>null</code>
     * <br> deployment is <code>null</code>
     * <br> activity is not found
     */
    public Mono<LearnerActivity> publish(final UUID activityId,
                                         final DeployedActivity deployment,
                                         @Nullable final UUID parentPathwayId,
                                         final boolean lockPluginVersionEnabled) {

        try {
            checkArgument(activityId != null, "activityId is required");
            checkArgument(deployment != null, "deployment is required");
        } catch (IllegalArgumentException e) {
            throw new PublishActivityException(activityId, e.getMessage());
        }

        Mono<Activity> activityMono = activityService.findById(activityId);
        Mono<ActivityConfig> activityConfigMono = activityService.findLatestConfig(activityId)
                .defaultIfEmpty(new ActivityConfig());
        Mono<ThemeConfig> themeConfigMono = getTheme(activityId);

        Mono<LearnerActivity> learnerActivityMono = Mono.zip(activityMono, Mono.just(deployment), activityConfigMono, themeConfigMono)
                .doOnError(ActivityNotFoundException.class, ex -> {
                    deploymentLogService.logFailedStep(deployment, activityId, CoursewareElementType.ACTIVITY, ex.getMessage())
                            .subscribe();
                    throw new PublishActivityException(activityId, ex.getMessage());
                })
                .flatMap(tuple -> learnerAssetService.publishAssetsFor(deployment, activityId, CoursewareElementType.ACTIVITY)
                        .then(deploymentLogService.logProgressStep(deployment, activityId, CoursewareElementType.ACTIVITY,
                                "[learnerActivityService] finished publishing assets for activity"))
                        .thenMany(learnerAssetService.publishMathAssetsFor(deployment, activityId, CoursewareElementType.ACTIVITY))
                        // save selected theme info for an activity id
                        .then(learnerThemeService.saveSelectedThemeByElement(activityId))
                        .then(deploymentLogService.logProgressStep(deployment, activityId, CoursewareElementType.ACTIVITY,
                                "[learnerActivityService] finished publishing math assets for activity"))
                        .then(publish(
                                tuple.getT1(),
                                tuple.getT2(),
                                tuple.getT3().getConfig(),
                                tuple.getT4().getConfig(),
                                lockPluginVersionEnabled)
                        ));

        if (parentPathwayId != null) {
            // make spotbugs reframe this call.
            final UUID _parentPathwayId = parentPathwayId;
            return learnerActivityMono
                    .flatMap(learnerActivity -> deploymentLogService.logProgressStep(deployment, activityId, CoursewareElementType.ACTIVITY,
                            "[learnerActivityService] persisting parent/child relationship with pathway")
                            .thenMany(learnerActivityGateway.persistParentPathway(buildParent(activityId, deployment, _parentPathwayId)))
                            .thenMany(learnerPathwayGateway.persistChildWalkable(buildChild(activityId, deployment, _parentPathwayId)))
                            .then(Mono.just(learnerActivity)));
        }

        return learnerActivityMono;
    }

    /**
     * Fetch selected themes for an element, if not found then fetch the default theme
     * if both not found, then return empty theme config object
     * @param activityId, the activity id
     * @return mono of theme config object
     */
    private Mono<ThemeConfig> getTheme(final UUID activityId) {
        Mono<ThemeVariant> defaultThemeVariantMono = themeService.fetchThemeByElementId(activityId)
                .flatMap(themePayload -> themeService.findThemeVariantByState(themePayload.getId(), ThemeState.DEFAULT))
                .defaultIfEmpty(new ThemeVariant());

        Mono<ActivityTheme> defaultActivityThemeMono = activityService.getLatestActivityThemeByActivityId(activityId)
                .defaultIfEmpty(new ActivityTheme());
        return Mono.zip(defaultThemeVariantMono, defaultActivityThemeMono)
                .map(tuple2 -> {
                    ThemeConfig themeConfig = new ThemeConfig();
                    ThemeVariant selectedTheme = tuple2.getT1();
                    ActivityTheme defaultTheme = tuple2.getT2();

                    if (selectedTheme != null && selectedTheme.getConfig() != null) {
                        return themeConfig.setConfig(selectedTheme.getConfig());
                    } else if (defaultTheme != null && defaultTheme.getConfig() != null) {
                        return themeConfig.setConfig(defaultTheme.getConfig());
                    } else {
                        return themeConfig;
                    }
                });
    }

    /**
     * Creates a new learner activity from an existing author activity and persist it to the database.
     *
     * @param activity the activity to create the learner activity from
     * @param deployment the deployment associated with the learner activity
     * @param config the activity config
     * @param themeConfig the activity theme config
     * @return a mono of learner activity
     */
    private Mono<LearnerActivity> publish(final Activity activity,
                                          final DeployedActivity deployment,
                                          final String config,
                                          final String themeConfig,
                                          final boolean lockPluginVersionEnabled) {
        LearnerActivity learnerActivity = new LearnerActivity()
                .setEvaluationMode(activity.getEvaluationMode())
                .setId(activity.getId())
                .setCreatorId(activity.getCreatorId())
                .setPluginId(activity.getPluginId())
                .setPluginVersionExpr(pluginService.resolvePluginVersion(activity.getPluginId(),
                        activity.getPluginVersionExpr(), lockPluginVersionEnabled))
                .setConfig(config)
                .setTheme(themeConfig)
                .setChangeId(deployment.getChangeId())
                .setDeploymentId(deployment.getId())
                .setStudentScopeURN(activity.getStudentScopeURN());

        //find root element from the path
        final UUID rootElementId = coursewareService.getRootElementId(learnerActivity.getId(), learnerActivity.getElementType()).block();

        return learnerActivityGateway.persist(learnerActivity, deployment)
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished persisting learner activity"))
                .thenMany(learnerService.replicateRegisteredStudentScopeElements(learnerActivity, deployment, lockPluginVersionEnabled))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished registering student scope elements"))
                .thenMany(learnerService.publishDocumentItemLinks(learnerActivity.getId(), CoursewareElementType.ACTIVITY, deployment))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished publishing document item links"))
                .thenMany(learnerService.publishConfigurationFields(deployment, learnerActivity.getId()))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished publishing configuration fields"))
                .thenMany(publishCompetencyDocumentService.publishDocumentsFor(learnerActivity))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished publishing documents"))
                .thenMany(manualGradeService.publishManualComponentByWalkable(learnerActivity.getId(), deployment))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished publishing manual component by activity"))
                .thenMany(coursewareElementMetaInformationService.publish(learnerActivity.getId(), deployment))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished publishing meta information"))
                .thenMany(learnerSearchableDocumentService.publishSearchableDocuments(learnerActivity, deployment.getCohortId()))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished mapping activity searchable fields"))
                .thenMany(annotationService.publishAnnotationMotivations(rootElementId, learnerActivity.getId(), deployment.getId(), deployment.getChangeId()))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished publishing annotation motivations"))
                .then(deploymentLogService.logProgressStep(deployment, activity.getId(), CoursewareElementType.ACTIVITY,
                        "[learnerActivityService] finished publishing evaluable"))
                .then(Mono.just(learnerActivity));
    }

    /**
     * Build a learner parent pathway element.
     *
     * @param activityId the activity id
     * @param deployment the deployment the activity is published to
     * @param parentPathwayId the activity parent pathway
     * @return a learner parent element object
     */
    private LearnerParentElement buildParent(final UUID activityId, final DeployedActivity deployment, @Nonnull final UUID parentPathwayId) {
        return new LearnerParentElement()
                .setElementId(activityId)
                .setParentId(parentPathwayId)
                .setDeploymentId(deployment.getId())
                .setChangeId(deployment.getChangeId());
    }

    /**
     * Build a walkable pathway child that represent the activity child relationship with its parent pathway.
     *
     * @param activityId the activity id
     * @param deployment the deployment the activity is published to
     * @param parentPathwayId the activity parent pathway id
     * @return an object representing the child relationship
     */
    private LearnerWalkablePathwayChildren buildChild(final UUID activityId, final DeployedActivity deployment, @Nonnull final UUID parentPathwayId) {
        return new LearnerWalkablePathwayChildren()
                .setPathwayId(parentPathwayId)
                .setWalkableIds(Lists.newArrayList(activityId))
                .setWalkableTypes(new HashMap<UUID, String>(){{put(activityId, CoursewareElementType.ACTIVITY.name());}})
                .setDeploymentId(deployment.getId())
                .setChangeId(deployment.getChangeId());
    }

    /**
     * Find an activity by id and deployment id
     * @param activityId the activity id
     * @param deploymentId the deployment id
     * @return a mono with LearnerActivity, empty mono if activity not found
     */
    @Trace(async = true)
    public Mono<LearnerActivity> findActivity(final UUID activityId, final UUID deploymentId) {
        checkArgument(activityId != null, "missing activityId");
        checkArgument(deploymentId != null, "missing deploymentId");

        UUID changeId = changeIdCache.get(deploymentId);
        String cacheName = String.format("learner:activity/%s/%s/%s", deploymentId, changeId, activityId);

        Mono<LearnerActivity> activity = learnerActivityGateway.findActivityByDeployment(activityId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return cacheService.computeIfAbsent(cacheName, LearnerActivity.class, activity, 365, TimeUnit.DAYS);
    }

    /**
     * Find child pathways for the given activity and deployment
     * @param activityId the activity id
     * @param deploymentId the deployment id
     * @return flux of LearnerPathway, empty flux if no child pathways
     */
    @Trace(async = true)
    public Flux<LearnerPathway> findChildPathways(final UUID activityId, final UUID deploymentId) {
        checkArgument(activityId != null, "missing activityId");
        checkArgument(deploymentId != null, "missing deploymentId");

        return learnerActivityGateway.findChildPathwayIds(activityId, deploymentId).flatMapIterable(list -> list)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .concatMap(pathwayId -> learnerPathwayGateway.findLatestDeployed(pathwayId, deploymentId));

    }

    /**
     * Find child pathway with the given id for the given activity and deployment
     * @param activityId the activity id
     * @param deploymentId the deployment id
     * @param pathwayId the pathway id
     * @return mono of LearnerPathway, empty mono if the pathway with the given id doesn't belong to the given activity
     */
    @Trace(async = true)
    public Mono<LearnerPathway> findChildPathway(final UUID activityId, final UUID deploymentId, final UUID pathwayId) {
        checkArgument(activityId != null, "missing activityId");
        checkArgument(deploymentId != null, "missing deploymentId");
        checkArgument(pathwayId != null, "missing pathwayId");

        return learnerActivityGateway.findChildPathwayIds(activityId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .filter(list -> list.contains(pathwayId))
                .flatMap(list -> learnerPathwayGateway.findLatestDeployed(pathwayId, deploymentId));
    }

    /**
     * Find the parent learner pathway for a learner activity
     *
     * @param activityId the activity id
     * @param deploymentId the deployment id
     * @return a mono of uuid representing the parent pathway id
     * @throws LearnerPathwayNotFoundFault when the parent pathway is not found
     */
    @Trace(async = true)
    public Mono<UUID> findParentPathwayId(final UUID activityId, final UUID deploymentId) {

        UUID changeId = changeIdCache.get(deploymentId);
        String cacheName = String.format("learner:activity:parentPathway/%s/%s/%s", deploymentId, changeId, activityId);

        Mono<UUID> parentPathwayId = learnerActivityGateway.findParentPathwayId(activityId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return cacheService.computeIfAbsent(cacheName, UUID.class, parentPathwayId, 365, TimeUnit.DAYS)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new LearnerPathwayNotFoundFault(
                            String.format("parent pathway not found for activity %s", activityId)
                    );
                });

    }

    /**
     * Find the children component ids for a deployed activity
     *
     * @param activityId the activity to search the children component for
     * @param deploymentId the deployment id associated with the activity
     * @return a mono list of uuid representing the component ids
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildComponents(UUID activityId, UUID deploymentId) {
        return learnerActivityGateway.findChildComponents(activityId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
