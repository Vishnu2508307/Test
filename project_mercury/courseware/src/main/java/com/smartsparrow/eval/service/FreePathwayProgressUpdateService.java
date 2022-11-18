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
import com.smartsparrow.courseware.pathway.FreeLearnerPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.FreePathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Mono;

@Singleton
public class FreePathwayProgressUpdateService implements PathwayProgressUpdateService<FreeLearnerPathway> {

    private final LearnerPathwayService learnerPathwayService;
    private final ProgressService progressService;
    private final AttemptService attemptService;

    @Inject
    public FreePathwayProgressUpdateService(final LearnerPathwayService learnerPathwayService,
                                            final ProgressService progressService,
                                            final AttemptService attemptService) {
        this.learnerPathwayService = learnerPathwayService;
        this.progressService = progressService;
        this.attemptService = attemptService;
    }

    @Trace(async = true)
    @Override
    public Mono<Progress> updateProgress(FreeLearnerPathway freeLearnerPathway, ProgressAction action, LearnerEvaluationResponseContext context) {
        /*
         * To calculate the completion value of a FREE pathway:
         *  the sum of all the progress of the children / # of children
         */

        final UUID pathwayId = freeLearnerPathway.getId();
        final UUID deploymentId = freeLearnerPathway.getDeploymentId();
        final UUID studentId = context.getResponse().getEvaluationRequest().getStudentId();
        final Progress childProgress = context.getProgresses().get(context.getProgresses().size() - 1);
        final UUID childAttemptId = childProgress.getAttemptId();


        Mono<Attempt> attemptMono = attemptService.findById(childAttemptId);
        Mono<FreePathwayProgress> progressMono = progressService.findLatestFreePathway(deploymentId, pathwayId, studentId)
                .defaultIfEmpty(new FreePathwayProgress());
        Mono<List<WalkableChild>> walkableChildrenMono = learnerPathwayService.findWalkables(pathwayId, deploymentId) //
                .collectList();

        return Mono.zip(attemptMono, progressMono, walkableChildrenMono)
                .flatMap(tuple3 -> {
                    final Attempt attempt = tuple3.getT1();
                    final FreePathwayProgress previousProgress = tuple3.getT2();
                    final List<WalkableChild> walkableChildren = tuple3.getT3();
                    final UUID attemptId = attempt.getParentId();


                    final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
                    final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

                    Map<UUID, Float> newCompletionValues = merge(walkableChildren, prevComplValues,
                            childProgress.getCoursewareElementId(),
                            childProgress.getCompletion().getValue());
                    Map<UUID, Float> newCompletionConfidence = merge(walkableChildren, prevComplConfidence,
                            childProgress.getCoursewareElementId(),
                            childProgress.getCompletion().getConfidence());

                    // Perform the completion calculation
                    Completion completion = buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(),
                            walkableChildren.size());

                    // track the walkables that have been completed for rendering on the learner side.
                    List<UUID> completedWalkables = buildCompletedItems(walkableChildren, newCompletionValues);

                    // Build the progress
                    FreePathwayProgress currentProgress = buildProgress(context, attemptId, childProgress,
                            newCompletionValues, newCompletionConfidence, completion, completedWalkables, pathwayId);

                    return progressService.persist(currentProgress)
                            .then(Mono.just(currentProgress));
                });
    }

    private FreePathwayProgress buildProgress(final LearnerEvaluationResponseContext responseContext,
                                              final UUID attemptId,
                                              final Progress childProgress,
                                              final Map<UUID, Float> newCompletionValues,
                                              final Map<UUID, Float> newCompletionConfidence,
                                              final Completion completion,
                                              final List<UUID> completedWalkables,
                                              final UUID pathwayId) {
        // if I am the direct parent of the evaluated element I want to know about the action progression type
        if (responseContext.getEvaluationActionState().getCoursewareElement().getElementId().equals(childProgress.getCoursewareElementId())) {
            return buildProgressFromAction(responseContext, attemptId, newCompletionValues, newCompletionConfidence, completion,
                    completedWalkables, pathwayId);
        }
        // build a normal progress
        return buildNormalProgress(responseContext, attemptId, newCompletionValues, newCompletionConfidence,
                completion, completedWalkables, pathwayId);
    }

    private FreePathwayProgress buildProgressFromAction(final LearnerEvaluationResponseContext context,
                                                        final UUID attemptId,
                                                        final Map<UUID, Float> newCompletionValues,
                                                        final Map<UUID, Float> newCompletionConfidence,
                                                        final Completion completion,
                                                        final List<UUID> completedWalkables,
                                                        final UUID pathwayId) {
        // get the progress action context and the progression type
        final ProgressActionContext actionContext = context.getEvaluationActionState().getProgressActionContext();
        ProgressionType progressionType = actionContext.getProgressionType();

        if (progressionType == ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE) {
            // if the child progression was interactive complete and pathway complete then

            final LearnerEvaluationRequest request = context.getResponse()
                    .getEvaluationRequest();
            // mark this pathway as completed

            return new FreePathwayProgress() //
                    .setId(UUIDs.timeBased()) //
                    .setAttemptId(attemptId) //
                    .setChangeId(request.getDeployment().getChangeId()) //
                    .setChildWalkableCompletionValues(newCompletionValues) //
                    .setChildWalkableCompletionConfidences(newCompletionConfidence) //
                    .setCompletion(new Completion()
                            .setValue(1f)
                            .setConfidence(1f)) //
                    .setCompletedWalkables(completedWalkables) //
                    .setCoursewareElementId(pathwayId) //
                    .setCoursewareElementType(CoursewareElementType.PATHWAY) //
                    .setDeploymentId(request.getDeployment().getId()) //
                    .setEvaluationId(context.getResponse().getWalkableEvaluationResult().getId()) //
                    .setStudentId(request.getStudentId());
        }
        // for any other action just build a normal progress
        return buildNormalProgress(context, attemptId, newCompletionValues, newCompletionConfidence,
                completion, completedWalkables, pathwayId);
    }

    private FreePathwayProgress buildNormalProgress(final LearnerEvaluationResponseContext context,
                                                    final UUID attemptId,
                                                    final Map<UUID, Float> newCompletionValues,
                                                    final Map<UUID, Float> newCompletionConfidence,
                                                    final Completion completion,
                                                    final List<UUID> completedWalkables,
                                                    final UUID pathwayId) {
        LearnerEvaluationRequest request = context.getResponse()
                .getEvaluationRequest();

        return new FreePathwayProgress() //
                .setId(UUIDs.timeBased()) //
                .setAttemptId(attemptId) //
                .setChangeId(request.getDeployment().getChangeId()) //
                .setChildWalkableCompletionValues(newCompletionValues) //
                .setChildWalkableCompletionConfidences(newCompletionConfidence) //
                .setCompletion(completion) //
                .setCompletedWalkables(completedWalkables) //
                .setCoursewareElementId(pathwayId) //
                .setCoursewareElementType(CoursewareElementType.PATHWAY) //
                .setDeploymentId(request.getDeployment().getId()) //
                .setEvaluationId(context.getResponse().getWalkableEvaluationResult().getId()) //
                .setStudentId(request.getStudentId());
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
