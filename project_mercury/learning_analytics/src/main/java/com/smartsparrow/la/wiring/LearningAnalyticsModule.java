package com.smartsparrow.la.wiring;


import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.smartsparrow.la.config.PLPFirehoseConfig;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.la.config.LearningAnalyticsConfig;
import com.smartsparrow.la.route.AnalyticsRoutes;
import com.smartsparrow.util.log.MercuryLoggerFactory;


public class LearningAnalyticsModule extends AbstractModule {

    private final static Logger log = MercuryLoggerFactory.getLogger(LearningAnalyticsModule.class);

    @Singleton
    @Provides
    @Nullable
    public LearningAnalyticsConfig getLearningAnalyticsConfiguration(ConfigurationService configurationService) {
        LearningAnalyticsConfig config = configurationService.get(LearningAnalyticsConfig.class, "learning_analytics");
        log.info("Learning Analytics config is {}", config);
        return config;
    }

    @Provides
    @Singleton
    public PLPFirehoseConfig providePLPFirehoseConfig(ConfigurationService configurationService) {
        PLPFirehoseConfig plpFirehoseConfig = configurationService.get(PLPFirehoseConfig.class, "plp_sqs");
        log.info("PLP firehose config is {}", plpFirehoseConfig);
        return plpFirehoseConfig;
    }

    @Override
    protected void configure() {
        bind(AnalyticsRoutes.class);
    }
}
