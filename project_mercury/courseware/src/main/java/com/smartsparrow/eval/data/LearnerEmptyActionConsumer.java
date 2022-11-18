package com.smartsparrow.eval.data;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

/**
 * This is an empty action consumer that does nothing. For all the actions that do not require
 * any backend logic
 */
public class LearnerEmptyActionConsumer implements ActionConsumer<Action<? extends ActionContext<?>>, EmptyActionResult> {

    private final ActionConsumerOptions options;

    @Inject
    public LearnerEmptyActionConsumer() {
        this.options = new ActionConsumerOptions()
                .setAsync(false);
    }

    @Trace(async = true)
    @Override
    public Mono<EmptyActionResult> consume(Action<? extends ActionContext<?>> action, LearnerEvaluationResponseContext context) {
        return Mono.just(new EmptyActionResult(action))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Override
    public Mono<ActionConsumerOptions> getActionConsumerOptions() {
        return Mono.just(options);
    }
}
