package com.smartsparrow.la.wiring;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.pearson.autobahn.common.domain.Environment;
import com.pearson.autobahn.common.domain.OperationalType;
import com.pearson.autobahn.producersdk.config.AutobahnProducerConfig;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;
import com.smartsparrow.la.config.LearningAnalyticsConfig;
import com.smartsparrow.la.config.ProducerConfig;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class AutobahnProducerModule extends AbstractModule {
    private final static Logger log = MercuryLoggerFactory.getLogger(AutobahnProducerModule.class);

    @Provides
    @Singleton
    @Inject
    private ProducerConfig getProducerConfig(LearningAnalyticsConfig learningAnalyticsConfig,
                                             IesSystemToSystemIdentityProvider identityProvider) {
        log.info("Setting up producer config for publishing to autobahn");
        final Environment environment = identityProvider.getAutobahnIdentityProviderEnvironment();

        AutobahnProducerConfig autobahnProducerConfig = new AutobahnProducerConfig.Builder(environment,
                learningAnalyticsConfig.getOriginatingSystemCode(),
                identityProvider).build();

        return new ProducerConfig()
                .setAutobahnProducerConfig(autobahnProducerConfig)
                .setOperationalType(OperationalType.OPERATIONAL);
    }
}
