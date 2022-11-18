package com.smartsparrow.learner.service;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.data.EvaluationRequest;
import com.smartsparrow.eval.data.EvaluationResponse;
import com.smartsparrow.eval.service.EvaluationService;

import reactor.core.publisher.Mono;

@Singleton
public class EvaluationSubmitService {

    private final Map<EvaluationRequest.Type, Provider<EvaluationService<? extends EvaluationRequest,
            ? extends EvaluationResponse<?>>>> evaluationTypes;

    @Inject
    public EvaluationSubmitService(Map<EvaluationRequest.Type, Provider<EvaluationService<? extends EvaluationRequest,
            ? extends EvaluationResponse<?>>>> evaluationTypes) {
        this.evaluationTypes = evaluationTypes;
    }

    /**
     * Submit a request for evaluation
     *
     * @param evaluationRequest the evaluation request
     * @param type the class of the returned object
     * @param <R> the expected type for the returned object
     * @return a mono containing the evaluation response
     */
    @Trace(async = true)
    @SuppressWarnings("unchecked")
    public <R extends EvaluationResponse<?>> Mono<R> submit(final EvaluationRequest evaluationRequest, Class<R> type) {
        // get the provided implementation
        return ((EvaluationService<EvaluationRequest, ? extends EvaluationResponse<?>>)
                evaluationTypes.get(evaluationRequest.getType()).get())
                // evaluate the request
                .evaluate(evaluationRequest)
                // type cast the response to the expected type
                .map(type::cast);
    }

}
