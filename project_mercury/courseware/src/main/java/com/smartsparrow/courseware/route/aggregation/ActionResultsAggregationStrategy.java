package com.smartsparrow.courseware.route.aggregation;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionResult;
import com.smartsparrow.eval.action.progress.ProgressActionResult;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;

public class ActionResultsAggregationStrategy implements AggregationStrategy {

    private final static Logger log = LoggerFactory.getLogger(ActionResultsAggregationStrategy.class);

    /**
     * Aggregation strategy that receives all the excchanges returning fron the Action routes
     *
     * Responsible for building the ActionResult classes and adding them to the original evaluation result
     * before being returned to client
     *
     * @param oldExchange can be ignored, aggregation happens in object carried in property
     * @param newExchange the exchange returned at the end of an action route
     * @return a modified newExchange
     */
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        EvaluationEventMessage evaluationEvent = newExchange
                .getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        EvaluationResult evaluationResult = evaluationEvent.getEvaluationResult();
        List<ActionResult> actionResults = evaluationResult.getActionResults();
        if(actionResults == null) {
            actionResults = new ArrayList<>();
            evaluationEvent.getEvaluationResult().setActionResults(actionResults);
        }

        Action.Type actionType =  newExchange.getProperty("actionType", Action.Type.class);
        Message in = newExchange.getIn();

        switch (actionType) {
        case CHANGE_PROGRESS:
            UpdateCoursewareElementProgressEvent coursewareProgress = in
                    .getBody(UpdateCoursewareElementProgressEvent.class);
            ProgressActionResult progressActionResult = new ProgressActionResult()
                    .setValue(coursewareProgress.getEventProgress());
            actionResults.add(progressActionResult);
            break;
        case SEND_FEEDBACK:
        case GRADE:
        case SET_COMPETENCY:
        case CHANGE_COMPETENCY:
        case UNSUPPORTED_ACTION:
            break;
        default:
            log.warn("Unknown Action.Type being aggregated in action results: " + actionType);
        }

        newExchange.getOut().setBody(evaluationEvent);
        return newExchange;
    }

}
