package com.smartsparrow.eval.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.smartsparrow.courseware.data.Walkable;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.eval.data.TestEvaluationRequest;
import com.smartsparrow.eval.data.TestEvaluationResponse;
import com.smartsparrow.learner.data.EvaluationTestContext;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class TestEvaluationService implements EvaluationService<TestEvaluationRequest, TestEvaluationResponse> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(TestEvaluationService.class);

    private final ScenarioService scenarioService;
    private final ScenarioEvaluationService scenarioEvaluationService;

    @Inject
    public TestEvaluationService(final ScenarioService scenarioService,
                                 final ScenarioEvaluationService scenarioEvaluationService) {
        this.scenarioService = scenarioService;
        this.scenarioEvaluationService = scenarioEvaluationService;
    }

    /**
     * Performs a test evaluation against a walkable
     *
     * @param testEvaluationRequest the test evaluation request to satisfy
     * @return a mono containing the test evaluation response
     */
    @Override
    public Mono<TestEvaluationResponse> evaluate(TestEvaluationRequest testEvaluationRequest) {
        // get the walkable
        final Walkable walkable = testEvaluationRequest.getWalkable();
        // create the context required for condition resolution
        final EvaluationTestContext context = new EvaluationTestContext(testEvaluationRequest.getData());
        // find all the scenarios for this walkable and given lifecycle
        return scenarioService.findAll(walkable.getId(), testEvaluationRequest.getScenarioLifecycle())
                // evaluate each scenario condition in an orderly manner
                .concatMap(scenario -> scenarioEvaluationService.evaluateCondition(scenario, context))
                // log any error
                .doOnEach(log.reactiveErrorThrowable("error while evaluating", throwable -> new HashedMap<String, Object>() {
                    {
                        put("walkableId",  walkable.getId());
                        put("walkableType",  walkable.getElementType());
                        put("testData",  testEvaluationRequest.getData());
                    }
                }))
                .collectList()
                // create the test evaluation response
                .map(scenarioEvaluationResults -> new TestEvaluationResponse()
                        .setEvaluationRequest(testEvaluationRequest)
                        .setScenarioEvaluationResults(scenarioEvaluationResults));
    }
}
