package com.smartsparrow.learner.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.EvaluationResultGateway;
import com.smartsparrow.learner.lang.EvaluationResultNotFoundFault;
import com.smartsparrow.learner.payload.StudentScopePayload;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

@Singleton
public class EvaluationResultService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationResultService.class);

    private final EvaluationResultGateway evaluationResultGateway;

    @Inject
    public EvaluationResultService(EvaluationResultGateway evaluationResultGateway) {
        this.evaluationResultGateway = evaluationResultGateway;
    }

    /**
     * Find an evaluation result by attempt
     * @param attemptId the attempt id to find the evaluation result for
     * @return a mono of evaluation result
     * @throws EvaluationResultNotFoundFault when the evaluation result is not found for the attempt
     */
    public Mono<EvaluationResult> findByAttempt(final UUID attemptId) {
        return evaluationResultGateway.findByAttempt(attemptId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new EvaluationResultNotFoundFault(attemptId);
                });
    }

    /**
     * Persist an evaluation
     *
     * @param evaluation the evaluation to persist
     * @return a mono of evaluation
     */
    @Trace(async = true)
    public Mono<Evaluation> persist(Evaluation evaluation) {
        return evaluationResultGateway.persist(evaluation)
                .doOnError(throwable -> {
                    log.error("error persisting evaluationResult", throwable);
                    throw Exceptions.propagate(throwable);
                })
                .then(Mono.just(evaluation));
    }

    /**
     * Fetch an evaluation by id
     *
     * @param evaluationId the id of the evaluation to find
     * @return a mono of evaluation
     * @throws NotFoundFault when the evaluation is not found for the supplied id
     */
    @Trace(async = true)
    public Mono<Evaluation> fetch(final UUID evaluationId) {
        return evaluationResultGateway.find(evaluationId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault(String.format("evaluation with id `%s` not found", evaluationId));
                });
    }

    /**
     * Fetch the historic scope for an evaluation id and returns it as a student scope payload to be consistent in format.
     *
     * @param evaluationId the evaluation to find the scope data for
     * @return a mono list of student scope payload
     */
    public Mono<List<StudentScopePayload>> fetchHistoricScope(final UUID evaluationId) {

        return evaluationResultGateway.fetchHistoricScope(evaluationId)
                .map(evaluationScopeData -> {
                    final UUID studentScopeURN = evaluationScopeData.getStudentScopeURN();

                    return evaluationScopeData.getStudentScopeDataMap()
                            .entrySet()
                            .stream()
                            .map(entry -> StudentScopePayload.from(entry.getKey(), studentScopeURN, entry.getValue()))
                            .collect(Collectors.toList());
                });
    }
}
