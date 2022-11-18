package com.smartsparrow.config.service;

import java.util.Map;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.smartsparrow.config.AWSConfig;
import com.smartsparrow.config.data.Configuration;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;

public abstract class AbstractConfiguration<T> implements Configuration<T> {

    private final Map<String, Provider<ConfigurationLoadStrategy>> loadStrategyMapBinder;
    private final AWSConfig awsConfig;

    @Inject
    public AbstractConfiguration(final Map<String, Provider<ConfigurationLoadStrategy>> loadStrategyMapBinder,
                                 final AWSConfig awsConfig) {
        this.loadStrategyMapBinder = loadStrategyMapBinder;
        this.awsConfig = awsConfig;
    }

    /**
     * Load config for provided response object from s3 configuration bucket based on env and region
     * Also, it will load config from resource path if env is local or sandbox
     * @param key the config key
     * @param type the response type
     * @return response object
     */
    @Override
    public <T> T load(String key, Class<T> type) {
        ConfigurationContext context = new ConfigurationContext().setEnv(awsConfig.getEnv())
                .setRegion(awsConfig.getRegion())
                .setFileName(getFileName())
                .setKey(key)
                .setPrefix(getPrefix());
        // load files from s3 or local based on env
        return (T) ((ConfigurationLoadStrategy) loadStrategyMapBinder.get(
                awsConfig.getEnv()).get()).load(context, type);
    }

    /** get file name for the config
     * @return file name
     */
    public abstract String getFileName();

    /** get file sub directory for the config
     * @return file name prefix
     */
    public abstract String getPrefix();
}
