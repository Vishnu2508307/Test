package com.smartsparrow.eval.data;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionResult;

import reactor.core.publisher.Mono;

/**
 * Interface that takes in an action, consumes it and returns an action result
 *
 * @param <T> the type of action parameter for the consume method
 * @param <R> the return type of action result for the consume method
 */
@SuppressWarnings("rawtypes")
public interface ActionConsumer<T extends Action, R extends ActionResult> {

    /**
     * Consumes an action and returns an action result
     *
     * @param t the action to consume
     * @param context the evaluation response context that holds all the information about evaluation
     *                note this method might make changes to the context
     * @return a mono with the action result
     */
    @Trace(async = true)
    Mono<R> consume(T t, LearnerEvaluationResponseContext context);

    /**
     * @return get the options for this action consumer
     */
    Mono<ActionConsumerOptions> getActionConsumerOptions();
}
