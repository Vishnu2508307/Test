package com.smartsparrow.eval.wiring;

/**
 * This enum describes the available values for the configurable evaluation feature
 */
public enum EvaluationFeatureConfigurationValues {
    // the evaluation implementation flows through CoursewareRoutes
    CAMEL_EVALUATION,
    // the evaluation implementation flows through LearnerWalkableService.evaluate(...)
    REACTIVE_EVALUATION
}
