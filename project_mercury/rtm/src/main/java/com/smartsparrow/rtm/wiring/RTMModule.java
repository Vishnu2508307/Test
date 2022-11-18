package com.smartsparrow.rtm.wiring;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.cache.wiring.RedissonModule;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.config.wiring.ConfigurationManagementModule;
import com.smartsparrow.dataevent.wiring.DataEventModule;
import com.smartsparrow.dse.wiring.CassandraModule;
import com.smartsparrow.graphql.wiring.GraphQLModule;
import com.smartsparrow.la.wiring.AutobahnProducerModule;
import com.smartsparrow.la.wiring.LearningAnalyticsModule;
import com.smartsparrow.plugin.wiring.PluginModule;
import com.smartsparrow.publication.wiring.PublicationModule;
import com.smartsparrow.rtm.route.RTMRoutes;

/**
 * Define a top level module that installs other modules required for RTM operations.
 */
public class RTMModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(RTMModule.class);

    @Override
    protected void configure() {
        // install the scope.
        install(new RTMScopeModule());

        // install the RTM Subscription/consumers wiring.
        install(new RTMSubscriptionModule());

        // install the web socket handler.
        install(new RTMWebSocketModule());

        install(new ConfigurationManagementModule());

        install(new RedissonModule());

        install(new CassandraModule());

        install(new PluginModule());

        install(new DataEventModule());

        install(new GraphQLModule());

        install(new LearningAnalyticsModule());

        install(new AutobahnProducerModule());

        install(new PublicationModule());

        bind(RTMRoutes.class);
    }

    @Provides
    @Nullable
    public LTIConfig getLTIConfig(ConfigurationService configurationService) {
        return configurationService.get(LTIConfig.class, "lti.credentials");
    }
}
