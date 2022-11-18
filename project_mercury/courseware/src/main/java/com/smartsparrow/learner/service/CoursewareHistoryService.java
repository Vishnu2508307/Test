package com.smartsparrow.learner.service;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.CompletedWalkableGateway;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareHistoryService {

    private final CompletedWalkableGateway completedWalkableGateway;
    private final AttemptService attemptService;

    @Inject
    public CoursewareHistoryService(CompletedWalkableGateway completedWalkableGateway,
                                    AttemptService attemptService) {
        this.completedWalkableGateway = completedWalkableGateway;
        this.attemptService = attemptService;
    }

    /**
     * Record a student and the evaluation result by creating and persisting a {@link CompletedWalkable}
     *
     * @param studentId the id of the student to record the completed walkable for
     * @param evaluationResult the evaluation result containing the completed walkable data
     * @param elementType the type of walkable to record
     * @return a mono of completed walkable
     */
    public Mono<CompletedWalkable> record(final UUID studentId, final EvaluationResult evaluationResult,
                                          final CoursewareElementType elementType) {

        final UUID evaluationId = evaluationResult.getId();

        CompletedWalkable completedWalkable = new CompletedWalkable()
                .setDeploymentId(evaluationResult.getDeployment().getId())
                .setChangeId(evaluationResult.getDeployment().getChangeId())
                .setStudentId(studentId)
                .setParentElementId(evaluationResult.getParentId())
                .setParentElementAttemptId(evaluationResult.getAttempt().getParentId())
                .setElementId(evaluationResult.getCoursewareElementId())
                .setEvaluationId(evaluationId)
                .setElementAttemptId(evaluationResult.getAttemptId())
                .setParentElementType(CoursewareElementType.PATHWAY)
                .setElementType(elementType)
                .setEvaluatedAt(DateFormat.asRFC1123(evaluationId));

        return completedWalkableGateway.persist(completedWalkable)
                .singleOrEmpty()
                .thenReturn(completedWalkable);
    }

    /**
     * Record a student and the evaluation result by creating and persisting a {@link CompletedWalkable}
     *
     * @param event the progress event being recorded
     * @param attempt the attempt being recorded
     * @param parentPathwayId the id of the parent pathway for this activity
     * @return a mono of completed walkable
     */
    @Trace(async = true)
    public Mono<CompletedWalkable> record(final UpdateCoursewareElementProgressEvent event,
                                          final Attempt attempt,
                                          final UUID parentPathwayId) {

        CompletedWalkable completedWalkable = new CompletedWalkable()
                .setDeploymentId(event.getUpdateProgressEvent().getDeploymentId())
                .setChangeId(event.getUpdateProgressEvent().getChangeId())
                .setStudentId(event.getUpdateProgressEvent().getStudentId())
                .setParentElementId(parentPathwayId)
                .setParentElementAttemptId(attempt.getParentId())
                .setElementId(event.getElement().getElementId())
                .setEvaluationId(event.getUpdateProgressEvent().getEvaluationId())
                .setElementAttemptId(attempt.getId())
                .setParentElementType(CoursewareElementType.PATHWAY)
                .setElementType(event.getElement().getElementType())
                .setEvaluatedAt(DateFormat.asRFC1123(event.getUpdateProgressEvent().getEvaluationId()));

        return completedWalkableGateway.persist(completedWalkable)
                .singleOrEmpty()
                .thenReturn(completedWalkable);
    }

    /**
     * Record a student and the evaluation result by creating and persisting a {@link CompletedWalkable}
     *
     * @param evaluationId the id of the walkable evaluation
     * @param evaluationRequest the evaluation request containing the completed walkable data
     * @param element the walkable to record
     * @return a mono of completed walkable
     */
    @Trace(async = true)
    public Mono<CompletedWalkable> record(final UUID evaluationId,
                                          final LearnerEvaluationRequest evaluationRequest,
                                          final CoursewareElement element,
                                          final Attempt attempt,
                                          final UUID parentPathwayId) {

        CompletedWalkable completedWalkable = new CompletedWalkable()
                .setDeploymentId(evaluationRequest.getDeployment().getId())
                .setChangeId(evaluationRequest.getDeployment().getChangeId())
                .setStudentId(evaluationRequest.getStudentId())
                .setParentElementId(parentPathwayId)
                .setParentElementAttemptId(attempt.getParentId())
                .setElementId(element.getElementId())
                .setEvaluationId(evaluationId)
                .setElementAttemptId(attempt.getId())
                .setParentElementType(CoursewareElementType.PATHWAY)
                .setElementType(element.getElementType())
                .setEvaluatedAt(DateFormat.asRFC1123(evaluationId));

        return completedWalkableGateway.persist(completedWalkable)
                .singleOrEmpty()
                .thenReturn(completedWalkable);
    }

    /**
     * Fetch the history of completed walkables for a student on a pathway. When the latest pathway attempt is not
     * found an empty flux of completed walkable is returned.
     *
     * @param learnerPathway the pathway to find the completed walkable elements for
     * @param studentId the student that completed the walkable elements
     * @param pathwayAttemptId the pathway attempt id
     * @return a flux of completed walkable elements
     */
    @Trace(async = true)
    public Flux<CompletedWalkable> fetchHistory(@Nonnull final LearnerPathway learnerPathway,
                                                @Nonnull final UUID studentId,
                                                final UUID pathwayAttemptId) {

        final Mono<UUID> wantedAttemptIdMono = getWantedAttemptId(pathwayAttemptId);

        return fetchHistoryFlux(learnerPathway,studentId,wantedAttemptIdMono)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch the history of completed walkables for a student on a pathway. When the latest pathway attempt is not
     * found an empty flux of completed walkable is returned.
     *
     * @param learnerPathway the pathway to find the completed walkable elements for
     * @param studentId the student that completed the walkable elements
     * @return a flux of completed walkable elements
     */
    @Trace(async = true)
    public Flux<CompletedWalkable> fetchHistory(@Nonnull final LearnerPathway learnerPathway,
                                                @Nonnull final UUID studentId) {

        final Mono<UUID> wantedAttemptIdMono = getWantedAttemptId(learnerPathway, studentId);

        return fetchHistoryFlux(learnerPathway,studentId,wantedAttemptIdMono)
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Fetch the history of completed walkables for a student on a pathway. When the latest pathway attempt is not
     * found an empty flux of completed walkable is returned.
     *
     * @param learnerPathway the pathway to find the completed walkable elements for
     * @param studentId the student that completed the walkable elements
     * @param wantedAttemptIdMono the pathway attempt id
     * @return a flux of completed walkable elements
     */
    @Trace(async = true)
    public Flux<CompletedWalkable> fetchHistoryFlux(@Nonnull final LearnerPathway learnerPathway,
                                                    @Nonnull final UUID studentId,
                                                    Mono<UUID> wantedAttemptIdMono) {

        final UUID deploymentId = learnerPathway.getDeploymentId();

        return wantedAttemptIdMono
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flux()
                .flatMap(wantedAttemptId -> completedWalkableGateway.findAll(deploymentId, studentId, learnerPathway.getId(),
                                                                                        wantedAttemptId)
                )
                .onErrorResume(AttemptNotFoundFault.class, ex -> {
                    // when the latest pathway attempt is not found, there is no history, therefore return empty flux
                    return Flux.empty();
                });

    }

    @Trace(async = true)
    private Mono<UUID> getWantedAttemptId(final UUID pathwayAttemptId) {
        return Mono.just(pathwayAttemptId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<UUID> getWantedAttemptId(final LearnerPathway learnerPathway, final UUID studentId) {
        return attemptService.findLatestAttempt(learnerPathway.getDeploymentId(),
                                                learnerPathway.getId(), studentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(Attempt::getId);
    }


    /**
     * Find a completed walkable for a student by element and attempt ids
     *
     * @param deploymentId the deployment the walkable belongs to
     * @param studentId the student that completed the walkable
     * @param elementId the walkable element id
     * @param elementAttemptId the walkable element attempt id
     * @return a mono of completed walkable or empty mono when not found
     */
    @Trace(async = true)
    public Mono<CompletedWalkable> findCompletedWalkable(final UUID deploymentId, final UUID studentId,
                                                         final UUID elementId, final UUID elementAttemptId) {
        return completedWalkableGateway.find(deploymentId, studentId, elementId, elementAttemptId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
