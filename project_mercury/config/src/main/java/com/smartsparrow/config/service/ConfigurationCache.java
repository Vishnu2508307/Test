package com.smartsparrow.config.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

public class ConfigurationCache {

    private Logger log = LoggerFactory.getLogger(ConfigurationCache.class);
    private Cache<String, Object> configurationCache;

    /**
     * Wrapper around a @Singleton instance of Cache<String, String>
     * Instances of this object should be acquired by provider
     * in {@link com.smartsparrow.config.wiring.ConfigurationManagementModule}
     *
     */
    public ConfigurationCache(Cache<String, Object> configurationCache) {
        this.configurationCache = configurationCache;
    }

    /**
     * Gets object associated with key if present in cache, tries to load it with loader callable if not present
     * then caches it.
     *
     * May return null if either load cant find a value or unexpected error {@link ExecutionException} or
     * {@link ClassCastException} ocurred.
     *
     * @param key key name of object
     * @param loader callable that loads required value if not cached
     * @param <T> class type of cached object
     * @return
     */
    @Nullable
    <T> T get(String key, Callable<T> loader) {
        try {
            //noinspection unchecked
            return (T) configurationCache.get(key, loader);
        } catch (ExecutionException | ClassCastException e) {
            log.error("Failed looking up cached configuration object with key " + key, e);
            return null;
        }
    }

}
