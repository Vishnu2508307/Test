package com.smartsparrow.config.service;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.smartsparrow.config.AWSConfig;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.config.data.ConfigurationType;

public abstract class DynamicConfiguration<T> extends AbstractConfiguration<T> {

    private static Integer DEFAULT_TTL = 1000;

    @Inject
    public DynamicConfiguration(final Map<String, Provider<ConfigurationLoadStrategy>> loadStrategyMapBinder,
                                final AWSConfig awsConfig) {
        super(loadStrategyMapBinder, awsConfig);
    }

    @Override
    public ConfigurationType getConfigType() {
        return ConfigurationType.DYNAMIC;
    }
}
