package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.ActivityChangeNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.exception.NotFoundException;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerFeedback;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.lang.PublishComponentException;
import com.smartsparrow.learner.lang.PublishCoursewareException;
import com.smartsparrow.learner.lang.PublishPathwayException;
import com.smartsparrow.learner.lang.PublishScenarioException;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DeploymentService {

    private final DeploymentGateway deploymentGateway;
    private final DeploymentLogService deploymentLogService;
    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final LearnerActivityService learnerActivityService;
    private final LearnerScenarioService learnerScenarioService;
    private final LearnerComponentService learnerComponentService;
    private final LearnerPathwayService learnerPathwayService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final LearnerFeedbackService learnerFeedbackService;
    private final LatestDeploymentChangeIdCache changeIdCache;
    private final CacheService cacheService;
    private final LearnerSearchableDocumentService learnerSearchableDocumentService;


    @Inject
    public DeploymentService(DeploymentGateway deploymentGateway,
                             DeploymentLogService deploymentLogService,
                             ActivityService activityService,
                             PathwayService pathwayService,
                             LearnerActivityService learnerActivityService,
                             LearnerScenarioService learnerScenarioService,
                             LearnerComponentService learnerComponentService,
                             LearnerPathwayService learnerPathwayService,
                             LearnerInteractiveService learnerInteractiveService,
                             LearnerFeedbackService learnerFeedbackService,
                             LatestDeploymentChangeIdCache changeIdCache,
                             CacheService cacheService,
                             LearnerSearchableDocumentService learnerSearchableDocumentService) {
        this.deploymentGateway = deploymentGateway;
        this.deploymentLogService = deploymentLogService;
        this.activityService = activityService;
        this.pathwayService = pathwayService;
        this.learnerActivityService = learnerActivityService;
        this.learnerScenarioService = learnerScenarioService;
        this.learnerComponentService = learnerComponentService;
        this.learnerPathwayService = learnerPathwayService;
        this.learnerInteractiveService = learnerInteractiveService;
        this.learnerFeedbackService = learnerFeedbackService;
        this.changeIdCache = changeIdCache;
        this.cacheService = cacheService;
        this.learnerSearchableDocumentService = learnerSearchableDocumentService;
    }

    /**
     * Find a deployment based on the latest change id
     *
     * @param activityId the activity id to find the deployment for
     * @param deploymentId the deployment to find
     * @return a mono of deployment
     * @throws DeploymentNotFoundException if the deployment is not found
     */
    public Mono<DeployedActivity> findLatestDeployment(UUID activityId, UUID deploymentId) {
        return deploymentGateway.findLatest(deploymentId, activityId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new DeploymentNotFoundException(activityId, deploymentId);
                });
    }

    /**
     * Find a deployment based on the latest change id
     *
     * @param activityId the activity id to find the deployment for
     * @param deploymentId the deployment to find
     * @return a mono of deployment or an empty mono
     */
    public Mono<DeployedActivity> findLatestDeploymentOrEmpty(UUID activityId, UUID deploymentId) {
        return deploymentGateway.findLatest(deploymentId, activityId);
    }

    /**
     * Find a deployment by id
     * @param deploymentId the deployment to find
     * @return a mono of deployment
     * @throws DeploymentNotFoundException if the deployment is not found
     */
    @Trace(async = true)
    public Mono<DeployedActivity> findDeployment(UUID deploymentId) {
        checkArgument(deploymentId != null, "deploymentId is required");

        UUID changeId = changeIdCache.get(deploymentId);
        String cacheName = String.format("learner:deployment:/%s/%s", deploymentId, changeId);

        Mono<DeployedActivity> latestDeployment = deploymentGateway.findLatest(deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return cacheService.computeIfAbsent(cacheName, DeployedActivity.class, latestDeployment, 365, TimeUnit.DAYS)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new DeploymentNotFoundException(null, deploymentId);
                });
    }

    public Mono<UUID> findLatestChangeId(UUID deploymentId) {
        checkArgument(deploymentId != null, "deploymentId is required");
        return deploymentGateway.findLatestChangeId(deploymentId);
    }

    /**
     * Find the most recent changeIds for a deployment, ordered by most recent first
     *
     * @param deploymentId the deployment id
     * @param limit limit of how many changeIds to return
     */
    public Flux<UUID> findLatestChangeIds(UUID deploymentId, int limit) {
        checkArgument(deploymentId != null, "deploymentId is required");
        return deploymentGateway.findLatestChangeIds(deploymentId, limit);
    }

    /**
     * Find deployments for a cohort
     * @param cohortId the cohort id
     * @return Flux of deployments for the given cohort, empty flux if no deployments found
     */
    @Trace(async = true)
    public Flux<DeployedActivity> findDeployments(UUID cohortId) {
        checkArgument(cohortId != null, "cohortId is required");

        return deploymentGateway.findByCohort(cohortId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(deploymentGateway::findLatest);
    }

    /**
     * Find deployment ids for a cohort
     * @param cohortId the cohort id
     * @return Flux of deployments for the given cohort, empty flux if no deployments found
     */
    @Trace(async = true)
    public Flux<UUID> findDeploymentIds(UUID cohortId) {
        checkArgument(cohortId != null, "cohortId is required");

        return deploymentGateway.findByCohort(cohortId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Publish an activity to the learner environment. When the <b>deploymentId</b> is not <code>null</code> the method
     * updates the published activity with a latest change id. When the <b>deploymentId</b> is <code>null</code> the
     * method creates a new deployment and publishes the activity for the first time.
     *
     * @param activityId the id of the activity to deploy
     * @param cohortId the cohort to add the deployment to
     * @param deploymentId the deployment to update with the latest activity change
     * @return a mono of deployment
     * @throws IllegalArgumentException when the activityId is not supplied
     * @throws PublishCoursewareException when failing to deploy any of the courseware elements
     * @throws StackOverflowError when there is a circular relationship in the courseware structure
     * TODO should it validate if it is a top level item as well? (I think so)
     */
    public Mono<DeployedActivity> deploy(UUID activityId, UUID cohortId, @Nullable UUID deploymentId,
                                         boolean lockPluginVersionEnabled) {

        checkArgument(activityId != null, "activityId is required");
        checkArgument(cohortId != null, "cohortId is required");

        if (deploymentId == null) {
            return publish(activityId, cohortId, UUIDs.timeBased(), lockPluginVersionEnabled);
        }

        return publish(activityId, cohortId, deploymentId, lockPluginVersionEnabled);
    }

    /**
     * Publish a new activity for the first time. A new {@link DeployedActivity} is created and saved to the db then a
     * publish method is invoked that will take care of publishing the top activity item to the created deployment
     *
     * @param activityId the activity to deploy
     * @param deploymentId the deployment id to deploy the activity to
     * @return a mono of deployment
     * @throws PublishCoursewareException when failing to deploy any of the courseware element in the structure or the
     * activity change is not found
     */
    private Mono<DeployedActivity> publish(final UUID activityId, UUID cohortId, UUID deploymentId,
                                           boolean lockPluginVersionEnabled) {
        return activityService.fetchLatestChange(activityId)
                .doOnError(ActivityChangeNotFoundException.class, ex -> {
                    throw new PublishCoursewareException(ex.getMessage());
                })
                .map(activityChange -> new DeployedActivity()
                        .setId(deploymentId)
                        .setCohortId(cohortId)
                        .setChangeId(activityChange.getChangeId())
                        .setActivityId(activityId))
                .flatMap(deployment -> deploymentGateway.persist(deployment)
                        .singleOrEmpty()
                        .thenReturn(deployment))
                .flatMap(deployment -> deploymentLogService.logStartedStep(deployment, activityId, CoursewareElementType.ACTIVITY,
                        "[deploymentService] Publishing ROOT activity")
                        .flatMap(loggedStep -> publishActivity(activityId, deployment, null, lockPluginVersionEnabled))
                        .doOnError(throwable -> {
                            deploymentLogService.logFailedStep(deployment, activityId, CoursewareElementType.ACTIVITY,
                                    "[deploymentService] ROOT activity " + Arrays.toString(throwable.getStackTrace()))
                                    .subscribe();
                            throw Exceptions.propagate(throwable);
                        })
                        .flatMap(published -> learnerSearchableDocumentService.pruneIndex(deployment.getId())
                                .thenReturn(published))
                        .flatMap(published -> deploymentLogService.logCompletedStep(deployment, activityId, CoursewareElementType.ACTIVITY,
                                "[deploymentService] Finished publishing ROOT activity"))
                        .flatMap(published -> cacheService.clearIfPresent("*" + deploymentId.toString() + "*"))
                        .thenReturn(deployment));
    }

    /**
     * Publish the activity to a deployment.
     *
     * @param deployment the deployment to deploy the activity to
     * @return a mono of void
     */
    private Mono<DeployedActivity> publishActivity(final UUID activityId,
                                                   final DeployedActivity deployment,
                                                   @Nullable UUID parentPathwayId,
                                                   boolean lockPluginVersionEnabled) {
        return learnerActivityService.publish(activityId, deployment, parentPathwayId, lockPluginVersionEnabled)
                .thenMany(publishScenariosFor(activityId, deployment))
                .thenMany(publishComponentsFor(activityId, CoursewareElementType.ACTIVITY, deployment, lockPluginVersionEnabled))
                .thenMany(publishPathwaysFor(activityId, deployment, lockPluginVersionEnabled))
                .then(Mono.just(deployment))
                .doOnError(throwable -> {
                    deploymentLogService.logFailedStep(deployment, activityId, CoursewareElementType.ACTIVITY,
                                                       "[deploymentService] " + Arrays.toString(throwable.getStackTrace()))
                            .subscribe();
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Publish the components for a parent courseware element.
     *
     * @param parentId the parent element to deploy the components for
     * @param parentType the parent element type
     * @param deployment the deployment to deploy the components to
     * @return a flux of learner components
     * @throws PublishComponentException when failing to deploy the components
     */
    private Flux<LearnerComponent> publishComponentsFor(UUID parentId,
                                                        CoursewareElementType parentType,
                                                        DeployedActivity deployment,
                                                        boolean lockPluginVersionEnabled) {
        return learnerComponentService.publish(parentId, deployment, parentType, lockPluginVersionEnabled);
    }

    /**
     * Publish the pathways for a parent activity along with each walkable pathway child.
     *
     * @param activityId the activity to deploy the pathway for
     * @param deployment the deployment to deploy the pathway to
     * @return a flux of learner pathway
     * @throws PublishPathwayException when failing to deploy the pathway
     */
    private Flux<LearnerPathway> publishPathwaysFor(final UUID activityId,
                                                    final DeployedActivity deployment,
                                                    boolean lockPluginVersionEnabled) {
        return learnerPathwayService.publish(activityId, deployment)
                .concatMap(learnerPathway -> pathwayService.getOrderedWalkableChildren(learnerPathway.getId())
                        .flatMapIterable(pathway -> pathway)
                        .concatMap(walkableChild -> {
                            switch (walkableChild.getElementType()) {
                                case ACTIVITY:
                                    return publishActivity(walkableChild.getElementId(),
                                                           deployment,
                                                           learnerPathway.getId(),
                                                           lockPluginVersionEnabled);
                                case INTERACTIVE:
                                    return publishInteractiveFor(learnerPathway.getId(),
                                                                 walkableChild.getElementId(),
                                                                 deployment,
                                                                 lockPluginVersionEnabled);
                                default:
                                    throw new UnsupportedOperationException(
                                            String.format("Broken pathway %s. Pathway can not have %s as a child",
                                                          learnerPathway.getId(), walkableChild));
                            }
                        })
                        .doOnError(throwable -> {
                            deploymentLogService.logFailedStep(deployment,
                                                               learnerPathway.getId(),
                                                               CoursewareElementType.PATHWAY,
                                                               "[deploymentService] " + Arrays.toString(throwable.getStackTrace()))
                                    .subscribe();
                            throw Exceptions.propagate(throwable);
                        })
                        .then(Mono.just(learnerPathway)));
    }

    /**
     * Publish the scenarios for a courseware element
     *
     * @param parentId the parent id to deploy the scenarios for
     * @param deployment the deployment to deploy the scenarios to
     * @return a mono of void
     * @throws PublishScenarioException when failing to deploy the scenario
     */
    private Flux<LearnerScenario> publishScenariosFor(UUID parentId, DeployedActivity deployment) {
        return learnerScenarioService.publish(parentId, deployment);
    }

    /**
     * Publish an interactive for a parent pathway. A learner interactive is published as well as all its component/feedback
     * children.
     *
     * @param parentPathwayId the interactive parent pathway id
     * @param interactiveId the interactive to deploy
     * @param deployment the deployment to deploy the interactive to
     * @return a mono of learner interactive
     */
    private Mono<LearnerInteractive> publishInteractiveFor(UUID parentPathwayId,
                                                           UUID interactiveId,
                                                           DeployedActivity deployment,
                                                           boolean lockPluginVersionEnabled) {
        return learnerInteractiveService.publish(parentPathwayId, interactiveId, deployment, lockPluginVersionEnabled)
                .flatMap(learnerInteractive -> publishComponentsFor(learnerInteractive.getId(),
                                                                    CoursewareElementType.INTERACTIVE,
                                                                    deployment,
                                                                    lockPluginVersionEnabled)
                        .thenMany(publishFeedbacksFor(learnerInteractive.getId(), deployment, lockPluginVersionEnabled))
                        .thenMany(publishScenariosFor(interactiveId, deployment))
                        .then(Mono.just(learnerInteractive)))
                .doOnError(throwable -> {
                    deploymentLogService.logFailedStep(deployment, interactiveId, CoursewareElementType.INTERACTIVE,
                                                       "[deploymentService] " + Arrays.toString(throwable.getStackTrace()))
                            .subscribe();
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Publish the feedback for an interactive.
     *
     * @param interactiveId the interactive id to deploy the feedback for
     * @param deployment the deployment to deploy the feedback to
     * @return a flux of learner feedback
     */
    private Flux<LearnerFeedback> publishFeedbacksFor(UUID interactiveId,
                                                      DeployedActivity deployment,
                                                      boolean lockPluginVersionEnabled) {
        return learnerFeedbackService.publish(interactiveId, deployment, lockPluginVersionEnabled);
    }

    /**
     * Associate the deployment id to a given product id
     *
     * @param productId the product id
     * @param deploymentId the deployment id
     * @return void mono or empty
     */
    @Trace(async = true)
    public Mono<Void> saveProductDeploymentId(String productId, UUID deploymentId) {
        return deploymentGateway.persist(productId, deploymentId)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the deployment id for a given product id
     *
     * @param productId the product id to find the deployment id for
     * @throws NotFoundException if the product id is not found
     */
    @Trace(async = true)
    public Mono<UUID> findProductDeploymentId(String productId) {
        return deploymentGateway.findDeploymentId(productId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Associate the cohort id to a given deployment id
     *
     * @param cohortId the cohort id
     * @param deploymentId the deployment id
     * @return void mono or empty
     */
    @Trace(async = true)
    public Mono<Void> saveDeploymentCohortId(UUID cohortId, UUID deploymentId) {
        return deploymentGateway.persist(cohortId, deploymentId)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
