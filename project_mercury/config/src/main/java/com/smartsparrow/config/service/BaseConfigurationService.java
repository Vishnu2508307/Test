package com.smartsparrow.config.service;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.smartsparrow.config.data.ConfigurationGateway;
import com.smartsparrow.config.data.EnvConfiguration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Provides methods to fetch configuration for specified region/environment.
 * The environment <b>region</b> is supplied at bootstrapping time.
 *
 */
@Singleton
public class BaseConfigurationService {

    private Logger log = LoggerFactory.getLogger(BaseConfigurationService.class);

    private ObjectMapper mapper;

    private final ConfigurationGateway configurationGateway;
    private final String region;

    public BaseConfigurationService(ConfigurationGateway configurationGateway, String region) {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true); //it allows to keep configuration with json more readable (allows to have unescaped '/n' symbols in json)

        this.configurationGateway = configurationGateway;
        this.region = region;
    }

    /**
     * Get a boolean property from configuration or false if property is not set.
     *
     * @param key The configuration key
     * @return a boolean property associated with the key, if property is not set, defaults to false;
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Get a boolean property from configuration or supplied default value if not set.
     *
     * @param key          The configuration key
     * @param defaultValue The value that should be returned if a property doesn't exist or empty.
     * @return a boolean property associated with the key. Returns defaultValue if such property doesn't exist.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Preconditions.checkNotNull(key);

        String value = getValue(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * @param key The configuration key
     * @return a int property associated with the key. Returns 0 if such property doesn't exist or empty.
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * @param key          The configuration key
     * @param defaultValue The value that should be returned if a property doesn't exist or empty.
     * @return a int property associated with the key. Returns <code>defaultValue</code> if such property doesn't exist or empty.
     */
    public int getInt(String key, int defaultValue) {
        Preconditions.checkNotNull(key);

        String value = getValue(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * @param key The configuration key
     * @return a Integer property associated with the key. Returns null if such property doesn't exist or empty.
     */
    @Nullable
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    /**
     * @param key          The configuration key
     * @param defaultValue The value that should be returned if a property doesn't exist or empty.
     * @return a Integer property associated with the key.
     * Returns defaultValue if such property doesn't exist or empty.
     */
    public Integer getInteger(String key, Integer defaultValue) {
        Preconditions.checkNotNull(key);

        String value = getValue(key);
        return value != null ? Integer.valueOf(value) : defaultValue;
    }

    /**
     * @param key The configuration key
     * @return a String property associated with the key. Returns empty string if such property doesn't exist or empty.
     */
    public String getString(String key) {
        return getString(key, "");
    }

    /**
     * @param key          - The configuration key
     * @param defaultValue - The value that should be returned if a property doesn't exist or empty.
     * @return a String property associated with the key. Returns empty string if such property doesn't exist or empty.
     */
    public String getString(String key, String defaultValue) {
        Preconditions.checkNotNull(key);
        String value = getValue(key);
        return value != null ? value : defaultValue;
    }

    /**
     * @param type The target class
     * @param key  The configuration key
     * @return a object of the specified type associated with the key.
     * Returns null if such property doesn't exist, is empty or can't be converted to an object.
     */
    @Nullable
    public <T> T getRenderedConfig(Class<T> type, String key) {
        return getRenderedConfig(type, key, null);
    }

    /**
     * @param type         The target class
     * @param key          The configuration key
     * @param defaultValue The value that should be returned if a property doesn't exist or empty.
     * @return a object of the specified type associated with the key.
     * Returns defaultValue if such property doesn't exist, is empty or can't be converted to an object.
     */
    public <T> T getRenderedConfig(Class<T> type, String key, T defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(type);

        T result = null;
        String json = this.getValue(key);

        if (json != null) {
            try {
                result = mapper.readValue(json, type);
            } catch (IOException e) {
                log.error(String.format("Exception during loading configuration for key=%s and region=%s. " +
                        "Return defaultValue.", key, region), e);
            }
        }

        return result != null ? result : defaultValue;
    }

    /**
     * Gets value from DB for the key
     * @param key - the key
     * @return a value from DB, returns <code>null</code> if value is empty or key doesn't exist in DB
     */
    @Nullable
    protected String getValue(String key) {
        return this.getValueAsync(key).publishOn(Schedulers.elastic()).block();
    }

    /**
     * Gets value from DB for the key asynchronously
     * @param key - the key
     * @return a value from DB, returns empty Mono if value is empty or key doesn't exist in DB
     */
    protected Mono<String> getValueAsync(String key) {
        Flux<EnvConfiguration> config = configurationGateway.fetchByKeyAndRegion(key, region);
        return config
                .map(EnvConfiguration::getValue)
                .filter(value -> !Strings.isNullOrEmpty(value)).singleOrEmpty();
    }

    protected String getRegion() {
        return region;
    }

}
