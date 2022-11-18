package com.smartsparrow.eval.service;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Mono;

/**
 * Allows the {@link Action.Type#CHANGE_PROGRESS} action consumer to call this interface for each element in the
 * ancestry that requires its progress to be updated.
 */
public interface ProgressUpdateService {

    /**
     * Updates the progress for the element based on the action value and the evaluation response context
     *
     * @param element the element to update the progress for
     * @param action the action that triggered the progress update
     * @param responseContext the context holding evaluation information that triggered the action consumer in the first
     *                        place
     * @return a mono containing the progress generated as a result of the progress update logic
     */
    @Trace(async = true)
    Mono<Progress> updateProgress(final CoursewareElement element, final ProgressAction action,
                                  final LearnerEvaluationResponseContext responseContext);
}
