package com.smartsparrow.eval.action;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.action.resolver.ActionLiteralResolver;
import com.smartsparrow.eval.action.resolver.DeprecatedActionScopeResolver;
import com.smartsparrow.eval.action.resolver.ActionScopeResolver;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.learner.event.EvaluationEventMessage;

import reactor.core.publisher.Mono;

public class ActionResolver {

    private final ActionLiteralResolver actionLiteralResolver;
    private final DeprecatedActionScopeResolver deprecatedActionScopeResolver;
    private final ActionScopeResolver actionScopeResolver;

    @Inject
    public ActionResolver(final ActionLiteralResolver actionLiteralResolver,
                          final DeprecatedActionScopeResolver deprecatedActionScopeResolver,
                          final ActionScopeResolver actionScopeResolver) {
        this.actionLiteralResolver = actionLiteralResolver;
        this.deprecatedActionScopeResolver = deprecatedActionScopeResolver;
        this.actionScopeResolver = actionScopeResolver;
    }

    /**
     * Resolve the action using the information stored in the evaluation event message
     *
     * @param action the action to resolve
     * @param eventMessage the event message providing additional information
     * @return a mono of resolved action
     * @throws UnsupportedOperationFault when the resolver type is not supported
     */
    @SuppressWarnings({"Duplicates", "rawtypes"})
    @Deprecated
    public Mono<Action> resolve(Action action, EvaluationEventMessage eventMessage) {
        ResolverContext resolverContext = action.getResolver();

        Resolver.Type resolverType = resolverContext.getType();

        switch (resolverType) {
            case LITERAL:
                return actionLiteralResolver.resolve(action,action.getContext());
            case SCOPE:
                return deprecatedActionScopeResolver.resolve(action, action.getContext().getDataType(), eventMessage);
            case WEB:
                throw new UnsupportedOperationFault(String.format("Unsupported action resolver %s", resolverType));
            default:
                throw new UnsupportedOperationFault(String.format("Unsupported action resolver %s", resolverType));
        }
    }

    /**
     * Resolve the action using the information stored in the evaluation event message
     *
     * @param action the action to resolve
     * @param context the evaluation response context providing additional information
     * @return a mono of resolved action
     * @throws UnsupportedOperationFault when the resolver type is not supported
     */
    @Trace(async = true)
    @SuppressWarnings({"Duplicates", "rawtypes"})
    public Mono<Action> resolve(Action action, LearnerEvaluationResponseContext context) {
        ResolverContext resolverContext = action.getResolver();

        Resolver.Type resolverType = resolverContext.getType();

        switch (resolverType) {
            case LITERAL:
                return actionLiteralResolver.resolve(action, action.getContext());
            case SCOPE:
                return actionScopeResolver.resolve(action, action.getContext().getDataType(), context);
            case WEB:
                throw new UnsupportedOperationFault(String.format("Unsupported action resolver %s", resolverType));
            default:
                throw new UnsupportedOperationFault(String.format("Unsupported action resolver %s", resolverType));
        }
    }
}
