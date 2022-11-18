package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.event.UpdateProgressMessage;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.LinearPathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;

/**
 * Process a Progress update for a LINEAR pathway
 */
@Singleton
public class UpdateLinearPathwayProgressHandler extends UpdateProgressHandler {

    private final LearnerPathwayService learnerPathwayService;
    private final ProgressService progressService;
    private final AttemptService attemptService;

    @Inject
    public UpdateLinearPathwayProgressHandler(StudentProgressRTMProducer studentProgressRTMProducer,
                                              LearnerPathwayService learnerPathwayService,
                                              ProgressService progressService,
                                              AttemptService attemptService) {
        super(studentProgressRTMProducer);
        this.learnerPathwayService = learnerPathwayService;
        this.progressService = progressService;
        this.attemptService = attemptService;
    }

    @Handler
    @SuppressWarnings("Duplicates")
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "the return value childrenWalkables will be an empty list, not null")
    public void updateProgress(final Exchange exchange) {

        final UpdateCoursewareElementProgressEvent event = exchange.getIn().getBody(UpdateCoursewareElementProgressEvent.class);
        // if this is an activity restart then the EvaluationEventMessage will be null
        final EvaluationEventMessage evaluationEventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        /*
         * To calculate the completion value of a LINEAR pathway:
         *  (sum of completion.value across all nodes) / total number of nodes
         */

        final UUID pathwayId = event.getElement().getElementId();
        final UUID deploymentId = event.getUpdateProgressEvent().getDeploymentId();
        final UUID studentId = event.getUpdateProgressEvent().getStudentId();

        //find a corresponding pathway attempt
        UUID childAttemptId = event.getEventProgress().get(event.getEventProgress().size() - 1).getAttemptId();
        Attempt attempt = attemptService.findById(childAttemptId).block();
        if (attempt == null) {
            throw new IllegalStateException("something is wrong. The attempt should exist");
        }

        final UUID attemptId = attempt.getParentId();

        // Load the previous progress; create if it doesn't exist as a placeholder.
        Mono<LinearPathwayProgress> ppm = progressService.findLatestLinearPathway(deploymentId, pathwayId, studentId);
        LinearPathwayProgress previousProgress = ppm.block();
        if (previousProgress == null) {
            previousProgress = new LinearPathwayProgress();
        }
        final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
        final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

        // Get the currently configured walkables.
        List<WalkableChild> childrenWalkables = learnerPathwayService.findWalkables(pathwayId, deploymentId) //
                .collectList() //
                .block();

        // Walk the children and aggregate in their completion values.
        // by re-walking the children (instead of copying the previous progress),
        // we keep the progress up to date if the courseware changes.
        Progress childProgress = event.getEventProgress().get(event.getEventProgress().size() - 1);
        Map<UUID, Float> newCompletionValues = merge(childrenWalkables, prevComplValues,
                                                     childProgress.getCoursewareElementId(),
                                                     childProgress.getCompletion().getValue());
        Map<UUID, Float> newCompletionConfidence = merge(childrenWalkables, prevComplConfidence,
                                                         childProgress.getCoursewareElementId(),
                                                         childProgress.getCompletion().getConfidence());

        // Perform the completion calculation
        Completion completion = buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(),
                                                    childrenWalkables.size());

        // track the walkables that have been completed for rendering on the learner side.
        List<UUID> completedWalkables = buildCompletedItems(childrenWalkables, newCompletionValues);

        // Build the progress
        LinearPathwayProgress currentProgress = buildProgress(evaluationEventMessage, attemptId, childProgress, event,
                newCompletionValues, newCompletionConfidence, completion, completedWalkables);

        // persist the progress
        progressService.persist(currentProgress).singleOrEmpty().block();

        /*
         * Propagate the progress, and calculate progress up the courseware tree.
         */

        // Broadcast the progress change.
        broadcastProgressEventMessage(currentProgress, event.getUpdateProgressEvent());

        // Send it onward.
        propagateProgressChangeUpwards(exchange, event, currentProgress);
    }

    /**
     * Build the progress for the linear pathway. If the evaluated element is a child of this pathway the progress will
     * be built based on the progression type value
     *
     */
    private LinearPathwayProgress buildProgress(final EvaluationEventMessage evaluationEventMessage,
                                                final UUID attemptId,
                                                final Progress childProgress,
                                                final UpdateCoursewareElementProgressEvent event,
                                                final Map<UUID, Float> newCompletionValues,
                                                final Map<UUID, Float> newCompletionConfidence,
                                                final Completion completion,
                                                final List<UUID> completedWalkables) {
        // if I am the direct parent of the evaluated element I want to know about the action progression type
        if (evaluationEventMessage != null && evaluationEventMessage.getEvaluationActionState() != null && evaluationEventMessage.getEvaluationActionState().getCoursewareElement().getElementId().equals(childProgress.getCoursewareElementId())) {
            return buildProgressFromAction(event, evaluationEventMessage, attemptId, newCompletionValues, newCompletionConfidence, completion,
                    completedWalkables);
        }
        // build a normal progress
        return buildNormalProgress(event, attemptId, newCompletionValues, newCompletionConfidence,
                completion, completedWalkables);
    }

    private LinearPathwayProgress buildProgressFromAction(final UpdateCoursewareElementProgressEvent event,
                                                          final EvaluationEventMessage evaluationEventMessage,
                                                          final UUID attemptId,
                                                          final Map<UUID, Float> newCompletionValues,
                                                          final Map<UUID, Float> newCompletionConfidence,
                                                          final Completion completion,
                                                          final List<UUID> completedWalkables) {
        // get the progress action context and the progression type
        final ProgressActionContext actionContext = evaluationEventMessage.getEvaluationActionState().getProgressActionContext();
        ProgressionType progressionType = actionContext.getProgressionType();

        if (progressionType == ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE) {
            // if the child progression was interactive complete and pathway complete then
            // mark this pathway as completed
            final UpdateProgressMessage updateProgressEvent = event.getUpdateProgressEvent();

            return new LinearPathwayProgress() //
                    .setId(UUIDs.timeBased()) //
                    .setAttemptId(attemptId) //
                    .setChangeId(updateProgressEvent.getChangeId()) //
                    .setChildWalkableCompletionValues(newCompletionValues) //
                    .setChildWalkableCompletionConfidences(newCompletionConfidence) //
                    .setCompletion(new Completion()
                            .setValue(1f)
                            .setConfidence(1f)) //
                    .setCompletedWalkables(completedWalkables) //
                    .setCoursewareElementId(event.getElement().getElementId()) //
                    .setCoursewareElementType(CoursewareElementType.PATHWAY) //
                    .setDeploymentId(updateProgressEvent.getDeploymentId()) //
                    .setEvaluationId(updateProgressEvent.getEvaluationId()) //
                    .setStudentId(updateProgressEvent.getStudentId());
        }
        // for any other action just build a normal progress
        return buildNormalProgress(event, attemptId, newCompletionValues, newCompletionConfidence,
                completion, completedWalkables);
    }

    private LinearPathwayProgress buildNormalProgress(final UpdateCoursewareElementProgressEvent event,
                                                      final UUID attemptId,
                                                      final Map<UUID, Float> newCompletionValues,
                                                      final Map<UUID, Float> newCompletionConfidence,
                                                      final Completion completion,
                                                      final List<UUID> completedWalkables) {
        final UpdateProgressMessage updateProgressEvent = event.getUpdateProgressEvent();
        // build the progress
        return new LinearPathwayProgress() //
                .setId(UUIDs.timeBased()) //
                .setAttemptId(attemptId) //
                .setChangeId(updateProgressEvent.getChangeId()) //
                .setChildWalkableCompletionValues(newCompletionValues) //
                .setChildWalkableCompletionConfidences(newCompletionConfidence) //
                .setCompletion(completion) //
                .setCompletedWalkables(completedWalkables) //
                .setCoursewareElementId(event.getElement().getElementId()) //
                .setCoursewareElementType(CoursewareElementType.PATHWAY) //
                .setDeploymentId(updateProgressEvent.getDeploymentId()) //
                .setEvaluationId(updateProgressEvent.getEvaluationId()) //
                .setStudentId(updateProgressEvent.getStudentId());
    }

    @SuppressWarnings("Duplicates")
    Map<UUID, Float> merge(final List<WalkableChild> walkableChildren,
            final Map<UUID, Float> prevValues,
            final UUID childElementId,
            final Float childValue) {
        //
        Map<UUID, Float> ret = walkableChildren.stream() //
                .filter(wc -> prevValues.containsKey(wc.getElementId()))
                .filter(wc -> prevValues.get(wc.getElementId()) != null)
                .collect(Collectors.toMap(WalkableChild::getElementId, wc -> prevValues.get(wc.getElementId())));

        if (childValue != null) {
            ret.put(childElementId, childValue);
        }

        return ret;
    }

    @SuppressWarnings("Duplicates")
    Completion buildCompletionData(final Collection<Float> completionValues,
            final Collection<Float> completionConfidences,
            int totalSize) {
        // sum up the current completion values.
        double pSum = completionValues.stream().filter(Objects::nonNull).mapToDouble(Float::floatValue).sum();
        // sum up the current confidence values
        double cSum = completionConfidences.stream().filter(Objects::nonNull).mapToDouble(Float::floatValue).sum();
        // calcuate the values
        float progressValue = (float) (pSum / totalSize);
        float confidenceValue = (float) (cSum / totalSize);
        // sanity check.
        progressValue = Math.min(progressValue, 1.0f);
        confidenceValue = Math.min(confidenceValue, 1.0f);

        // build the completion
        return new Completion().setValue(progressValue).setConfidence(confidenceValue);
    }

    List<UUID> buildCompletedItems(List<WalkableChild> childElements, final Map<UUID, Float> completionValues) {
        // quick out checks.
        if (completionValues.isEmpty() || childElements.isEmpty()) {
            return Lists.newArrayList();
        }

        // reduce the map to only contain fully completed ids.
        final Set<UUID> fullyCompletedIds = completionValues.entrySet().stream() //
                .filter(entry -> 1.0f == entry.getValue()) //
                .map(entry -> entry.getKey()) //
                .collect(Collectors.toSet());

        // choose the ones that are in the child elements collection.
        return childElements.stream() //
                .filter(walkableChild -> fullyCompletedIds.contains(walkableChild.getElementId())) //
                .map(WalkableChild::getElementId).collect(Collectors.toList());
    }
}
