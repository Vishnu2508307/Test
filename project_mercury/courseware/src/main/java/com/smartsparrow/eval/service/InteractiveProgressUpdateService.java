package com.smartsparrow.eval.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GeneralProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.CoursewareHistoryService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Mono;

@Singleton
public class InteractiveProgressUpdateService implements ProgressUpdateService {

    private final ProgressService progressService;
    private final CoursewareHistoryService coursewareHistoryService;

    @Inject
    public InteractiveProgressUpdateService(final ProgressService progressService,
                                            final CoursewareHistoryService coursewareHistoryService) {
        this.progressService = progressService;
        this.coursewareHistoryService = coursewareHistoryService;
    }

    @Trace(async = true)
    @Override
    public Mono<Progress> updateProgress(CoursewareElement element, ProgressAction action, LearnerEvaluationResponseContext responseContext) {
        /*
         * Progress on an interactive is calculated based on the progress action context and # of attempts
         */
        Mono<Progress> progressMono = null;

        ProgressionType progressionType = action.getContext().getProgressionType();

        switch (progressionType) {

            case INTERACTIVE_COMPLETE:
            case INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE:
            case INTERACTIVE_COMPLETE_AND_GO_TO:
                progressMono = handleInteractiveComplete(element, responseContext);
                // the GO_TO part of the progression type is handled by the parent graph pathway. If the parent pathway
                // is not a graph type then the GO_TO part is never handled
                break;
            case INTERACTIVE_REPEAT:
                progressMono = handleInteractiveIncomplete(element, responseContext);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        // persist the progress.
        return progressMono.flatMap(progress -> progressService.persist(progress)
                .then(Mono.just(progress)));
    }

    /**
     * Record the completed interactive evaluation for history purposes and generate Progress for when an interactive
     * is completed.
     *
     * @param element the courseware element
     * @param responseContext the context holding evaluation information that triggered the action consumer in the first
     *                        place
     * @return a mono containing the progress as a result of the progress update logic
     */
    Mono<Progress> handleInteractiveComplete(final CoursewareElement element,
                                             final LearnerEvaluationResponseContext responseContext) {
        //
        // set progress as completed and fully confident!
        //
        Completion completion = new Completion().setValue(1f).setConfidence(1f);
        //
        // save the completed walkable for history purposes
        //
        UUID evaluationId = responseContext.getResponse().getWalkableEvaluationResult().getId();
        final LearnerEvaluationRequest request = responseContext.getResponse()
                .getEvaluationRequest();
        return coursewareHistoryService.record(evaluationId, request, element, request.getAttempt(), request.getParentPathwayId())
                .then(Mono.just(buildProgress(evaluationId, responseContext.getResponse().getEvaluationRequest(), completion, element)));
    }


    /**
     * Generate Progress for when an interactive has not been completed
     *
     * @param element the courseware element
     * @param responseContext the context holding evaluation information that triggered the action consumer in the first
     *                        place
     * @return a mono containing the progress as a result of the progress update logic
     */
    Mono<Progress> handleInteractiveIncomplete(final CoursewareElement element,
                                               final LearnerEvaluationResponseContext responseContext) {

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

        Attempt attempt = responseContext.getResponse().getEvaluationRequest().getAttempt();
        float value = 1 - (1.0f / attempt.getValue());
        float _confidence = 1 - (0.8f / attempt.getValue());
        float confidence = Math.min(0.9f, _confidence);

        Completion completion = new Completion().setValue(value).setConfidence(confidence);

        return Mono.just(buildProgress(responseContext.getResponse().getWalkableEvaluationResult().getId(),
                             responseContext.getResponse().getEvaluationRequest(),
                             completion, element));
    }

    Progress buildProgress(final UUID evaluationId, final LearnerEvaluationRequest evaluationRequest, final Completion completion,
                           final CoursewareElement element) {
        return new GeneralProgress() //
                .setId(UUIDs.timeBased())
                .setDeploymentId(evaluationRequest.getDeployment().getId())
                .setChangeId(evaluationRequest.getDeployment().getChangeId())
                .setCoursewareElementId(element.getElementId())
                .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
                .setStudentId(evaluationRequest.getStudentId())
                .setAttemptId(evaluationRequest.getAttempt().getId())
                .setEvaluationId(evaluationId)
                .setCompletion(completion);
    }
}
