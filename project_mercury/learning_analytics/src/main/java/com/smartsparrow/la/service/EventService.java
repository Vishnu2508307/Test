package com.smartsparrow.la.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.la.data.EventFailure;
import com.smartsparrow.la.data.EventGateway;
import com.smartsparrow.la.data.EventSummary;
import com.smartsparrow.la.data.EventTracking;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EventService {
    private final EventGateway eventGateway;

    @Inject
    public EventService(EventGateway eventGateway) {
        this.eventGateway = eventGateway;
    }

    /**
     * Fetch an event by its id
     *
     * @param eventId - unique identifier for an event
     * @return {@link Mono< EventSummary >} - Mono of event summary
     */
    public Mono<EventSummary> findEvent(UUID eventId) {
        affirmArgument(eventId != null, "eventId is required");
        return eventGateway.findEvent(eventId);
    }

    /**
     * Fetch tracking info for an event
     *
     * @param eventId - unique identifier for an event
     * @return {@link Mono< EventTracking >} - Mono of event tracking
     */
    public Mono<EventTracking> findEventTracking(UUID eventId) {
        affirmArgument(eventId != null, "eventId is required");
        return eventGateway.findEventTracking(eventId);
    }

    /**
     * Fetch event failure info given eventId and failId
     *
     * @param eventId - unique identifier for an event
     * @param failId  - unique identifier for a failed event
     * @return {@link Mono< EventFailure >} - Mono of event failure
     */
    public Mono<EventFailure> findEventFailureById(UUID eventId, UUID failId) {
        affirmArgument(eventId != null, "eventId is required");
        affirmArgument(failId != null, "failId is required");
        return eventGateway.findByEventAndFail(eventId, failId);
    }

    /**
     * Fetch all event failures for an event
     *
     * @param eventId - unique identifier for an event
     * @return {@link Flux< EventFailure >} - Flux of event failures
     */
    public Flux<EventFailure> findAllFailuresForEvent(UUID eventId) {
        affirmArgument(eventId != null, "eventId is required");
        return eventGateway.findEventFailures(eventId);
    }

    /**
     * Creates an event summary
     *
     * @param namespace       - the namespace of the message schema
     * @param version         - the version of the message schema
     * @param messageTypeCode - message type code
     * @param streamType      - whether the message is an activity or an event
     * @param correlationId   - a unique identifier to group messages
     * @param payload         - the json payload
     * @param tags            - headers for routing
     * @return {@link Mono< EventSummary >} - A mono of EventSummary
     */
    public Mono<EventSummary> createEvent(String namespace, String version, String messageTypeCode, String streamType,
                                          String createType, String correlationId, String payload, Map<String, String> tags) {
        affirmArgument(namespace != null, "namespace is required");
        affirmArgument(version != null, "version is required");
        affirmArgument(messageTypeCode != null, "messageTypeCode is required");
        affirmArgument(streamType != null, "streamType is required");
        affirmArgument(createType != null, "createType is required");
        affirmArgument(payload != null, "payload is required");

        UUID eventId = UUIDs.timeBased();
        EventSummary eventSummary = new EventSummary()
                .setId(eventId)
                .setNamespace(namespace)
                .setVersion(version)
                .setMessageTypeCode(messageTypeCode)
                .setStreamType(streamType)
                .setCreateType(createType)
                .setCorrelationId(correlationId)
                .setPayload(payload)
                .setTags(tags);

        return eventGateway.persist(eventSummary).then(Mono.just(eventSummary));
    }

    /**
     * Persist tracking Id
     *
     * @param eventId    - unique identifier for an event
     * @param trackingId - unique identifier for a successfully published event
     * @return {@link Mono< EventTracking >} - mono of event tracking
     */
    public Mono<EventTracking> createTracking(UUID eventId, UUID trackingId) {
        affirmArgument(eventId != null, "eventId is required");
        affirmArgument(trackingId != null, "trackingId is required");

        EventTracking eventTracking = new EventTracking()
                .setEventId(eventId)
                .setTrackingId(trackingId);

        return eventGateway.persist(eventTracking).then(Mono.just(eventTracking));
    }

    /**
     * Create an event failure
     *
     * @param eventId          - unique identifier for an event
     * @param exceptionMessage - Cause for failure to publish a message
     * @return {@link Mono< EventFailure >} - a mono of event failure
     */
    public Mono<EventFailure> createEventFailure(UUID eventId, String exceptionMessage) {
        affirmArgument(eventId != null, "eventId is required");
        affirmArgument(exceptionMessage != null, "exceptionMessage is required");

        UUID failId = UUIDs.timeBased();
        EventFailure eventFailure = new EventFailure()
                .setEventId(eventId)
                .setFailId(failId)
                .setExceptionMessage(exceptionMessage);

        return eventGateway.persist(eventFailure).then(Mono.just(eventFailure));
    }

    /**
     * Delete event failures
     *
     * @param eventId - a unique identifier for an event
     */
    public Flux<Void> deleteEventFailure(UUID eventId) {
        affirmArgument(eventId != null, "eventId is required");

        EventFailure eventFailure = new EventFailure()
                .setEventId(eventId);

        return eventGateway.delete(eventFailure);
    }
}
