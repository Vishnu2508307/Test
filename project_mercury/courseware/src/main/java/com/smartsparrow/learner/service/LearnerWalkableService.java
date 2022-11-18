package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.service.EvaluationErrorService;
import com.smartsparrow.eval.service.LearnerEvaluationResponseEnricher;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.lang.LearnerEvaluationException;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class LearnerWalkableService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerWalkableService.class);

    private final LearnerActivityService learnerActivityService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final AcquireAttemptService acquireAttemptService;
    private final EvaluationSubmitService evaluationSubmitService;
    private final DeploymentService deploymentService;
    private final LearnerEvaluationResponseEnricher learnerEvaluationResponseEnricher;
    private final LearnerActionConsumerService learnerActionConsumerService;
    private final EvaluationResultService evaluationResultService;
    private final EvaluationErrorService evaluationErrorService;
    private final CacheService cacheService;

    @Inject
    public LearnerWalkableService(final LearnerActivityService learnerActivityService,
                                  final LearnerInteractiveService learnerInteractiveService,
                                  final AcquireAttemptService acquireAttemptService,
                                  final EvaluationSubmitService evaluationSubmitService,
                                  final DeploymentService deploymentService,
                                  final LearnerEvaluationResponseEnricher learnerEvaluationResponseEnricher,
                                  final LearnerActionConsumerService learnerActionConsumerService,
                                  final EvaluationResultService evaluationResultService,
                                  final EvaluationErrorService evaluationErrorService,
                                  final CacheService cacheService) {
        this.learnerActivityService = learnerActivityService;
        this.learnerInteractiveService = learnerInteractiveService;
        this.acquireAttemptService = acquireAttemptService;
        this.evaluationSubmitService = evaluationSubmitService;
        this.deploymentService = deploymentService;
        this.learnerEvaluationResponseEnricher = learnerEvaluationResponseEnricher;
        this.learnerActionConsumerService = learnerActionConsumerService;
        this.evaluationResultService = evaluationResultService;
        this.evaluationErrorService = evaluationErrorService;
        this.cacheService = cacheService;
    }

    /**
     * Find the learner walkable by deployment id and type
     *
     * @param deploymentId the deployment the walkable belongs to
     * @param walkableId   the id of the walkable
     * @param walkableType the type of the walkable
     * @return a mono with the found learner walkable
     * @throws com.smartsparrow.exception.IllegalArgumentFault when any of the method argument is null or invalid
     * @throws UnsupportedOperationFault                       when an invalid walkable type is supplied
     */
    @Trace(async = true)
    public Mono<? extends LearnerWalkable> findLearnerWalkable(final UUID deploymentId, UUID walkableId, final CoursewareElementType walkableType) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(walkableId != null, "walkableId is required");
        affirmArgument(walkableType != null, "walkableType is required");
        affirmArgument(CoursewareElementType.isAWalkable(walkableType), String.format("%s not a walkableType", walkableType));

        switch (walkableType) {
            case ACTIVITY:
                return learnerActivityService.findActivity(walkableId, deploymentId);
            case INTERACTIVE:
                return learnerInteractiveService.findInteractive(walkableId, deploymentId);
            default:
                throw new UnsupportedOperationFault(String.format("walkableType %s not supported", walkableType));
        }
    }

    /**
     * Find the parent pathway id for a walkable
     *
     * @param deploymentId the deployment the walkable belongs to
     * @param walkableId   the id of the walkable to find the parent pathway id for
     * @param walkableType the type of walkable
     * @return a mono containing the parent pathway id
     * @throws com.smartsparrow.exception.IllegalArgumentFault when any of the method argument is null or invalid
     * @throws UnsupportedOperationFault                       when an invalid walkable type is supplied
     * @throws LearnerPathwayNotFoundFault                     when parent pathway is not found
     */
    @Trace(async = true)
    public Mono<UUID> findParentPathwayId(final UUID deploymentId, final UUID walkableId, final CoursewareElementType walkableType) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(walkableId != null, "walkableId is required");
        affirmArgument(walkableType != null, "walkableType is required");
        affirmArgument(CoursewareElementType.isAWalkable(walkableType), String.format("%s not a walkableType", walkableType));

        switch (walkableType) {
            case ACTIVITY:
                return learnerActivityService.findParentPathwayId(walkableId, deploymentId);
            case INTERACTIVE:
                return learnerInteractiveService.findParentPathwayId(walkableId, deploymentId);
            default:
                throw new UnsupportedOperationFault(String.format("walkableType %s not supported", walkableType));
        }
    }

    /**
     * Allows to evaluate a walkable. Prepares an evaluation request that is submitted to the evaluation service.
     * This generates an evaluation response that id enriched and for which all the action consumers are triggered
     *
     * @param deploymentId      the deployment id the walkable belongs to
     * @param walkableId        the id of the walkable to evaluate
     * @param walkableType      the type of the walkable to evaluate
     * @param studentId         the student id triggering the evaluation
     * @param producingClientId the websocket client id the request was fired from
     * @return a mono containing the evaluation response
     * @throws LearnerEvaluationException when an error occurs during evaluation
     */
    @Trace(async = true)
    public Mono<LearnerEvaluationResponse> evaluate(final UUID deploymentId, final UUID walkableId,
                                                    final CoursewareElementType walkableType,
                                                    final UUID studentId,
                                                    final String producingClientId) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(walkableId != null, "walkableId is required");
        affirmArgument(walkableType != null, "walkableType is required");
        affirmArgument(CoursewareElementType.isAWalkable(walkableType), "elementType must be a walkable type");
        affirmArgument(studentId != null, "studentId is required");
        affirmArgumentNotNullOrEmpty(producingClientId, "producingClientId is required");

        // read the parent pathway id
        Mono<UUID> usedParentPathwayIdMono = findParentPathwayId(deploymentId, walkableId, walkableType);
        // find the walkable
        Mono<? extends LearnerWalkable> learnerWalkableMono = findLearnerWalkable(deploymentId, walkableId, walkableType);
        // find the latest attempt
        Mono<Attempt> attemptMono = acquireAttemptService.acquireLatestAttempt(deploymentId, walkableId, walkableType, studentId);
        // find the deployment
        Mono<DeployedActivity> deployedActivityMono = deploymentService.findDeployment(deploymentId);

        // zip the sources so we can build an evaluation request
        return Mono.zip(learnerWalkableMono, attemptMono, usedParentPathwayIdMono, deployedActivityMono)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // if any error happens then log it to the console
                .doOnEach(log.reactiveErrorThrowable("reactive evaluation error"))
                // if any error happens resume by storing the error to the db and throwing an evaluation exception
                .onErrorResume(Throwable.class, throwable -> {
                    final UUID id = UUIDs.timeBased();
                    return evaluationErrorService.createGeneric(throwable, id)
                            .then(Mono.error(new LearnerEvaluationException("reactive evaluation error", throwable, id)));
                })
                // build the learner evaluation request
                .map(tuple4 -> new LearnerEvaluationRequest()
                        .setScenarioLifecycle(ScenarioLifecycle.defaultScenarioLifecycle(walkableType))
                        .setLearnerWalkable(tuple4.getT1())
                        .setAttempt(tuple4.getT2())
                        .setParentPathwayId(tuple4.getT3())
                        .setStudentId(studentId)
                        .setProducingClientId(producingClientId)
                        .setDeployment(tuple4.getT4()))
                // evaluate the request and get the evaluation response
                .flatMap(learnerEvaluationRequest -> evaluationSubmitService.submit(learnerEvaluationRequest, LearnerEvaluationResponse.class))
                // enrich the evaluation response and return the response context
                .flatMap(learnerEvaluationResponse -> learnerEvaluationResponseEnricher.enrich(new LearnerEvaluationResponseContext()
                        .setResponse(learnerEvaluationResponse)))
                // trigger the action consumers via the action consumer service(exclude grade passback)
                .flatMap(enrichedContext -> learnerActionConsumerService.consume(enrichedContext.getResponse().getWalkableEvaluationResult().getTriggeredActions().
                                                                                         stream().filter(a -> !a.getType().equals(Action.Type.GRADE_PASSBACK))
                                                                                         .collect(Collectors.toList()), enrichedContext)
                        .collectList()
                        .flatMap(actionResults -> {
                            // set the action results to the response so they can be returned
                            enrichedContext.getResponse()
                                    .getWalkableEvaluationResult()
                                    .setActionResults(actionResults);
                            // return the enriched context
                            return Mono.just(enrichedContext);
                        }))
                //Trigger grade passback consumer once all other actions are completed
                .flatMap(enrichedContext -> learnerActionConsumerService.consume(enrichedContext.getResponse().getWalkableEvaluationResult().getTriggeredActions().
                                                                                         stream().filter(a -> a.getType().equals(Action.Type.GRADE_PASSBACK))
                                                                                         .collect(Collectors.toList()), enrichedContext)
                        .collectList()
                        .flatMap(actionResults -> {
                            // set the action results to the response so they can be returned
                            enrichedContext.getResponse()
                                    .getWalkableEvaluationResult()
                                    .getActionResults().addAll(actionResults);
                            // return the enriched context
                            return Mono.just(enrichedContext);
                        }))
                // alright the final step is to persist the evaluation and return the response
                .flatMap(enrichedContext -> {
                    // prepare the required variables
                    final LearnerEvaluationResponse response = enrichedContext.getResponse();
                    final WalkableEvaluationResult result = response.getWalkableEvaluationResult();
                    final LearnerEvaluationRequest request = response.getEvaluationRequest();

                    // build the evaluation object
                    Evaluation evaluation = new Evaluation()
                            .setId(result.getId())
                            .setElementId(request.getLearnerWalkable().getId())
                            .setElementType(request.getLearnerWalkable().getElementType())
                            .setDeployment(request.getDeployment())
                            .setStudentId(request.getStudentId())
                            .setAttemptId(request.getAttempt().getId())
                            .setElementScopeDataMap(enrichedContext.getScopeEntriesMap())
                            .setStudentScopeURN(request.getLearnerWalkable().getStudentScopeURN())
                            .setParentId(request.getParentPathwayId())
                            .setParentType(CoursewareElementType.PATHWAY)
                            .setParentAttemptId(request.getAttempt().getParentId())
                            .setCompleted(result.isWalkableComplete())
                            .setTriggeredScenarioIds(response.getTriggeredScenarioIds(response.getScenarioEvaluationResults()))
                            .setScenarioCorrectness(result.getTruthfulScenario().getScenarioCorrectness())
                            .setTriggeredActions(Json.stringify(result.getTriggeredActions()));

                    // persist the evaluation
                    return evaluationResultService.persist(evaluation)
                            // return the response
                            .then(Mono.just(response));
                });
    }


    /**
     * Allows to evaluate a walkable. Prepares an evaluation request that is submitted to the evaluation service.
     * This generates an evaluation response that id enriched and for which all the action consumers are triggered
     *
     * @param deploymentId      the deployment id the walkable belongs to
     * @param walkableId        the id of the walkable to evaluate
     * @param walkableType      the type of the walkable to evaluate
     * @param studentId         the student id triggering the evaluation
     * @param producingClientId the websocket client id the request was fired from
     * @param timeId            the id for the student scope
     * @return a mono containing the evaluation response
     * @throws LearnerEvaluationException when an error occurs during evaluation
     */
    @Trace(async = true)
    public Mono<LearnerEvaluationResponse> evaluate(final UUID deploymentId, final UUID walkableId,
                                                    final CoursewareElementType walkableType,
                                                    final UUID studentId,
                                                    final String producingClientId,
                                                    final UUID timeId) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(walkableId != null, "walkableId is required");
        affirmArgument(walkableType != null, "walkableType is required");
        affirmArgument(CoursewareElementType.isAWalkable(walkableType), "elementType must be a walkable type");
        affirmArgument(studentId != null, "studentId is required");
        affirmArgumentNotNullOrEmpty(producingClientId, "producingClientId is required");
        affirmArgument(timeId != null, "timeId is required");

        // read the parent pathway id
        Mono<UUID> usedParentPathwayIdMono = findParentPathwayId(deploymentId, walkableId, walkableType);
        // find the walkable
        Mono<? extends LearnerWalkable> learnerWalkableMono = findLearnerWalkable(deploymentId, walkableId, walkableType);
        // find the latest attempt
        Mono<Attempt> attemptMono = acquireAttemptService.acquireLatestAttempt(deploymentId, walkableId, walkableType, studentId);
        // find the deployment
        Mono<DeployedActivity> deployedActivityMono = deploymentService.findDeployment(deploymentId);

        // zip the sources so we can build an evaluation request
        return Mono.zip(learnerWalkableMono, attemptMono, usedParentPathwayIdMono, deployedActivityMono)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // if any error happens then log it to the console
                .doOnEach(log.reactiveErrorThrowable("reactive evaluation error"))
                // if any error happens resume by storing the error to the db and throwing an evaluation exception
                .onErrorResume(Throwable.class, throwable -> {
                    final UUID id = UUIDs.timeBased();
                    return evaluationErrorService.createGeneric(throwable, id)
                            .then(Mono.error(new LearnerEvaluationException("reactive evaluation error", throwable, id)));
                })
                // build the learner evaluation request
                .map(tuple4 -> new LearnerEvaluationRequest()
                        .setScenarioLifecycle(ScenarioLifecycle.defaultScenarioLifecycle(walkableType))
                        .setLearnerWalkable(tuple4.getT1())
                        .setAttempt(tuple4.getT2())
                        .setParentPathwayId(tuple4.getT3())
                        .setStudentId(studentId)
                        .setProducingClientId(producingClientId)
                        .setDeployment(tuple4.getT4()))
                // evaluate the request and get the evaluation response
                .flatMap(learnerEvaluationRequest -> evaluationSubmitService.submit(learnerEvaluationRequest, LearnerEvaluationResponse.class))
                // enrich the evaluation response and return the response context
                .flatMap(learnerEvaluationResponse -> learnerEvaluationResponseEnricher.enrich(new LearnerEvaluationResponseContext()
                                                                                                       .setResponse(learnerEvaluationResponse)
                                                                                                       .setTimeId(timeId)))
                // trigger the action consumers via the action consumer service(exclude grade passback)
                .flatMap(enrichedContext -> learnerActionConsumerService.consume(enrichedContext.getResponse().getWalkableEvaluationResult().getTriggeredActions().
                                                                                         stream().filter(a -> !a.getType().equals(Action.Type.GRADE_PASSBACK))
                                                                                         .collect(Collectors.toList()), enrichedContext)
                        .collectList()
                        .flatMap(actionResults -> {
                            // set the action results to the response so they can be returned
                            enrichedContext.getResponse()
                                    .getWalkableEvaluationResult()
                                    .setActionResults(actionResults);
                            // return the enriched context
                            return Mono.just(enrichedContext);
                        }))
                //Trigger grade passback consumer once all other actions are completed
                .flatMap(enrichedContext -> learnerActionConsumerService.consume(enrichedContext.getResponse().getWalkableEvaluationResult().getTriggeredActions().
                                                                                                      stream().filter(a -> a.getType().equals(Action.Type.GRADE_PASSBACK))
                                                                                                      .collect(Collectors.toList()), enrichedContext)
                        .collectList()
                        .flatMap(actionResults -> {
                            // set the action results to the response so they can be returned
                            enrichedContext.getResponse()
                                    .getWalkableEvaluationResult()
                                    .getActionResults().addAll(actionResults);
                            // return the enriched context
                            return Mono.just(enrichedContext);
                        }))
                // alright the final step is to persist the evaluation and return the response
                .flatMap(enrichedContext -> {
                    // prepare the required variables
                    final LearnerEvaluationResponse response = enrichedContext.getResponse();
                    final WalkableEvaluationResult result = response.getWalkableEvaluationResult();
                    final LearnerEvaluationRequest request = response.getEvaluationRequest();

                    // build the evaluation object
                    Evaluation evaluation = new Evaluation()
                            .setId(result.getId())
                            .setElementId(request.getLearnerWalkable().getId())
                            .setElementType(request.getLearnerWalkable().getElementType())
                            .setDeployment(request.getDeployment())
                            .setStudentId(request.getStudentId())
                            .setAttemptId(request.getAttempt().getId())
                            .setElementScopeDataMap(enrichedContext.getScopeEntriesMap())
                            .setStudentScopeURN(request.getLearnerWalkable().getStudentScopeURN())
                            .setParentId(request.getParentPathwayId())
                            .setParentType(CoursewareElementType.PATHWAY)
                            .setParentAttemptId(request.getAttempt().getParentId())
                            .setCompleted(result.isWalkableComplete())
                            .setTriggeredScenarioIds(response.getTriggeredScenarioIds(response.getScenarioEvaluationResults()))
                            .setScenarioCorrectness(result.getTruthfulScenario().getScenarioCorrectness())
                            .setTriggeredActions(Json.stringify(result.getTriggeredActions()));

                    // persist the evaluation
                    return evaluationResultService.persist(evaluation)
                            // return the response
                            .then(Mono.just(response));
                });
    }
}
