package com.smartsparrow.config.service;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.smartsparrow.config.data.Configuration;
import com.smartsparrow.config.data.ConfigurationGateway;
import com.smartsparrow.config.data.ConfigurationLoadCache;
import com.smartsparrow.config.data.ConfigurationType;

import reactor.core.publisher.Mono;

/**
 * The service uses Guava in memory cache to store configuration objects. Each get method checks if value exists in the cache,
 * if it does not exist then gets it get from DB and put into the cache.
 *
 * Cache configurations like TTL and size are defined in {@link com.smartsparrow.config.wiring.ConfigurationManagementModule}
 */
@Singleton
public class ConfigurationService extends BaseConfigurationService {

    private Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationCache configurationCache;

    // Inject this once this is being called from config module.
    private Map<String, Provider<Configuration>> boundConfiguration;
    private final Map<ConfigurationType, Provider<ConfigurationLoadCache>> configurationTypeProvider;

    @Inject
    public ConfigurationService(final ConfigurationGateway configurationGateway, @Named("env.region") String region,
                                final ConfigurationCache configurationCache,
                                final Map<ConfigurationType, Provider<ConfigurationLoadCache>> configurationTypeProvider) {
        super(configurationGateway, region);
        this.configurationCache = configurationCache;
        this.configurationTypeProvider = configurationTypeProvider;
    }

    /**
     * Gets configuration string value (usually json) from cache for the key, if value doesn't exist in cache try to load it
     * from DB and put into the cache.
     *
     * @param key
     * @return value associated with the key. Returns empty Mono if no value exists or value is empty
     */
    @Override
    protected Mono<String> getValueAsync(String key) {
        return configurationCache.get(key, () -> super.getValueAsync(key));
    }

    /**
     * Gets long lived and hardly ever changing concrete configuration objects from configuration cache, like AssetConfig.
     *
     * @param type type of the configuration object
     * @param key name of the configuration key
     * @return object associated with the key or null if nothing found or loaded, or if
     * {@link java.util.concurrent.ExecutionException} or {@link ClassCastException} ocurred.
     */
    @Nullable
    public <T> T get(Class<T> type, String key) {
        return configurationCache.get(type.getTypeName() + "-" + key, () -> super.getRenderedConfig(type, key));
    }

    /**
     * Load configuration based on key from either static or dynamic cache loader ,
     * if not available then load from s3 or local
     *
     * @param key the config key
     * @param type the response type
     * @return response object
     */
    @Nullable
    public <T> T load(String key, Class<T> type) {

        Configuration configuration = boundConfiguration.get(key).get();
        //get dynamic or static cache loader based on config type
        ConfigurationLoadCache configurationLoadCache = configurationTypeProvider.get(configuration.getConfigType()).get();

        T cacheResponse = configurationLoadCache.getIfPresent(key);

        if (cacheResponse == null) {
            //load config from s3 or local
            cacheResponse = (T) configuration.load(key, type);
            configurationLoadCache.put(key, cacheResponse);
        }
        return cacheResponse;
    }
}
