package com.smartsparrow.eval.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.lang.ScenarioEvaluationException;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.lang.LearnerEvaluationException;
import com.smartsparrow.learner.service.LearnerScenarioService;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerEvaluationService implements EvaluationService<LearnerEvaluationRequest, LearnerEvaluationResponse> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerEvaluationService.class);

    private final LearnerScenarioService learnerScenarioService;
    private final ScenarioEvaluationService scenarioEvaluationService;
    private final StudentScopeService studentScopeService;

    @Inject
    public LearnerEvaluationService(final LearnerScenarioService learnerScenarioService,
                                    final ScenarioEvaluationService scenarioEvaluationService,
                                    final StudentScopeService studentScopeService) {
        this.learnerScenarioService = learnerScenarioService;
        this.scenarioEvaluationService = scenarioEvaluationService;
        this.studentScopeService = studentScopeService;
    }

    /**
     * Performs a real evaluation against a learner walkable
     *
     * @param learnerEvaluationRequest the learner evaluation request to satisfy
     * @return a mono containing the learner evaluation response
     */
    @Trace(async = true)
    @Override
    public Mono<LearnerEvaluationResponse> evaluate(LearnerEvaluationRequest learnerEvaluationRequest) {
        // get the learner walkable that was requested to be evaluated
        final LearnerWalkable walkable = learnerEvaluationRequest.getLearnerWalkable();
        // build the context, required for scope resolution
        final EvaluationLearnerContext evaluationLearnerContext = new EvaluationLearnerContext()
                .setStudentId(learnerEvaluationRequest.getStudentId())
                .setDeploymentId(walkable.getDeploymentId());

        // perform scenario evaluation
        Flux<ScenarioEvaluationResult> evaluationResultFlux = learnerScenarioService
                // find all the scenarios
                .findAll(walkable.getDeploymentId(), walkable.getChangeId(), walkable.getId(), learnerEvaluationRequest.getScenarioLifecycle())
                // evaluate each one
                .concatMap(learnerScenario -> scenarioEvaluationService.evaluateCondition(learnerScenario, evaluationLearnerContext))
                // log any error
                .doOnEach(log.reactiveErrorThrowable("error while evaluating", throwable -> new HashedMap<String, Object>() {
                    {
                        put("walkableId", walkable.getId());
                        put("walkableType", walkable.getElementType());
                        put("studentId", learnerEvaluationRequest.getStudentId());
                    }
                }))
                .doOnError(ScenarioEvaluationException.class, cause -> {
                    throw new LearnerEvaluationException(cause.getMessage(), cause);
                });

        // figure out the EvaluationMode and returns the results accordingly
        Mono<List<ScenarioEvaluationResult>> resultsMono;

        if (walkable.getEvaluationMode().equals(EvaluationMode.DEFAULT)) {
            // stop at the first scenario that evaluates to true
            resultsMono = evaluationResultFlux
                    .takeUntil(scenarioEvaluationResult -> Boolean.TRUE.equals(scenarioEvaluationResult.getEvaluationResult()))
                    .collectList();
        } else {
            // return all the scenario evaluation results otherwise
            resultsMono = evaluationResultFlux.collectList();
        }

        // find the scope entries map for this evaluation
        Mono<Map<UUID, String>> entriesMapMono = studentScopeService.findLatestEntries(walkable.getDeploymentId(), learnerEvaluationRequest.getStudentId(),
                walkable.getStudentScopeURN());

        // return the build evaluation response
        return Mono.zip(resultsMono, entriesMapMono)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // build the learner evaluation response
                .map(tuple2 -> new LearnerEvaluationResponse()
                        .setScenarioEvaluationResults(tuple2.getT1())
                        .setEvaluationRequest(learnerEvaluationRequest)
                        .setWalkableEvaluationResult(new WalkableEvaluationResult()
                                .setId(UUIDs.timeBased())
                                .setWalkableId(walkable.getId())
                                .setWalkableType(walkable.getElementType()))
                        // set the correctness at the end
                        .setScenarioCorrectness());
    }
}
