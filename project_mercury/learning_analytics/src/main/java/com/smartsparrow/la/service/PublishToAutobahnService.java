package com.smartsparrow.la.service;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.pearson.autobahn.common.domain.ActionType;
import com.pearson.autobahn.common.domain.StreamType;
import com.pearson.autobahn.common.exception.InitializationException;
import com.pearson.autobahn.common.exception.PublishException;
import com.pearson.autobahn.common.exception.SchemaNotFoundException;
import com.pearson.autobahn.common.exception.SchemaValidationException;
import com.pearson.autobahn.producersdk.domain.ProducerMessage;
import com.smartsparrow.la.data.EventSummary;
import com.smartsparrow.la.event.AutobahnPublishMessage;
import com.smartsparrow.la.lang.AutobahnEventPublishFault;
import com.smartsparrow.la.lang.AutobahnPublishException;
import com.smartsparrow.la.lang.EventSummaryCreationException;
import com.smartsparrow.la.service.autobahn.AutobahnProducerService;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Singleton
public class PublishToAutobahnService {

    private static final Logger log = MercuryLoggerFactory.getLogger(PublishToAutobahnService.class);

    private final AutobahnProducerService autobahnProducerService;
    private final EventService eventService;

    @Inject
    public PublishToAutobahnService(AutobahnProducerService autobahnProducerService,
                                    EventService eventService) {
        this.autobahnProducerService = autobahnProducerService;
        this.eventService = eventService;
    }

    public UUID publish(AutobahnPublishMessage autobahnPublishMessage) {
        final String payload = autobahnPublishMessage.getPayload();
        final Map<String, String> tags = autobahnPublishMessage.getTags();
        final String correlationId = autobahnPublishMessage.getCorrelationId();
        final String messageTypeCode = autobahnPublishMessage.getMessageTypeCode();
        final String streamType = autobahnPublishMessage.getStreamType();
        final String namespace = autobahnPublishMessage.getNamespace();
        final String version = autobahnPublishMessage.getVersion();
        final String createType = autobahnPublishMessage.getCreateType();

        EventSummary eventSummary = eventService
                .createEvent(namespace, version, messageTypeCode, streamType, createType, correlationId, payload, tags)
                .block();

        if (eventSummary == null) {
            throw new EventSummaryCreationException(autobahnPublishMessage.toString());
        }
        try {
            UUID trackingId = sendToAutobahn(eventSummary, namespace, version, messageTypeCode,
                    correlationId, streamType, createType, payload, tags);

            log.info("Got response from sendToAutobahn {}", trackingId);

            eventService.createTracking(eventSummary.getId(), trackingId).block();

            return trackingId;
        } catch (AutobahnEventPublishFault e) {
            eventService.createEventFailure(e.getEventId(), e.getMessage()).block();
            throw e;
        } catch (AutobahnPublishException e) {
            log.info("There was an exception, eventId {} & message {}", eventSummary.getId(), e.getMessage());
            throw e;
        }
    }

    private UUID sendToAutobahn(EventSummary eventSummary,
                                String namespace,
                                String version,
                                String messageTypeCode,
                                String correlationId,
                                String streamType,
                                String actionType,
                                String payload,
                                Map<String, String> tags) {
        try {

            ProducerMessage message = new ProducerMessage(namespace, messageTypeCode, version,
                    StreamType.create(streamType), String.valueOf(correlationId),
                    ActionType.create(actionType), payload, tags);

            return autobahnProducerService.produceMessage(message);

        } catch (InitializationException | SchemaNotFoundException | SchemaValidationException e) {
            log.error("Caught exception.", e);
            throw new AutobahnPublishException(eventSummary.getId(), e);
        } catch (PublishException e) {
            log.error("Caught publish exception", e);
            throw new AutobahnEventPublishFault(eventSummary.getId(), e);
        }
    }
}
