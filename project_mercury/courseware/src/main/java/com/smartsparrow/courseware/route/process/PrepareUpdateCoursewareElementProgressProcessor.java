package com.smartsparrow.courseware.route.process;

import static com.smartsparrow.courseware.route.CoursewareRoutes.ACTION_PROGRESS_CONTEXT;
import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;

public class PrepareUpdateCoursewareElementProgressProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        EvaluationEventMessage event = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        ProgressActionContext progressActionContext = exchange.getIn().getHeader(ACTION_PROGRESS_CONTEXT, ProgressActionContext.class);

        //
        // Kickoff the Progress updates, by setting initial body for this Interactive.
        //
        UpdateCoursewareElementProgressEvent u = new UpdateCoursewareElementProgressEvent() //
                .setUpdateProgressEvent(event) //
                .setElement(event.getAncestryList().get(0));

        //set the progress action context and element details to evaluation state object
        event.setEvaluationActionState(new EvaluationActionState().setProgressActionContext(progressActionContext)
                                               .setCoursewareElement(CoursewareElement.from(event.getEvaluationResult().getCoursewareElementId(),
                                                                                            CoursewareElementType.INTERACTIVE)));
        exchange.getIn().setBody(u);
    }

}
