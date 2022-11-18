package com.smartsparrow.eval.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LinearLearnerPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.LinearPathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Mono;

@Singleton
public class LinearPathwayProgressUpdateService implements PathwayProgressUpdateService<LinearLearnerPathway> {

    private final AttemptService attemptService;
    private final ProgressService progressService;
    private final LearnerPathwayService learnerPathwayService;

    @Inject
    public LinearPathwayProgressUpdateService(final AttemptService attemptService,
                                              final ProgressService progressService,
                                              final LearnerPathwayService learnerPathwayService) {
        this.attemptService = attemptService;
        this.progressService = progressService;
        this.learnerPathwayService = learnerPathwayService;
    }

    @Trace(async = true)
    @Override
    public Mono<Progress> updateProgress(LinearLearnerPathway linearLearnerPathway, ProgressAction action, LearnerEvaluationResponseContext context) {
        /*
         * To calculate the completion value of a LINEAR pathway:
         *  (sum of completion.value across all nodes) / total number of nodes
         */

        final UUID pathwayId = linearLearnerPathway.getId();
        final UUID deploymentId = context.getResponse().getEvaluationRequest().getLearnerWalkable().getDeploymentId();
        final UUID studentId = context.getResponse().getEvaluationRequest().getStudentId();

        // Load the previous progress; create if it doesn't exist as a placeholder.
        return progressService.findLatestLinearPathway(deploymentId, pathwayId, studentId)
            .defaultIfEmpty(new LinearPathwayProgress())
            .flatMap(previousProgress -> {
                final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
                final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

                // Get the currently configured walkables.
                return learnerPathwayService.findWalkables(pathwayId, deploymentId) //
                    .collectList()
                    .flatMap(childrenWalkables -> {
                        // Walk the children and aggregate in their completion values.
                        // by re-walking the children (instead of copying the previous progress),
                        // we keep the progress up to date if the courseware changes.
                        Progress childProgress = context.getProgresses().get(context.getProgresses().size() - 1);
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
                        //find a corresponding pathway attempt
                        UUID childAttemptId = context.getProgresses().get(context.getProgresses().size() - 1).getAttemptId();

                        return attemptService.findById(childAttemptId)
                            .flatMap(attempt -> {
                                if (attempt == null) {
                                    return Mono.error(new IllegalStateException("something is wrong. The attempt should exist"));
                                }
                                final UUID attemptId = attempt.getParentId();
                                // Build the progress
                                LinearPathwayProgress currentProgress = buildProgress(linearLearnerPathway, context, attemptId, childProgress,
                                                                                      newCompletionValues, newCompletionConfidence, completion, completedWalkables);

                                // persist the progress
                                return progressService.persist(currentProgress)
                                        .then(Mono.just(currentProgress));
                            });
                    });
            });
    }

    /**
     * Build the progress for the linear pathway. If the evaluated element is a child of this pathway the progress will
     * be built based on the progression type value
     *
     */
    private LinearPathwayProgress buildProgress(final LinearLearnerPathway linearLearnerPathway,
                                                final LearnerEvaluationResponseContext context,
                                                final UUID attemptId,
                                                final Progress childProgress,
                                                final Map<UUID, Float> newCompletionValues,
                                                final Map<UUID, Float> newCompletionConfidence,
                                                final Completion completion,
                                                final List<UUID> completedWalkables) {
        // if I am the direct parent of the evaluated element I want to know about the action progression type
        if (context.getEvaluationActionState().getCoursewareElement().getElementId().equals(childProgress.getCoursewareElementId())) {
            return buildProgressFromAction(linearLearnerPathway, context, attemptId, newCompletionValues, newCompletionConfidence, completion,
                                           completedWalkables);
        }
        // build a normal progress
        return buildNormalProgress(linearLearnerPathway, context, attemptId, newCompletionValues, newCompletionConfidence,
                                   completion, completedWalkables);
    }

    private LinearPathwayProgress buildProgressFromAction(final LinearLearnerPathway linearLearnerPathway,
                                                          final LearnerEvaluationResponseContext context,
                                                          final UUID attemptId,
                                                          final Map<UUID, Float> newCompletionValues,
                                                          final Map<UUID, Float> newCompletionConfidence,
                                                          final Completion completion,
                                                          final List<UUID> completedWalkables) {
        // get the progress action context and the progression type
        final ProgressActionContext actionContext = context.getEvaluationActionState().getProgressActionContext();
        ProgressionType progressionType = actionContext.getProgressionType();

        if (progressionType == ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE) {
            // if the child progression was interactive complete and pathway complete then
            // mark this pathway as completed
            return new LinearPathwayProgress() //
                    .setId(UUIDs.timeBased()) //
                    .setAttemptId(attemptId) //
                    .setChangeId(context.getResponse().getEvaluationRequest().getDeployment().getChangeId()) //
                    .setChildWalkableCompletionValues(newCompletionValues) //
                    .setChildWalkableCompletionConfidences(newCompletionConfidence) //
                    .setCompletion(new Completion()
                                           .setValue(1f)
                                           .setConfidence(1f)) //
                    .setCompletedWalkables(completedWalkables) //
                    .setCoursewareElementId(linearLearnerPathway.getId()) //
                    .setCoursewareElementType(CoursewareElementType.PATHWAY) //
                    .setDeploymentId(context.getResponse().getEvaluationRequest().getDeployment().getId()) //
                    .setEvaluationId(context.getResponse().getWalkableEvaluationResult().getId()) //
                    .setStudentId(context.getResponse().getEvaluationRequest().getStudentId());
        }
        // for any other action just build a normal progress
        return buildNormalProgress(linearLearnerPathway, context, attemptId, newCompletionValues, newCompletionConfidence,
                                   completion, completedWalkables);
    }

    private LinearPathwayProgress buildNormalProgress(final LinearLearnerPathway linearLearnerPathway,
                                                      final LearnerEvaluationResponseContext context,
                                                      final UUID attemptId,
                                                      final Map<UUID, Float> newCompletionValues,
                                                      final Map<UUID, Float> newCompletionConfidence,
                                                      final Completion completion,
                                                      final List<UUID> completedWalkables) {
        // build the progress
        return new LinearPathwayProgress() //
                .setId(UUIDs.timeBased()) //
                .setAttemptId(attemptId) //
                .setChangeId(context.getResponse().getEvaluationRequest().getDeployment().getChangeId()) //
                .setChildWalkableCompletionValues(newCompletionValues) //
                .setChildWalkableCompletionConfidences(newCompletionConfidence) //
                .setCompletion(completion) //
                .setCompletedWalkables(completedWalkables) //
                .setCoursewareElementId(linearLearnerPathway.getId()) //
                .setCoursewareElementType(CoursewareElementType.PATHWAY) //
                .setDeploymentId(context.getResponse().getEvaluationRequest().getDeployment().getId()) //
                .setEvaluationId(context.getResponse().getWalkableEvaluationResult().getId()) //
                .setStudentId(context.getResponse().getEvaluationRequest().getStudentId());
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
                .map(Map.Entry::getKey) //
                .collect(Collectors.toSet());

        // choose the ones that are in the child elements collection.
        return childElements.stream() //
                .map(WalkableChild::getElementId) //
                .filter(fullyCompletedIds::contains)
                .collect(Collectors.toList());
    }
}
