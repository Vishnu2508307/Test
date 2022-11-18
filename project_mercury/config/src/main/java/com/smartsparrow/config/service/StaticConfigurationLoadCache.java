package com.smartsparrow.config.service;


import com.google.common.cache.Cache;
import com.smartsparrow.config.data.ConfigurationLoadCache;

public class StaticConfigurationLoadCache extends ConfigurationLoadCache {


    public StaticConfigurationLoadCache(final Cache<String, Object> configurationCache) {
        super(configurationCache);
    }
}
