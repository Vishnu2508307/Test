package com.smartsparrow.annotation.service;

import static com.smartsparrow.annotation.service.Motivation.commenting;
import static com.smartsparrow.annotation.service.Motivation.replying;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.data.AnnotationGateway;
import com.smartsparrow.annotation.lang.AnnotationAlreadyExistsFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Manage annotations; this functionality is utilized across Builder/Workspace and Learner roles.
 */
@Singleton
public class AnnotationService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AnnotationService.class);

    private final AnnotationGateway annotationGateway;

    @Inject
    public AnnotationService(AnnotationGateway annotationGateway) {
        this.annotationGateway = annotationGateway;
    }

    /**
     * Persist a courseware annotation
     *
     * @param annotation the annotation to persist
     * @return a flux void to add to a reactive chain
     */
    @Trace(async = true)
    public Flux<Void> create(final CoursewareAnnotation annotation) {
        affirmNotNull(annotation, "missing annotation");
        affirmNotNull(annotation.getRootElementId(), "missing root element id");
        affirmNotNull(annotation.getCreatorAccountId(), "missing annotation creator account id");
        affirmNotNull(annotation.getMotivation(), "missing annotation motivation");

        return annotationGateway.persist(annotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a courseware annotation, with an optional annotationId
     *
     * @param annotation the annotation to persist
     * @param annotationId optional annotation id, if not supplied a new id will be created
     * @return a flux void to add to a reactive chain
     * @throws AnnotationAlreadyExistsFault if provided annotation id already exists
     */
    @Trace(async = true)
    public Flux<Void> create(final CoursewareAnnotation annotation, final UUID annotationId) {
        affirmNotNull(annotation, "missing annotation");
        affirmNotNull(annotation.getRootElementId(), "missing root element id");
        affirmNotNull(annotation.getCreatorAccountId(), "missing annotation creator account id");
        affirmNotNull(annotation.getMotivation(), "missing annotation motivation");

        // If an annotation id has been supplied
        // check it does not already exist
        Mono<UUID> id =
                annotationGateway.findCoursewareAnnotation(annotationId)
                        .hasElement()
                        .handle((hasElement, sink) -> {
                            if (hasElement) {
                                sink.error(new AnnotationAlreadyExistsFault(annotationId));
                            } else {
                                sink.next(annotationId);
                            }
                        });


        Mono<CoursewareAnnotation> annotationMono = id.map(annotation::setId);
        return annotationMono.flatMapMany(annotationGateway::persist)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a courseware annotation by annotation id
     *
     * @param annotationId the annotation id
     * @return a reactive Mono of the annotation
     */
    @Trace(async = true)
    public Mono<CoursewareAnnotation> findCoursewareAnnotation(final UUID annotationId) {
        affirmNotNull(annotationId, "missing annotation id");

        return annotationGateway.findCoursewareAnnotation(annotationId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Find a courseware annotation by annotation id
     *
     * @param annotationId the annotation id
     * @param accountId    the user's account id
     * @return a reactive Mono of the annotation
     */
    @Trace(async = true)
    public Mono<CoursewareAnnotationPayload> findCoursewareAnnotation(final UUID annotationId, final UUID accountId) {
        affirmNotNull(annotationId, "missing annotation id");
        affirmNotNull(accountId, "missing account id");

        return annotationGateway.findCoursewareAnnotation(annotationId)
                .flatMap(annotation -> annotationGateway.findAnnotationRead(annotation, accountId)
                        .switchIfEmpty(Mono.just(new CoursewareAnnotationReadByUser()))
                        .map(readByUser -> CoursewareAnnotationPayload.from(annotation, readByUser)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the courseware annotations for a root element and motivation.
     *
     * @param rootElementId the root element id
     * @param motivation the motivation
     * @param accountId  the id of the current logged in account
     * @return a reactive stream of annotations
     */
    @Trace(async = true)
    public Flux<CoursewareAnnotationPayload> findCoursewareAnnotation(final UUID rootElementId,
                                                               final Motivation motivation,
                                                               final UUID accountId) {
        affirmNotNull(rootElementId, "missing root element id");
        affirmNotNull(motivation, "missing motivation");
        affirmNotNull(accountId, "missing accountId");

        return annotationGateway.findCoursewareAnnotation(rootElementId, motivation)
                .flatMap(annotation -> annotationGateway.findAnnotationRead(annotation, accountId)
                        .switchIfEmpty(Mono.just(new CoursewareAnnotationReadByUser()))
                        .map(readByUser -> CoursewareAnnotationPayload.from(annotation, readByUser)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the courseware annotations for a root element, creator account and motivation on a specific element.
     *
     * @param rootElementId the root element id
     * @param elementId the element
     * @param motivation the motivation
     * @return a reactive stream of annotations
     */
    @Trace(async = true)
    public Flux<CoursewareAnnotation> findCoursewareAnnotation(final UUID rootElementId,
            final UUID elementId,
            final Motivation motivation) {
        affirmNotNull(rootElementId, "missing root element id");
        affirmNotNull(motivation, "missing motivation");
        affirmNotNull(elementId, "missing element id");

        return annotationGateway.findCoursewareAnnotation(rootElementId, elementId, motivation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a learner annotation
     *
     * @param annotation the annotation to persist
     * @return a flux void to add to a reactive chain
     */
    @Trace(async = true)
    public Flux<Void> create(final LearnerAnnotation annotation) {
        affirmNotNull(annotation, "missing annotation");
        affirmNotNull(annotation.getDeploymentId(), "missing annotation deployment id");
        affirmNotNull(annotation.getCreatorAccountId(), "missing annotation creator account id");
        affirmNotNull(annotation.getMotivation(), "missing annotation motivation");

        return annotationGateway.persist(annotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    public Flux<Void> create(final DeploymentAnnotation annotation) {
        affirmNotNull(annotation, "missing annotation");
        affirmNotNull(annotation.getDeploymentId(), "missing annotation deployment id");
        affirmNotNull(annotation.getChangeId(), "missing annotation change id");
        affirmNotNull(annotation.getCreatorAccountId(), "missing annotation creator account id");
        affirmNotNull(annotation.getMotivation(), "missing annotation motivation");

        return annotationGateway.persist(annotation);
    }

    /**
     * Find a learner annotation by annotation id
     *
     * @param annotationId the annotation id
     * @return a reactive Mono of the annotation
     */
    @Trace(async = true)
    public Mono<LearnerAnnotation> findLearnerAnnotation(final UUID annotationId) {
        affirmNotNull(annotationId, "missing annotation id");

        return annotationGateway.findLearnerAnnotation(annotationId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the learner annotations for a deployment, student and motivation.
     *
     * @param deploymentId the deployment
     * @param creatorAccountId the creator of the annotation
     * @param motivation the motivation
     * @return a reactive stream of annotations
     */
    @Trace(async = true)
    public Flux<LearnerAnnotation> findLearnerAnnotation(final UUID deploymentId,
            final UUID creatorAccountId,
            final Motivation motivation) {
        affirmNotNull(deploymentId, "missing deployment id");
        affirmNotNull(creatorAccountId, "missing creator account id");
        affirmNotNull(motivation, "missing motivation");

        return annotationGateway.findLearnerAnnotation(deploymentId, creatorAccountId, motivation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the learner annotations for a deployment, student and motivation on a specific element.
     *
     * @param deploymentId the deployment
     * @param creatorAccountId the creator of the annotation
     * @param motivation the motivation
     * @param elementId the element
     * @return a reactive stream of annotations
     */
    @Trace(async = true)
    public Flux<LearnerAnnotation> findLearnerAnnotation(final UUID deploymentId,
            final UUID creatorAccountId,
            final Motivation motivation,
            final UUID elementId) {
        affirmNotNull(deploymentId, "missing deployment id");
        affirmNotNull(creatorAccountId, "missing creator account id");
        affirmNotNull(motivation, "missing motivation");
        affirmNotNull(elementId, "missing element id");

        return annotationGateway.findLearnerAnnotation(deploymentId, creatorAccountId, motivation, elementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find deployment annotations for a deployment filtered by motivation
     *
     * @param deploymentId the deployment the annotations belong to
     * @param changeId the deployment changeId
     * @param motivation the annotation motivation to find
     * @return a flux of deployment annotation
     * @throws IllegalArgumentFault when a required argument is missing
     */
    public Flux<DeploymentAnnotation> findDeploymentAnnotations(final UUID deploymentId,
                                                                final UUID changeId,
                                                                final Motivation motivation) {
        affirmNotNull(deploymentId, "deploymentId is required");
        affirmNotNull(changeId, "changeId is required");
        affirmNotNull(motivation, "motivation is required");

        return annotationGateway.findDeploymentAnnotations(deploymentId, changeId, motivation);
    }

    /**
     * Find deployment annotations for an element in a deployment filtered by motivation
     *
     * @param deploymentId the deployment the annotations belong to
     * @param changeId the deployment changeId
     * @param motivation the annotation motivation to find
     * @param elementId the element the annotation belongs to
     * @return a flux of deployment annotations
     * @throws IllegalArgumentFault when a required argument is missing
     */
    public Flux<DeploymentAnnotation> findDeploymentAnnotations(final UUID deploymentId,
                                                                final UUID changeId,
                                                                final Motivation motivation,
                                                                final UUID elementId) {
        affirmNotNull(deploymentId, "deploymentId is required");
        affirmNotNull(motivation, "motivation is required");
        affirmNotNull(changeId, "changeId is required");
        affirmNotNull(elementId, "elementId is required");
        return annotationGateway.findDeploymentAnnotations(deploymentId, changeId, motivation, elementId);
    }

    /**
     * Delete a courseware annotation
     *
     * @param annotation the annotation to delete
     * @return a flux void to add to a reactive chain
     */
    @Trace(async = true)
    public Flux<Void> deleteAnnotation(final CoursewareAnnotation annotation) {
        affirmNotNull(annotation, "missing annotation");

        return annotationGateway.deleteAnnotation(annotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the annotations from courseware
     *
     * @param rootElementId the annotation to delete
     * @return a flux void to add to a reactive chain
     */
    public Flux<Void> deleteAnnotationByRootElementId(final UUID rootElementId) {
        affirmNotNull(rootElementId, "missing rootElementId");

        return annotationGateway.deleteAnnotationByRootElementId(rootElementId);
    }

    /**
     * Delete a learner annotation
     *
     * @param annotation the annotation to delete
     * @return a flux void to wire into the reactive stream
     */
    @Trace(async = true)
    public Flux<Void> deleteAnnotation(final LearnerAnnotation annotation) {
        affirmNotNull(annotation, "missing annotation");

        return annotationGateway.deleteAnnotation(annotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * This method update the courseware annotation
     * @param annotationId annotation id
     * @param body the annotation body
     * @param target the annotation target
     * @return a reactive Mono of the courseware annotation
     */
    @Trace(async = true)
    public Mono<CoursewareAnnotation> updateCoursewareAnnotation(final UUID annotationId,
                                                                 final String body,
                                                                 final String target) {
        JsonNode bodyNode = Json.toJsonNode(body, "invalid body json");
        JsonNode targetNode = Json.toJsonNode(target, "invalid target json");
        return findCoursewareAnnotation(annotationId)
                .switchIfEmpty(Mono.just(new CoursewareAnnotation()))
                .map(annotations -> new CoursewareAnnotation()
                        .setId(annotations.getId())
                        .setCreatorAccountId(annotations.getCreatorAccountId())
                        .setElementId(annotations.getElementId())
                        .setMotivation(annotations.getMotivation())
                        .setRootElementId(annotations.getRootElementId())
                        .setBodyJson(bodyNode)
                        .setTargetJson(targetNode)
                        .setVersion(UUIDs.timeBased()))
                .flatMap(coursewareAnnotation -> create(coursewareAnnotation)
                        .then(Mono.just(coursewareAnnotation)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
    * Fetch courseware annotations.
    * fetch the courseware annotations for a root element and motivation.
    * @param rootElementId the rootElementId
    * @param motivation the motivation
    * @param accountId  the id of the current logged in account
    * @return flux of courseware annotation
    */
    @Trace(async = true)
    public Flux<CoursewareAnnotationPayload> fetchCoursewareAnnotation(final UUID rootElementId,
                                                                final Motivation motivation,
                                                                final UUID accountId) {
        return findCoursewareAnnotation(rootElementId, motivation, accountId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
    /**
     * Fetch courseware annotations.
     * fetch the courseware annotations for a root element and motivation on a specific element otherwise
     * @param rootElementId the rootElementId
     * @param elementId  the elementId
     * @param motivation the motivation
     * @param accountId  the id of the current logged in account
     * @return flux of courseware annotation
     */
    @Trace(async = true)
    public Flux<CoursewareAnnotationPayload> fetchCoursewareAnnotation(final UUID rootElementId,
                                                                       final UUID elementId,
                                                                       final Motivation motivation,
                                                                       final UUID accountId) {

        return findCoursewareAnnotation(rootElementId, elementId, motivation)
                .flatMap(annotation -> annotationGateway.findAnnotationRead(annotation, accountId)
                        .switchIfEmpty(Mono.just(new CoursewareAnnotationReadByUser()))
                        .map(readByUser -> CoursewareAnnotationPayload.from(annotation, readByUser)))
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Method to persist a learner annotation
     * @param deploymentId the deploymentId
     * @param elementId the element id
     * @param body the annotation body json
     * @param target the annotation target json
     * @param accountId the account id
     * @param motivation the motivation
     * @return mono of learner annotation object
     */
    @Trace(async = true)
    public Mono<LearnerAnnotation> create(final UUID deploymentId,
                                          final UUID elementId,
                                          final String body,
                                          final String target,
                                          final UUID accountId,
                                          final Motivation motivation) {
        UUID id = UUIDs.timeBased();
        JsonNode bodyNode = Json.toJsonNode(body, "invalid body json");
        JsonNode targetNode = Json.toJsonNode(target, "invalid target json");

        LearnerAnnotation learnerAnnotation = new LearnerAnnotation() //
                .setId(id) //
                .setVersion(id) //
                .setMotivation(motivation) //
                .setDeploymentId(deploymentId) //
                .setElementId(elementId) //
                .setBodyJson(bodyNode) //
                .setTargetJson(targetNode) //
                .setCreatorAccountId(accountId);

        return create(learnerAnnotation)
                .singleOrEmpty()
                .thenReturn(learnerAnnotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Publish annotation motivations of type identifying, classifying, linking and tagging from courseware to learner
     *
     * @param rootElementId the root element id
     * @param elementId the element id
     * @param deploymentId the deployment id
     * @return a flux void to wire into the reactive stream
     */
    public Flux<Void> publishAnnotationMotivations(final UUID rootElementId, final UUID elementId,
                                                   final UUID deploymentId, final UUID changeId) {
        affirmNotNull(rootElementId, "missing root element id");
        affirmNotNull(elementId, "missing element id");
        affirmNotNull(deploymentId, "missing deployment id");
        affirmNotNull(changeId, "missing change id");

        return Flux.merge(
                findCoursewareAnnotation(rootElementId, elementId, Motivation.identifying)
                        .flatMap(coursewareAnnotation -> create(new DeploymentAnnotation(coursewareAnnotation, deploymentId, changeId))),
                findCoursewareAnnotation(rootElementId, elementId, Motivation.classifying)
                        .flatMap(coursewareAnnotation -> create(new DeploymentAnnotation(coursewareAnnotation, deploymentId, changeId))),
                findCoursewareAnnotation(rootElementId, elementId, Motivation.linking)
                        .flatMap(coursewareAnnotation -> create(new DeploymentAnnotation(coursewareAnnotation, deploymentId, changeId))),
                findCoursewareAnnotation(rootElementId, elementId, Motivation.tagging)
                        .flatMap(coursewareAnnotation -> create(new DeploymentAnnotation(coursewareAnnotation, deploymentId, changeId)))
        );
    }

    /**
     * This method updates the learner annotation
     * @param annotationId annotation id
     * @param body the annotation body
     * @param target the annotation target
     * @return a reactive Mono of the courseware annotation
     */
    @Trace(async = true)
    public Mono<LearnerAnnotation> updateLearnerAnnotation(final UUID annotationId,
                                                           final String body,
                                                           final String target) {
        JsonNode bodyNode = Json.toJsonNode(body, "invalid body json");
        JsonNode targetNode = Json.toJsonNode(target, "invalid target json");
        return findLearnerAnnotation(annotationId)
                .map(annotations -> new LearnerAnnotation()
                        .setId(annotations.getId())
                        .setCreatorAccountId(annotations.getCreatorAccountId())
                        .setElementId(annotations.getElementId())
                        .setMotivation(annotations.getMotivation())
                        .setDeploymentId(annotations.getDeploymentId())
                        .setBodyJson(bodyNode)
                        .setTargetJson(targetNode)
                        .setVersion(UUIDs.timeBased()))
                .flatMap(learnerAnnotation -> create(learnerAnnotation)
                        .then(Mono.just(learnerAnnotation)))
                .doOnError(throwable -> log.reactiveErrorThrowable(String.format(
                        "error while updating learner annotation %s",
                        annotationId)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * finds the annotations with oldRootElementId deletes and insert same annotation with newRootElementId
     * @implNote As rootElementId is partition key to handle move deleting the existing record and creating new with newRootElementId
     * @param oldRootElementId old root element id
     * @param elementId element id INTERACTIVE type
     * @param newRootElementId new root element id
     * @param motivation motivation type
     * @return void
     */
    @Trace(async = true)
    private Flux<Void> moveAnnotations(final UUID oldRootElementId, final UUID elementId, final UUID newRootElementId, final Motivation motivation) {
        return findCoursewareAnnotation(oldRootElementId, elementId, motivation)
                .flatMap(retrievedCoursewareAnnotation -> {
                    CoursewareAnnotation updatedCoursewareAnnotation = retrievedCoursewareAnnotation;
                    updatedCoursewareAnnotation.setRootElementId(newRootElementId);
                    //As rootElementId is partition key to handle move deleting the existing record and creating new with newRootElementId
                    return deleteAnnotation(retrievedCoursewareAnnotation)
                            .thenMany(annotationGateway.persist(updatedCoursewareAnnotation));
                });
    }

    /**
     * Moves annotations from oldRootElementId to newRootElementId
     * @param oldRootElementId old root element id
     * @param elementId element id INTERACTIVE type
     * @param newRootElementId new root element id
     * @return void
     */
    @Trace(async = true)
    public Flux<Void> moveAnnotations(final UUID oldRootElementId, final UUID elementId, final UUID newRootElementId) {
        affirmNotNull(oldRootElementId, "missing old root element id");
        affirmNotNull(newRootElementId, "missing new root element id");
        affirmNotNull(elementId, "missing element id");

        return Flux.merge(moveAnnotations(oldRootElementId, elementId, newRootElementId, Motivation.identifying),
                          moveAnnotations(oldRootElementId, elementId, newRootElementId, Motivation.classifying),
                          moveAnnotations(oldRootElementId, elementId, newRootElementId, Motivation.linking),
                          moveAnnotations(oldRootElementId, elementId, newRootElementId, Motivation.tagging));
    }

    /**
     * This deletes only annotation by motivation in combination with rootActivityId and elementId
     * @param rootActivityId is activity id (top root elementId)
     * @param elementId element id
     */
    public Flux<Void> deleteAnnotation(final UUID rootActivityId, final UUID elementId) {
        affirmNotNull(rootActivityId, "rootActivityId is required");
        affirmNotNull(elementId, "elementId is required");
       return Flux.merge(annotationGateway.deleteAnnotation(elementId, rootActivityId, Motivation.identifying),
               annotationGateway.deleteAnnotation(elementId, rootActivityId, Motivation.classifying),
               annotationGateway.deleteAnnotation(elementId, rootActivityId, Motivation.linking),
               annotationGateway.deleteAnnotation(elementId, rootActivityId, Motivation.tagging)
        );
    }

    /**
     * Resolve multiple annotation values
     *
     * @param coursewareAnnotationKeys list of CoursewareAnnotation
     * @param resolved the value to be set
     */
    @Trace(async = true)
    public Flux<Void> resolveComments(final List<CoursewareAnnotationKey> coursewareAnnotationKeys, final Boolean resolved) {
        affirmArgument(coursewareAnnotationKeys != null, "coursewareAnnotationKeys is required");
        affirmArgument(resolved != null, "resolved is required");

        return coursewareAnnotationKeys.stream()
                .map(coursewareAnnotationKey -> annotationGateway.resolveComments(coursewareAnnotationKey, resolved))
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Read/ unread multiple annotation values
     *
     * @param rootElementId the root element id the annotations belong to
     * @param elementId     the element id the annotations belong to
     * @param annotationIds list of CoursewareAnnotation ids
     * @param read          the value to be set
     * @param userId        the id of the logged in user
     */
    @Trace(async = true)
    public Flux<Void> readComments(final UUID rootElementId, final UUID elementId,
                                      final List<UUID> annotationIds, final Boolean read, final UUID userId) {
        affirmArgument(rootElementId != null, "rootElementId is required");
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(annotationIds != null, "annotationIds is required");
        affirmArgument(read != null, "read is required");
        affirmArgument(userId != null, "userId is required");

        return annotationIds.stream()
                .map(annotationId -> {
                    if (read) {
                        return annotationGateway.readComments(rootElementId, elementId, annotationId, userId);
                    }
                    return annotationGateway.unreadComments(rootElementId, elementId, annotationId, userId);
                })
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * fetch the courseware annotation aggregation values for a root element, account and optionally on a specific element
     *
     * @param rootElementId the rootElementId
     * @param elementId the elementId
     * @param accountId the id of the current logged in account
     * @return mono of CoursewareAnnotationAggregate
     */
    @Trace(async = true)
    public Mono<CoursewareAnnotationAggregate> aggregateCoursewareAnnotation(final UUID rootElementId,
                                                                             final UUID elementId,
                                                                             final UUID accountId) {
        Flux<CoursewareAnnotationPayload> coursewareAnnotationPayloadFlux;

        if (elementId == null) {
            coursewareAnnotationPayloadFlux = Flux.merge(fetchCoursewareAnnotation(rootElementId,
                                                                                   commenting,
                                                                                   accountId),
                                                         fetchCoursewareAnnotation(rootElementId,
                                                                                   replying,
                                                                                   accountId));
        } else {
            coursewareAnnotationPayloadFlux = Flux.merge(fetchCoursewareAnnotation(rootElementId,
                                                                                   elementId,
                                                                                   commenting,
                                                                                   accountId),
                                                         fetchCoursewareAnnotation(rootElementId,
                                                                                   elementId,
                                                                                   replying,
                                                                                   accountId));
        }

        // check if CoursewareAnnotationPayload read/resolved are null or not. If they are null set the value to 0, otherwise keep counting.
        return coursewareAnnotationPayloadFlux.reduce(new CoursewareAnnotationAggregate(0, 0, 0, 0),
                                                      (aggregate, payload) ->
                                                              new CoursewareAnnotationAggregate(
                                                                      payload.getRead() == null ? 0 : payload.getRead() ? aggregate.setRead(
                                                                              aggregate.getRead() + 1) : aggregate.getRead(),
                                                                      payload.getRead() == null ? 0 : !payload.getRead() ? aggregate.setUnRead(
                                                                              aggregate.getUnRead() + 1) : aggregate.getUnRead(),
                                                                      payload.getResolved() ? aggregate.setResolved(
                                                                              aggregate.getResolved() + 1) : aggregate.getResolved(),
                                                                      !payload.getResolved() ? aggregate.setUnResolved(
                                                                              aggregate.getUnResolved() + 1) : aggregate.getUnResolved()))
                .doOnEach(ReactiveTransaction.linkOnNext());

    }
}
