package com.smartsparrow.user_content.wiring;

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.smartsparrow.config.service.ConfigurationService;

/**
 * Define a UserContent module that installs other modules required for UserContent operations.
 */
public class UserContentModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(UserContentModule.class);

    @Provides
    @Nullable
    @Singleton
    public UserContentConfig getUserContentConfig(ConfigurationService configurationService) {
        UserContentConfig userContentConfig = configurationService.get(UserContentConfig.class, "user_content");

        userContentConfig.setCacheNameOrArn(System.getProperty("user_content.cacheNameOrArn",
                                                               userContentConfig.getCacheNameOrArn()));

        if (isNullOrEmpty( userContentConfig.getCacheNameOrArn())) {
            log.error("missing configuration value: user_content.cacheNameOrArn");
        }

        if (isNullOrEmpty( userContentConfig.getCacheNameOrArn())) {
            log.error("missing configuration value: user_content.cacheNameOrArn");
        }

        return userContentConfig;
    }
}
