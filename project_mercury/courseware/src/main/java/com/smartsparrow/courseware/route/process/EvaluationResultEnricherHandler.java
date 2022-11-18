package com.smartsparrow.courseware.route.process;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.EvaluationEventMessage;

public class EvaluationResultEnricherHandler {

    @Inject
    public EvaluationResultEnricherHandler() {
    }

    @Handler
    public void handle(Exchange exchange) {

        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        final EvaluationResult evaluationResult = eventMessage.getEvaluationResult();

        // get the incoming aggregated actions
        Message in = exchange.getIn();
        @SuppressWarnings("unchecked")
        List<Action> actions = in.getBody(List.class);

        // set the triggered actions to the evaluation result
        evaluationResult.setTriggeredActions(actions);
        // mark the interactive complete value based on the actions
        evaluationResult.setInteractiveComplete(isCompleted(actions));

        // send back the actions on the wire
        exchange.getOut().setBody(actions);
    }

    /**
     * Filter the triggered {@link Action.Type#CHANGE_PROGRESS} actions and check if any is of
     * {@link ProgressionType#INTERACTIVE_COMPLETE}
     *
     * @param actions the triggered actions
     * @return <code>true</code> when an action type {@link Action.Type#CHANGE_PROGRESS} has a
     * {@link ProgressionType#INTERACTIVE_COMPLETE} or <code>false</code> for any other different action/progressionType
     * when a {@link ClassCastException} is thrown
     */
    private boolean isCompleted(List<Action> actions) {

        // filter all actions by change progress
        final List<Action> changeProgressActions = actions.stream()
                .filter(action -> action.getType().equals(Action.Type.CHANGE_PROGRESS))
                .collect(Collectors.toList());

        // if the list is empty, there are no change progress actions therefore the interactive is not completed
        if (changeProgressActions.isEmpty()) {
            return false;
        }

        return changeProgressActions.stream()
                .anyMatch(action -> {
                    try {
                        ProgressAction progressAction = (ProgressAction) action;
                        return progressAction.isInteractiveCompleted();
                    } catch (ClassCastException e) {
                        return false;
                    }
                });
    }
}
