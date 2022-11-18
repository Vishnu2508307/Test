package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.BKT;
import com.smartsparrow.courseware.pathway.BKTPathway;
import com.smartsparrow.courseware.pathway.LearnerBKTPathway;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;
import com.smartsparrow.util.UUIDs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;

@Singleton
public class UpdateBKTPathwayProgressHandler extends UpdateProgressHandler{

    private static final Logger log = LoggerFactory.getLogger(UpdateBKTPathwayProgressHandler.class);

    private final LearnerPathwayService learnerPathwayService;
    private final ProgressService progressService;
    private final AttemptService attemptService;
    private final CompetencyMetService competencyMetService;
    private final LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Inject
    protected UpdateBKTPathwayProgressHandler(final StudentProgressRTMProducer studentProgressRTMProducer,
                                              final LearnerPathwayService learnerPathwayService,
                                              final ProgressService progressService,
                                              final AttemptService attemptService,
                                              CompetencyMetService competencyMetService,
                                              LearnerCompetencyDocumentService learnerCompetencyDocumentService) {
        super(studentProgressRTMProducer);
        this.learnerPathwayService = learnerPathwayService;
        this.progressService = progressService;
        this.attemptService = attemptService;
        this.competencyMetService = competencyMetService;
        this.learnerCompetencyDocumentService = learnerCompetencyDocumentService;
    }

    @Handler
    @SuppressWarnings("Duplicates")
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "the return value childrenWalkables will be an empty list, not null")
    public void updateProgress(final Exchange exchange) {
        final UpdateCoursewareElementProgressEvent event = exchange.getIn().getBody(UpdateCoursewareElementProgressEvent.class);
        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        final EvaluationResult evaluationResult = eventMessage.getEvaluationResult();

        final UUID pathwayId = event.getElement().getElementId();
        final UUID changeId = event.getUpdateProgressEvent().getChangeId();
        final UUID deploymentId = event.getUpdateProgressEvent().getDeploymentId();
        final UUID evaluationId = event.getUpdateProgressEvent().getEvaluationId();
        final UUID studentId = event.getUpdateProgressEvent().getStudentId();

        // find and build the current BKT pathway
        LearnerBKTPathway pathway = learnerPathwayService.find(pathwayId, deploymentId, LearnerBKTPathway.class)
                .block();

        affirmArgument(pathway != null, "pathway cannot be null");

        // find the latest progress on this pathway
        BKTPathwayProgress previousProgress = progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, 1)
                // init an empty progress if not found
                .singleOrEmpty()
                .switchIfEmpty(Mono.just(new BKTPathwayProgress()))
                .block();

        // compute the BKT when necessary
        BKT.BKTResult bktResult = computeOrCarryOverBKT(evaluationResult, pathway, previousProgress);

        // find a corresponding pathway attempt
        final Progress childProgress = event.getEventProgress().get(event.getEventProgress().size() - 1);

        Attempt attempt = attemptService.findById(childProgress.getAttemptId()).block();
        if (attempt == null) {
            throw new IllegalStateFault("something is wrong. The attempt should exist");
        }
        final UUID attemptId = attempt.getParentId();

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

        final List<UUID> completedWalkables = buildCompletedItems(childrenWalkables, newCompletionValues);

        final int maintainFor = pathway.getMaintainFor();

        // need to check how long the pLn value is maintained for, we'll use this current progress plus previous n-1 progresses
        final int latestN = maintainFor - 1;

        // find the latest n -1 progresses
        List<BKTPathwayProgress> latestNProgresses = progressService
                .findLatestNBKTPathway(deploymentId, pathwayId, studentId, latestN)
                .collectList()
                .block();

        final Completion completion = buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(),
                pathway, latestNProgresses, maintainFor, completedWalkables, bktResult);

        // set the in progress walkable if this is not completed
        CoursewareElement inProgress = new CoursewareElement();

        // set the in progress child only when the screen is not completed
        if (!childProgress.getCompletion().isCompleted()) {
            inProgress.setElementId(childProgress.getCoursewareElementId());
            inProgress.setElementType(childProgress.getCoursewareElementType());
        }

        // set the competency met only when this is the first attempt at this screen
        if (previousProgress.getInProgressElementId() == null) {
            // award any configured competency document item
            awardCompetencyMet(pathway, eventMessage, evaluationResult, bktResult);
        }

        // build the new progress
        BKTPathwayProgress newProgress = new BKTPathwayProgress()
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
                .setCompletedWalkables(completedWalkables)
                .setpLnMinusGivenActual(bktResult.pLnMinus1GivenActual)
                .setpLn(bktResult.pLn)
                .setpCorrect(bktResult.pCorrect);

        // persist the BKT pathway progress
        progressService.persist(newProgress).singleOrEmpty().block();

        // Broadcast the progress change.
        broadcastProgressEventMessage(newProgress, event.getUpdateProgressEvent());

        // Send it onward.
        propagateProgressChangeUpwards(exchange, event, newProgress);
    }

    /**
     * Compute the BKT result on student first attempt at the screen, otherwise carry over the result from the previous
     * progress.
     *
     * @param evaluationResult the result of the current evaluation
     * @param pathway the bkt pathway to compute the bkt result for
     * @param previousProgress the previous progress
     * @return the computed bkt result
     */
    private BKT.BKTResult computeOrCarryOverBKT(final EvaluationResult evaluationResult,
                                                final LearnerBKTPathway pathway,
                                                final BKTPathwayProgress previousProgress) {
        if (previousProgress.getInProgressElementId() != null) {
            // if the previous progress has the in progress set, then this is not the first time the student attempts at this
            // screen, the BKT value should not be computed but carried over.
            BKT.BKTResult result = new BKT.BKTResult();
            result.pLnMinus1GivenActual = previousProgress.getpLnMinusGivenActual();
            result.pCorrect = previousProgress.getpCorrect();
            result.pLn = previousProgress.getpLn();
            return result;
        }
        Double pLn = getPreviousPLNValue(pathway, previousProgress);

        // the scenario correctness determine the actual boolean value for the bkt computation
        // when no scenarios are triggered and the scenario correctness is null, then this value is defaulted to false
        boolean actual = (evaluationResult.getScenarioCorrectness() != null && evaluationResult.getScenarioCorrectness().equals(ScenarioCorrectness.correct));

        // compute the bkt result
        return BKT.calculate(actual, pLn,
                pathway.getSlipProbability(), pathway.getGuessProbability(), pathway.getTransitProbability());
    }

    private void awardCompetencyMet(final LearnerBKTPathway pathway,
                                    final EvaluationEventMessage eventMessage,
                                    final EvaluationResult evaluationResult,
                                    final BKT.BKTResult bktResult) {
        List<BKTPathway.ConfiguredDocumentItem> competency = pathway.getCompetency();
        competency.forEach(configuredCompetency -> {
            final LearnerDocument document = learnerCompetencyDocumentService.findDocument(configuredCompetency.getDocumentId())
                    .block();

            // skip the iteration if the document is null and log it
            if (document == null) {
                log.warn("could not award the configured competecny {} because the document was not found", configuredCompetency.toString());
                return;
            }

            // create a new competency met entry for this student - well done mate!
            CompetencyMet awarded = competencyMetService.create(eventMessage.getStudentId(),
                    eventMessage.getDeploymentId(),
                    eventMessage.getChangeId(),
                    pathway.getId(),
                    CoursewareElementType.PATHWAY,
                    evaluationResult.getId(),
                    configuredCompetency.getDocumentId(),
                    document.getDocumentVersionId(),
                    configuredCompetency.getDocumentItemId(),
                    eventMessage.getAttemptId(),
                    (float) bktResult.pLn,
                    1F
            ).block();
            // compute the competency met for each parent (this has to be rolled up the document structure)
            computeCompetencyMetValueForParentsOf(configuredCompetency.getDocumentItemId(), awarded);
        });
    }

    /**
     * Compute the competency met for each parent of the given document item
     *
     * @param documentItemId the document item id to find the parents and compute the awareded competency for
     * @param awarded the initial awarded competency
     */
    private void computeCompetencyMetValueForParentsOf(final UUID documentItemId, final CompetencyMet awarded) {
        List<ItemAssociation> parents = learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF)
                .collectList()
                .block();

        affirmArgument(parents != null, "parents not found");

        parents.forEach(itemAssociation -> computeCompetencyMetValue(itemAssociation, awarded));
    }

    /**
     * Compute the competency met for a parent document item. The idea is to get all the competency met entries for this item
     * children sum all values and divide by the total number of children. This will give a value over the progress on
     * a parent competency before it is awared.
     *
     * @param element the item association that represent the parent relationship with the awarded document item
     * @param awarded the competency met that has been awared in the first place, carries over necessary information to build
     *                parent competencyMet
     */
    private void computeCompetencyMetValue(final ItemAssociation element, final CompetencyMet awarded) {
        final UUID documentItemId = element.getDestinationItemId();
        final UUID documentId = element.getDocumentId();
        final UUID studentId = awarded.getStudentId();

        // find all the children
        List<UUID> children = learnerCompetencyDocumentService.findAssociationsTo(documentItemId, AssociationType.IS_CHILD_OF)
                .map(ItemAssociation::getOriginItemId)
                .collectList()
                .block();

        affirmArgument(children != null, "children not found");

        // find all the competency met for each children
        List<CompetencyMetByStudent> competencies = children.stream()
                .map(itemId -> competencyMetService.findLatest(studentId, documentId, itemId)
                        // if a child has not competency met entry create an empty one to keep the count
                        .defaultIfEmpty(new CompetencyMetByStudent())
                        .block())
                .collect(Collectors.toList());

        int total = competencies.size();

        // compute the sum of all values
        double valueSum = competencies.stream()
                .filter(one -> one.getValue() != null)
                .mapToDouble(CompetencyMetByStudent::getValue)
                .sum();

        // divide by the total number of children to get the competency met value
        float value = (float) (valueSum / total);
        float confidence = 1f; // since competency are awarded by actions the confidence is always 100%
        // sanity check.
        value = Math.min(value, 1.0f);

        // persist the competency met for this document item
        competencyMetService.create(
                studentId,
                awarded.getDeploymentId(),
                awarded.getChangeId(),
                awarded.getCoursewareElementId(),
                awarded.getCoursewareElementType(),
                awarded.getEvaluationId(),
                documentId,
                awarded.getDocumentVersionId(),
                documentItemId,
                awarded.getAttemptId(),
                value,
                confidence
        ).block();

        // compute competency met for each parent of this item
        computeCompetencyMetValueForParentsOf(documentItemId, awarded);
    }

    private Double getPreviousPLNValue(LearnerBKTPathway pathway, BKTPathwayProgress previousProgress) {
        Double pLn = previousProgress.getpLn();

        if (pLn != null) {
            return pLn;
        }
        // return configured value when the pLn from the previous progress is null
        return pathway.getL0();
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

    /**
     * Compute the completion of this Pathway. There are 2 possible way to mark this pathway as completed:
     * <br> 1. The student was able to maintain a pLn value equal or greater than the configured pLn value for the
     * configured <i>n</i> number of consecutive screens.
     * <br> 2. The student has completed a number of screens that is equal or higher the configured exitAfter value.
     * This method calculate both completion values when necessary and always returns the highest completion value
     * (this means in a newer progress the completion value could be lower)
     *
     * @param completionValues new completion values
     * @param completionConfidences new completion confidences
     * @param pathway the bkt pathway
     * @param latestNProgresses the latest n progresses where n = to the value of maintainFor
     * @param maintainFor the configured value for maintainFor
     * @param completedWalkables the completed walkables
     * @param bktResult the current bkt result
     * @return the completion data
     */
    Completion buildCompletionData(final Collection<Float> completionValues,
                                   final Collection<Float> completionConfidences,
                                   final LearnerBKTPathway pathway,
                                   final List<BKTPathwayProgress> latestNProgresses,
                                   final int maintainFor,
                                   final List<UUID> completedWalkables,
                                   final BKT.BKTResult bktResult) {
        // compute the exitAfter completion value
        Completion exitAfterCompletion = getExitAfterCompletion(completionValues, completionConfidences, pathway);

        if (!completedWalkables.isEmpty()) {
            // now compute the progress with the maintainFor configuration
            Completion BKTCompletion = getBKTCompletion(pathway.getPLN(), maintainFor, latestNProgresses, bktResult);

            float progressValue = exitAfterCompletion.getValue();
            float BKTProgressValue = BKTCompletion.getValue();

            if (progressValue >= BKTProgressValue) {
                // build the completion with the exit after progress which is the highest
                return exitAfterCompletion;
            }

            // build the completion with the bkt progress which is the highest
            return BKTCompletion;
        }

        return exitAfterCompletion;
    }

    private Completion getExitAfterCompletion(final Collection<Float> completionValues,
                                              final Collection<Float> completionConfidences,
                                              final LearnerBKTPathway pathway) {
        // first compute the progress with the exitAfter configuration
        final int exitAfter = pathway.getExitAfter();
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

        return new Completion().setValue(progressValue).setConfidence(confidenceValue);
    }

    private Completion getBKTCompletion(final Double pLn,
                                        final int maintainFor,
                                        final List<BKTPathwayProgress> latestNProgresses,
                                        final BKT.BKTResult bktResult) {
        List<Double> maintainedValues = latestNProgresses.stream()
                // only return the pLn maintained values
                .map(BKTPathwayProgress::getpLn)
                .filter(one -> Double.doubleToRawLongBits(one) >= Double.doubleToRawLongBits(pLn))
                .collect(Collectors.toList());

        // add the latest result to the maintained values
        maintainedValues.add(bktResult.pLn);

        float BKTProgressValue = (float) maintainedValues.size() / maintainFor;
        float BKTConfidenceValue = (float) maintainedValues.size() / maintainFor;

        // sanity check.
        BKTProgressValue = Math.min(BKTProgressValue, 1.0f);
        BKTConfidenceValue = Math.min(BKTConfidenceValue, 1.0f);

        return new Completion().setValue(BKTProgressValue).setConfidence(BKTConfidenceValue);
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
                .filter(walkableChild -> fullyCompletedIds.contains(walkableChild.getElementId())) //
                .map(WalkableChild::getElementId).collect(Collectors.toList());
    }
}
