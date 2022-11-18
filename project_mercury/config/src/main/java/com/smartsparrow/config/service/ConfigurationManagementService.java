package com.smartsparrow.config.service;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.smartsparrow.config.data.ConfigurationGateway;
import com.smartsparrow.config.data.EnvConfiguration;

/**
 * Provides methods to manage configuration: fetch, create, update.
 * This service can be used in Admin Console.
 */
@Singleton
public class ConfigurationManagementService {

    private final ConfigurationGateway configurationGateway;

    @Inject
    public ConfigurationManagementService(ConfigurationGateway configurationGateway) {
        this.configurationGateway = configurationGateway;
    }

    /**
     * Fetch list of all configuration for the specified region
     * @param region the region
     * @return a list of {@link EnvConfiguration}. If no configuration is associated with the region, returns <code>null</code>.
     */
    @Nullable
    public List<EnvConfiguration> fetchByRegion(String region) {
        Preconditions.checkNotNull(region);
        return configurationGateway.fetchByRegion(region).collectList().block();
    }

    /**
     * Fetch a particular {@link EnvConfiguration} associated with the key for the specified region
     * @param region the region
     * @param key the configuration key
     * @return a configuration object, returns null if no configuration is found
     */
    @Nullable
    public EnvConfiguration fetchByRegionAndKey(String region, String key) {
        Preconditions.checkNotNull(region);
        Preconditions.checkNotNull(key);
        return configurationGateway.fetchByKeyAndRegion(key, region).blockFirst();
    }

    /**
     * Persist configuration.
     * @param envConfiguration the configuration to persist
     */
    public void persistConfiguration(EnvConfiguration envConfiguration) {
        Preconditions.checkNotNull(envConfiguration);
        configurationGateway.persist(envConfiguration);
    }
}
