package com.smartsparrow.courseware.wiring;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.smartsparrow.cache.config.RouteConsumersInfraResponse;
import com.smartsparrow.config.AWSConfig;
import com.smartsparrow.config.data.ConfigurationConstants;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.config.service.StaticConfiguration;

public class RouteConsumersInfraConfiguration extends StaticConfiguration<RouteConsumersInfraResponse> {

    @Inject
    public RouteConsumersInfraConfiguration(final Map<String, Provider<ConfigurationLoadStrategy>> loadStrategyMapBinder,
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