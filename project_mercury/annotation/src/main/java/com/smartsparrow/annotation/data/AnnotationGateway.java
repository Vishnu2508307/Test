package com.smartsparrow.annotation.data;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.CoursewareAnnotationKey;
import com.smartsparrow.annotation.service.CoursewareAnnotationReadByUser;
import com.smartsparrow.annotation.service.DeploymentAnnotation;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AnnotationGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AnnotationGateway.class);

    private final Session session;

    private final CoursewareAnnotationMutator coursewareAnnotationMutator;
    private final CoursewareAnnotationByMotivationMutator coursewareAnnotationByMotivationMutator;
    private final LearnerAnnotationMutator learnerAnnotationMutator;
    private final LearnerAnnotationByMotivationMutator learnerAnnotationByMotivationMutator;
    private final DeploymentAnnotationMutator deploymentAnnotationMutator;
    private final CoursewareAnnotationReadByUserMutator coursewareAnnotationReadByUserMutator;

    private final CoursewareAnnotationMaterializer coursewareAnnotationMaterializer;
    private final CoursewareAnnotationByMotivationMaterializer coursewareAnnotationByMotivationMaterializer;
    private final LearnerAnnotationMaterializer learnerAnnotationMaterializer;
    private final LearnerAnnotationByMotivationMaterializer learnerAnnotationByMotivationMaterializer;
    private final DeploymentAnnotationMaterializer deploymentAnnotationMaterializer;
    private final CoursewareAnnotationReadByUserMaterializer coursewareAnnotationReadByUserMaterializer;

    @Inject
    public AnnotationGateway(Session session,
                             CoursewareAnnotationMutator coursewareAnnotationMutator,
                             CoursewareAnnotationByMotivationMutator coursewareAnnotationByMotivationMutator,
                             LearnerAnnotationMutator learnerAnnotationMutator,
                             LearnerAnnotationByMotivationMutator learnerAnnotationByMotivationMutator,
                             DeploymentAnnotationMutator deploymentAnnotationMutator,
                             CoursewareAnnotationReadByUserMutator coursewareAnnotationReadByUserMutator,
                             CoursewareAnnotationMaterializer coursewareAnnotationMaterializer,
                             CoursewareAnnotationByMotivationMaterializer coursewareAnnotationByMotivationMaterializer,
                             LearnerAnnotationMaterializer learnerAnnotationMaterializer,
                             LearnerAnnotationByMotivationMaterializer learnerAnnotationByMotivationMaterializer,
                             DeploymentAnnotationMaterializer deploymentAnnotationMaterializer,
                             CoursewareAnnotationReadByUserMaterializer coursewareAnnotationReadByUserMaterializer) {
        this.session = session;
        this.coursewareAnnotationMutator = coursewareAnnotationMutator;
        this.coursewareAnnotationByMotivationMutator = coursewareAnnotationByMotivationMutator;
        this.learnerAnnotationMutator = learnerAnnotationMutator;
        this.learnerAnnotationByMotivationMutator = learnerAnnotationByMotivationMutator;
        this.deploymentAnnotationMutator = deploymentAnnotationMutator;
        this.coursewareAnnotationReadByUserMutator = coursewareAnnotationReadByUserMutator;
        this.coursewareAnnotationMaterializer = coursewareAnnotationMaterializer;
        this.coursewareAnnotationByMotivationMaterializer = coursewareAnnotationByMotivationMaterializer;
        this.learnerAnnotationMaterializer = learnerAnnotationMaterializer;
        this.learnerAnnotationByMotivationMaterializer = learnerAnnotationByMotivationMaterializer;
        this.deploymentAnnotationMaterializer = deploymentAnnotationMaterializer;
        this.coursewareAnnotationReadByUserMaterializer = coursewareAnnotationReadByUserMaterializer;
    }

    /**
     * Persist a CoursewareAnnotation
     *
     * @param annotation the annotation to persist
     * @return a flux void to add to the reactive chain
     */
    @Trace(async = true)
    public Flux<Void> persist(final CoursewareAnnotation annotation) {
        return Mutators.execute(session, Flux.just(coursewareAnnotationMutator.upsert(annotation),
                                                   coursewareAnnotationByMotivationMutator.upsert(annotation)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while persisting courseware annotation",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("annotationId", annotation.getId());
                                                         }
                                                     }));
    }

    /**
     * Find a courseware annotation by annotation id
     *
     * @param annotationId the annotation id
     * @return a reactive Mono of the annotation
     */
    @Trace(async = true)
    public Mono<CoursewareAnnotation> findCoursewareAnnotation(final UUID annotationId) {
        return ResultSets.query(session, coursewareAnnotationMaterializer.fetchLatestById(annotationId))
                .flatMapIterable(row -> row) //
                .map(coursewareAnnotationMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the courseware annotations for a root element id, creator account and motivation.
     *
     * @param rootElementId the root element id
     * @param motivation the motivation
     * @return a reactive stream of annotations
     */
    @Trace(async = true)
    public Flux<CoursewareAnnotation> findCoursewareAnnotation(final UUID rootElementId,
            final Motivation motivation) {
        //
        return ResultSets.query(session, //
                                coursewareAnnotationByMotivationMaterializer.fetch(rootElementId, motivation))
                .flatMapIterable(row -> row) //
                .map(coursewareAnnotationByMotivationMaterializer::fromRow) //
                .flatMap(this::findCoursewareAnnotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the courseware annotations for a root element id, creator account and motivation for a specific element
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
        //
        return ResultSets.query(session, //
                                coursewareAnnotationByMotivationMaterializer.fetch(rootElementId, elementId,
                                                                                   motivation))
                .flatMapIterable(row -> row) //
                .map(coursewareAnnotationByMotivationMaterializer::fromRow) //
                .flatMap(this::findCoursewareAnnotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a LearnerAnnotation
     *
     * @param annotation the annotation to persist
     * @return a flux void to add to the reactive chain
     */
    @Trace(async = true)
    public Flux<Void> persist(final LearnerAnnotation annotation) {
        // persist in the main learner annotation table
        return Mutators.execute(session, Flux.just(learnerAnnotationMutator.upsert(annotation),
                                                   // persist in learner annotation by motivation table (for the learner)
                                                   learnerAnnotationByMotivationMutator.upsert(annotation)))
                .doOnEach(log.reactiveErrorThrowable("error while persisting learner annotation",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("annotationId", annotation.getId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a learner annotation by annotation id
     *
     * @param annotationId the annotation id
     * @return a reactive Mono of the annotation
     */
    @Trace(async = true)
    public Mono<LearnerAnnotation> findLearnerAnnotation(final UUID annotationId) {
        return ResultSets.query(session, learnerAnnotationMaterializer.fetchLatestById(annotationId))
                .flatMapIterable(row -> row) //
                .map(learnerAnnotationMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the learner annotations for a deployment, creator account and motivation.
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
        //
        return ResultSets.query(session, //
                                learnerAnnotationByMotivationMaterializer.fetch(deploymentId, creatorAccountId,
                                                                                motivation))
                .flatMapIterable(row -> row) //
                .map(learnerAnnotationByMotivationMaterializer::fromRow) //
                .flatMap(this::findLearnerAnnotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the learner annotations for a deployment, creator account and motivation for a specific element
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
        //
        return ResultSets.query(session, //
                                learnerAnnotationByMotivationMaterializer.fetch(deploymentId, creatorAccountId,
                                                                                motivation, elementId))
                .flatMapIterable(row -> row) //
                .map(learnerAnnotationByMotivationMaterializer::fromRow) //
                .flatMap(this::findLearnerAnnotation)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a DeploymentAnnotation object
     *
     * @param deploymentAnnotation the obj to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final DeploymentAnnotation deploymentAnnotation) {
        // persist in the deployment annotation table (for annotations published with a deployment)
        return Mutators.execute(session, Flux.just(deploymentAnnotationMutator.upsert(deploymentAnnotation),
                // persist in the main learner annotation table
                learnerAnnotationMutator.upsert(deploymentAnnotation)))
                .doOnEach(log.reactiveError("error persisting DeploymentAnnotation"));
    }

    /**
     * Find deployment  annotations by motivation
     *
     * @param deploymentId the deployment the annotations belong to
     * @param changeId the deployment changeId to fetch the annotations for
     * @param motivation the annotation motivation to look for
     * @return a flux of deployment annotations
     */
    public Flux<DeploymentAnnotation> findDeploymentAnnotations(final UUID deploymentId, final UUID changeId,
                                                                final Motivation motivation) {
        // find all the deployment annotation ids
        return ResultSets.query(session, deploymentAnnotationMaterializer.fetch(deploymentId, changeId, motivation))
                .flatMapIterable(row -> row)
                .map(deploymentAnnotationMaterializer::fromRow)
                // for each id find the learner annotation by id
                .flatMap(this::findLearnerAnnotation)
                // map the learner annotation into a deployment annotation
                .map(learnerAnnotation -> new DeploymentAnnotation(learnerAnnotation, changeId))
                .doOnEach(log.reactiveError("error fetching DeploymentAnnotations"));
    }

    /**
     * Find deployment annotations by motivation for an element
     *
     * @param deploymentId the deployment the annotations belong to
     * @param changeId the deployment changeId to fetch the annotations for
     * @param motivation the annotation motivation to look for
     * @param elementId the element the annotations belong to within the deployment
     * @return a flux of deployment annotations
     */
    public Flux<DeploymentAnnotation> findDeploymentAnnotations(final UUID deploymentId, final UUID changeId,
                                                                final Motivation motivation, final UUID elementId) {
        // find all the deployment annotation ids
        return ResultSets.query(session, deploymentAnnotationMaterializer.fetch(deploymentId, changeId, motivation, elementId))
                .flatMapIterable(row -> row)
                .map(deploymentAnnotationMaterializer::fromRow)
                // for each id find the learner annotation by id
                .flatMap(this::findLearnerAnnotation)
                // map the learner annotation into a deployment annotation
                .map(learnerAnnotation -> new DeploymentAnnotation(learnerAnnotation, changeId))
                .doOnEach(log.reactiveError("error fetching DeploymentAnnotations"));
    }

    /**
     * Delete a courseware annotation
     *
     * @param annotation the courseware annotation to delete
     * @return a flux void
     */
    @Trace(async = true)
    public Flux<Void> deleteAnnotation(final CoursewareAnnotation annotation) {
        return Mutators.execute(session, Flux.just(coursewareAnnotationMutator.delete(annotation),
                                                   coursewareAnnotationByMotivationMutator.delete(annotation),
                                                   coursewareAnnotationReadByUserMutator.deleteAnnotation(
                                                           annotation.getRootElementId(),
                                                           annotation.getElementId(),
                                                           annotation.getId())))
                .doOnEach(log.reactiveErrorThrowable("error while deleting courseware annotation",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("annotationId", annotation.getId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the relationship between a element and an annotation
     *
     * @param rootElementId to delete annotations
     * @return a flux of void
     */
    public Flux<Void> deleteAnnotationByRootElementId(final UUID rootElementId) {
        return Mutators.execute(session, Flux.just(coursewareAnnotationByMotivationMutator.deleteByRootElementId(rootElementId)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting courseware annotation by motivation [%s]", rootElementId), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Delete a learner annotation
     *
     * @param annotation the learner annotation
     * @return a flux void
     */
    @Trace(async = true)
    public Flux<Void> deleteAnnotation(final LearnerAnnotation annotation) {
        return Mutators.execute(session, Flux.just(learnerAnnotationMutator.delete(annotation), //
                                                   learnerAnnotationByMotivationMutator.delete(annotation)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting learner annotation",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("annotationId", annotation.getId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Deletes CourseWareAnnotations By Motivation *only*
     * @param elementId element id
     * @param rootElementId parent activity id - top root element id
     * @param motivation annotation's motivation
     * @return a flux void
     */
    public Flux<Void> deleteAnnotation(final UUID elementId, final UUID rootElementId, final Motivation motivation) {
        return Mutators.execute(session, Flux.just(coursewareAnnotationByMotivationMutator.deleteAnnotations(elementId, rootElementId, motivation)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting courseware annotation",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("annotationId", elementId);
                                put("rootElementId", rootElementId);
                                put("motivation", motivation);
                            }
                        }));
    }

    /**
     * Find an annotations read status for the user
     *
     * @param annotation the annotation to check status for
     * @param accountId  the user to check status for
     * @return a mono of annotation read status
     */
    public Mono<CoursewareAnnotationReadByUser> findAnnotationRead(final CoursewareAnnotation annotation,
                                                                   final UUID accountId) {
        return ResultSets.query(session, coursewareAnnotationReadByUserMaterializer.fetch(annotation.getRootElementId(),
                                                                                          annotation.getElementId(),
                                                                                          annotation.getId(),
                                                                                          accountId))
                .flatMapIterable(row -> row)
                .map(coursewareAnnotationReadByUserMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(log.reactiveError("error fetching annotation read"));
    }

    /**
     * Resolve annotation value
     *
     * @param coursewareAnnotationKey the CoursewareAnnotationKey object
     * @param resolved the value to be set
     */
    @Trace(async = true)
    public Flux<Void> resolveComments(final CoursewareAnnotationKey coursewareAnnotationKey, final Boolean resolved) {
        return Mutators.execute(session, Flux.just(coursewareAnnotationMutator.resolveComments(coursewareAnnotationKey, resolved)
        )).doOnError(throwable -> {
            log.error(String.format("Error resolving courseware annotation comment [%s]", coursewareAnnotationKey), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Read annotation value
     *
     * @param rootElementId the root element id the annotations belong to
     * @param elementId     the element id the annotations belong to
     * @param annotationId  courseware annotation id
     * @param userId        the id of the logged in user
     */
    @Trace(async = true)
    public Flux<Void> readComments(final UUID rootElementId, final UUID elementId,
                                   final UUID annotationId, final UUID userId) {
        CoursewareAnnotationReadByUser readByUser = new CoursewareAnnotationReadByUser()
                .setUserId(userId)
                .setAnnotationId(annotationId)
                .setElementId(elementId)
                .setRootElementId(rootElementId);
        return Mutators.execute(session, Flux.just(coursewareAnnotationReadByUserMutator.upsert(readByUser)
        )).doOnError(throwable -> {
            log.error(String.format("Error marking courseware annotation comment as read [%s]", readByUser), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Unread annotation value
     *
     * @param rootElementId the root element id the annotations belong to
     * @param elementId     the element id the annotations belong to
     * @param annotationId  courseware annotation id
     * @param userId        the id of the logged in user
     */
    @Trace(async = true)
    public Flux<Void> unreadComments(final UUID rootElementId, final UUID elementId,
                                   final UUID annotationId, final UUID userId) {
        CoursewareAnnotationReadByUser readByUser = new CoursewareAnnotationReadByUser()
                .setUserId(userId)
                .setAnnotationId(annotationId)
                .setElementId(elementId)
                .setRootElementId(rootElementId);
        return Mutators.execute(session, Flux.just(coursewareAnnotationReadByUserMutator.delete(readByUser)
        )).doOnError(throwable -> {
            log.error(String.format("Error marking courseware annotation comment as unread [%s]", readByUser), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
