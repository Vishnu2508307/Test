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
public class StudentScoreGateway {

    private static final Logger log = LoggerFactory.getLogger(StudentScoreGateway.class);

    private final Session session;

    private final ScoreEntryByElementMutator scoreEntryByElementMutator;
    private final ScoreEntryByElementMaterializer scoreEntryByElementMaterializer;

    @Inject
    public StudentScoreGateway(Session session,
                               ScoreEntryByElementMutator scoreEntryByElementMutator,
                               ScoreEntryByElementMaterializer scoreEntryByElementMaterializer) {
        this.session = session;
        this.scoreEntryByElementMutator = scoreEntryByElementMutator;
        this.scoreEntryByElementMaterializer = scoreEntryByElementMaterializer;
    }

    /**
     * Persist a student score entry object to the database
     *
     * @param studentScoreEntry the student score entry to persist
     * @return a mono of the persisted student score entry
     */
    @Trace(async = true)
    public Mono<StudentScoreEntry> persist(final StudentScoreEntry studentScoreEntry) {
        return Mutators.execute(session, Flux.just(scoreEntryByElementMutator.upsert(studentScoreEntry)))
                .doOnError(throwable -> {
                    if (log.isErrorEnabled()) {
                        log.error("error persisting student score entry", throwable);
                    }
                    throw Exceptions.propagate(throwable);
                })
                .then(Mono.just(studentScoreEntry));
    }

    /**
     * Find student score entries for a specific element attempt
     *
     * @param deploymentId the deployment id the element belongs to
     * @param studentId the student to find the score entries for
     * @param elementId the element the score entries refers to
     * @param attemptId the specific attempt id to find the element score entries for
     * @return a flux of student score entries
     */
    @Trace(async = true)
    public Flux<StudentScoreEntry> find(final UUID deploymentId, final UUID studentId, final UUID elementId,
                                        final UUID attemptId) {
        return ResultSets.query(session, scoreEntryByElementMaterializer
                .find(deploymentId, studentId, elementId, attemptId))
                .flatMapIterable(row -> row)
                .map(scoreEntryByElementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
