package com.smartsparrow.courseware.route.process;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.eval.action.Action.Type.CHANGE_PROGRESS;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.route.EvaluationRoutes;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionResolver;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.event.EvaluationEventMessage;

public class ScenarioActionEnricherHandler {

    private final ActionResolver actionResolver;

    @Inject
    public ScenarioActionEnricherHandler(ActionResolver actionResolver) {
        this.actionResolver = actionResolver;
    }

    @Handler
    public void handle(Exchange exchange) {
        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        // Exchange body is a list of lists
        @SuppressWarnings("unchecked")
        List<List<Action>> aggregatedParsedActionList = exchange.getIn().getBody(List.class);

        List<Action> actions = aggregatedParsedActionList.stream()
                .flatMap(List::stream)
                // resolve action
                .map(action -> actionResolver.resolve(action, eventMessage).block())
                .collect(Collectors.toList());

        List<Action> filteredActions = Stream.concat(
                actions.stream()
                        .filter(action -> action.getType() == CHANGE_PROGRESS)
                        .limit(1),
                    actions.stream()
                        .filter(action -> action.getType() != CHANGE_PROGRESS)
                )
                .collect(Collectors.toList());

        // If this processor was reached, there were evaluated scenarios, but none evaluated true or had progress
        // actions, so inject a default progression of type INTERACTIVE_REPEAT
        if(filteredActions.size() == 0) {
            filteredActions.add(EvaluationRoutes.generateDefaultProgressAction(ProgressionType.INTERACTIVE_REPEAT));
        }

        Action evaluationAction = actions.stream()
                .filter(action -> action.getType() == CHANGE_PROGRESS)
                .limit(1)
                .findFirst()
                .orElse(null);

        if (evaluationAction != null) {
            eventMessage.setEvaluationActionState(new EvaluationActionState().setProgressActionContext((ProgressActionContext) evaluationAction.getContext())
                                                        .setCoursewareElement(CoursewareElement.from(eventMessage.getEvaluationResult().getCoursewareElementId(),
                                                                                                     CoursewareElementType.INTERACTIVE)));
        }
        exchange.getOut().setBody(filteredActions);
    }
}
