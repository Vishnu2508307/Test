package com.smartsparrow.la.wiring;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;
import com.smartsparrow.la.config.LearningAnalyticsInfraConfiguration;

public class LearningAnalyticsOperationsBinding {

    private final ConfigurationBindingOperations binder;

    public LearningAnalyticsOperationsBinding(ConfigurationBindingOperations configurationBindingOperations) {
        this.binder = configurationBindingOperations;
    }

    public void bind() {
        binder.bind("learning_analytics.infra")
                .toConfigType(LearningAnalyticsInfraConfiguration.class);
    }
}
