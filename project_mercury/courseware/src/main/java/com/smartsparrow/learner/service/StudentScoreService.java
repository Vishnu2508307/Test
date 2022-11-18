package com.smartsparrow.learner.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.AdditiveInverser;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.ManualGradeEntry;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.ScoreFormatter;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.ScoreReducer;
import com.smartsparrow.learner.data.StudentManualGrade;
import com.smartsparrow.learner.data.StudentScoreEntry;
import com.smartsparrow.learner.data.StudentScoreGateway;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.lang.ScoreNotFoundFault;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class StudentScoreService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(StudentScoreService.class);
    private final StudentScoreGateway studentScoreGateway;
    private final AttemptService attemptService;
    private final EvaluationRequestService evaluationRequestService;
    private final DeploymentService deploymentService;
    private final LearnerCoursewareService learnerCoursewareService;
    private final ManualGradeService manualGradeService;

    @Inject
    public StudentScoreService(StudentScoreGateway studentScoreGateway,
                               AttemptService attemptService,
                               EvaluationRequestService evaluationRequestService,
                               DeploymentService deploymentService,
                               LearnerCoursewareService learnerCoursewareService,
                               ManualGradeService manualGradeService) {
        this.studentScoreGateway = studentScoreGateway;
        this.attemptService = attemptService;
        this.evaluationRequestService = evaluationRequestService;
        this.deploymentService = deploymentService;
        this.learnerCoursewareService = learnerCoursewareService;
        this.manualGradeService = manualGradeService;
    }

    /**
     * Create a student score entry from an action and evaluation event message
     *
     * @param action the action to create the student scope entry from
     * @param eventMessage the event message carrying the evaluation information
     * @return a mono of the created student score entry
     */
    public Mono<StudentScoreEntry> create(final ChangeScoreAction action, final EvaluationEventMessage eventMessage) {
        return Mono.just(this)
                .flatMap(ignored -> getAdjustmentValue(
                        action.getContext().getOperator(),
                        action.getResolvedValue(),
                        eventMessage.getEvaluationResult().getDeployment().getId(),
                        action.getContext().getElementId(),
                        eventMessage.getStudentId(),
                        eventMessage.getAttemptId()))
                .flatMap(adjustmentValue -> {

                    final EvaluationResult evaluationResult = eventMessage.getEvaluationResult();

                    final ScenarioEvaluationResult truthful = evaluationRequestService
                            .findTruthful(evaluationResult.getScenarioEvaluationResults());

                    final Deployment deployment = evaluationResult.getDeployment();

                    if (truthful.getScenarioId() == null) {
                        // ops, this should not happen. Something went seriously wrong if we are here
                        throw new IllegalStateFault("Score cannot be awarded via action when truthful scenario is not found");
                    }

                    StudentScoreEntry entry = new StudentScoreEntry()
                            .setCohortId(deployment.getCohortId())
                            .setDeploymentId(deployment.getId())
                            .setChangeId(deployment.getChangeId())
                            .setStudentId(eventMessage.getStudentId())
                            .setElementId(action.getContext().getElementId())
                            .setElementType(action.getContext().getElementType())
                            .setAttemptId(eventMessage.getAttemptId())
                            .setId(UUIDs.timeBased())
                            .setValue(action.getResolvedValue())
                            .setAdjustmentValue(adjustmentValue)
                            .setEvaluationId(eventMessage.getEvaluationId())
                            .setSourceElementId(null) // null when defined by the action
                            .setSourceScenarioId(truthful.getScenarioId())
                            .setSourceAccountId(null) // null when defined via action
                            .setOperator(action.getContext().getOperator());

                    return studentScoreGateway.persist(entry);
                });
    }

    /**
     * Create a student score entry from an action and response context
     *
     * @param action the action to create the student scope entry from
     * @param responseContext the response context carrying the evaluation information
     * @return a mono of the created student score entry
     */
    @Trace(async = true)
    public Mono<StudentScoreEntry> create(final ChangeScoreAction action, final LearnerEvaluationResponseContext responseContext) {

        WalkableEvaluationResult result = responseContext.getResponse()
                .getWalkableEvaluationResult();
        LearnerEvaluationRequest request = responseContext.getResponse()
                .getEvaluationRequest();
        return Mono.just(this)
                .flatMap(ignored -> getAdjustmentValue(
                        action.getContext().getOperator(),
                        action.getResolvedValue(),
                        request.getDeployment().getId(),
                        action.getContext().getElementId(),
                        request.getStudentId(),
                        request.getAttempt().getId()))
                .flatMap(adjustmentValue -> {

                    final Deployment deployment = request.getDeployment();

                    if (result.getTruthfulScenario().getScenarioId() == null) {
                        // ops, this should not happen. Something went seriously wrong if we are here
                        throw new IllegalStateFault("Score cannot be awarded via action when truthful scenario is not found");
                    }

                    StudentScoreEntry entry = new StudentScoreEntry()
                            .setCohortId(deployment.getCohortId())
                            .setDeploymentId(deployment.getId())
                            .setChangeId(deployment.getChangeId())
                            .setStudentId(request.getStudentId())
                            .setElementId(action.getContext().getElementId())
                            .setElementType(action.getContext().getElementType())
                            .setAttemptId(request.getAttempt().getId())
                            .setId(UUIDs.timeBased())
                            .setValue(action.getResolvedValue())
                            .setAdjustmentValue(adjustmentValue)
                            .setEvaluationId(result.getId())
                            .setSourceElementId(null) // null when defined by the action
                            .setSourceScenarioId(result.getTruthfulScenario().getScenarioId())
                            .setSourceAccountId(null) // null when defined via action
                            .setOperator(action.getContext().getOperator());

                    return studentScoreGateway.persist(entry);
                });
    }

    /**
     * For each ancestor element in the ancestry list. Create a score log entry. Each ancestor is processed in an
     * ordered manner so that the data produced is consistent.
     *
     * @param originalEntry the original score entry created from the action
     * @param ancestry the ancestry list to create score entries for
     * @return a flux of student score entries
     */
    @Trace(async = true)
    public Flux<StudentScoreEntry> rollUpScoreEntries(StudentScoreEntry originalEntry, List<CoursewareElement> ancestry) {
        return ancestry.stream()
                .map(ancestor -> createAncestorEntry(originalEntry, ancestor)
                        .flux())
                .reduce(Flux::concatWith)
                .orElse(Flux.empty());
    }

    /**
     * Create a student score entry for an ancestor from the original score entry
     *
     * @param originalEntry the previously generated score entry
     * @param ancestorElement the element in the ancestry list to generate the
     * @return a mono of the created student score entry for the ancestor element
     */
    @Trace(async = true)
    public Mono<StudentScoreEntry> createAncestorEntry(final StudentScoreEntry originalEntry,
                                                       final CoursewareElement ancestorElement) {

        final UUID studentId = originalEntry.getStudentId();

        // build the deployment object to easily pass it around as argument
        final Deployment deployment = new Deployment()
                .setId(originalEntry.getDeploymentId())
                .setCohortId(originalEntry.getCohortId())
                .setChangeId(originalEntry.getChangeId());

        return attemptService.findLatestAttempt(deployment.getId(), ancestorElement.getElementId(), studentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(Attempt::getId)
                .flatMap(latestAttemptId -> {
                    StudentScoreEntry ancestorEntry = new StudentScoreEntry()
                            .setCohortId(originalEntry.getCohortId())
                            .setDeploymentId(originalEntry.getDeploymentId())
                            .setChangeId(originalEntry.getChangeId())
                            .setStudentId(studentId)
                            .setElementId(ancestorElement.getElementId())
                            .setElementType(ancestorElement.getElementType())
                            .setAttemptId(latestAttemptId)
                            .setId(UUIDs.timeBased())
                            .setValue(originalEntry.getValue())
                            .setEvaluationId(originalEntry.getEvaluationId())
                            .setAdjustmentValue(originalEntry.getAdjustmentValue())
                            .setSourceElementId(originalEntry.getElementId())
                            .setSourceScenarioId(originalEntry.getSourceScenarioId())
                            .setSourceAccountId(originalEntry.getSourceAccountId())
                            .setOperator(originalEntry.getOperator());
                    return studentScoreGateway.persist(ancestorEntry);
                });
    }

    /**
     * Create a student score entry from a manual graded component
     *
     * @param manualGradeEntry the manual grade entry to create the student score entry for
     * @return a mono of student score entry
     * @throws NotFoundFault when the deployment is not found
     */
    public Mono<StudentScoreEntry> create(final ManualGradeEntry manualGradeEntry) {
        // find the deployment
        Mono<DeployedActivity> deploymentMono = deploymentService.findDeployment(manualGradeEntry.getDeploymentId())
                .doOnError(DeploymentNotFoundException.class, ex -> {
                    throw new NotFoundFault("deployment not found");
                });
        // get the adjustment value
        Mono<Double> adjustmentValueMono = getAdjustmentValue(
                manualGradeEntry.getOperator(),
                manualGradeEntry.getScore(),
                manualGradeEntry.getDeploymentId(),
                manualGradeEntry.getComponentId(),
                manualGradeEntry.getStudentId(),
                manualGradeEntry.getAttemptId());

        return Mono.zip(deploymentMono, adjustmentValueMono)
                .flatMap(tuple2 -> {
                    final Deployment deployment = tuple2.getT1();
                    final Double adjustmentValue = tuple2.getT2();

                    // build the student score entry from the manual grade
                    StudentScoreEntry entry = new StudentScoreEntry()
                            .setCohortId(deployment.getCohortId())
                            .setDeploymentId(manualGradeEntry.getDeploymentId())
                            .setChangeId(manualGradeEntry.getChangeId())
                            .setStudentId(manualGradeEntry.getStudentId())
                            .setElementId(manualGradeEntry.getComponentId())
                            .setElementType(CoursewareElementType.COMPONENT)
                            .setAttemptId(manualGradeEntry.getAttemptId())
                            .setId(UUIDs.timeBased())
                            .setEvaluationId(null) // null when assigned by the instructor
                            .setValue(manualGradeEntry.getScore())
                            .setAdjustmentValue(adjustmentValue)
                            .setSourceElementId(null) // null when assigned by the instructor
                            .setSourceScenarioId(null) // when assigned by the instructor
                            .setSourceAccountId(manualGradeEntry.getInstructorId())
                            .setOperator(manualGradeEntry.getOperator());

                    // persist the student score entry
                    return studentScoreGateway.persist(entry);
                });
    }

    /**
     * Compute the adjustment value for a score entry. Following rules apply:
     * <ul>
     *     <li><strong>ADD</strong> - the adjustment value is the same as the value</li>
     *     <li><strong>REMOVE</strong> - the adjustment value is the opposite number as the value</li>
     *     <li><strong>SET</strong> - the adjustment value is the addend required to reach the value in a sum,
     *     based on the current computed score</li>
     * </ul>
     *
     * @param operator the score operator
     * @param value the score value
     * @param deploymentId the deployment the element id belongs to
     * @param elementId the element id to score
     * @param studentId the student id the score is awarded for
     * @param attemptId the student attempt id at the element
     * @return a mono of holding the adjustment value
     */
    Mono<Double> getAdjustmentValue(final MutationOperator operator,
                                    final Double value,
                                    final UUID deploymentId,
                                    final UUID elementId,
                                    final UUID studentId,
                                    @Nullable UUID attemptId) {
        return Mono.just(this)
                .flatMap(ignored -> {
                    switch (operator) {
                        case ADD:
                            return Mono.just(value);
                        case REMOVE:
                            Double removeValue = value;
                            removeValue = -removeValue;
                            return Mono.just(removeValue);
                        case SET:
                            // check that the value is not negative
                            if (value < 0) {
                                throw new IllegalArgumentFault("Negative value is not allowed with a SET operator");
                            }

                            // find all the score entries and compute the score (read before write)
                            return _computeScore(deploymentId, studentId, elementId, attemptId)
                                    // return a value of 0 when the score is not found
                                    .onErrorResume(ScoreNotFoundFault.class, ex -> Mono.just(0d))
                                    // the difference between the setValue and the currentScore is the adjustment value
                                    .flatMap(currentScore -> Mono.just(value - currentScore));
                        default:
                            throw new UnsupportedOperationException(String.format(
                                    "Unsupported operator type %s", operator
                            ));
                    }
                });
    }

    /**
     * Fetch all the score adjustment values then reduce them to a single formatted value.
     *
     * @param deploymentId the deployment to find the score entries in
     * @param studentId the student id to find the score entries for
     * @param elementId the courseware element id the score entries refers to
     * @param attemptId the student attempt at the courseware element
     * @return a mono of double representing the computed score value
     * @throws ScoreNotFoundFault when score cannot be computed because no score entries were found
     */
    @Trace(async = true)
    private Mono<Double> _computeScore(final UUID deploymentId, final UUID studentId, final UUID elementId,
                                       @Nullable final UUID attemptId) {
        return fetchScoreValues(deploymentId, studentId, elementId, attemptId)
                // reduces all adjustment values to a single value (not invoked when flux emits only 1 item)
                .reduce(new ScoreReducer())
                // ensures that the reduced score is never a negative value
                .map(new AdditiveInverser())
                // applies formatting to decimal values
                .map(new ScoreFormatter())
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ScoreNotFoundFault("Student score entries not found");
                });
    }

    /**
     * Compute the score by reducing all score adjustment values via {@link ScoreReducer}.
     *
     * @param deploymentId the deployment the element belongs to
     * @param studentId the student id the score is awarded to
     * @param elementId the element id to compute the score for
     * @param attemptId the student attempt at the courseware element
     * @return return a mono of student score with {@link ScoreReason#SCORED} when the score is computed succesfully or:
     * <ul>
     *     <li>- {@link ScoreReason#UNSCORED} when no score entries are found</li>
     *     <li>- {@link ScoreReason#NOT_ATTEMPTED} when student attempt at the element is not found</li>
     * </ul>
     */
    @Trace(async = true)
    public Mono<Score> computeScore(final UUID deploymentId, final UUID studentId,
                                    final UUID elementId, @Nullable final UUID attemptId) {
        log.info("computeScore() :: " + deploymentId + ", " + studentId + ", " + elementId + ", " + attemptId);
        return _computeScore(deploymentId, studentId, elementId, attemptId)
                // build a score object with reason SCORED
                .map(score -> new Score()
                        .setValue(score)
                        .setReason(ScoreReason.SCORED))
                .doOnEach(ReactiveTransaction.linkOnNext())
                // if no score entries were found then return 0 with reason UNSCORED
                .onErrorResume(ScoreNotFoundFault.class, ex -> Mono.just(new Score()
                        .setValue(0d)
                        .setReason(ScoreReason.UNSCORED)))
                .onErrorResume(AttemptNotFoundFault.class, ex -> Mono.just(new Score()
                        .setValue(0d)
                        .setReason(ScoreReason.NOT_ATTEMPTED)));
    }

    /**
     * Fetch all the score entries for a student over an element attempt and returns the ordered adjustment values.
     *
     * @param deploymentId the deployment the scored courseware element belongs to
     * @param studentId the student id the score has been awarded for
     * @param elementId the element id the score belongs to
     * @param attemptId the student attempt at the courseware element
     * @return a flux of adjustment values or an empty stream when no values are found
     */
    @Trace(async = true)
    private Flux<Double> fetchScoreValues(final UUID deploymentId, final UUID studentId, final UUID elementId,
                                          @Nullable UUID attemptId) {
        return fetchScoreEntries(deploymentId, studentId, elementId, attemptId)
                .concatMap(studentScoreEntry -> Mono.just(studentScoreEntry.getAdjustmentValue()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the score entries for a student on a particular element attempt in a deployment. When the attempt id is
     * <code>null</code> the latest attempt is found for the element
     *
     * @param deploymentId the deployment to find the score entries in
     * @param studentId the student id to find the score entries for
     * @param elementId the element id the score entries refers to
     * @param attemptId the student attempt at the courseware element
     * @return a flux of student score entries
     */
    @Trace(async = true)
    public Flux<StudentScoreEntry> fetchScoreEntries(final UUID deploymentId, final UUID studentId,
                                                     final UUID elementId, @Nullable final UUID attemptId) {
        Mono<UUID> attemptIdMono;

        if (attemptId == null) {
            attemptIdMono = attemptService.findLatestAttempt(deploymentId, elementId, studentId)
                    .map(Attempt::getId)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } else {
            attemptIdMono = Mono.just(attemptId);
        }

        return attemptIdMono
                .flux()
                .flatMap(_attemptId -> studentScoreGateway.find(deploymentId, studentId, elementId, _attemptId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create the student manual grade. Once the grade is persisted, the student score entries are generated and
     * rolled up the courseware structure.
     *
     * @param deploymentId the deployment the component belongs to
     * @param componentId the component id to grade
     * @param studentId the student receiving the grade
     * @param attemptId the attempt the grade refers to
     * @param score the grade score
     * @param operator the grade score operator
     * @param instructorId the instructor that assigned the grade
     * @return a mono of student manual grade
     */
    @Trace(async = true)
    public Mono<StudentManualGrade> createStudentManualGrade(final UUID deploymentId, final UUID componentId,
                                                             final UUID studentId, final UUID attemptId, final Double score,
                                                             final MutationOperator operator, final UUID instructorId) {
        // create the manual grade entry
        return manualGradeService.createManualGrade(deploymentId, componentId, studentId, attemptId, score, operator, instructorId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // create the student score entries
                .flatMap(persistedManualGradeEntry -> createStudentScoreEntriesFor(persistedManualGradeEntry)
                        .thenReturn(persistedManualGradeEntry))
                // map the manual grade entry to a student manual grade
                .map(persistedManualGradeEntry -> new StudentManualGrade()
                        .setId(persistedManualGradeEntry.getId())
                        .setCreatedAt(DateFormat.asRFC1123(persistedManualGradeEntry.getId()))
                        .setOperator(persistedManualGradeEntry.getOperator())
                        .setScore(persistedManualGradeEntry.getScore())
                        .setInstructorId(persistedManualGradeEntry.getInstructorId()));
    }

    /**
     * Create a student score entry for the manual graded component then roll up the score entry to the courseware
     * structure
     *
     * @param manualGradeEntry the manual grade entry to create a student score entry from
     * @return a mono of the created student score entry from the manual grade
     */
    private Mono<StudentScoreEntry> createStudentScoreEntriesFor(final ManualGradeEntry manualGradeEntry) {
        // find the ancestry for the current element
        Mono<List<CoursewareElement>> ancestryList = learnerCoursewareService.getAncestry(
                manualGradeEntry.getDeploymentId(),
                manualGradeEntry.getComponentId(),
                CoursewareElementType.COMPONENT);

        return ancestryList
                .flatMap(ancestry -> {
                    // remove the first element from the ancestry which is the actual component
                    final List<CoursewareElement> filteredAncestry = ancestry.stream()
                            .filter(one -> !one.getElementId().equals(manualGradeEntry.getComponentId()))
                            .collect(Collectors.toList());
                    // create the student score entry
                    return create(manualGradeEntry)
                            // roll up the score
                            .flatMap(originalStudentScoreEntry -> {
                                return rollUpScoreEntries(originalStudentScoreEntry, filteredAncestry)
                                        .then(Mono.just(originalStudentScoreEntry));
                            });
                });
    }
}
