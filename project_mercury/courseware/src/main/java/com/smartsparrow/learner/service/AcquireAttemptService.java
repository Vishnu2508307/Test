package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * Build and manage Attempt information.
 * <p>
 * Notes:
 * 1. Attempt values are 1 (one) based
 * 2. Attempts should be considered immutable
 * 3. Attempts are created based on their parent courseware element's attempt data (all the way up the tree)
 * 4. A change of a parent courseware element's attempt data will create new attempt data on ALL elements below.
 * 5. An activity and pathway's attempt value can only be incremented by an external operation, not by an acquire();
 * 6. An interactive's attempt value will be incremented if it has had an evaluation performed against it.
 * TODO all {@link IllegalStateException} thrown in this class should be replaced by an {@link IllegalStateFault}
 */

@Singleton
public class AcquireAttemptService {

    private static final Logger log = LoggerFactory.getLogger(AcquireAttemptService.class);
    private static final String ILLEGAL_STATE_MSG = "Unable to find attempt";

    private final LearnerActivityService learnerActivityService;
    private final LearnerPathwayService learnerPathwayService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final AttemptService attemptService;
    private final PathwayAttemptResolverProvider pathwayAttemptResolverProvider;

    @Inject
    public AcquireAttemptService(LearnerActivityService learnerActivityService,
                                 LearnerPathwayService learnerPathwayService,
                                 LearnerInteractiveService learnerInteractiveService,
                                 AttemptService attemptService,
                                 PathwayAttemptResolverProvider pathwayAttemptResolverProvider) {
        this.learnerActivityService = learnerActivityService;
        this.learnerPathwayService = learnerPathwayService;
        this.learnerInteractiveService = learnerInteractiveService;
        this.attemptService = attemptService;
        this.pathwayAttemptResolverProvider = pathwayAttemptResolverProvider;
    }

    /**
     * Acquire (get or create) the latest interactive attempt
     *
     * @param deploymentId  the deployment id
     * @param interactiveId the interactive id
     * @param studentId     the student id
     * @return a mono of latest interactive attempt
     * @throws IllegalArgumentFault  when any of the required argument is <code>null</code>
     * @throws IllegalStateException when the courseware element structure is broken
     */
    @Trace(async = true)
    public Mono<Attempt> acquireLatestInteractiveAttempt(final UUID deploymentId, final UUID interactiveId, final UUID studentId) {
        affirmNotNull(deploymentId, "deploymentId is required");
        affirmNotNull(interactiveId, "interactiveId is required");
        affirmNotNull(studentId, "studentId is required");

        // FIXME: add authorization
        // FIXME: add business checks, e.g. exceeding number of defined attempts.

        Mono<Attempt> parentPathwayAttemptMono = learnerInteractiveService.findParentPathwayId(interactiveId, deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(ParentPathwayNotFoundException.class, ex -> {
                    // the parent pathway is not found, this should not happen
                    throw new IllegalStateException(ILLEGAL_STATE_MSG);
                })
                // get the parent pathway id
                .flatMap(parentPathwayId -> {
                    // obtain the latest attempt for the the parent pathway.
                    return acquireLatestPathwayAttempt(deploymentId, parentPathwayId, studentId)
                            // ensure that a parent pathway exists; do not continue if this is the case.
                            .doOnError(IllegalStateException.class, ex -> {
                                log.debug("did not find parent activity attempt; deploymentId {} interactiveId {} parentPathwayId {}",
                                        deploymentId, interactiveId, parentPathwayId);
                                throw Exceptions.propagate(ex);
                            });
                });

        return parentPathwayAttemptMono
                // acquire the attempt for the interactive
                .flatMap(parentPathwayAttempt -> {
                    return acquireLatestAttempt(studentId, deploymentId, INTERACTIVE, interactiveId, parentPathwayAttempt)
                            .flatMap(interactiveAttempt -> {
                                // return an attempt based on pathway type
                                return resolveInteractiveAttempt(deploymentId, interactiveId, studentId,
                                        parentPathwayAttempt, interactiveAttempt);
                            });
                });
    }

    /**
     * Create a new increased interactive attempt if an evaluation result is found or resolve to actual interactive
     * attempt
     *
     * @param deploymentId         the deployment id
     * @param interactiveId        the interactive id
     * @param studentId            the student id
     * @param parentPathwayAttempt the parent pathway attempt
     * @param interactiveAttempt   the current interactive attempt
     * @return an interactive attempt with a resolved value
     */
    private Mono<Attempt> resolveInteractiveAttempt(final UUID deploymentId, final UUID interactiveId, final UUID studentId,
                                                    final Attempt parentPathwayAttempt, final Attempt interactiveAttempt) {

        return learnerPathwayService.find(parentPathwayAttempt.getCoursewareElementId(), deploymentId)
                .map(Pathway::getType)
                .map(pathwayAttemptResolverProvider::get)
                .flatMap(resolver -> resolver.resolveInteractiveAttempt(deploymentId, interactiveId, studentId,
                        parentPathwayAttempt, interactiveAttempt));
    }

    /**
     * Acquire (get or create) the latest attempt for the specified pathway.
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the pathway id
     * @param studentId    the student id
     * @return a mono of the latest pathway attempt
     * @throws IllegalArgumentFault  when any of the required argument is <code>null</code>
     * @throws IllegalStateException when the parent activity is not found
     */
    @Trace(async = true)
    public Mono<Attempt> acquireLatestPathwayAttempt(final UUID deploymentId, final UUID pathwayId, final UUID studentId) {
        affirmNotNull(deploymentId, "deploymentId is required");
        affirmNotNull(pathwayId, "pathwayId is required");
        affirmNotNull(studentId, "studentId is required");

        // FIXME: add authorization

        // Get the parent activity id.
        Mono<UUID> parentActivityIdMono = learnerPathwayService.findParentActivityId(pathwayId, deploymentId);

        return parentActivityIdMono.flatMap(parentActivityId -> {
            return acquireLatestActivityAttempt(deploymentId, parentActivityId, studentId)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .flatMap(parentActivityAttempt -> {
                        return acquireLatestAttempt(studentId, deploymentId, PATHWAY, pathwayId, parentActivityAttempt);
                    });
        }).doOnError(ParentActivityNotFoundException.class, ex -> {
            log.debug("did not find parent activity; deploymentId {} pathwayId {}", deploymentId, pathwayId);
            throw new IllegalStateException(ILLEGAL_STATE_MSG);
        });
    }

    /**
     * Acquire an attempt for a given courseware element (child) without parent attempt.
     *
     * @param studentId             the student id to find the attempt for
     * @param deploymentId          the deployment id
     * @param coursewareElementType the courseware element type
     * @param coursewareElementId   the courseware element id (or the child in the tree)
     * @return the latest attempt or a new one if the applicable.
     */
    @Trace(async = true)
    private Mono<Attempt> acquireLatestAttemptWithOutParentAttempt(final UUID studentId, final UUID deploymentId,
                                               final CoursewareElementType coursewareElementType,
                                               final UUID coursewareElementId) {

        // special case for handling the root node - use the latest or create new
        // Return the found attempt.
        return attemptService.findLatestAttempt(deploymentId, coursewareElementId, studentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .onErrorResume(AttemptNotFoundFault.class, ex -> {
                    // Create a new attempt.
                    return attemptService.newAttempt(deploymentId, studentId, coursewareElementType, coursewareElementId, null);
                });
    }

    /**
     * Acquire an attempt for a given courseware element (child) with the provided parent attempt.
     * <p>
     * This method:
     * 1. Checks if there is a current attempt for the target, returns a new one if not.
     * 2. Checks if the current attempt's parent matches up (same id), creates a new one if not.
     * 3. Returns the current attempt in all other cases.
     * X. Custom business logic to be performed after should go in the type-specific acquire methods.
     *
     * @param studentId             the student id to find the attempt for
     * @param deploymentId          the deployment id
     * @param coursewareElementType the courseware element type
     * @param coursewareElementId   the courseware element id (or the child in the tree)
     * @param parentAttempt         the attempt of the parent element; can be null for the root element.
     * @return the latest attempt or a new one if the applicable.
     */
    @Trace(async = true)
    private Mono<Attempt> acquireLatestAttempt(final UUID studentId, final UUID deploymentId,
                                               final CoursewareElementType coursewareElementType,
                                               final UUID coursewareElementId, Attempt parentAttempt) {

        final UUID parentAttemptId = parentAttempt.getId();

        // find the current attempt for the target
        return attemptService.findLatestAttempt(deploymentId, coursewareElementId, studentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(targetAttempt -> {
                    // does this target attempt have a parentId? if not we are the root!
                    if (targetAttempt.getParentId() != null) {
                        // has the parent attempt changed? then we need to reset/create a new current attempt.
                        if (!targetAttempt.getParentId().equals(parentAttemptId)) {
                            // create a new attempt, associate to pathway's attempt id; set value = 1
                            return attemptService.newAttempt(deploymentId, studentId, coursewareElementType, coursewareElementId, parentAttemptId);
                        }
                    }

                    return Mono.just(targetAttempt);
                })
                // if there is no previous attempt (the child), simply create one.
                .onErrorResume(AttemptNotFoundFault.class, ex -> {
                    // create a new attempt, associate to parent's attempt id; set value = 1
                    return attemptService.newAttempt(deploymentId, studentId, coursewareElementType, coursewareElementId, parentAttemptId);
                });

    }

    /**
     * Acquire the latest attempt for the specified courseware element
     *
     * @param deploymentId the deployment id
     * @param elementId    the courseware element id
     * @param elementType  the courseware element type
     * @param studentId    the student id
     * @return Mono of the latest attempt
     */
    @Trace(async = true)
    public Mono<Attempt> acquireLatestAttempt(final UUID deploymentId, final UUID elementId,
                                              final CoursewareElementType elementType, final UUID studentId) {
        affirmNotNull(elementType, "elementType is required");

        switch (elementType) {
            case ACTIVITY: {
                return acquireLatestActivityAttempt(deploymentId, elementId, studentId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            }
            case INTERACTIVE: {
                return acquireLatestInteractiveAttempt(deploymentId, elementId, studentId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            }
            case PATHWAY: {
                return acquireLatestPathwayAttempt(deploymentId, elementId, studentId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            }
            default: {
                throw new UnsupportedOperationException("Attempts are not supported for this element type: " + elementType);
            }
        }
    }

    /**
     * Acquire the latest attempt for the specified activity
     *
     * @param deploymentId the deployment id
     * @param activityId   the activity id
     * @param studentId    the student id
     * @return a mono of the latest activity attempt
     * @throws IllegalArgumentFault when any of the required argument is <code>null</code>
     */
    @Trace(async = true)
    public Mono<Attempt> acquireLatestActivityAttempt(final UUID deploymentId, final UUID activityId, final UUID studentId) {
        affirmNotNull(deploymentId, "deploymentId is required");
        affirmNotNull(activityId, "activityId is required");
        affirmNotNull(studentId, "studentId is required");

        // FIXME: add authorization

        // Get the parent pathway id.
        Mono<UUID> parentPathwayIdMono = learnerActivityService.findParentPathwayId(activityId, deploymentId).
                doOnEach(ReactiveTransaction.linkOnNext());

        // The parent pathway can be null (we are the root!)
        Mono<Attempt> parentPathwayAttemptMono = parentPathwayIdMono.flatMap(parentPathwayId -> {
            // we are not the root, fetch the pathway's attempt to feed in.
            return acquireLatestPathwayAttempt(deploymentId, parentPathwayId, studentId);
        });

        return parentPathwayAttemptMono.flatMap(parentPathwayAttempt -> {
            return acquireLatestAttempt(studentId, deploymentId, ACTIVITY, activityId, parentPathwayAttempt);
        }).doOnEach(ReactiveTransaction.linkOnNext()).onErrorResume(LearnerPathwayNotFoundFault.class, ex -> {
            // The parent pathway can be null (we are the root!)
            return acquireLatestAttemptWithOutParentAttempt(studentId, deploymentId, ACTIVITY, activityId);
        });
    }

}
