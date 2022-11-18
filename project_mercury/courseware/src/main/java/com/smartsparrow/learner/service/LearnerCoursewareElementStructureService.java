package com.smartsparrow.learner.service;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.LearnerActivityPayload;
import com.smartsparrow.learner.data.LearnerCoursewareWalkable;
import com.smartsparrow.learner.data.LearnerInteractivePayload;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class LearnerCoursewareElementStructureService {

    private final DeploymentGateway deploymentGateway;
    private final LearnerActivityService learnerActivityService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final LearnerPathwayService learnerPathwayService;
    private final LearnerService learnerService;
    private final LearnerCoursewareService learnerCoursewareService;

    @Inject
    public LearnerCoursewareElementStructureService(final DeploymentGateway deploymentGateway,
                                                    final LearnerActivityService learnerActivityService,
                                                    final LearnerPathwayService learnerPathwayService,
                                                    final LearnerService learnerService,
                                                    final LearnerCoursewareService learnerCoursewareService,
                                                    final LearnerInteractiveService learnerInteractiveService) {
        this.deploymentGateway = deploymentGateway;
        this.learnerActivityService = learnerActivityService;
        this.learnerPathwayService = learnerPathwayService;
        this.learnerService = learnerService;
        this.learnerCoursewareService = learnerCoursewareService;
        this.learnerInteractiveService = learnerInteractiveService;
    }

    /**
     * Get Learner courseware walkable for given elemnet id and deploymentId,
     * elementId can be optional
     * Walkables will be fetched based on preload pathway fetch info
     *
     * @param deploymentId the deploymnetId
     * @param elementId the learner courseware element id
     * @return mono of learner courseware walkable
     */
    @Trace(async = true)
    public Mono<List<LearnerCoursewareWalkable>> getLearnerCoursewareWalkable(final UUID deploymentId,
                                                                              final UUID elementId) {
        if (elementId != null) {
            Mono<LearnerCoursewareWalkable> learnerCoursewareWalkableMono = learnerService.findElementByDeployment(
                    elementId,
                    deploymentId)
                    .flatMap(learnerCoursewareElement -> {
                        return learnerCoursewareService.getLearnerRootElementId(deploymentId,
                                                                                elementId,
                                                                                learnerCoursewareElement.getElementType())
                                .flatMap(rootElementId -> {

                                    if (elementId == rootElementId) {
                                        return generateStructureFromRootElement(rootElementId,
                                                                                deploymentId)
                                                .doOnEach(ReactiveTransaction.linkOnNext());
                                    }
                                    return generateStructureFromNonRootElement(
                                            rootElementId,
                                            elementId,
                                            learnerCoursewareElement.getElementType(),
                                            deploymentId)
                                            .doOnEach(ReactiveTransaction.linkOnNext());
                                });
                    });
            return learnerCoursewareWalkableMono
                    .expandDeep(coursewareElementNode -> Flux.fromIterable(coursewareElementNode.getChildren())
                            .subscribeOn(Schedulers.elastic())
                            .publishOn(Schedulers.elastic()))
                    .collectList();
        }
        return deploymentGateway.findActivityByDeployment(deploymentId)
                .flatMap(deployedActivity -> generateStructureFromRootElement(deployedActivity.getActivityId(),
                                                                              deploymentId))
                .expandDeep(coursewareElementNode -> Flux.fromIterable(coursewareElementNode.getChildren())
                        .subscribeOn(Schedulers.elastic())
                        .publishOn(Schedulers.elastic()))
                .collectList();
    }

    /**
     * Generate walkables from root elementId
     *
     * @param rootElementId the root element id
     * @param deploymentId the deploymnetId
     * @return mono of learner courseware walkable
     */
    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> generateStructureFromRootElement(final UUID rootElementId,
                                                                             final UUID deploymentId) {

        LearnerCoursewareWalkable elementNode = new LearnerCoursewareWalkable()
                .setElementId(rootElementId)
                .setType(CoursewareElementType.ACTIVITY)
                .setParentId(null)
                .setTopParentId(rootElementId);

        return populateChildren(elementNode, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Generate walkables from any elementId
     *
     * @param rootElementId the root elementId
     * @param elementId the element Id
     * @param elementType courseware element type
     * @param deploymentId the deployment id
     * @return mono of learner courseware walkables
     */
    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> generateStructureFromNonRootElement(final UUID rootElementId,
                                                                                final UUID elementId,
                                                                                final CoursewareElementType elementType,
                                                                                final UUID deploymentId) {
        return learnerCoursewareService.findPathFor(deploymentId, elementId, elementType)
                .collectList()
                .flatMap(path -> {
                    LearnerCoursewareWalkable elementNode = new LearnerCoursewareWalkable()
                            .setElementId(elementId)
                            .setType(elementType)
                            // The last item in the path is the current element, so the parent is the second last item.
                            .setTopParentId(rootElementId)
                            .setParentId(path.get(path.size() - 2).getElementId());

                    return populateChildren(elementNode, deploymentId)
                            .doOnEach(ReactiveTransaction.linkOnNext());
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> populateChildren(final LearnerCoursewareWalkable node,
                                                             final UUID deploymentId) {
        CoursewareElementType type = node.getType();
        switch (type) {
            case ACTIVITY:
                return learnerActivityService.findActivity(node.getElementId(), deploymentId)
                        .flatMap(learnerActivity -> {
                            node.setLearnerWalkablePayload(new LearnerActivityPayload(learnerActivity));
                            return populateChildrenForActivity(node, deploymentId);
                        })
                        .doOnEach(ReactiveTransaction.linkOnNext());

            case INTERACTIVE:
                return learnerInteractiveService.findInteractive(node.getElementId(), deploymentId)
                        .flatMap(learnerInteractive -> {
                            node.setLearnerWalkablePayload(new LearnerInteractivePayload(learnerInteractive));
                            return populateChildrenForInteractive(node, deploymentId);
                        })
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case PATHWAY:
                return populateChildrenForPathway(node, deploymentId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case COMPONENT:
                return Mono.just(node)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            default:
                throw new UnsupportedOperationFault("Unsupported courseware element type " + type);
        }
    }

    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> populateChildrenForActivity(final LearnerCoursewareWalkable node,
                                                                        final UUID deploymentId) {
        return learnerActivityService.findChildPathways(node.getElementId(), deploymentId)
                .collectList()
                .flatMapIterable(p -> p)
                .flatMap(learnerPathway -> createChildNodeAndPopulateChildren(
                        new LearnerCoursewareWalkable()
                                .setElementId(learnerPathway.getId())
                                .setType(CoursewareElementType.PATHWAY)
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), node, deploymentId))
                .flatMap(newNode -> learnerActivityService.findChildComponents(node.getElementId(), deploymentId)
                        .flatMapIterable(c -> c)
                        .flatMap(componentId -> createChildNodeAndPopulateChildren(
                                new LearnerCoursewareWalkable()
                                        .setElementId(componentId)
                                        .setType(CoursewareElementType.COMPONENT)
                                        .setParentId(node.getElementId())
                                        .setTopParentId(node.getTopParentId()), newNode, deploymentId)))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> populateChildrenForInteractive(final LearnerCoursewareWalkable node,
                                                                           final UUID deploymentId) {

        return learnerInteractiveService.findChildrenComponent(node.getElementId(), deploymentId)
                .flatMapIterable(c -> c)
                .flatMap(componentId -> createChildNodeAndPopulateChildren(
                        new LearnerCoursewareWalkable()
                                .setElementId(componentId)
                                .setType(CoursewareElementType.COMPONENT)
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), node, deploymentId))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> createChildNodeAndPopulateChildren(final LearnerCoursewareWalkable element,
                                                                               final LearnerCoursewareWalkable parentNode,
                                                                               final UUID deploymentId) {

        return populateChildren(element, deploymentId)
                .flatMap(updatedNode -> Mono.just(parentNode.addChild(updatedNode)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> populateChildrenForPathway(final LearnerCoursewareWalkable node,
                                                                       final UUID deploymentId) {
        return learnerPathwayService.findPathways(node.getElementId(), deploymentId)
                .flatMap(pathway -> populateChildrenForPathway(pathway, node, deploymentId))
                .then(Mono.just(node));

    }

    /**
     * Populate children for pathway based on the pathway type and preload pathway type.
     * In case of linear pathway type irrespective of preload pathway it will always fetch first child of pathway.
     *
     * @param pathway the learner pathway object
     * @param node the courseware walkable object
     * @param deploymentId the deployment id
     * @return mono of learner courseware walkable
     */
    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> populateChildrenForPathway(LearnerPathway pathway,
                                                                       final LearnerCoursewareWalkable node,
                                                                       final UUID deploymentId) {
        PreloadPathway preloadPathway = pathway.getPreloadPathway();
        PathwayType pathwayType = pathway.getType();
        switch (pathwayType) {
            case LINEAR:
                switch (preloadPathway) {
                    case ALL:
                    case FIRST:
                        return getFirstChildOfPathway(node, deploymentId);
                    case NONE:
                        break;
                    default:
                        throw new UnsupportedOperationFault("Unsupported preload pathway " + preloadPathway);
                }
                break;
            case GRAPH:
                switch (preloadPathway) {
                    case ALL:
                        return getAllChildrenOfPathway(node, deploymentId);
                    case FIRST:
                        return getFirstChildOfPathway(node, deploymentId);
                    case NONE:
                        break;
                    default:
                        throw new UnsupportedOperationFault("Unsupported preload pathway " + preloadPathway);

                }
                break;
            case FREE:
                switch (preloadPathway) {
                    case ALL:
                        return getAllChildrenOfPathway(node, deploymentId);
                    case FIRST:
                        return getFirstChildOfPathway(node, deploymentId);
                    case NONE:
                        break;
                    default:
                        throw new UnsupportedOperationFault("Unsupported preload pathway " + preloadPathway);

                }
                break;
            case RANDOM:
                switch (preloadPathway) {
                    case ALL:
                        return getAllChildrenOfPathway(node, deploymentId);
                    case FIRST:
                        return getFirstChildOfPathway(node, deploymentId);
                    case NONE:
                        break;
                    default:
                        throw new UnsupportedOperationFault("Unsupported preload pathway " + preloadPathway);

                }
                break;
            default:
                throw new UnsupportedOperationFault("Unsupported pathwayType " + pathwayType);
        }
        return Mono.just(node);
    }

    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> getAllChildrenOfPathway(final LearnerCoursewareWalkable node,
                                                                    final UUID deploymentId) {
        return learnerPathwayService.findWalkables(node.getElementId(), deploymentId)
                .collectList()
                .filter(walkableChildren -> !walkableChildren.isEmpty())
                .flatMapIterable(c -> c)
                .concatMap(walkable -> createChildNodeAndPopulateChildren(
                        new LearnerCoursewareWalkable()
                                .setElementId(walkable.getElementId())
                                .setType(walkable.getElementType())
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), node, deploymentId))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<LearnerCoursewareWalkable> getFirstChildOfPathway(final LearnerCoursewareWalkable node,
                                                                   final UUID deploymentId) {
        return learnerPathwayService.findWalkables(node.getElementId(), deploymentId)
                .collectList()
                .filter(walkableChildren -> !walkableChildren.isEmpty())
                .map(walkableChildren -> walkableChildren.get(0))
                .flatMap(walkable -> createChildNodeAndPopulateChildren(
                        new LearnerCoursewareWalkable()
                                .setElementId(walkable.getElementId())
                                .setType(walkable.getElementType())
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), node, deploymentId))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
