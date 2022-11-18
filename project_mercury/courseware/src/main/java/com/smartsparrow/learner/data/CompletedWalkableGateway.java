package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CompletedWalkableGateway {

    private static final Logger log = LoggerFactory.getLogger(CompletedWalkableGateway.class);

    private final Session session;

    private final CompletedElementByParentMaterializer completedElementByParentMaterializer;
    private final CompletedElementByParentMutator completedElementByParentMutator;
    private final CompletedElementMaterializer completedElementMaterializer;
    private final CompletedElementMutator completedElementMutator;

    @Inject
    public CompletedWalkableGateway(Session session,
                                    CompletedElementByParentMaterializer completedElementByParentMaterializer,
                                    CompletedElementByParentMutator completedElementByParentMutator,
                                    CompletedElementMaterializer completedElementMaterializer,
                                    CompletedElementMutator completedElementMutator) {
        this.session = session;
        this.completedElementByParentMaterializer = completedElementByParentMaterializer;
        this.completedElementByParentMutator = completedElementByParentMutator;
        this.completedElementMaterializer = completedElementMaterializer;
        this.completedElementMutator = completedElementMutator;
    }

    /**
     * Persist a completed walkable
     *
     * @param completedWalkable the completed walkable to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(CompletedWalkable completedWalkable) {
        return Mutators.execute(session, Flux.just(
                completedElementByParentMutator.upsert(completedWalkable),
                completedElementMutator.upsert(completedWalkable)
        )).doOnError(throwable -> {
            log.error("error while persisting completed walkable", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find all completed walkable for a parent element on a specific attempt
     *
     * @param deploymentId the deployment id
     * @param studentId the student to find the completed walkable for
     * @param parentId the parent element of the walkable to find
     * @param parentAttemptId the parent element attempt
     * @return a flux of completed walkable
     */
    public Flux<CompletedWalkable> findAll(UUID deploymentId, UUID studentId, UUID parentId,
                                           UUID parentAttemptId) {
        return ResultSets.query(session, completedElementByParentMaterializer
                .fetchAllCompleted(deploymentId, studentId, parentId, parentAttemptId))
                .flatMapIterable(row -> row)
                .map(completedElementByParentMaterializer::fromRow);
    }

    /**
     * Find a completed walkable for a student by element and attempt id
     *
     * @param deploymentId the deployment the walkable belongs to
     * @param studentId the student id that completed the walkable
     * @param elementId the walkable element id
     * @param elementAttemptId the walkable element attempt id
     * @return a mono of completed walkable or an empty mono when not found
     */
    @Trace(async = true)
    public Mono<CompletedWalkable> find(UUID deploymentId, UUID studentId, UUID elementId, UUID elementAttemptId) {
        return ResultSets.query(session, completedElementMaterializer
                .findByAttempt(deploymentId, studentId, elementId, elementAttemptId))
                .flatMapIterable(row -> row)
                .map(completedElementMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
