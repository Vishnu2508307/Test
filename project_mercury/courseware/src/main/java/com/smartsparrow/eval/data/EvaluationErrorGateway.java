package com.smartsparrow.eval.data;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class EvaluationErrorGateway {

    private static final Logger log = LoggerFactory.getLogger(EvaluationErrorGateway.class);

    private final Session session;
    private final EvaluationErrorMutator evaluationErrorMutator;
    private final EvaluationErrorMaterializer evaluationErrorMaterializer;

    @Inject
    public EvaluationErrorGateway(final Session session,
                                  final EvaluationErrorMutator evaluationErrorMutator,
                                  final EvaluationErrorMaterializer evaluationErrorMaterializer) {
        this.session = session;
        this.evaluationErrorMutator = evaluationErrorMutator;
        this.evaluationErrorMaterializer = evaluationErrorMaterializer;
    }

    /**
     * Persist an evaluation error to the database
     *
     * @param evaluationError the evaluation error to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final EvaluationError evaluationError) {
        return Mutators.execute(session, Flux.just(
                evaluationErrorMutator.upsert(evaluationError)
        )).doOnError(throwable -> {
            log.error(String.format("unable to persist evaluation error %s", evaluationError.toString()), throwable);
            // lol where are we gonna save this error now !?
            throw Exceptions.propagate(throwable);
        });
    }
}
