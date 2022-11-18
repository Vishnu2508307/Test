package com.smartsparrow.courseware.wiring;

import java.util.Map;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.smartsparrow.config.AWSConfig;
import com.smartsparrow.config.data.ConfigurationConstants;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.config.service.DynamicConfiguration;
import com.smartsparrow.eval.wiring.EvaluationFeatureResponse;

public class EvaluationFeatureConfiguration extends DynamicConfiguration<EvaluationFeatureResponse> {

    @Inject
    public EvaluationFeatureConfiguration(final Map<String, Provider<ConfigurationLoadStrategy>> loadStrategyMapBinder,
                                          final AWSConfig awsConfig) {
        super(loadStrategyMapBinder, awsConfig);
    }

    @Override
    public String getFileName() {
        return ConfigurationConstants.WS_FEATURE_EVALUATION_FILENAME;
    }

    @Override
    public String getPrefix() {
        return ConfigurationConstants.WS_FEATURE_PREFIX;
    }
}
