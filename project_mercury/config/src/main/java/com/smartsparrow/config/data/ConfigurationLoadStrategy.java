package com.smartsparrow.config.data;

import com.smartsparrow.config.service.ConfigurationContext;


public interface ConfigurationLoadStrategy<T> {

    /**
     * Load configuration from s3 bucket and local resource path bassed on env and region
     *
     * @param context the configuration context
     * @param type the reponse type object
     * @param <T> the class type object
     * @return config response object
     */
    <T> T load(final ConfigurationContext context, final Class<T> type);
}
