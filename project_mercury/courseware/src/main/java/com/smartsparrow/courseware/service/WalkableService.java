package com.smartsparrow.courseware.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.data.Walkable;
import com.smartsparrow.eval.data.TestEvaluationRequest;
import com.smartsparrow.eval.data.TestEvaluationResponse;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.learner.service.EvaluationSubmitService;

import reactor.core.publisher.Mono;

@Singleton
public class WalkableService {

    private final ActivityService activityService;
    private final InteractiveService interactiveService;
    private final EvaluationSubmitService evaluationSubmitService;

    @Inject
    public WalkableService(final ActivityService activityService,
                           final InteractiveService interactiveService,
                           final EvaluationSubmitService evaluationSubmitService) {
        this.activityService = activityService;
        this.interactiveService = interactiveService;
        this.evaluationSubmitService = evaluationSubmitService;
    }

    /**
     * Allows to set the EvaluationMode for a courseware element
     *
     * @param elementId the element id to set the evaluation mode for
     * @param elementType the element type to set the evaluation for
     * @param evaluationMode the evaluation mode to set
     * @return a void mono
     * @throws com.smartsparrow.exception.IllegalStateFault when the elementType does not support evaluation mode
     */
    public Mono<Void> updateEvaluationMode(final UUID elementId, final CoursewareElementType elementType,
                                           final EvaluationMode evaluationMode) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");
        affirmArgument(evaluationMode != null, "evaluationMode is required");

        switch (elementType) {
            case ACTIVITY:
                return activityService.updateEvaluationMode(elementId, evaluationMode);
            case INTERACTIVE:
                return interactiveService.updateEvaluationMode(elementId, evaluationMode);
            default:
                throw new UnsupportedOperationFault(String.format("EvaluationMode unsupported for element type %s", elementType));
        }
    }

    /**
     * Find the evaluation mode for an element. This method only exists so not to cause breaking changes with the existing
     * messaging api. Once the FE starts reading the evaluation_mode from the walkable object then this method can be
     * deleted along with the related message that consumes it.
     *
     * @param elementId the element id to find the evaluation mode for
     * @param elementType the element type to find the evaluation mode for
     * @return a mono with the evaluation mode
     * @throws com.smartsparrow.exception.IllegalArgumentFault when any of the method arguments is null
     */
    public Mono<EvaluationMode> fetchEvaluationMode(final UUID elementId, final CoursewareElementType elementType) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");

        switch (elementType) {
            case ACTIVITY:
                return activityService.findById(elementId)
                        .flatMap(activity -> {
                            final EvaluationMode evaluationMode = activity.getEvaluationMode();
                            return Mono.just(evaluationMode);
                        });
            case INTERACTIVE:
                return interactiveService.findById(elementId)
                        .flatMap(interactive -> {
                            final EvaluationMode evaluationMode = interactive.getEvaluationMode();
                                return Mono.just(evaluationMode);
                        });
            default:
                throw new UnsupportedOperationFault(String.format("EvaluationMode unsupported for element type %s", elementType));
        }
    }

    /**
     * Find a walkable by id
     *
     * @param walkableId the id of the walkable to find
     * @param walkableType the type of walkable to find
     * @return a mono with the found walkable
     * @throws com.smartsparrow.exception.IllegalArgumentFault when any of the method argument is null or invalid
     * @throws UnsupportedOperationFault when an invalid walkable type is supplied
     */
    public Mono<? extends Walkable> findWalkable(final UUID walkableId, final CoursewareElementType walkableType) {
        affirmArgument(walkableId != null, "walkableId is required");
        affirmArgument(walkableType != null, "walkableType is required");

        switch (walkableType) {
            case ACTIVITY:
                return activityService.findById(walkableId);
            case INTERACTIVE:
                return interactiveService.findById(walkableId);
            default:
                throw new UnsupportedOperationFault(String.format("walkableType %s not supported", walkableType));
        }

    }

    /**
     * Allows to test evaluate a walkable
     *
     * @param walkableId the id of the walkable to test evaluate
     * @param walkableType the type of the walkable to test evaluate
     * @param testData the test data to use during evaluation
     * @return a mono containing the test evaluation response
     */
    public Mono<TestEvaluationResponse> evaluate(final UUID walkableId, final CoursewareElementType walkableType, final String testData) {
        // find the walkable
        return findWalkable(walkableId, walkableType)
                // create the test evaluation request
                .map(walkable -> new TestEvaluationRequest()
                        .setWalkable(walkable)
                        .setScenarioLifecycle(ScenarioLifecycle.defaultScenarioLifecycle(walkableType))
                        .setData(testData))
                // submit for evaluation and get the response
                .flatMap(testEvaluationRequest -> evaluationSubmitService.submit(testEvaluationRequest, TestEvaluationResponse.class));

    }
}
