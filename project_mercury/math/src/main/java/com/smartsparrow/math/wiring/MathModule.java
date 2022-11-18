package com.smartsparrow.math.wiring;

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.math.config.MathConfig;
import com.smartsparrow.math.route.MathRoute;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class MathModule extends AbstractModule {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MathModule.class);

    @Override
    protected void configure() {
        bind(MathRoute.class);
    }

    @Provides
    @Singleton
    @SuppressFBWarnings(value = "DM_EXIT", justification = "should exit on missing configuration props")
    MathConfig provideMathMLConfig(ConfigurationService configurationService) {
        MathConfig mathConfig = configurationService.get(MathConfig.class, "math");
        log.info("math config is {}", mathConfig);
        // Allow for override or setting of the config by using:
        //   -Dmath.submitTopicNameOrArn=submit-topic-name
        mathConfig.setSubmitTopicNameOrArn(System.getProperty("math.submitTopicNameOrArn",
                                                              mathConfig.getSubmitTopicNameOrArn()));
        //   -Dmath.delayQueueNameOrArn=delay-queue-name
        mathConfig.setDelayQueueNameOrArn(System.getProperty("math.delayQueueNameOrArn",
                                                             mathConfig.getDelayQueueNameOrArn()));
        boolean fatal = false;
        if (isNullOrEmpty(mathConfig.getSubmitTopicNameOrArn())) {
            log.error("missing configuration value: math.submitTopicNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(mathConfig.getDelayQueueNameOrArn())) {
            log.error("missing configuration value: math.delayQueueNameOrArn");
            fatal = true;
        }

        // die if missing properties.
        if (fatal) {
            System.exit(-1);
        }

        return mathConfig;
    }
}
