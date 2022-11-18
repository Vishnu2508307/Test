package com.smartsparrow.learner.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.action.ActionResult;
import com.smartsparrow.eval.data.ActionConsumer;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;

@Singleton
public class LearnerActionConsumerService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerActionConsumerService.class);

    private final Map<Action.Type, Provider<ActionConsumer<? extends Action<? extends ActionContext<?>>, ? extends ActionResult<?>>>> actionConsumers;

    @Inject
    public LearnerActionConsumerService(Map<Action.Type, Provider<ActionConsumer<? extends Action<? extends ActionContext<?>>, ? extends ActionResult<?>>>> actionConsumers) {
        this.actionConsumers = actionConsumers;
    }

    /**
     * Invoke all the action consumers for an evaluation response
     *
     * @param responseContext the context holding all the evaluation information. It is possible that the CHANGE_PROGRESS
     *                        action consumer is gonna change the response context object. Make sure those changes in the
     *                        context have no effect over other consumers
     * @return a flux containing all the action results
     */

    //Not using this method currently but keeping it as backup. We will remove it in the future sprints.
    @Trace(async = true)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Flux<ActionResult> consume(final LearnerEvaluationResponseContext responseContext) {
        log.info("LearnerActionConsumerService.consume() Executing regular actions");
        // get the evaluation response
        return responseContext.getResponse()
                // find the evaluation result
                .getWalkableEvaluationResult()
                // get the triggered actions that have been previously enriched
                .getTriggeredActions()
                // parallel the action processing
                .parallelStream()
                // for each action find the consumer and invoke it
                .map(action -> ((ActionConsumer<Action<? extends ActionContext<?>>, ? extends ActionResult<?>>) actionConsumers.get(action.getType())
                        .get())
                        .consume(action, responseContext).flux())
                // reduce all the action results in a single flux
                .reduce(Flux::concat)
                .orElse(Flux.empty());
    }

    /**
     * Invoke all the action consumers for an evaluation response
     *
     * @param triggeredActions actions that need to be triggered.
     * @param responseContext the context holding all the evaluation information. It is possible that the CHANGE_PROGRESS
     *                        action consumer is gonna change the response context object. Make sure those changes in the
     *                        context have no effect over other consumers
     * @return a flux containing all the action results
     */
    @Trace(async = true)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Flux<ActionResult> consume(List<Action> triggeredActions, LearnerEvaluationResponseContext responseContext) {
        log.info("LearnerActionConsumerService.consume() overloaded method");
        //Added the following log for testing purpose, will clean up later
        if(CollectionUtils.isNotEmpty(triggeredActions) &&  triggeredActions.size() > 0) {
            log.info("number of actions triggered :: " + triggeredActions.size());
            for(Action action : triggeredActions) {
                log.info("action name :: " + action.getType().name());
            }
        }

        // get the evaluation response
        return triggeredActions
                // parallel the action processing
                .parallelStream()
                // for each action find the consumer and invoke it
                .map(action -> ((ActionConsumer<Action<? extends ActionContext<?>>, ? extends ActionResult<?>>) actionConsumers.get(action.getType())
                        .get())
                        .consume(action, responseContext).flux())
                // reduce all the action results in a single flux
                .reduce(Flux::concat)
                .orElse(Flux.empty());
    }
}
