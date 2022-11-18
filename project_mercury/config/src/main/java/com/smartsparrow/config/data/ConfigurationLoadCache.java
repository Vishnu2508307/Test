package com.smartsparrow.config.data;

import com.google.common.cache.Cache;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public abstract class ConfigurationLoadCache {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ConfigurationLoadCache.class);
    private Cache<String, Object> configurationCache;

    public ConfigurationLoadCache(Cache<String, Object> configurationCache) {
        this.configurationCache = configurationCache;
    }

    /**
     * Gets object associated with key if present in cache
     * <p>
     * May return null if either load cant find a value or
     * {@link ClassCastException} ocurred.
     *
     * @param key key name of object
     * @return <T> class type of cached object
     */
    public <T> T getIfPresent(final String key) {
        try {
            //noinspection unchecked
            return (T) configurationCache.getIfPresent(key);
        } catch (ClassCastException e) {
            log.error("Failed looking up cached configuration object with key " + key, e);
            return null;
        }
    }

    /**
     * Save object associated with key in cache
     *
     * @param key key name of object
     * @param value object value
     */
    public void put(final String key, final Object value) {
        try {
            configurationCache.put(key, value);
        } catch (Exception e) {
            log.error("Failed saving up configuration object with key " + key, e);
        }
    }


}
