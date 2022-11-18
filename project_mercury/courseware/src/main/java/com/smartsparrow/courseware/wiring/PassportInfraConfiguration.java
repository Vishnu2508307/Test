package com.smartsparrow.courseware.wiring;

import java.util.Map;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.smartsparrow.cohort.wiring.PassportInfraResponse;
import com.smartsparrow.config.AWSConfig;
import com.smartsparrow.config.data.ConfigurationConstants;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.config.service.StaticConfiguration;

public class PassportInfraConfiguration extends StaticConfiguration<PassportInfraResponse> {

    @Inject
    public PassportInfraConfiguration(final Map<String, Provider<ConfigurationLoadStrategy>> loadStrategyMapBinder,
                                      final AWSConfig awsConfig) {
        super(loadStrategyMapBinder, awsConfig);
    }

    @Override
    public String getFileName() {
        return ConfigurationConstants.WS_INFRA_FILENAME;
    }

    @Override
    public String getPrefix() {
        return ConfigurationConstants.WS_INFRA_PREFIX;
    }
}
