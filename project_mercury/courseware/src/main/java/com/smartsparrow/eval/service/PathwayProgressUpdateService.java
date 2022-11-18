package com.smartsparrow.eval.service;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Mono;

/**
 * Interface for updating a pathway progress
 *
 * @param <T> the type of pathway
 */
public interface PathwayProgressUpdateService<T extends LearnerPathway> {

    /**
     * Updates the progress for the pathway based on the action value and the evaluation response context
     *
     * @param t the learner pathway to update the progress for
     * @param action the action that triggered the progress update
     * @param context the context holding evaluation information that triggered the action consumer in the first
     *                place
     * @return a mono containing the progress generated as a result of the progress update logic
     */
    @Trace(async = true)
    Mono<Progress> updateProgress(final T t, final ProgressAction action, final LearnerEvaluationResponseContext context);
}
