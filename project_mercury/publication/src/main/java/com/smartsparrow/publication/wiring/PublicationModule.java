package com.smartsparrow.publication.wiring;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.service.ConfigurationService;

/**
 * Define a Publication module that installs other modules required for Publication operations.
 */
public class PublicationModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(PublicationModule.class);

    @Provides
    @Nullable
    public PublicationConfig getPublicationConfig(ConfigurationService configurationService) {
        return configurationService.get(PublicationConfig.class, "oculus.url");
    }
}
