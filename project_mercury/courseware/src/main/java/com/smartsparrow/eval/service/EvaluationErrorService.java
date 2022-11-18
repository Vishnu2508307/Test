package com.smartsparrow.eval.service;

import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.data.EvaluationError;
import com.smartsparrow.eval.data.EvaluationErrorGateway;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class EvaluationErrorService {

    private final EvaluationErrorGateway evaluationErrorGateway;

    @Inject
    public EvaluationErrorService(final EvaluationErrorGateway evaluationErrorGateway) {
        this.evaluationErrorGateway = evaluationErrorGateway;
    }

    /**
     * Create an evaluation error of {@link EvaluationError.Type#GENERIC}
     *
     * @param throwable the throwable to create the error from
     * @param evaluationId the evaluation id the error occurred for
     * @return a mono of evaluation error
     */
    @Trace(async = true)
    public Mono<EvaluationError> createGeneric(final Throwable throwable, final UUID evaluationId) {
        return create(throwable, evaluationId, EvaluationError.Type.GENERIC)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create an evaluation error
     *
     * @param throwable the throwable to create the error from
     * @param evaluationId the evaluation id the error occurred for
     * @param errorType the error type
     * @return a mono of evaluation error
     */
    public Mono<EvaluationError> create(final Throwable throwable, final UUID evaluationId,
                                        final EvaluationError.Type errorType) {
        final UUID id = UUIDs.timeBased();

        String stacktrace = Arrays.stream(throwable.getStackTrace())
                // build the stacktrace string
                .map(StackTraceElement::toString)
                // add all built stacktrace strings
                .reduce((prev, next) -> {
                    return String.format("%s%s%s", prev, System.lineSeparator(), next);
                }).orElse("");

        final EvaluationError evaluationError = new EvaluationError()
                .setEvaluationId(evaluationId)
                .setId(id)
                .setType(errorType)
                .setOccurredAt(DateFormat.asRFC1123(id))
                .setError(throwable.getMessage())
                .setStacktrace(stacktrace);

        return evaluationErrorGateway.persist(evaluationError)
                .then(Mono.just(evaluationError));
    }
}
