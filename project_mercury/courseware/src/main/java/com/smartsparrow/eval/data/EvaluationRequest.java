package com.smartsparrow.eval.data;

import com.smartsparrow.courseware.data.ScenarioLifecycle;

/**
 * Represents an object that holds all the required information to submit an EvaluationRequest to the EvaluationService
 */
public interface EvaluationRequest {

    enum Type {
        LEARNER,
        TEST
    }

    /**
     * @return the EvaluationRequest type
     */
    Type getType();

    /**
     * @return the scenario lifecyle of this evaluation request
     */
    ScenarioLifecycle getScenarioLifecycle();
}
