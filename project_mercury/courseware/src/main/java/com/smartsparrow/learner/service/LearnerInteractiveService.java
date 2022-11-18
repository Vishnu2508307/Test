package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.DeploymentStepLog;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerInteractiveGateway;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.LearnerPathwayGateway;
import com.smartsparrow.learner.data.LearnerWalkablePathwayChildren;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishInteractiveException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class LearnerInteractiveService {

    private final LearnerInteractiveGateway learnerInteractiveGateway;
    private final LearnerPathwayGateway learnerPathwayGateway;
    private final InteractiveService interactiveService;
    private final LearnerAssetService learnerAssetService;
    private final LearnerService learnerService;
    private final PublishCompetencyDocumentService publishCompetencyDocumentService;
    private final DeploymentLogService deploymentLogService;
    private final ManualGradeService manualGradeService;
    private final LatestDeploymentChangeIdCache changeIdCache;
    private final CacheService cacheService;
    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;
    private final PluginService pluginService;
    private final LearnerSearchableDocumentService learnerSearchableDocumentService;
    private final AnnotationService annotationService;
    private final CoursewareService coursewareService;

    @Inject
    public LearnerInteractiveService(final LearnerInteractiveGateway learnerInteractiveGateway,
                                     final LearnerPathwayGateway learnerPathwayGateway,
                                     final InteractiveService interactiveService,
                                     final LearnerAssetService learnerAssetService,
                                     final LearnerService learnerService,
                                     final PublishCompetencyDocumentService publishCompetencyDocumentService,
                                     final DeploymentLogService deploymentLogService,
                                     final ManualGradeService manualGradeService,
                                     final LatestDeploymentChangeIdCache changeIdCache,
                                     final CacheService cacheService,
                                     final CoursewareElementMetaInformationService coursewareElementMetaInformationService,
                                     final PluginService pluginService,
                                     final LearnerSearchableDocumentService learnerSearchableDocumentService,
                                     final AnnotationService annotationService,
                                     final CoursewareService coursewareService) {
        this.learnerInteractiveGateway = learnerInteractiveGateway;
        this.learnerPathwayGateway = learnerPathwayGateway;
        this.interactiveService = interactiveService;
        this.learnerAssetService = learnerAssetService;
        this.learnerService = learnerService;
        this.publishCompetencyDocumentService = publishCompetencyDocumentService;
        this.deploymentLogService = deploymentLogService;
        this.manualGradeService = manualGradeService;
        this.changeIdCache = changeIdCache;
        this.cacheService = cacheService;
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
        this.pluginService = pluginService;
        this.learnerSearchableDocumentService = learnerSearchableDocumentService;
        this.annotationService = annotationService;
        this.coursewareService = coursewareService;
    }

    /**
     * Publish an interactive for a parent pathway. Find the interactive data, build a learner interactive from it,
     * persist the learner interactive as well as the parent/child relationship with the parent pathway.
     *
     * @param parentPathwayId the interactive parent pathway id
     * @param interactiveId the interactive to deploy
     * @param deployment the deployment to deploy the learner interactive to
     * @return a mono of learner interactive
     * @throws PublishInteractiveException when either:
     * <br> parentPathwayId is <code>null</code>
     * <br> interactiveId is <code>null</code>
     * <br> deployment is <code>null</code>
     * <br> interactive is not found
     */
    public Mono<LearnerInteractive> publish(final UUID parentPathwayId, final UUID interactiveId, final DeployedActivity deployment,
                                            boolean lockPluginVersionEnabled) {

        try {
            checkArgument(parentPathwayId != null, "parentPathwayId is required");
            checkArgument(interactiveId != null, "interactiveId is required");
            checkArgument(deployment != null, "deployment is required");
        } catch (IllegalArgumentException e) {
            throw new PublishInteractiveException(interactiveId, e.getMessage());
        }

        Mono<Interactive> interactiveMono = interactiveService.findById(interactiveId);
        Mono<InteractiveConfig> interactiveConfigMono = interactiveService.findLatestConfig(interactiveId)
                .defaultIfEmpty(new InteractiveConfig());

        return Mono.zip(interactiveMono, interactiveConfigMono)
                .doOnError(InteractiveNotFoundException.class, ex -> {
                    deploymentLogService.logFailedStep(deployment, interactiveId, CoursewareElementType.INTERACTIVE,
                            "[learnerInteractiveService] " + Arrays.toString(ex.getStackTrace()))
                            .subscribe();
                    throw new PublishInteractiveException(interactiveId, ex.getMessage());
                })
                .map(tuple -> {
                    LearnerInteractive learnerInteractive = new LearnerInteractive()
                            .setEvaluationMode(tuple.getT1().getEvaluationMode())
                            .setId(interactiveId)
                            .setDeploymentId(deployment.getId())
                            .setChangeId(deployment.getChangeId())
                            .setPluginId(tuple.getT1().getPluginId())
                            .setPluginVersionExpr(pluginService.resolvePluginVersion(tuple.getT1().getPluginId(),
                                    tuple.getT1().getPluginVersionExpr(), lockPluginVersionEnabled))
                            .setConfig(tuple.getT2().getConfig())
                            .setStudentScopeURN(tuple.getT1().getStudentScopeURN());

                    return publishInteractive(learnerInteractive, parentPathwayId, deployment, lockPluginVersionEnabled);
                })
                .flatMap(one->one);
    }

    /**
     * Persist the learner interactive and its parent/child relationship with the parent pathway.
     *
     * @param learnerInteractive the learner interactive to persist
     * @param parentPathwayId the parent pathway to save the parent/child relationship with
     * @param deployment the deployment
     * @return a mono of learner interactive
     */
    private Mono<LearnerInteractive> publishInteractive(LearnerInteractive learnerInteractive, UUID parentPathwayId,
                                                        Deployment deployment, boolean lockPluginVersionEnabled) {
        final UUID deploymentId = learnerInteractive.getDeploymentId();
        final UUID changeId = learnerInteractive.getChangeId();
        final UUID learnerInteractiveId = learnerInteractive.getId();

        //find root element from the path
        final UUID rootElementId = coursewareService.getRootElementId(learnerInteractive.getId(), learnerInteractive.getElementType()).block();

        return learnerInteractiveGateway.persist(learnerInteractive, deployment)
                .thenMany(learnerInteractiveGateway.persistParentPathway(new LearnerParentElement()
                        .setElementId(learnerInteractive.getId())
                        .setParentId(parentPathwayId)
                        .setChangeId(changeId)
                        .setDeploymentId(deploymentId)))
                .thenMany(learnerPathwayGateway.persistChildWalkable(new LearnerWalkablePathwayChildren()
                        .setPathwayId(parentPathwayId)
                        .setDeploymentId(deploymentId)
                        .setChangeId(changeId)
                        .setWalkableIds(Lists.newArrayList(learnerInteractiveId))
                        .setWalkableTypes(new HashMap<UUID, String>() {{put(learnerInteractiveId, CoursewareElementType.INTERACTIVE.name());}})))
                .then(logProgress(deployment, learnerInteractiveId, "finished persisting parent/child relationship"))
                .thenMany(learnerAssetService.publishAssetsFor(deployment, learnerInteractiveId, CoursewareElementType.INTERACTIVE))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing assets"))
                .thenMany(learnerAssetService.publishMathAssetsFor(deployment, learnerInteractiveId, CoursewareElementType.INTERACTIVE))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing math assets"))
                .thenMany(learnerService.replicateRegisteredStudentScopeElements(learnerInteractive, deployment, lockPluginVersionEnabled))
                .then(logProgress(deployment, learnerInteractiveId, "finished replicating student scope registry"))
                .thenMany(learnerService.publishDocumentItemLinks(learnerInteractiveId, CoursewareElementType.INTERACTIVE, deployment))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing document item links"))
                .thenMany(learnerService.publishConfigurationFields(deployment, learnerInteractiveId))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing configuration fields"))
                .thenMany(publishCompetencyDocumentService.publishDocumentsFor(learnerInteractive))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing documents"))
                .thenMany(manualGradeService.publishManualComponentByWalkable(learnerInteractiveId, deployment))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing manual component by interactive"))
                .thenMany(coursewareElementMetaInformationService.publish(learnerInteractiveId, deployment))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing meta information"))
                .thenMany(learnerSearchableDocumentService.publishSearchableDocuments(learnerInteractive, deployment.getCohortId()))
                .then(logProgress(deployment, learnerInteractiveId, "finished mapping interactive searchable fields"))
                .thenMany(annotationService.publishAnnotationMotivations(rootElementId, learnerInteractive.getId(), deployment.getId(), deployment.getChangeId()))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing annotation motivations"))
                .then(logProgress(deployment, learnerInteractiveId, "finished publishing evaluable"))
                .then(Mono.just(learnerInteractive));
    }

    /**
     * Find a deployed interactive
     * @param interactiveId the interactive id
     * @param deploymentId the deployment id
     * @return mono of LearnerInteractive, empty mono if interactive not found
     */
    @Trace(async = true)
    public Mono<LearnerInteractive> findInteractive(UUID interactiveId, UUID deploymentId) {
        checkArgument(interactiveId != null, "missing interactiveId");
        checkArgument(deploymentId != null, "missing deploymentId");

        UUID changeId = changeIdCache.get(deploymentId);
        String cacheName = String.format("learner:interactive:/%s/%s/%s", deploymentId, changeId, interactiveId);

        Mono<LearnerInteractive> latestDeployed = learnerInteractiveGateway
                .findLatestDeployed(interactiveId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());

        return cacheService.computeIfAbsent(cacheName, LearnerInteractive.class, latestDeployed, 365, TimeUnit.DAYS);
    }

    @Trace(async = true)
    public Mono<UUID> findParentPathwayId(final UUID interactiveId, final UUID deploymentId) {

        UUID changeId = changeIdCache.get(deploymentId);
        String cacheName = String.format("learner:interactive:parentPathway:/%s/%s/%s", deploymentId, changeId, interactiveId);

        Mono<UUID> parentPathway = learnerInteractiveGateway.findParentPathway(interactiveId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return cacheService.computeIfAbsent(cacheName, UUID.class, parentPathway, 365, TimeUnit.DAYS)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ParentPathwayNotFoundException(interactiveId);
                });
    }

    private Mono<DeploymentStepLog> logProgress(final Deployment deployment, final UUID interactiveId, final String message) {
        return deploymentLogService.logProgressStep(deployment, interactiveId, CoursewareElementType.INTERACTIVE,
                String.format("[learnerInteractiveService] %s", message));
    }

    /**
     * Find the children component for a deployed interactive based on the latest change id
     *
     * @param interactiveId the interactive to search the children component for
     * @param deploymentId the deployment associated with the interactive
     * @return a mono list of uuid representing the children component ids
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildrenComponent(UUID interactiveId, UUID deploymentId) {
        return learnerInteractiveGateway.findChildrenComponent(interactiveId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
