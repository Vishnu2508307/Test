package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.LearnerPathwayBuilder;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.learner.data.LearnerActivityGateway;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.LearnerPathwayGateway;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishPathwayException;
import com.smartsparrow.util.Walkables;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerPathwayService {

    private final LearnerPathwayGateway learnerPathwayGateway;
    private final LearnerActivityGateway learnerActivityGateway;
    private final PathwayService pathwayService;
    private final ActivityService activityService;
    private final Provider<LearnerPathwayBuilder> learnerPathwayBuilderProvider;
    private final DeploymentLogService deploymentLogService;
    private final LatestDeploymentChangeIdCache changeIdCache;
    private final CacheService cacheService;

    @Inject
    public LearnerPathwayService(LearnerPathwayGateway learnerPathwayGateway,
                                 LearnerActivityGateway learnerActivityGateway,
                                 PathwayService pathwayService,
                                 ActivityService activityService,
                                 Provider<LearnerPathwayBuilder> learnerPathwayBuilderProvider,
                                 DeploymentLogService deploymentLogService,
                                 LatestDeploymentChangeIdCache changeIdCache,
                                 CacheService cacheService) {
        this.learnerPathwayGateway = learnerPathwayGateway;
        this.learnerActivityGateway = learnerActivityGateway;
        this.pathwayService = pathwayService;
        this.activityService = activityService;
        this.learnerPathwayBuilderProvider = learnerPathwayBuilderProvider;
        this.deploymentLogService = deploymentLogService;
        this.changeIdCache = changeIdCache;
        this.cacheService = cacheService;
    }

    /**
     * Publish all pathways for a parent learner activity. Finds all the children pathways, convert them to a
     * learner pathway object, persist to the database and save the parent/child relationship.
     *
     * @param parentActivityId the parent activity to deploy the children pathways for
     * @param deployment the deployment to deploy each pathway to
     * @return a flux of learner pathway
     * @throws PublishPathwayException when either:
     * <br> parentActivityId is <code>null</code>
     * <br> deployment is <code>null</code>
     * <br> pathway is not found
     */
    public Flux<LearnerPathway> publish(final UUID parentActivityId, final DeployedActivity deployment) {

        try {
            checkArgument(parentActivityId != null, "parentActivityId is required");
            checkArgument(deployment != null, "deployment is required");
        } catch (IllegalArgumentException e) {
            throw new PublishPathwayException(parentActivityId, e.getMessage());
        }

        return activityService.findChildPathwayIds(parentActivityId)
                .flatMapMany(pathwayIds -> publish(parentActivityId, deployment, pathwayIds));
    }

    /**
     * For each pathway id build a learner pathway and persist it to the database
     *
     * @param parentActivityId the pathway parent activity
     * @param deployment the deployment to deploy the learner pathway to
     * @param pathwayIds the list of pathway ids to deploy
     * @return a flux of learner pathway
     */
    private Flux<LearnerPathway> publish(final UUID parentActivityId, final DeployedActivity deployment, final List<UUID> pathwayIds) {

        if (pathwayIds.isEmpty()) {
            return Flux.empty();
        }

        return pathwayIds.stream()
                .map(pathwayId -> build(pathwayId, deployment, parentActivityId)
                        .flux()
                        .doOnError(throwable -> {
                            deploymentLogService.logFailedStep(deployment, pathwayId, CoursewareElementType.PATHWAY,
                                    "[learnerPathwayService] " + Arrays.toString(throwable.getStackTrace()))
                                    .subscribe();
                            throw Exceptions.propagate(throwable);
                        }))
                .reduce(Flux::concat)
                .orElse(Flux.empty())
                .concatMap(pathway -> persist(pathway, parentActivityId, deployment));
    }

    /**
     * Persist the learner pathway object together with the parent/child relationship with the parent activity.
     *
     * @param pathway the learner pathway to persist
     * @param parentActivityId the parent activity to save the parent/child relationship with
     * @return a mono of learner pathway
     */
    private Mono<LearnerPathway> persist(final LearnerPathway pathway, final UUID parentActivityId, final DeployedActivity deployment) {
        return learnerPathwayGateway.persist(pathway)
                .then(learnerActivityGateway
                        .persistChildPathway(parentActivityId, pathway.getDeploymentId(), pathway.getChangeId(), pathway.getId())
                        .singleOrEmpty())
                .thenMany(learnerPathwayGateway.persistParentActivity(new LearnerParentElement()
                        .setElementId(pathway.getId())
                        .setParentId(parentActivityId)
                        .setDeploymentId(pathway.getDeploymentId())
                        .setChangeId(pathway.getChangeId())))
                .then(deploymentLogService.logProgressStep(deployment, pathway.getId(), CoursewareElementType.PATHWAY,
                        "[learnerPathwayService] finished persisting parent/child relationship"))
                .then(Mono.just(pathway));
    }

    /**
     * Build a learner pathway given id and deployment. Find the pathway latest config to build the learnerPathway.
     * If no configurations are found then an empty config object is passed down to the learnerBuilder
     *
     * @param pathwayId the pathway id to find and convert
     * @param deployment the deployment to deploy the learner pathway to
     * @param parentActivityId the parent activity element
     * @return a mono of learner pathway
     * @throws PublishPathwayException when the pathway is not found
     */
    private Mono<LearnerPathway> build(final UUID pathwayId, final DeployedActivity deployment, final UUID parentActivityId) {

        Mono<Pathway> pathwayMono = pathwayService.findById(pathwayId)
                .flatMap(pathway -> deploymentLogService.logProgressStep(deployment, pathwayId, CoursewareElementType.PATHWAY,
                        "[learnerPathwayService] started publishing pathway " + pathwayId)
                        .thenReturn(pathway))
                .doOnError(PathwayNotFoundException.class, ex -> {
                    throw new PublishPathwayException(parentActivityId, ex.getMessage());
                });
        Mono<PathwayConfig> configMono = pathwayService.findLatestConfig(pathwayId)
                .defaultIfEmpty(new PathwayConfig());

        return Mono.zip(pathwayMono, configMono)
                .map(tuple2 -> {
                    Pathway pathway = tuple2.getT1();
                    PathwayConfig config = tuple2.getT2();

                    return learnerPathwayBuilderProvider.get()
                            .build(pathway.getType(), pathwayId, deployment.getId(), deployment.getChangeId(), config.getConfig(), pathway.getPreloadPathway());
                });
    }

    /**
     * Find a list of walkables for the pathway in the deployment
     * @param pathwayId the pathway id
     * @param deploymentId the deployment id
     * @return flux of walkable, empty flux if no walkables found
     */
    @Trace(async = true)
    public Flux<WalkableChild> findWalkables(final UUID pathwayId, final UUID deploymentId) {
        checkArgument(pathwayId != null, "pathwayId is required");
        checkArgument(deploymentId != null, "deploymentId is required");

//        UUID changeId = changeIdCache.get(deploymentId);
//        String cacheName = String.format("learner:pathway:walkableChildren:/%s/%s/%s", deploymentId, changeId, pathwayId);
//
//@Marker
//        return cacheService.getList(cacheName)
//                .switchIfEmpty(
//                    cacheService.setList(cacheName,
//                        learnerPathwayGateway.findWalkableChildren(pathwayId, deploymentId)
//                        .map(Walkables::toList)
//                        .flatMapIterable(list -> list))
//                )
//                .map(WalkableChild.class::cast);

        return learnerPathwayGateway.findWalkableChildren(pathwayId, deploymentId)
                .map(Walkables::toList)
                .flatMapIterable(list -> list);

    }

    /**
     * Find the parent activity id for a pathway
     *
     * @param pathwayId the pathway id to find the parent activity id for
     * @param deploymentId the deployment id
     * @return a mono of uuid representing the parent activity id of the pathway
     * @throws ParentActivityNotFoundException when the parent activity is not found
     */
    @Trace(async = true)
    public Mono<UUID> findParentActivityId(final UUID pathwayId, final UUID deploymentId) {

        UUID changeId = changeIdCache.get(deploymentId);
        String cacheName = String.format("learner:pathway:parentActivityId:/%s/%s/%s", deploymentId, changeId, pathwayId);

        Mono<UUID> parentActivityId = learnerPathwayGateway.findParentActivityId(pathwayId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return cacheService.computeIfAbsent(cacheName, UUID.class, parentActivityId, 365, TimeUnit.DAYS)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ParentActivityNotFoundException(pathwayId);
                });

    }

    /**
     * Find a learner Pathway
     *
     * @param id the id of the pathway to find
     * @param deploymentId the deployment id to find the pathway in
     * @return a mono of a learner pathway or an empty mono when the pathway is not found
     */
    @Trace(async = true)
    public Mono<LearnerPathway> find(final UUID id, final UUID deploymentId) {
        return learnerPathwayGateway.findLatestDeployed(id, deploymentId);
    }

    /**
     * Find a learner Pathway
     *
     * @param id the id of the pathway to find
     * @param deploymentId the deployment id to find the pathway in
     * @return a mono of a learner pathway or an empty mono when the pathway is not found
     */
    @Trace(async = true)
    public Flux<LearnerPathway> findPathways(final UUID id, final UUID deploymentId) {
        return learnerPathwayGateway.findPathwaysByLatestDeployed(id, deploymentId);
    }


    /**
     * Find a learner pathway. The found pathway is then built to its proper type via
     * {@link LearnerPathwayBuilder#build(PathwayType, UUID, UUID, UUID, String, com.smartsparrow.courseware.pathway.PreloadPathway)} (PathwayType)}
     *
     * @param id the id of the pathway to find
     * @param deploymentId the deployment to find the pathway in
     * @param type the class type of the pathway to return
     * @param <T> a generic that extends a {@link LearnerPathway} to cast the found pathway to
     * @return a mono of learner pathway cast to the expected type
     * @throws IllegalStateFault when failing to cast the pathway and a {@link ClassCastException} is thrown
     */
    public <T extends LearnerPathway> Mono<T> find(final UUID id, final UUID deploymentId, Class<T> type) {
        return find(id, deploymentId)
                .map(found -> learnerPathwayBuilderProvider.get()
                        .build(found.getType(), found.getId(), found.getDeploymentId(), found.getChangeId(), found.getConfig(), found.getPreloadPathway()))
                .map(type::cast)
                .doOnError(ClassCastException.class, ex -> {
                    throw new IllegalStateFault(ex.getMessage());
                });
    }
}
