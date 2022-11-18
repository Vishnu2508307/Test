package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GeneralProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.UnsupportedOperationException;

@Singleton
public class UpdateInteractiveProgressHandler extends UpdateProgressHandler {

    private final ProgressService progressService;
    private final CoursewareHistoryService coursewareHistoryService;

    @Inject
    public UpdateInteractiveProgressHandler(StudentProgressRTMProducer studentProgressRTMProducer,
                                            ProgressService progressService,
                                            CoursewareHistoryService coursewareHistoryService) {
        super(studentProgressRTMProducer);
        this.progressService = progressService;
        this.coursewareHistoryService = coursewareHistoryService;
    }

    @Deprecated
    @Handler
    @SuppressFBWarnings(value = "NP_NULL_PARAM_DEREF",
            justification = "FB thinks progress can be null, but the switch will throw and stop the method if it doesnt call a method that builds progress object")
    public void updateProgress(final Exchange exchange) {

        final UpdateCoursewareElementProgressEvent event = exchange.getIn().getBody(UpdateCoursewareElementProgressEvent.class);
        // extract the original evaluation event message from the exchange
        final EvaluationEventMessage evaluationEventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        /*
         * Progress on an interactive is calculated based on the progress action context and # of attempts
         */
        Progress progress = null;

        ProgressionType progressionType = evaluationEventMessage.getEvaluationActionState().getProgressActionContext().getProgressionType();

        switch (progressionType) {

            case INTERACTIVE_COMPLETE:
            case INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE:
            case INTERACTIVE_COMPLETE_AND_GO_TO:
                progress = handleInteractiveComplete(event, evaluationEventMessage.getEvaluationResult());
                // the GO_TO part of the progression type is handled by the parent graph pathway. If the parent pathway
                // is not a graph type then the GO_TO part is never handled
                break;
            case INTERACTIVE_REPEAT:
                progress = handleInteractiveIncomplete(event);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        // persist the progress.
        progressService.persist(progress).blockLast();

        // Broadcast the progress change.
        broadcastProgressEventMessage(progress, event.getUpdateProgressEvent());

        /*
         * Propagate the progress, and calculate progress up the courseware tree.
         */
        // Send it onward.
        propagateProgressChangeUpwards(exchange, event, progress);

    }

    /**
     * Record the completed interactive evaluation for history purposes and generate Progress for when an interactive
     * is completed.
     *
     * @param event the incoming event message
     * @param evaluationResult the result of the evaluation
     * @return the progress
     */
    @Deprecated
    Progress handleInteractiveComplete(final UpdateCoursewareElementProgressEvent event, final EvaluationResult evaluationResult) {

        //
        // save the completed walkable for history purposes
        //
        coursewareHistoryService.record(event.getUpdateProgressEvent().getStudentId(), evaluationResult,
                CoursewareElementType.INTERACTIVE)
                .block();

        //
        // set progress as completed and fully confident!
        //
        Completion completion = new Completion().setValue(1f).setConfidence(1f);
        return buildProgress(event, completion);
    }

    /**
     * Generate Progress for when an interactive has not been completed
     *
     * @param event the incoming event message
     * @return the progress
     */
    @Deprecated
    Progress handleInteractiveIncomplete(final UpdateCoursewareElementProgressEvent event) {

        // The interactive has not been marked as completed;
        //  Completion:
        //      if max # attempts set:
        //          value = attempt # / max #
        //          confidence = value
        //      no max # attempts:
        //          value = 1 - (1 / attempt number)
        //          confidence = 1 - (0.8 / attempt number) [maximum of 0.9]
        //

        // there are no max number of attempts available!

        Attempt attempt = event.getUpdateProgressEvent().getAttempt();
        float value = 1 - (1.0f / attempt.getValue());
        float _confidence = 1 - (0.8f / attempt.getValue());
        float confidence = Math.min(0.9f, _confidence);

        Completion completion = new Completion().setValue(value).setConfidence(confidence);

        return buildProgress(event, completion);
    }

    @Deprecated
    Progress buildProgress(final UpdateCoursewareElementProgressEvent event, final Completion completion) {
        return new GeneralProgress() //
                .setId(UUIDs.timeBased())
                .setDeploymentId(event.getUpdateProgressEvent().getDeploymentId())
                .setChangeId(event.getUpdateProgressEvent().getChangeId())
                .setCoursewareElementId(event.getElement().getElementId())
                .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
                .setStudentId(event.getUpdateProgressEvent().getStudentId())
                .setAttemptId(event.getUpdateProgressEvent().getAttempt().getId())
                .setEvaluationId(event.getUpdateProgressEvent().getEvaluationId())
                .setCompletion(completion);
    }
}
