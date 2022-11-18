package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class EvaluationResultGateway {

    private static final Logger log = LoggerFactory.getLogger(EvaluationResultGateway.class);

    private final Session session;

    private final EvaluationResultByAttemptMutator evaluationResultByAttemptMutator;
    private final EvaluationResultByAttemptMaterializer evaluationResultByAttemptMaterializer;
    private final EvaluationMaterializer evaluationMaterializer;
    private final EvaluationMutator evaluationMutator;

    @Inject
    public EvaluationResultGateway(Session session,
                                   EvaluationResultByAttemptMutator evaluationResultByAttemptMutator,
                                   EvaluationResultByAttemptMaterializer evaluationResultByAttemptMaterializer,
                                   EvaluationMaterializer evaluationMaterializer,
                                   EvaluationMutator evaluationMutator) {
        this.session = session;
        this.evaluationResultByAttemptMutator = evaluationResultByAttemptMutator;
        this.evaluationResultByAttemptMaterializer = evaluationResultByAttemptMaterializer;
        this.evaluationMaterializer = evaluationMaterializer;
        this.evaluationMutator = evaluationMutator;
    }

    /**
     * Persist an evaluation
     *
     * @param evaluation the evaluation to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final Evaluation evaluation) {
        return Mutators.execute(session, Flux.just(
                evaluationResultByAttemptMutator.upsert(evaluation),
                evaluationMutator.upsert(evaluation)
        )).doOnError(throwable -> {
            log.error("error while persisting evaluation", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the Evaluation Result ID by attempt id
     *
     * @param attemptId the attempt id
     * @return a Mono of the evaluation result id
     */
    public Mono<EvaluationResult> findByAttempt(final UUID attemptId) {
        return ResultSets.query(session, evaluationResultByAttemptMaterializer.findByAttempt(attemptId))
                .flatMapIterable(row -> row)
                .map(evaluationResultByAttemptMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the evaluation object by id
     *
     * @param evaluationId the id to find the evaluation for
     * @return a mono of evaluation
     */
    @Trace(async = true)
    public Mono<Evaluation> find(final UUID evaluationId) {
        return ResultSets.query(session, evaluationMaterializer.find(evaluationId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(evaluationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch the evaluation scope data
     *
     * @param evaluationId the evaluation id to find the element scope data for
     * @return a mono of evaluation scope data
     */
    public Mono<EvaluationScopeData> fetchHistoricScope(final UUID evaluationId) {
        return ResultSets.query(session, evaluationMaterializer.findHistoricScope(evaluationId))
                .flatMapIterable(row -> row)
                .map(evaluationMaterializer::fromRowSummary)
                .singleOrEmpty();
    }

}
