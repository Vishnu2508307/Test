package com.smartsparrow.la.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.la.data.EventFailure;
import com.smartsparrow.la.data.EventGateway;
import com.smartsparrow.la.data.EventSummary;
import com.smartsparrow.la.data.EventTracking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EventServiceTest {

    @InjectMocks
    EventService eventService;

    @Mock
    EventGateway eventGateway;

    UUID eventId = UUID.randomUUID();
    String payload = "{\"text\": \"This is a message\"}";
    String version = "1.1.2";
    String namespace = "ns";
    String streamType = "ACTIVITY";
    String createType = "CREATE";
    String messageTypeCode = "code";
    UUID correlationId = UUID.randomUUID();
    UUID trackingId = UUID.randomUUID();
    UUID failId = UUID.randomUUID();
    String exceptionMessage = "Cannot connect exception. Cannot Publish message, try again later";
    EventSummary eventSummary = new EventSummary()
            .setId(eventId)
            .setPayload(payload)
            .setCorrelationId(correlationId.toString())
            .setStreamType(streamType)
            .setMessageTypeCode(messageTypeCode)
            .setVersion(version)
            .setNamespace(namespace);

    EventTracking eventTracking = new EventTracking()
            .setTrackingId(trackingId)
            .setEventId(eventId);

    EventFailure eventFailure = new EventFailure()
            .setEventId(eventId)
            .setFailId(failId)
            .setExceptionMessage(exceptionMessage);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findEvent_missing_eventId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.findEvent(null));
        assertEquals("eventId is required", f.getMessage());
    }

    @Test
    void findEvent_success() {
        when(eventGateway.findEvent(eventId)).thenReturn(Mono.just(eventSummary));
        EventSummary res = eventService.findEvent(eventId).block();
        assertEquals(eventSummary, res);
    }

    @Test
    void findEventTracking_missing_eventId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.findEventTracking(null));
        assertEquals("eventId is required", f.getMessage());
    }

    @Test
    void findEventTracking_success() {
        when(eventGateway.findEventTracking(eventId)).thenReturn(Mono.just(eventTracking));
        EventTracking res = eventService.findEventTracking(eventId).block();
        assertEquals(eventTracking, res);
    }

    @Test
    void findEventFailureById_missing_eventId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.findEventFailureById(null, null));
        assertEquals("eventId is required", f.getMessage());
    }

    @Test
    void findEventFailureById_missing_failId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.findEventFailureById(eventId, null));
        assertEquals("failId is required", f.getMessage());
    }

    @Test
    void findEventFailureById_success() {
        when(eventGateway.findByEventAndFail(eventId, failId)).thenReturn(Mono.just(eventFailure));
        EventFailure res = eventService.findEventFailureById(eventId, failId).block();
        assertEquals(eventFailure, res);
    }

    @Test
    void findAllFailuresForEvent_missing_eventId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.findAllFailuresForEvent(null));
        assertEquals("eventId is required", f.getMessage());
    }

    @Test
    void findAllFailuresForEvent_success() {
        when(eventGateway.findEventFailures(eventId)).thenReturn(Flux.just(eventFailure));
        EventFailure res = eventService.findAllFailuresForEvent(eventId).blockLast();
        assertEquals(eventFailure, res);
    }

    @Test
    void createEvent_missing_namespace() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEvent(null, null, null, null, null,null, null, null));
        assertEquals("namespace is required", f.getMessage());
    }

    @Test
    void createEvent_missing_version() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEvent(namespace, null, null, null, null,null, null, null));
        assertEquals("version is required", f.getMessage());
    }

    @Test
    void createEvent_missing_messageTypeCode() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEvent(namespace, version, null, null, null, null,null, null));
        assertEquals("messageTypeCode is required", f.getMessage());
    }

    @Test
    void createEvent_missing_streamType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEvent(namespace, version, messageTypeCode, null, null, null,null, null));
        assertEquals("streamType is required", f.getMessage());
    }

    @Test
    void createEvent_missing_createType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEvent(namespace, version, messageTypeCode, streamType, null, null,null, null));
        assertEquals("createType is required", f.getMessage());
    }

    @Test
    void createEvent_missing_payload() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEvent(namespace, version, messageTypeCode, streamType, createType, null,null, null));
        assertEquals("payload is required", f.getMessage());
    }

    @Test
    void createEvent_success() {
        when(eventGateway.persist(any(EventSummary.class))).thenReturn(Mono.empty());
        EventSummary eventSummary = eventService.createEvent(namespace,
                version,
                messageTypeCode,
                streamType,
                createType,
                correlationId.toString(),
                payload,
                null).block();
        assertNotNull(eventSummary);
        assertNotNull(eventSummary.getId());
        assertEquals(namespace, eventSummary.getNamespace());
        assertEquals(version, eventSummary.getVersion());
        assertEquals(messageTypeCode, eventSummary.getMessageTypeCode());
        assertEquals(streamType, eventSummary.getStreamType());
        assertEquals(createType, eventSummary.getCreateType());
        assertEquals(correlationId.toString(), eventSummary.getCorrelationId());
        assertEquals(payload, eventSummary.getPayload());
    }

    @Test
    void createEventTracking_missing_eventId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createTracking(null, null));
        assertEquals("eventId is required", f.getMessage());
    }

    @Test
    void createEventTracking_missing_trackingId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createTracking(eventId, null));
        assertEquals("trackingId is required", f.getMessage());
    }

    @Test
    void createEventTracking_success() {
        when(eventGateway.persist(any(EventTracking.class))).thenReturn(Mono.empty());
        EventTracking eventTracking = eventService.createTracking(eventId, trackingId).block();
        assertNotNull(eventTracking);
        assertEquals(eventId, eventTracking.getEventId());
        assertEquals(trackingId, eventTracking.getTrackingId());
    }

    @Test
    void createEventFailure_missingEventId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEventFailure(null, null));
        assertEquals("eventId is required", f.getMessage());
    }

    @Test
    void createEventFailure_missingExceptionMessage() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.createEventFailure(eventId, null));
        assertEquals("exceptionMessage is required", f.getMessage());
    }

    @Test
    void createEventFailure_success() {
        when(eventGateway.persist(any(EventFailure.class))).thenReturn(Mono.empty());
        EventFailure eventTracking = eventService.createEventFailure(eventId, exceptionMessage).block();
        assertNotNull(eventTracking);
        assertNotNull(eventTracking.getFailId());
        assertEquals(eventId, eventTracking.getEventId());
        assertEquals(exceptionMessage, eventTracking.getExceptionMessage());
    }

    @Test
    void deleteEventFailure_missingEventId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> eventService.deleteEventFailure(null));
        assertEquals("eventId is required", f.getMessage());
    }

    @Test
    void deleteEventFailure_success() {
        when(eventGateway.delete(any(EventFailure.class))).thenReturn(Flux.just(new Void[]{}));
        ArgumentCaptor<EventFailure> projectCaptor = ArgumentCaptor.forClass(EventFailure.class);
        eventService.deleteEventFailure(eventId).blockFirst();
        verify(eventGateway).delete(projectCaptor.capture());
        EventFailure deleted = projectCaptor.getValue();
        assertEquals(eventId, deleted.getEventId());
    }
}
