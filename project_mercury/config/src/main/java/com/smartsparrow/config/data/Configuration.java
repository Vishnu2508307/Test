package com.smartsparrow.config.data;

import reactor.core.publisher.Mono;

public interface Configuration<T> {

    /**
     * Load configuration files from s3 bucket or local based on env and region
     * @param type the response type
     * @return response object of provided type class
     */
    <T> T load(String key, Class<T> type);

    /**
     * Return config type {@link ConfigurationType)}
     * @return configuration type
     */
    ConfigurationType getConfigType();
}
