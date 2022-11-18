package com.smartsparrow.courseware.route.process;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.service.LearnerInteractiveService;
import com.smartsparrow.learner.service.LearnerPathwayService;

public class PathwayDefaultActionProviderHandler {

    private final LearnerInteractiveService learnerInteractiveService;
    private final LearnerPathwayService learnerPathwayService;

    @Inject
    public PathwayDefaultActionProviderHandler(LearnerInteractiveService learnerInteractiveService,
                                               LearnerPathwayService learnerPathwayService) {
        this.learnerInteractiveService = learnerInteractiveService;
        this.learnerPathwayService = learnerPathwayService;
    }


    @Handler
    public void handle(Exchange exchange) {
        // it should supply the pathway default action when there are no scenarios
        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        final EvaluationResult result = eventMessage.getEvaluationResult();

        final Deployment deployment = result.getDeployment();

        // find the pathway, get the default action
        LearnerPathway learnerPathway = learnerInteractiveService.findParentPathwayId(result.getCoursewareElementId(), deployment.getId())
                .flatMap(pathwayId -> learnerPathwayService.find(pathwayId, deployment.getId()))
                .block();

        affirmNotNull(learnerPathway, "something is wrong, this element must have a parent pathway");

        List<Action> actions = Lists.newArrayList(learnerPathway.getDefaultAction());

        Action evaluationAction = actions.get(0);

        if (evaluationAction != null) {
            eventMessage.setEvaluationActionState(new EvaluationActionState().setProgressActionContext((ProgressActionContext) evaluationAction.getContext())
                    .setCoursewareElement(CoursewareElement.from(eventMessage.getEvaluationResult().getCoursewareElementId(),
                            CoursewareElementType.INTERACTIVE)));
        }

        exchange.getIn().setBody(actions);

    }
}
