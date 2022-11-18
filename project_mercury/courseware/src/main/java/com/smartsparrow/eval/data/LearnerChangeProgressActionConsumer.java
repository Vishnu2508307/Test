package com.smartsparrow.eval.data;


import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionResult;
import com.smartsparrow.eval.service.ProgressUpdateService;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Action consumer implementation for a {@link com.smartsparrow.eval.action.Action.Type#CHANGE_PROGRESS} progression
 * type.
 */
public class LearnerChangeProgressActionConsumer implements ActionConsumer<ProgressAction, ProgressActionResult> {

    private final ActionConsumerOptions options;
    private final Map<CoursewareElementType, Provider<ProgressUpdateService>> progressUpdateServiceImplementations;
    private final StudentProgressRTMProducer studentProgressRTMProducer;

    @Inject
    public LearnerChangeProgressActionConsumer(final Map<CoursewareElementType, Provider<ProgressUpdateService>> progressUpdateServiceImplementations,
                                               final StudentProgressRTMProducer studentProgressRTMProducer) {
        this.studentProgressRTMProducer = studentProgressRTMProducer;
        options = new ActionConsumerOptions().setAsync(false);
        this.progressUpdateServiceImplementations = progressUpdateServiceImplementations;
    }

    /**
     * Update the progresses starting from the evaluated element up to the root activity
     *
     * @param action  the action that triggered the update
     * @param context the evaluation response context that holds all the information about evaluation
     *                note this method might make changes to the context
     * @return a mono with the progress action result
     */
    @Trace(async = true)
    @Override
    public Mono<ProgressActionResult> consume(ProgressAction action, LearnerEvaluationResponseContext context) {
        // get the ancestry
        final List<CoursewareElement> ancestry = context.getAncestry();

        // flux the ancestry list
        return Flux.fromIterable(ancestry)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // filter out any element that cannot have a progress, this step is for precaution only
                // there should never be an element in the ancestry that is different from either an INTERACTIVE, ACTIVITY
                // or PATHWAY
                .filter(coursewareElement -> CoursewareElementType.canHaveProgress(coursewareElement.getElementType()))
                // for each element in order find the progress update service implementation
                .concatMap(coursewareElement -> progressUpdateServiceImplementations.get(coursewareElement.getElementType())
                        .get()
                        .updateProgress(coursewareElement, action, context))
                .concatMap(progress -> {
                    // we have got a new progress let's broadcast that out
                    // Create event notifying listeners about progress upgrade
                    final LearnerEvaluationRequest request = context.getResponse()
                            .getEvaluationRequest();

                    //produces consumable event for student progress
                    studentProgressRTMProducer
                            .buildStudentProgressRTMConsumable(request.getStudentId(),
                                                               progress.getCoursewareElementId(),
                                                               request.getDeployment().getId(),
                                                               progress).produce();
                    return Mono.just(progress);
                })
                // we got the progress, let's add it to the context for the next progress update call
                // in the chain (this is an important step as we read the previous progress for attempt info)
                .concatMap(progress -> {
                    context.addProgress(progress);
                    return Mono.just(Lists.newArrayList(progress));
                })
                // reduce all the progresses together
                .reduce((accumulator, combiner) -> {
                    accumulator.addAll(combiner);
                    return accumulator;
                })
                // finally we have all the progresses, create the progress action result and return
                .map(progresses -> new ProgressActionResult()
                        .setValue(progresses));
    }

    @Override
    public Mono<ActionConsumerOptions> getActionConsumerOptions() {
        return Mono.just(options);
    }
}
