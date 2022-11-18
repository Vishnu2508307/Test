package com.smartsparrow.learner.service;

import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;

@Singleton
public class UpdatePathwayProgressHandler extends UpdateProgressHandler {

    private final LearnerPathwayService learnerPathwayService;
    private final StudentProgressRTMProducer studentProgressRTMProducer;

    @Inject
    protected UpdatePathwayProgressHandler(StudentProgressRTMProducer studentProgressRTMProducer,
                                           AuthenticationContextProvider authenticationContextProvider,
                                           LearnerPathwayService learnerPathwayService) {
        super(studentProgressRTMProducer);
        this.studentProgressRTMProducer = studentProgressRTMProducer;
        this.learnerPathwayService = learnerPathwayService;
    }

    @Handler
    public void routeEvent(final Exchange exchange) {

        final UpdateCoursewareElementProgressEvent event = exchange.getIn().getBody(UpdateCoursewareElementProgressEvent.class);

        //
        // This class acts like a router to the type specific pathway
        //

        UUID pathwayId = event.getElement().getElementId();
        UUID deploymentId = event.getUpdateProgressEvent().getDeploymentId();

        // get the pathway.
        LearnerPathway pathway = learnerPathwayService.find(pathwayId, deploymentId).block();

        if (pathway == null) {
            throw new IllegalStateException(String.format("pathway %s does not exist in deployment %s", pathwayId, deploymentId));
        }

        // send to the pathway specific, e.g. progress_update/PATHWAY/LINEAR
        String name = LEARNER_PROGRESS_UPDATE_PATHWAY + "/" + pathway.getType();
        exchange.getIn().setHeader("pathwayUri", name);
    }
}
