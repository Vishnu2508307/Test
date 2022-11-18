package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_EVALUATE_COMPLETE;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.commons.collections4.map.HashedMap;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.dataevent.RouteUri;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.lang.ScenarioEvaluationException;
import com.smartsparrow.eval.service.ScenarioEvaluationService;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.lang.LearnerEvaluationException;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class EvaluationRequestService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(EvaluationRequestService.class);

    private final CamelReactiveStreamsService camel;
    private final LearnerCoursewareService learnerCoursewareService;
    private final ScenarioEvaluationService scenarioEvaluationService;
    private final DeploymentService deploymentService;
    private final AcquireAttemptService acquireAttemptService;
    private final LearnerScenarioService learnerScenarioService;
    private final EvaluationResultService evaluationResultService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final StudentScopeService studentScopeService;

    @Inject
    public EvaluationRequestService(CamelReactiveStreamsService camel,
                                    LearnerCoursewareService learnerCoursewareService,
                                    ScenarioEvaluationService scenarioEvaluationService,
                                    DeploymentService deploymentService,
                                    AcquireAttemptService acquireAttemptService,
                                    LearnerScenarioService learnerScenarioService,
                                    EvaluationResultService evaluationResultService,
                                    LearnerInteractiveService learnerInteractiveService,
                                    StudentScopeService studentScopeService) {
        this.camel = camel;
        this.learnerCoursewareService = learnerCoursewareService;
        this.scenarioEvaluationService = scenarioEvaluationService;
        this.deploymentService = deploymentService;
        this.acquireAttemptService = acquireAttemptService;
        this.learnerScenarioService = learnerScenarioService;
        this.evaluationResultService = evaluationResultService;
        this.learnerInteractiveService = learnerInteractiveService;
        this.studentScopeService = studentScopeService;
    }

    /**
     * Evaluate all {@link ScenarioLifecycle#INTERACTIVE_EVALUATE} scenarios for an interactive
     *
     * @param deploymentId the deployment id to evaluate the scenarios for
     * @param interactiveId the interactive id that holds the scenarios
     * @param producingClientId the clientId that requested the evaluation
     * @param studentId the student id the interactive should be evaluated for
     * @return a mono of evaluation result
     * @throws IllegalArgumentException when any of the required argument is <code>null</code>
     * @throws LearnerEvaluationException when failing to evaluate
     */
    @Deprecated
    public Mono<EvaluationResult> evaluate(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                           final UUID studentId) {
        checkArgument(deploymentId != null, "deploymentId is required");
        checkArgument(interactiveId != null, "interactiveId is required");
        checkArgument(producingClientId != null, "producingClientId is required");
        checkArgument(studentId != null, "studentId is required");

        return deploymentService.findDeployment(deploymentId)
                .doOnError(DeploymentNotFoundException.class, cause -> {
                    throw new LearnerEvaluationException(cause.getMessage(), cause);
                })
                .doOnEach(log.reactiveDebugSignal("starting evaluation", publishedActivity -> new HashedMap<String, Object>(){
                    {
                        put("interactiveId", interactiveId);
                        put("deploymentId", deploymentId);
                        put("studentId", studentId);
                    }
                }))
                .flatMap(deployment -> learnerInteractiveService.findInteractive(interactiveId, deploymentId)
                        .flatMap(interactive -> evaluateInteractive(deployment, interactiveId, producingClientId, studentId, interactive.getEvaluationMode())));
    }

    /**
     * Perform an evaluation of all the {@link ScenarioLifecycle#INTERACTIVE_EVALUATE} scenarios for an interactive and
     * send an {@link EvaluationEventMessage} to the {@link RouteUri#LEARNER_EVALUATE_COMPLETE} for processing with the
     * results of the evaluation.
     *
     * @param deployment the deployment object
     * @param interactiveId the interactive id
     * @param producingClientId the client id requesting the evaluation
     * @param studentId the student id the interactive should be evaluated for
     * @return the result of the evaluation
     * @throws LearnerEvaluationException when an unexpected errors happens during evaluation
     */
    private Mono<EvaluationResult> evaluateInteractive(final Deployment deployment, final UUID interactiveId,
                                                       final String producingClientId, final UUID studentId,
                                                       final EvaluationMode evaluationMode) {
        final UUID deploymentId = deployment.getId();


        // find the parent pathway id
        Mono<UUID> parentPathwayIdMono = learnerInteractiveService.findParentPathwayId(interactiveId, deploymentId);
        // find interactive attempt
        Mono<Attempt> latestInteractiveAttemptMono = acquireAttemptService.acquireLatestInteractiveAttempt(deploymentId, interactiveId, studentId);

        // evaluate the scenarios
        Mono<List<ScenarioEvaluationResult>> scenarioEvaluationResultsMono = evaluateScenariosByMode(deployment, interactiveId, studentId, evaluationMode);

        // build the initial evaluation result for the evaluation event message
        Mono<EvaluationResult> evaluationResultMono = buildEvaluationResultByMode(deployment, interactiveId,
                latestInteractiveAttemptMono, scenarioEvaluationResultsMono, parentPathwayIdMono, evaluationMode);

        // get the ancestry for the interactive
        Mono<List<CoursewareElement>> ancestryMono = learnerCoursewareService.getAncestry(deploymentId,
                interactiveId, CoursewareElementType.INTERACTIVE);

        // find the scope entries data map
        Mono<Map<UUID, String>> entriesMapMono = learnerInteractiveService.findInteractive(interactiveId, deploymentId)
                .flatMap(interactive -> studentScopeService.findLatestEntries(deployment.getId(), studentId, interactive.getStudentScopeURN()));

        // find the learnerInteractive (need access to studentScopeURN)
        Mono<LearnerInteractive> learnerInteractiveMono = learnerInteractiveService.findInteractive(interactiveId, deploymentId);

        // fire the evaluation event then return the evaluation result
        Mono<EvaluationEventMessage> eventMessageMono = fireEvaluationEvent(producingClientId, studentId, evaluationResultMono, ancestryMono, deployment);

        // Zip the evaluation event result with the learner interactive and the scope data map - post courseware routes processing
        return Mono.zip(eventMessageMono, learnerInteractiveMono, entriesMapMono)
                .flatMap(tuple3 -> {
                    final EvaluationEventMessage eventMessage = tuple3.getT1();
                    final LearnerInteractive learnerInteractive = tuple3.getT2();
                    final Map<UUID, String> entriesDataMap = tuple3.getT3();
                    final EvaluationResult evaluationResult = eventMessage.getEvaluationResult();
                    final List<UUID> triggeredScenarioIds = evaluationResult.getScenarioEvaluationResults().stream()
                            .map(ScenarioEvaluationResult::getScenarioId)
                            .collect(Collectors.toList());

                    Evaluation evaluation = new Evaluation()
                            .setId(evaluationResult.getId())
                            .setElementId(learnerInteractive.getId())
                            .setElementType(CoursewareElementType.INTERACTIVE)
                            .setDeployment(evaluationResult.getDeployment())
                            .setStudentId(eventMessage.getStudentId())
                            .setAttemptId(evaluationResult.getAttemptId())
                            .setElementScopeDataMap(entriesDataMap)
                            .setStudentScopeURN(learnerInteractive.getStudentScopeURN())
                            .setParentId(evaluationResult.getParentId())
                            .setParentType(CoursewareElementType.PATHWAY)
                            .setParentAttemptId(evaluationResult.getAttempt().getParentId())
                            .setCompleted(evaluationResult.getInteractiveComplete())
                            .setTriggeredScenarioIds(triggeredScenarioIds)
                            .setScenarioCorrectness(evaluationResult.getScenarioCorrectness())
                            .setTriggeredActions(Json.stringify(evaluationResult.getTriggeredActions()));

                    return evaluationResultService.persist(evaluation)
                            .thenReturn(eventMessage);
                })
                .doOnEach(log.reactiveDebugSignal("evaluation completed", evaluationEventMessage -> new HashedMap<String, Object>(){
                    {
                        put("interactiveId", evaluationEventMessage.getEvaluationResult().getCoursewareElementId());
                        put("studentId", evaluationEventMessage.getStudentId());
                        put("deploymentId", evaluationEventMessage.getDeploymentId());
                    }
                }))
                // return the enriched evaluation result by the courseware routes processing
                .map(EvaluationEventMessage::getEvaluationResult);
    }

    /**
     * Zip all the pieces of data together in order to build the Evaluation result to set to the event message
     *
     * @param deployment the deployment the evaluated interactive belongs to
     * @param interactiveId the learner interactive id
     * @param latestInteractiveAttemptMono the latest learner interactive attempt mono
     * @param scenarioEvaluationResultsMono the scenario evaluation result list mono
     * @param parentPathwayIdMono the parent pathway id mono
     * @param mode the evaluation mode
     * @return an evaluation result
     */
    private Mono<EvaluationResult> buildEvaluationResultByMode(final Deployment deployment,
                                                         final UUID interactiveId,
                                                         final Mono<Attempt> latestInteractiveAttemptMono,
                                                         final Mono<List<ScenarioEvaluationResult>> scenarioEvaluationResultsMono,
                                                         final Mono<UUID> parentPathwayIdMono,
                                                         final EvaluationMode mode) {

        if (mode.equals(EvaluationMode.DEFAULT)) {
            return buildEvaluationResult(deployment, interactiveId,
                                  latestInteractiveAttemptMono, scenarioEvaluationResultsMono, parentPathwayIdMono);
        } else {
            return buildCombinedEvaluationResult(deployment, interactiveId,
                                  latestInteractiveAttemptMono, scenarioEvaluationResultsMono, parentPathwayIdMono);
        }
    }

    /**
     * Zip all the pieces of data together in order to build the Evaluation result to set to the event message
     *
     * @param deployment the deployment the evaluated interactive belongs to
     * @param interactiveId the learner interactive id
     * @param latestInteractiveAttemptMono the latest learner interactive attempt mono
     * @param scenarioEvaluationResultsMono the scenario evaluation result list mono
     * @param parentPathwayIdMono the parent pathway id mono
     * @return an evaluation result
     */
    private Mono<EvaluationResult> buildEvaluationResult(final Deployment deployment,
                                                         final UUID interactiveId,
                                                         final Mono<Attempt> latestInteractiveAttemptMono,
                                                         final Mono<List<ScenarioEvaluationResult>> scenarioEvaluationResultsMono,
                                                         final Mono<UUID> parentPathwayIdMono) {

        return Mono.zip(latestInteractiveAttemptMono, scenarioEvaluationResultsMono, parentPathwayIdMono)
                .flatMap(tuple3 -> {
                    final Attempt interactiveAttempt = tuple3.getT1();
                    final List<ScenarioEvaluationResult> scenarioEvaluationResults = tuple3.getT2();
                    final UUID parentPathwayId = tuple3.getT3();
                    final ScenarioEvaluationResult truthful = findTruthful(scenarioEvaluationResults);

                    return Mono.just(new EvaluationResult()
                            .setId(UUIDs.timeBased())
                            .setCoursewareElementId(interactiveId)
                            .setDeployment(deployment)
                            .setAttempt(interactiveAttempt)
                            .setAttemptId(interactiveAttempt.getId())
                            .setScenarioCorrectness(truthful.getScenarioCorrectness())
                            .setScenarioEvaluationResults(scenarioEvaluationResults)
                            .setParentId(parentPathwayId));
                });
    }

    /**
     * Zip all the pieces of data together in order to build the Evaluation result to set to the event message
     *
     * @param deployment the deployment the evaluated interactive belongs to
     * @param interactiveId the learner interactive id
     * @param latestInteractiveAttemptMono the latest learner interactive attempt mono
     * @param scenarioEvaluationResultsMono the scenario evaluation result list mono
     * @param parentPathwayIdMono the parent pathway id mono
     * @return an evaluation result
     */
    private Mono<EvaluationResult> buildCombinedEvaluationResult(final Deployment deployment,
                                                         final UUID interactiveId,
                                                         final Mono<Attempt> latestInteractiveAttemptMono,
                                                         final Mono<List<ScenarioEvaluationResult>> scenarioEvaluationResultsMono,
                                                         final Mono<UUID> parentPathwayIdMono) {

        return Mono.zip(latestInteractiveAttemptMono, scenarioEvaluationResultsMono, parentPathwayIdMono)
                .flatMap(tuple3 -> {
                    final Attempt interactiveAttempt = tuple3.getT1();
                    final List<ScenarioEvaluationResult> scenarioEvaluationResults = tuple3.getT2();
                    final UUID parentPathwayId = tuple3.getT3();
                    final List<ScenarioEvaluationResult> truthful = findAllTruthful(scenarioEvaluationResults);
                    final ScenarioCorrectness scenarioCorrectness = calculateCorrectness(truthful);

                    return Mono.just(new EvaluationResult()
                                             .setId(UUIDs.timeBased())
                                             .setCoursewareElementId(interactiveId)
                                             .setDeployment(deployment)
                                             .setAttempt(interactiveAttempt)
                                             .setAttemptId(interactiveAttempt.getId())
                                             .setScenarioCorrectness(scenarioCorrectness)
                                             .setScenarioEvaluationResults(scenarioEvaluationResults)
                                             .setParentId(parentPathwayId));
                });
    }

    /**
     * Fire an evaluation event to the {@link RouteUri#LEARNER_EVALUATE_COMPLETE} route
     *
     * @param producingClientId the client that requested the evaluation
     * @param studentId the student id the interactive should be evaluated for
     * @param evaluationResultMono the result of the scenario evaluation in a mono
     * @param ancestryMono the ancestry for the interactive in a mono
     * @return a publisher exchange mono
     * @throws LearnerEvaluationException when anything fails on courseware routes
     */
    private Mono<EvaluationEventMessage> fireEvaluationEvent(final String producingClientId, final UUID studentId,
                                                             final Mono<EvaluationResult> evaluationResultMono,
                                                             final Mono<List<CoursewareElement>> ancestryMono,
                                                             final Deployment deployment) {
        return Mono.zip(ancestryMono, evaluationResultMono)
                .map(tuple -> {
                    List<CoursewareElement> ancestry = tuple.getT1();
                    EvaluationResult evaluationResult = tuple.getT2();

                    return new EvaluationEventMessage()
                            .setAncestryList(ancestry)
                            .setEvaluationResult(evaluationResult)
                            .setProducingClientId(producingClientId)
                            .setStudentId(studentId)
                            .setDeployment(deployment);
                })
                .doOnEach(log.reactiveDebug("triggering learner evaluation complete listeners"))
                .flatMap(eventMessage -> {
                    //
                    // trigger listeners who care about having evaluation completed (update progress, scores, scenario actions, ...)
                    //
                    return Mono.just(eventMessage) //
                            .map(event -> camel.toStream(LEARNER_EVALUATE_COMPLETE, event, EvaluationEventMessage.class)) //
                            .flatMap(Mono::from);
                });
    }

    /**
     * Evaluate all the {@link ScenarioLifecycle#INTERACTIVE_EVALUATE} for an interactive in a {@link EvaluationLearnerContext}
     * based on the evaluation mode.
     *
     * @param deployment the deployment the evaluation is run for
     * @param interactiveId the id of the interactive to test the scenarios for
     * @param studentId the id of the student requesting the scenarios evaluation
     * @param mode the evaluation mode
     * @return a mono list of scenario evaluation results until the first scenario that evaluates to <code>true</code>
     * @throws LearnerEvaluationException when failing to evaluate a scenario
     */
    private Mono<List<ScenarioEvaluationResult>> evaluateScenariosByMode(Deployment deployment, UUID interactiveId, UUID studentId, EvaluationMode mode) {
        if (mode.equals(EvaluationMode.DEFAULT)) {
            return evaluateScenarios(deployment, interactiveId, studentId);
        } else {
            return evaluateCombinedScenarios(deployment, interactiveId, studentId);
        }
    }

    /**
     * Evaluate all the {@link ScenarioLifecycle#INTERACTIVE_EVALUATE} for an interactive in a {@link EvaluationLearnerContext}
     *
     * @param deployment the deployment the evaluation is run for
     * @param interactiveId the id of the interactive to test the scenarios for
     * @param studentId the id of the student requesting the scenarios evaluation
     * @return a mono list of scenario evaluation results until the first scenario that evaluates to <code>true</code>
     * @throws LearnerEvaluationException when failing to evaluate a scenario
     */
    private Mono<List<ScenarioEvaluationResult>> evaluateScenarios(Deployment deployment, UUID interactiveId, UUID studentId) {
        final EvaluationLearnerContext evaluationLearnerContext = buildLearnerContext(deployment, studentId);
        return learnerScenarioService.findAll(deployment, interactiveId, ScenarioLifecycle.INTERACTIVE_EVALUATE)
                .concatMap(scenario -> scenarioEvaluationService.evaluateCondition(scenario, evaluationLearnerContext))
                .map(scenarioEvaluationResult -> scenarioEvaluationResult.setCoursewareElement(CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE)))
                .doOnEach(log.reactiveErrorThrowable("error while evaluating", throwable -> new HashedMap<String, Object>() {
                    {
                        put("interactiveId",  interactiveId);
                        put("studentId",  studentId);
                    }
                }))
                .doOnError(ScenarioEvaluationException.class, cause -> {
                    throw new LearnerEvaluationException(cause.getMessage(), cause);
                })
                .takeUntil(scenarioEvaluationResult -> Boolean.TRUE.equals(scenarioEvaluationResult.getEvaluationResult()))
                .collectList();
    }


    /**
     * Evaluate all the {@link ScenarioLifecycle#INTERACTIVE_EVALUATE} for an interactive in a {@link EvaluationLearnerContext}
     * when the evaluation mode is COMBINED.
     *
     * @param deployment the deployment the evaluation is run for
     * @param interactiveId the id of the interactive to test the scenarios for
     * @param studentId the id of the student requesting the scenarios evaluation
     * @return a mono list of scenario evaluation results until the first scenario that evaluates to <code>true</code>
     * @throws LearnerEvaluationException when failing to evaluate a scenario
     */
    private Mono<List<ScenarioEvaluationResult>> evaluateCombinedScenarios(Deployment deployment, UUID interactiveId, UUID studentId) {
        final EvaluationLearnerContext evaluationLearnerContext = buildLearnerContext(deployment, studentId);
        return learnerScenarioService.findAll(deployment, interactiveId, ScenarioLifecycle.INTERACTIVE_EVALUATE)
                .concatMap(scenario -> scenarioEvaluationService.evaluateCondition(scenario, evaluationLearnerContext))
                .map(scenarioEvaluationResult -> scenarioEvaluationResult.setCoursewareElement(CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE)))
                .doOnEach(log.reactiveErrorThrowable("error while evaluating", throwable -> new HashedMap<String, Object>() {
                    {
                        put("interactiveId",  interactiveId);
                        put("studentId",  studentId);
                    }
                }))
                .doOnError(ScenarioEvaluationException.class, cause -> {
                    throw new LearnerEvaluationException(cause.getMessage(), cause);
                })
                .collectList();
    }

    /**
     * Find a scenarioEvaluationResult in the list that evaluated to <code>true</code>
     *
     * @param scenarioEvaluationResults the list of scenario evaluation results to extract the truthful result from
     * @return the scenario that evaluated to <code>true</code> or an empty scenario when all of them evaluated to
     * <code>false</code> or the list was empty
     */
    public ScenarioEvaluationResult findTruthful(List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        return scenarioEvaluationResults.stream()
                .filter(res -> Boolean.TRUE.equals(res.getEvaluationResult()))
                .findFirst()
                .orElse(new ScenarioEvaluationResult());
    }

    /**
     * Find all scenarioEvaluationResult in the list that evaluated to <code>true</code>
     *
     * @param scenarioEvaluationResults the list of scenario evaluation results to extract the truthful result from
     * @return the scenario that evaluated to <code>true</code> or an empty scenario when all of them evaluated to
     * <code>false</code> or the list was empty
     */
    public List<ScenarioEvaluationResult> findAllTruthful(List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        return scenarioEvaluationResults.stream()
                .filter(res -> Boolean.TRUE.equals(res.getEvaluationResult()))
                .collect(Collectors.toList());
    }

    /**
     * Build the learner context given deployment and student id
     *
     * @param deployment the deployment to build the context with
     * @param studentId the student id to build the context with
     * @return a mono of learner context
     */
    private EvaluationLearnerContext buildLearnerContext(Deployment deployment, UUID studentId) {
        return new EvaluationLearnerContext()
                .setDeploymentId(deployment.getId())
                .setStudentId(studentId);
    }

    /**
     * Calculate the overall scenario correctness from the list of truthful results
     *
     * @param scenarioEvaluationResults the list of scenario evaluation results with a truthful result
     * @return the scenario correctness for the combined results based on precedence
     */
    private ScenarioCorrectness calculateCorrectness(List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        ScenarioCorrectness correctness = null;
        for (ScenarioEvaluationResult result: scenarioEvaluationResults) {
            if (result.getScenarioCorrectness() == ScenarioCorrectness.incorrect) {
                correctness = ScenarioCorrectness.incorrect;
            } else if (result.getScenarioCorrectness() == ScenarioCorrectness.correct && correctness != ScenarioCorrectness.incorrect) {
                correctness = ScenarioCorrectness.correct;
            }
        }
        return correctness;
    }

}
