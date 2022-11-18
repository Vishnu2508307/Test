package com.smartsparrow.sso.wiring;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.smartsparrow.config.AWSConfig;
import com.smartsparrow.config.data.ConfigurationConstants;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.config.service.StaticConfiguration;

public class MyCloudInfraConfiguration extends StaticConfiguration<MyCloudInfraResponse> {

    @Inject
    public MyCloudInfraConfiguration(final Map<String, Provider<ConfigurationLoadStrategy>> loadStrategyMapBinder,
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
