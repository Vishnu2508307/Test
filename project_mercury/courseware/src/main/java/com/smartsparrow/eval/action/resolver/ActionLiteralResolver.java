package com.smartsparrow.eval.action.resolver;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Mono;

public class ActionLiteralResolver implements Resolver<Action, DataType, ActionContext> {

    @Trace(async = true)
    @SuppressWarnings("unchecked")
    @Override
    public Mono<Action> resolve(Action resolvable,  ActionContext context) {
        resolvable.setResolvedValue(context.getValue());
        return Mono.just(resolvable);
    }
    @Trace(async = true)
    @SuppressWarnings("unchecked")
    @Override
    public Mono<Action> resolve(Action resolvable, DataType dataType, ActionContext context) {
        resolvable.setResolvedValue(context.getValue());
        return Mono.just(resolvable);
    }
}
