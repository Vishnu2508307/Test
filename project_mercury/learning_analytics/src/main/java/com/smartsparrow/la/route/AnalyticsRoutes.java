package com.smartsparrow.la.route;

import static com.smartsparrow.dataevent.RouteUri.CONTENT_SEEDING_EVENT;
import static com.smartsparrow.dataevent.RouteUri.RS;
import static com.smartsparrow.dataevent.RouteUri.DIRECT;
import static com.smartsparrow.dataevent.RouteUri.FIREHOSE;

import javax.inject.Inject;

import com.smartsparrow.la.config.PLPFirehoseConfig;
import org.apache.camel.builder.RouteBuilder;

import com.smartsparrow.la.route.process.ContentSeedingEventHandler;
import org.apache.camel.model.dataformat.JsonLibrary;

public class AnalyticsRoutes extends RouteBuilder {

    private final ContentSeedingEventHandler contentSeedingEventHandler;
    private final PLPFirehoseConfig plpFirehoseConfig;


    @Inject
    public AnalyticsRoutes(ContentSeedingEventHandler contentSeedingEventHandler,
                           PLPFirehoseConfig plpFirehoseConfig) {
        this.contentSeedingEventHandler = contentSeedingEventHandler;
        this.plpFirehoseConfig = plpFirehoseConfig;
    }

    @Override
    public void configure() {
        from(RS + CONTENT_SEEDING_EVENT)
                .routeId(CONTENT_SEEDING_EVENT)
                .log("Received content seeding event ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(contentSeedingEventHandler);

        // sending all event messages to SQS queue
        from(DIRECT + FIREHOSE)
                .choice()
                .when(constant(plpFirehoseConfig.isEnabled()))
                .marshal().json(JsonLibrary.Jackson)
                .toD("aws-sqs:" + plpFirehoseConfig.getSQSURL())
                .endChoice();
    }
}
