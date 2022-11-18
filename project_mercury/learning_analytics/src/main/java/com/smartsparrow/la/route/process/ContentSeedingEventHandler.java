package com.smartsparrow.la.route.process;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;

import com.smartsparrow.la.event.AutobahnPublishMessage;
import com.smartsparrow.la.event.ContentSeedingMessage;
import com.smartsparrow.la.mapper.pla.data.LearningResource;
import com.smartsparrow.la.service.ActivitySeedingService;
import com.smartsparrow.la.service.PublishToAutobahnService;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class ContentSeedingEventHandler {

    private static final Logger log = MercuryLoggerFactory.getLogger(ContentSeedingEventHandler.class);

    private final ActivitySeedingService activitySeedingService;
    private final PublishToAutobahnService publishToAutobahnService;

    @Inject
    public ContentSeedingEventHandler(ActivitySeedingService activitySeedingService,
                                      PublishToAutobahnService publishToAutobahnService) {
        this.activitySeedingService = activitySeedingService;
        this.publishToAutobahnService = publishToAutobahnService;
    }

    @Handler
    public void handle(Exchange exchange) {
        try {
            ContentSeedingMessage contentSeedingMessage = (ContentSeedingMessage) exchange.getIn().getBody();
            AutobahnPublishMessage autobahnPublishMessage = walkThroughContentAndPublish(contentSeedingMessage);
            exchange.getOut().setBody(autobahnPublishMessage);
        } catch (Exception e) {
            log.error("Exception while publishing", e);
            exchange.getOut().setBody(e.getMessage());
        }
    }

    private AutobahnPublishMessage walkThroughContentAndPublish(ContentSeedingMessage contentSeedingMessage) {
        UUID activityId = contentSeedingMessage.getElementId();

        // Root level activity -> LearningResource & publish it
        return Mono.just(activityId)
                .map(id -> {
                    LearningResource learningResource = activitySeedingService.toLearningResource(id);
                    AutobahnPublishMessage autobahnPublishMessage = activitySeedingService.learningResourceToAutobahnPublishMessage(learningResource);
                    UUID trackingId = publishToAutobahnService.publish(autobahnPublishMessage);
                    return autobahnPublishMessage.setTrackingId(trackingId);
                }).block();
    }
}
