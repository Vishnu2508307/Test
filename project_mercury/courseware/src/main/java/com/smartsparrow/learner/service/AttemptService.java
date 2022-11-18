package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.AttemptGateway;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

/**
 * This service contains only CRUD methods for attempts.
 * Logic for acquiring new attempts can be found in {@link AcquireAttemptService}
 */
@Singleton
public class AttemptService {

    private static final Logger log = LoggerFactory.getLogger(AttemptService.class);

    private final AttemptGateway attemptGateway;

    @Inject
    public AttemptService(AttemptGateway attemptGateway) {
        this.attemptGateway = attemptGateway;
    }

    /**
     * Find the latest attempt for the specified courseware element
     *
     * @param deploymentId        the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId           the student id
     * @return a mono of the latest attempt for the specified courseware element
     * @throws AttemptNotFoundFault when the latest attempt is not found
     * @throws IllegalArgumentFault when any of the required argument is <code>null</code>
     */
    @Trace(async = true)
    public Mono<Attempt> findLatestAttempt(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        affirmNotNull(deploymentId, "deploymentId is required");
        affirmNotNull(coursewareElementId, "courseware element id is required");
        affirmNotNull(studentId, "student id is required");

        return attemptGateway.findLatest(deploymentId, coursewareElementId, studentId)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, cause -> {
                    throw new AttemptNotFoundFault(
                            String.format("attempt not found for deployment %s, element %s, student %s",
                                    deploymentId, coursewareElementId, studentId)
                    );
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find attempt by id
     *
     * @param attemptId the attempt id
     * @return Mono of the attempt; empty mono if no attempt found
     */
    @Trace(async = true)
    public Mono<Attempt> findById(final UUID attemptId) {
        return attemptGateway.findById(attemptId);
    }

    /**
     * Create and persist a new attempt, with the specified value
     *
     * @param deploymentId          the deployment id
     * @param studentId             the student id
     * @param coursewareElementType the courseware element type
     * @param coursewareElementId   the courseware element id
     * @param parentId              the attempt parent id
     * @param val                   the value to set
     * @return a mono of the created attempt
     * @throws IllegalArgumentFault when the supplied value of the attempt is less than <strong>1</strong>
     */
    @Trace(async = true)
    public Mono<Attempt> newAttempt(final UUID deploymentId, final UUID studentId,
                                    final CoursewareElementType coursewareElementType, final UUID coursewareElementId,
                                    final UUID parentId, final int val) {
        affirmArgument(val >= 1, "attempt value must be positive");

        Attempt a = new Attempt() //
                .setId(UUIDs.timeBased()) //
                .setParentId(parentId) //
                .setDeploymentId(deploymentId) //
                .setCoursewareElementId(coursewareElementId) //
                .setCoursewareElementType(coursewareElementType) //
                .setStudentId(studentId) //
                .setValue(val);

        if (log.isDebugEnabled()) {
            log.debug("creating attempt: {}", a);
        }

        return attemptGateway.persist(a)
                .then(Mono.just(a));
    }

    /**
     * Create and persist a new attempt, with a value of 1.
     *
     * @param deploymentId          the deployment id
     * @param studentId             the student id
     * @param coursewareElementType the courseware element type
     * @param coursewareElementId   the courseware element id
     * @param parentId              the attempt parent id
     * @return a new attempt
     */
    Mono<Attempt> newAttempt(final UUID deploymentId, final UUID studentId,
                             final CoursewareElementType coursewareElementType, final UUID coursewareElementId,
                             final UUID parentId) {
        return newAttempt(deploymentId, studentId, coursewareElementType, coursewareElementId, parentId, 1);
    }

}
