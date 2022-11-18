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
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AttemptGateway {
    private static final Logger log = LoggerFactory.getLogger(AttemptGateway.class);

    private final Session session;

    private final AttemptMutator attemptMutator;
    private final AttemptMaterializer attemptMaterializer;
    private final AttemptByCoursewareMutator attemptByCoursewareMutator;
    private final AttemptByCoursewareMaterializer attemptByCoursewareMaterializer;

    @Inject
    public AttemptGateway(Session session,
            AttemptMutator attemptMutator,
            AttemptMaterializer attemptMaterializer,
            AttemptByCoursewareMutator attemptByCoursewareMutator,
            AttemptByCoursewareMaterializer attemptByCoursewareMaterializer) {
        this.session = session;
        this.attemptMutator = attemptMutator;
        this.attemptMaterializer = attemptMaterializer;
        this.attemptByCoursewareMutator = attemptByCoursewareMutator;
        this.attemptByCoursewareMaterializer = attemptByCoursewareMaterializer;
    }

    @Trace(async = true)
    public Flux<Void> persist(final Attempt attempt) {
        return Mutators.execute(session, Flux.just(attemptMutator.upsert(attempt),
                                                   attemptByCoursewareMutator.upsert(attempt))) //
                .doOnError(throwable -> {
                    log.warn(throwable.getMessage());
                    throw Exceptions.propagate(throwable);
                });
    }

    @Trace(async = true)
    public Mono<Attempt> findById(final UUID attemptId) {
        return ResultSets.query(session, attemptMaterializer.find(attemptId))
                .flatMapIterable(row -> row)
                .map(attemptMaterializer::fromRow)
                .singleOrEmpty();
    }

    @Trace(async = true)
    public Mono<Attempt> findLatest(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        return ResultSets.query(session, attemptByCoursewareMaterializer.findLatest(deploymentId, coursewareElementId,
                                                                                    studentId))
                .flatMapIterable(row -> row)
                .map(attemptByCoursewareMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
