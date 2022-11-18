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

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerRandomPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

@Singleton
public class RandomPathwayProgressUpdateService implements PathwayProgressUpdateService<LearnerRandomPathway> {

    private final LearnerPathwayService learnerPathwayService;
    private final ProgressService progressService;
    private final AttemptService attemptService;

    @Inject
    public RandomPathwayProgressUpdateService(final LearnerPathwayService learnerPathwayService,
                                              final ProgressService progressService,
                                              final AttemptService attemptService) {
        this.learnerPathwayService = learnerPathwayService;
        this.progressService = progressService;
        this.attemptService = attemptService;
    }

    @Trace(async = true)
    @Override
    public Mono<Progress> updateProgress(LearnerRandomPathway pathway, ProgressAction action, LearnerEvaluationResponseContext context) {

        final LearnerEvaluationResponse response = context.getResponse();
        final LearnerEvaluationRequest request = response.getEvaluationRequest();

        final UUID pathwayId = pathway.getId();
        final UUID changeId = pathway.getChangeId();
        final UUID deploymentId = pathway.getDeploymentId();
        final UUID evaluationId = response.getWalkableEvaluationResult().getId();
        final UUID studentId = request.getStudentId();
        final Progress childProgress = context.getProgresses().get(context.getProgresses().size() - 1);

        // find the exit condition for this pathway
        Integer exitAfter = pathway.getExitAfter();

        // find the attempt
        Mono<Attempt> attemptMono = attemptService.findById(childProgress.getAttemptId());
        // find the previous progress
        Mono<RandomPathwayProgress> previousProgressMono = progressService.findLatestRandomPathway(deploymentId, pathwayId, studentId)
                .defaultIfEmpty(new RandomPathwayProgress());
        // Get all the walkables for this pathway.
        Mono<List<WalkableChild>> walkableChildrenMono = learnerPathwayService.findWalkables(pathwayId, deploymentId) //
                .collectList();

        return Mono.zip(attemptMono, previousProgressMono, walkableChildrenMono)
                .flatMap(tuple3 -> {
                    final Attempt attempt = tuple3.getT1();
                    final RandomPathwayProgress previousProgress = tuple3.getT2();
                    final List<WalkableChild> walkableChildren = tuple3.getT3();
                    final UUID attemptId = attempt.getParentId();

                    // read the previous completion information (will be empty maps when initialised)
                    final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
                    final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

                    // Walk the children and aggregate in their completion values.
                    // by re-walking the children (instead of copying the previous progress),
                    // we keep the progress up to date if the courseware changes.

                    final Map<UUID, Float> newCompletionValues = merge(walkableChildren, prevComplValues,
                            childProgress.getCoursewareElementId(),
                            childProgress.getCompletion().getValue());
                    final Map<UUID, Float> newCompletionConfidence = merge(walkableChildren, prevComplConfidence,
                            childProgress.getCoursewareElementId(),
                            childProgress.getCompletion().getConfidence());

                    final Completion completion = buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(), exitAfter);

                    final List<UUID> completedWalkables = buildCompletedItems(walkableChildren, newCompletionValues);

                    // set the in progress walkable if this is not completed
                    CoursewareElement inProgress = new CoursewareElement();

                    if (!childProgress.getCompletion().isCompleted()) {
                        inProgress.setElementId(childProgress.getCoursewareElementId());
                        inProgress.setElementType(childProgress.getCoursewareElementType());
                    }

                    // build the random pathway progress
                    RandomPathwayProgress progress = new RandomPathwayProgress()
                            .setId(UUIDs.timeBased())
                            .setDeploymentId(deploymentId)
                            .setChangeId(changeId)
                            .setCoursewareElementId(pathwayId)
                            .setCoursewareElementType(CoursewareElementType.PATHWAY)
                            .setStudentId(studentId)
                            .setAttemptId(attemptId)
                            .setEvaluationId(evaluationId)
                            .setCompletion(completion)
                            .setInProgressElementId(inProgress.getElementId())
                            .setInProgressElementType(inProgress.getElementType())
                            .setChildWalkableCompletionValues(newCompletionValues)
                            .setChildWalkableCompletionConfidences(newCompletionConfidence)
                            .setCompletedWalkables(completedWalkables);

                    // persist the random pathway progress
                    return progressService.persist(progress)
                            .singleOrEmpty()
                            .then(Mono.just(progress));
                });
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
                                   int exitAfter) {
        // sum up the current completion values.
        double pSum = completionValues.stream().filter(Objects::nonNull).mapToDouble(Float::floatValue).sum();
        // sum up the current confidence values
        double cSum = completionConfidences.stream().filter(Objects::nonNull).mapToDouble(Float::floatValue).sum();
        // calcuate the values
        float progressValue = (float) (pSum / exitAfter);
        float confidenceValue = (float) (cSum / exitAfter);
        // sanity check.
        progressValue = Math.min(progressValue, 1.0f);
        confidenceValue = Math.min(confidenceValue, 1.0f);

        // build the completion
        return new Completion().setValue(progressValue).setConfidence(confidenceValue);
    }

    @SuppressWarnings("Duplicates")
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
