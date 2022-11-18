package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

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
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerRandomPathway;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;
import com.smartsparrow.util.UUIDs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Singleton
public class UpdateRandomPathwayProgressHandler extends UpdateProgressHandler {

    private final LearnerPathwayService learnerPathwayService;
    private final ProgressService progressService;
    private final AttemptService attemptService;

    @Inject
    protected UpdateRandomPathwayProgressHandler(final StudentProgressRTMProducer studentProgressRTMProducer,
                                                 final LearnerPathwayService learnerPathwayService,
                                                 final ProgressService progressService,
                                                 final AttemptService attemptService) {
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

        final UUID pathwayId = event.getElement().getElementId();
        final UUID changeId = event.getUpdateProgressEvent().getChangeId();
        final UUID deploymentId = event.getUpdateProgressEvent().getDeploymentId();
        final UUID evaluationId = event.getUpdateProgressEvent().getEvaluationId();
        final UUID studentId = event.getUpdateProgressEvent().getStudentId();

        // find and build the current random pathway
        LearnerRandomPathway pathway = learnerPathwayService.find(pathwayId, deploymentId, LearnerRandomPathway.class)
                .block();

        affirmArgument(pathway != null, "pathway cannot be null");

        // find the exit condition for this pathway
        Integer exitAfter = pathway.getExitAfter();

        //find a corresponding pathway attempt
        final Progress childProgress = event.getEventProgress().get(event.getEventProgress().size() - 1);

        Attempt attempt = attemptService.findById(childProgress.getAttemptId()).block();
        if (attempt == null) {
            throw new IllegalStateFault("something is wrong. The attempt should exist");
        }
        final UUID attemptId = attempt.getParentId();

        // find the previous progress
        RandomPathwayProgress previousProgress = progressService.findLatestRandomPathway(deploymentId, pathwayId, studentId)
                .block();

        // initialise the progress if it is null
        if (previousProgress == null) {
            previousProgress = new RandomPathwayProgress();
        }

        // read the previous completion information (will be empty maps when initialised)
        final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
        final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

        // Get all the walkables for this pathway.
        List<WalkableChild> childrenWalkables = learnerPathwayService.findWalkables(pathwayId, deploymentId) //
                .collectList() //
                .block();

        // Walk the children and aggregate in their completion values.
        // by re-walking the children (instead of copying the previous progress),
        // we keep the progress up to date if the courseware changes.

        final Map<UUID, Float> newCompletionValues = merge(childrenWalkables, prevComplValues,
                childProgress.getCoursewareElementId(),
                childProgress.getCompletion().getValue());
        final Map<UUID, Float> newCompletionConfidence = merge(childrenWalkables, prevComplConfidence,
                childProgress.getCoursewareElementId(),
                childProgress.getCompletion().getConfidence());

        final Completion completion = buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(), exitAfter);

        final List<UUID> completedWalkables = buildCompletedItems(childrenWalkables, newCompletionValues);

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
        progressService.persist(progress).singleOrEmpty().block();

        // Broadcast the progress change.
        broadcastProgressEventMessage(progress, event.getUpdateProgressEvent());

        // Send it onward.
        propagateProgressChangeUpwards(exchange, event, progress);

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
