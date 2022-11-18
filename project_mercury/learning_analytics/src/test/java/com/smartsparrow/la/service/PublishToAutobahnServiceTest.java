package com.smartsparrow.la.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pearson.autobahn.common.exception.InitializationException;
import com.pearson.autobahn.common.exception.PublishException;
import com.pearson.autobahn.common.exception.SchemaNotFoundException;
import com.pearson.autobahn.common.exception.SchemaValidationException;
import com.smartsparrow.la.data.EventFailure;
import com.smartsparrow.la.data.EventSummary;
import com.smartsparrow.la.data.EventTracking;
import com.smartsparrow.la.event.AutobahnPublishMessage;
import com.smartsparrow.la.lang.AutobahnEventPublishFault;
import com.smartsparrow.la.lang.AutobahnPublishException;
import com.smartsparrow.la.service.autobahn.AutobahnProducerService;

import reactor.core.publisher.Mono;

public class PublishToAutobahnServiceTest {

    @InjectMocks
    PublishToAutobahnService publishToAutobahnService;

    @Mock
    AutobahnProducerService autobahnProducerService;

    @Mock
    EventService eventService;

    private Exchange exchange;

    private AutobahnPublishMessage autobahnPublishMessage;

    private final static UUID eventId = UUID.randomUUID();
    private final static String payload = "{\"text\": \"This is a message\"}";
    private final static String version = "1.1.2";
    private final static String namespace = "ns";
    private final static String streamType = "ACTIVITY";
    private final static String createType = "CREATE";
    private final static String messageTypeCode = "code";
    private final static UUID correlationId = UUID.randomUUID();
    private final static UUID trackingId = UUID.randomUUID();
    private final static EventSummary eventSummary = new EventSummary()
            .setId(eventId)
            .setPayload(payload)
            .setCorrelationId(correlationId.toString())
            .setStreamType(streamType)
            .setMessageTypeCode(messageTypeCode)
            .setVersion(version)
            .setNamespace(namespace);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Message message = mock(Message.class);
        autobahnPublishMessage = mock(AutobahnPublishMessage.class);
        exchange = mock(Exchange.class);
        when(exchange.getIn()).thenReturn(message);
        when(exchange.getOut()).thenReturn(message);
        when(exchange.getIn().getBody()).thenReturn(autobahnPublishMessage);
        when(autobahnPublishMessage.getPayload()).thenReturn(payload);
        when(autobahnPublishMessage.getCorrelationId()).thenReturn(correlationId.toString());
        when(autobahnPublishMessage.getCreateType()).thenReturn(createType);
        when(autobahnPublishMessage.getMessageTypeCode()).thenReturn(messageTypeCode);
        when(autobahnPublishMessage.getNamespace()).thenReturn(namespace);
        when(autobahnPublishMessage.getStreamType()).thenReturn(streamType);
        when(autobahnPublishMessage.getTags()).thenReturn(null);
        when(autobahnPublishMessage.getVersion()).thenReturn(version);
    }

    @Test
    void handle_InitializationException() throws SchemaNotFoundException, PublishException, InitializationException, SchemaValidationException {
        when(eventService.createEvent(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(eventSummary));
        when(autobahnProducerService.produceMessage(any())).thenThrow(new InitializationException(""));
        AutobahnPublishException f = assertThrows(AutobahnPublishException.class,
                () -> publishToAutobahnService.publish(autobahnPublishMessage));
        assertEquals(String.format("unable to publish event %s to Autobahn.", eventId), f.getMessage());
    }

    @Test
    void handle_SchemaNotFoundException() throws SchemaNotFoundException, PublishException, InitializationException, SchemaValidationException {
        when(eventService.createEvent(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(eventSummary));
        when(autobahnProducerService.produceMessage(any())).thenThrow(new SchemaNotFoundException(""));
        AutobahnPublishException f = assertThrows(AutobahnPublishException.class,
                () -> publishToAutobahnService.publish(autobahnPublishMessage));
        assertEquals(String.format("unable to publish event %s to Autobahn.", eventId), f.getMessage());
    }

    @Test
    void handle_SchemaValidationException() throws SchemaNotFoundException, PublishException, InitializationException, SchemaValidationException {
        when(eventService.createEvent(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(eventSummary));
        when(autobahnProducerService.produceMessage(any())).thenThrow(new SchemaValidationException(""));
        AutobahnPublishException f = assertThrows(AutobahnPublishException.class,
                () -> publishToAutobahnService.publish(autobahnPublishMessage));
        assertEquals(String.format("unable to publish event %s to Autobahn.", eventId), f.getMessage());
    }

    @Test
    void handle_PublishException() throws SchemaNotFoundException, PublishException, InitializationException, SchemaValidationException {
        when(eventService.createEvent(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(eventSummary));
        UUID failId = UUID.randomUUID();
        EventFailure eventFailure = new EventFailure()
                .setEventId(eventId)
                .setExceptionMessage("Failed to publish")
                .setFailId(failId);
        when(eventService.createEventFailure(any(), any())).thenReturn(Mono.just(eventFailure));
        when(autobahnProducerService.produceMessage(any())).thenThrow(new PublishException(""));
        AutobahnEventPublishFault f = assertThrows(AutobahnEventPublishFault.class,
                () -> publishToAutobahnService.publish(autobahnPublishMessage));
        assertEquals(String.format("unable to publish event %s.", eventId), f.getMessage());
    }

    @Test
    void handle_success() throws SchemaNotFoundException, PublishException, InitializationException, SchemaValidationException {
        AutobahnPublishMessage expected = ((AutobahnPublishMessage) exchange.getIn().getBody()).setTrackingId(trackingId);
        when(eventService.createEvent(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(eventSummary));
        when(autobahnProducerService.produceMessage(any())).thenReturn(trackingId);
        EventTracking eventTracking = new EventTracking()
                .setEventId(eventId)
                .setTrackingId(trackingId);
        when(eventService.createTracking(any(), any())).thenReturn(Mono.just(eventTracking));
        publishToAutobahnService.publish(autobahnPublishMessage);
        assertEquals(expected, exchange.getOut().getBody(AutobahnPublishMessage.class));
    }

}
