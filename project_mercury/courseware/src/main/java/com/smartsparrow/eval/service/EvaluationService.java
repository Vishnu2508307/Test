package com.smartsparrow.eval.service;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.data.EvaluationRequest;
import com.smartsparrow.eval.data.EvaluationResponse;

import reactor.core.publisher.Mono;

/**
 * The evaluation service interface
 *
 * @param <T> the type of evaluation request to take in as argument
 * @param <R> the type of evaluation response to return
 */
public interface EvaluationService<T extends EvaluationRequest, R extends EvaluationResponse<T>> {

    /**
     * Takes in an {@link EvaluationRequest} of type {@link T} and responds with a generated
     * {@link EvaluationResponse} of type {@link T}
     *
     * @param t the evaluation request to evaluate
     * @return a mono cotaining the evaluation response {@link R}
     */
    @Trace(async = true)
    Mono<R> evaluate(final T t);
}
