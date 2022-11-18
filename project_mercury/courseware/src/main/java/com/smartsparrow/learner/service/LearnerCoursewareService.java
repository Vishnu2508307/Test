package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.learner.data.LearnerElement;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
@SuppressWarnings("Duplicates")
public class LearnerCoursewareService {

    private final LearnerActivityService learnerActivityService;
    private final LearnerPathwayService learnerPathwayService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final LearnerComponentService learnerComponentService;
    private final LearnerFeedbackService learnerFeedbackService;
    private final LatestDeploymentChangeIdCache changeIdCache;
    private final CacheService cacheService;
    private final LearnerService learnerService;


    @Inject
    public LearnerCoursewareService(LearnerActivityService learnerActivityService,
            LearnerPathwayService learnerPathwayService,
            LearnerInteractiveService learnerInteractiveService,
            LearnerComponentService learnerComponentService,
            LearnerFeedbackService learnerFeedbackService,
            LatestDeploymentChangeIdCache changeIdCache,
            CacheService cacheService,
            LearnerService learnerService) {
        this.learnerActivityService = learnerActivityService;
        this.learnerPathwayService = learnerPathwayService;
        this.learnerInteractiveService = learnerInteractiveService;
        this.learnerComponentService = learnerComponentService;
        this.learnerFeedbackService = learnerFeedbackService;
        this.changeIdCache = changeIdCache;
        this.cacheService = cacheService;
        this.learnerService = learnerService;
    }

    /**
     * Get the ancestry from the specified node (of type) to the root.
     *
     * @param deploymentId the deployment id
     * @param fromElement the starting element
     * @param elementType the starting type
     *
     * @return a list of ordered elements from the specified node up to the root
     */
    @Trace(async = true)
    public Mono<List<CoursewareElement>> getAncestry(final UUID deploymentId,
            final UUID fromElement,
            final CoursewareElementType elementType) {

        UUID changeId = changeIdCache.get(deploymentId);
        String cacheName = String.format("learner:element:ancestryFrom:/%s/%s/%s", deploymentId, changeId, fromElement);

        Mono<List<CoursewareElement>> ancestry = findPathFor(deploymentId, fromElement, elementType)
                .collectList()
                .map(a -> {
                    Collections.reverse(a);
                    return a;
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
        //noinspection unchecked
        return cacheService.computeIfAbsent(cacheName, (Class<List<CoursewareElement>>)(Class<?>) List.class, ancestry,
                                            365, TimeUnit.DAYS);
    }

    @Trace(async = true)
    public Mono<UUID> getLearnerRootElementId(final UUID deploymentId,
                                              final UUID elementId,
                                              final CoursewareElementType elementType) {
        affirmArgument(elementId != null, "elementId is missing");
        affirmArgument(elementType != null, "elementType is missing");
        return findPathFor(deploymentId, elementId, elementType)
                .collectList()
                .flatMap(path -> Mono.just(path.get(0).getElementId()))
                .onErrorReturn(elementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the path for a courseware element from the root element down to itself
     *
     * @param deploymentId the deployment id
     * @param elementId the element to find the path for
     * @param elementType the element type
     * @return a list of ordered elements from the root element down to the specified elementId
     */
    public Flux<CoursewareElement> findPathFor(final UUID deploymentId,
                                                      final UUID elementId,
                                                      final CoursewareElementType elementType) {
        //FIXME: this is a heavily executed operation; it should get this data in a single query
        //FIXME: the code returns from root to node, we want the reverse.

        switch (elementType) {
            case ACTIVITY:
                return getPathForActivity(elementId, deploymentId);
            case PATHWAY:
                return getPathForPathway(elementId, deploymentId);
            case INTERACTIVE:
                return getPathForInteractive(elementId, deploymentId);
            case FEEDBACK:
                return getPathForFeedback(elementId, deploymentId);
            case COMPONENT:
                return getPathForComponent(elementId, deploymentId);
            default:
                throw new UnsupportedOperationException("Unsupported courseware element type " + elementType);
        }
    }

    /**
     * Returns the path from the most top activity to the given activity.
     *
     * @param activityId the activity to fetch the path for
     * @return mono with list of courseware elements.
     */
    @Trace(async = true)
    private Flux<CoursewareElement> getPathForActivity(final UUID activityId, final UUID deploymentId) {
        return learnerActivityService.findParentPathwayId(activityId, deploymentId)
                .onErrorResume(LearnerPathwayNotFoundFault.class, ex -> Mono.empty())
                .flux()
                .flatMap(parentId -> getPathForPathway(parentId, deploymentId))
                .concatWith(Mono.just(new CoursewareElement(activityId, CoursewareElementType.ACTIVITY)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given pathway.
     *
     * @param pathwayId the pathway to fetch the path for
     * @return mono with list of courseware elements.
     */
    @Trace(async = true)
    private Flux<CoursewareElement> getPathForPathway(final UUID pathwayId, final UUID deploymentId) {
        return learnerPathwayService.findParentActivityId(pathwayId, deploymentId)
                .flux()
                .flatMap(parentId -> getPathForActivity(parentId, deploymentId))
                .concatWith(Mono.just(new CoursewareElement(pathwayId, CoursewareElementType.PATHWAY)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given interactive.
     *
     * @param interactiveId the interactive to fetch the path for
     * @return mono with list of courseware elements.
     */
    @Trace(async = true)
    private Flux<CoursewareElement> getPathForInteractive(final UUID interactiveId, final UUID deploymentId) {
        return learnerInteractiveService.findParentPathwayId(interactiveId, deploymentId)
                .flux()
                .flatMap(parentId -> getPathForPathway(parentId, deploymentId))
                .concatWith(Mono.just(new CoursewareElement(interactiveId, CoursewareElementType.INTERACTIVE)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given feedback.
     *
     * @param feedbackId the feedback to fetch the path for
     * @return a mono list of courseware elements.
     */
    @Trace(async = true)
    private Flux<CoursewareElement> getPathForFeedback(final UUID feedbackId, final UUID deploymentId) {
        return learnerFeedbackService.findParentId(feedbackId, deploymentId)
                .flux()
                .flatMap(parentId -> getPathForInteractive(parentId, deploymentId))
                .concatWith(Mono.just(new CoursewareElement(feedbackId, CoursewareElementType.FEEDBACK)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given component
     *
     * @param componentId the component to fetch the path for
     * @return a mono list of courseware elements.
     * @throws UnsupportedOperationException if the parent by component type is not allowed
     */
    @Trace(async = true)
    private Flux<CoursewareElement> getPathForComponent(final UUID componentId, final UUID deploymentId) {
        return learnerComponentService.findParentFor(componentId, deploymentId) //
                .flux()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(parentByComponent -> {
                    switch (parentByComponent.getParentType()) {
                    case INTERACTIVE:
                        return getPathForInteractive(parentByComponent.getParentId(), deploymentId);
                    case ACTIVITY:
                        return getPathForActivity(parentByComponent.getParentId(), deploymentId);
                    default:
                        throw new UnsupportedOperationException(String.format("parentType %s not allowed for component",
                                                                              parentByComponent.getParentType()));
                    }
                }).concatWith(Mono.just(new CoursewareElement(componentId, CoursewareElementType.COMPONENT)));
    }

    /**
     * Find a learner element type and ancestry given an id
     *
     * @param elementId the element id
     * @param deploymentId the deployment id
     * @return a mono with the courseware element ancestry
     * @throws IllegalArgumentFault when the element is not found
     */
    public Mono<CoursewareElementAncestry> findCoursewareElementAncestry(final UUID elementId, final UUID deploymentId) {
        return learnerService.findElementByDeployment(elementId, deploymentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentFault(String.format("type not found for element %s", elementId))))
                .flatMap(element -> {
                    return getAncestry(deploymentId, elementId, element.getElementType())
                            .flatMap(ancestry -> {
                                // remove first element which is the actual element requesting the ancestry
                                ancestry.remove(0);
                                // return the ancestry
                                return Mono.just(new CoursewareElementAncestry()
                                        .setElementId(elementId)
                                        .setType(element.getElementType())
                                        .setAncestry(ancestry));
                            });
                });
    }

    /**
     * Fetch config for learner element based on the type of element
     *
     * @param elementId the element id
     * @param deploymentId the deployment id
     * @param type the element type
     * @return Mono of String
     * @throws UnsupportedOperationException if given courseware type cannot have config
     */
    public Mono<String> fetchConfig(UUID elementId, UUID deploymentId, CoursewareElementType type) {
        switch(type) {
            case ACTIVITY:
                return learnerActivityService.findActivity(elementId, deploymentId)
                        .map(getLearnerElementConfig());
            case INTERACTIVE:
                return learnerInteractiveService.findInteractive(elementId, deploymentId)
                        .map(getLearnerElementConfig());
            case COMPONENT:
                return learnerComponentService.findComponent(elementId, deploymentId)
                        .map(getLearnerElementConfig());
            case FEEDBACK:
                return learnerFeedbackService.findFeedback(elementId, deploymentId)
                        .map(getLearnerElementConfig());
            case PATHWAY:
                return learnerPathwayService.find(elementId, deploymentId)
                        .map(getLearnerPathwayConfig());
            default : throw new UnsupportedOperationException(String.format("learner element %s cannot have config", type));
        }
    }

    /**
     * @return return the element config or an empty string if the config is null
     */
    private Function<LearnerElement, String> getLearnerElementConfig() {
        return learnerElement -> {
            if (learnerElement.getConfig() != null) {
                return learnerElement.getConfig();
            }
            return StringUtils.EMPTY;
        };
    }

    /**
     * @return return the pathway config or an empty string if the config is null
     */
    private Function<LearnerPathway, String> getLearnerPathwayConfig() {
        return learnerElement -> {
            if (learnerElement.getConfig() != null) {
                return learnerElement.getConfig();
            }
            return StringUtils.EMPTY;
        };
    }


}
