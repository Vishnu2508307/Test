package com.smartsparrow.courseware.wiring;


import com.smartsparrow.config.wiring.ConfigurationBindingOperations;

public class CoursewareOperationsBinding {

    private final ConfigurationBindingOperations binder;

    public CoursewareOperationsBinding(ConfigurationBindingOperations configurationBindingOperations) {
        this.binder = configurationBindingOperations;
    }

    public void bind() {
        binder.bind("cache.infra")
                .toConfigType(CacheInfraConfiguration.class);
        binder.bind("passport.infra")
                .toConfigType(PassportInfraConfiguration.class);
        binder.bind("evaluation.feature")
                .toConfigType(EvaluationFeatureConfiguration.class);
        binder.bind("routeConsumers.infra")
                .toConfigType(RouteConsumersInfraConfiguration.class);
        binder.bind("csg.infra")
                .toConfigType(CsgInfraConfiguration.class);
        binder.bind("evaluation_mode.feature")
                .toConfigType(EvaluationModeFeatureConfiguration.class);
    }
}
