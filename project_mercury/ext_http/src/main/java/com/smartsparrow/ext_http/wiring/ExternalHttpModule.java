package com.smartsparrow.ext_http.wiring;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.smartsparrow.ext_http.service.AlfrescoPushNotificationHandler;
import com.smartsparrow.ext_http.service.GradePassbackNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.ext_http.route.ExternalHttpRoute;
import com.smartsparrow.ext_http.service.AlfrescoAssetSyncNotificationHandler;
import com.smartsparrow.ext_http.service.CsgDeleteNotificationHandler;
import com.smartsparrow.ext_http.service.CsgIndexNotificationHandler;
import com.smartsparrow.ext_http.service.GeneralNotificationHandler;
import com.smartsparrow.ext_http.service.NotificationHandler;
import com.smartsparrow.ext_http.service.RequestPurpose;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ExternalHttpModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(ExternalHttpModule.class);

    private MapBinder<RequestPurpose, NotificationHandler> requestHandlerBinder;

    public ExternalHttpModule() {
    }

    @Override
    protected void configure() {
        super.configure();

        // setup a map binder to store the handler definitions within.
        requestHandlerBinder = MapBinder.newMapBinder(binder(), //
                                                      new TypeLiteral<RequestPurpose>() {
                                                      },
                                                      new TypeLiteral<NotificationHandler>() {
                                                      });

        // setup the handlers.
        requestHandlerBinder.addBinding(RequestPurpose.GENERAL).to(GeneralNotificationHandler.class);
        requestHandlerBinder.addBinding(RequestPurpose.CSG_INDEX).to(CsgIndexNotificationHandler.class);
        requestHandlerBinder.addBinding(RequestPurpose.CSG_DELETE).to(CsgDeleteNotificationHandler.class);
        requestHandlerBinder.addBinding(RequestPurpose.ALFRESCO_ASSET_PUSH).to(AlfrescoPushNotificationHandler.class);
        requestHandlerBinder.addBinding(RequestPurpose.ALFRESCO_ASSET_PULL).to(AlfrescoAssetSyncNotificationHandler.class);
        requestHandlerBinder.addBinding(RequestPurpose.GRADE_PASSBACK).to(GradePassbackNotificationHandler.class);

        // setup the Camel routes
        bind(ExternalHttpRoute.class);
    }

    @Provides
    @Singleton
    @SuppressFBWarnings(value = "DM_EXIT", justification = "should exit on missing configuration props")
    ExternalHttpConfig getConfig(ConfigurationService configurationService) {

        // Load the config from the database.
        ExternalHttpConfig config = configurationService.get(ExternalHttpConfig.class, "ext_http");
        if (config == null) {
            // create an empty config if one does not exist.
            config = new ExternalHttpConfig();
        }

        // Allow for override or setting of the config by using:
        //   -Dext_http.submitTopicNameOrArn=submit-topic-name
        config.setSubmitTopicNameOrArn(System.getProperty("ext_http.submitTopicNameOrArn",
                                                          config.getSubmitTopicNameOrArn()));
        //   -Dext_http.delayQueueNameOrArn=delay-queue-name
        config.setDelayQueueNameOrArn(System.getProperty("ext_http.delayQueueNameOrArn",
                                                         config.getDelayQueueNameOrArn()));

        // sanity check that arguments exist.
        boolean fatal = false;
        if (isNullOrEmpty(config.getSubmitTopicNameOrArn())) {
            log.error("missing configuration value: ext_http.submitTopicNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(config.getDelayQueueNameOrArn())) {
            log.error("missing configuration value: ext_http.delayQueueNameOrArn");
            fatal = true;
        }

        // die if missing properties.
        if (fatal) {
            System.exit(-1);
        }

        return config;
    }
}
