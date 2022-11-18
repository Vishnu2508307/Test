package com.smartsparrow.la.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class EventGateway {

    private static final Logger log = MercuryLoggerFactory.getLogger(EventGateway.class);

    private final Session session;
    private final EventSummaryMaterializer eventSummaryMaterializer;
    private final EventSummaryMutator eventSummaryMutator;
    private final EventFailureMaterializer eventFailureMaterializer;
    private final EventFailureMutator eventFailureMutator;
    private final EventTrackingMaterializer eventTrackingMaterializer;
    private final EventTrackingMutator eventTrackingMutator;

    @Inject
    public EventGateway(Session session,
                        EventSummaryMaterializer eventSummaryMaterializer,
                        EventSummaryMutator eventSummaryMutator,
                        EventFailureMaterializer eventFailureMaterializer,
                        EventFailureMutator eventFailureMutator,
                        EventTrackingMaterializer eventTrackingMaterializer,
                        EventTrackingMutator eventTrackingMutator) {
        this.session = session;
        this.eventSummaryMaterializer = eventSummaryMaterializer;
        this.eventSummaryMutator = eventSummaryMutator;
        this.eventFailureMaterializer = eventFailureMaterializer;
        this.eventFailureMutator = eventFailureMutator;
        this.eventTrackingMaterializer = eventTrackingMaterializer;
        this.eventTrackingMutator = eventTrackingMutator;
    }

    /**
     * Get event summary for a given event
     *
     * @param eventId - unique identifier for an event
     * @return Mono<EventSummary> {@link Mono<EventSummary>} - mono of event summary
     */
    public Mono<EventSummary> findEvent(final UUID eventId) {
        return ResultSets.query(session, eventSummaryMaterializer.findById(eventId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToEventSummary)
                .singleOrEmpty();
    }

    /**
     * Persist an Event summary
     *
     * @param eventSummary - An event summary object
     */
    public Mono<Void> persist(final EventSummary eventSummary) {
        return Mutators.execute(session,
                Flux.just(eventSummaryMutator.upsert(eventSummary)))
                .singleOrEmpty();
    }

    /**
     * Find tracking id for an event
     *
     * @param eventId - unique identifier for an event
     * @return Mono<EventTracking> {@link Mono<EventTracking>} - mono of event tracking
     */
    public Mono<EventTracking> findEventTracking(final UUID eventId) {
        return ResultSets.query(session, eventTrackingMaterializer.findByEventId(eventId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToEventTracking)
                .singleOrEmpty();
    }

    /**
     * Persist event tracking
     *
     * @param eventTracking - event tracking object
     */
    public Mono<Void> persist(final EventTracking eventTracking) {
        return Mutators.execute(session, Flux.just(eventTrackingMutator.upsert(eventTracking)))
                .singleOrEmpty();
    }

    /**
     * Find all failures for an event
     *
     * @param eventId - unique identifier for an event
     * @return Flux<EventFailure> {@link Flux<EventFailure>} - a flux of Event failures
     */
    public Flux<EventFailure> findEventFailures(final UUID eventId) {
        return ResultSets.query(session, eventFailureMaterializer.findByEvent(eventId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToEventFailure)
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching event with id: %s", eventId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find an event failure
     *
     * @param eventId - unique identifier for an event
     * @param failId  - unique identifier for a failure
     * @return Mono<EventFailure> {@link Mono<EventFailure>} - a Mono of Event Failure
     */
    public Mono<EventFailure> findByEventAndFail(final UUID eventId, final UUID failId) {
        return ResultSets.query(session, eventFailureMaterializer.findByEventIdAndFailId(eventId, failId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToEventFailure)
                .singleOrEmpty();
    }

    /**
     * Persist and event failure
     *
     * @param eventFailure - Event Failure object
     */
    public Mono<Void> persist(final EventFailure eventFailure) {
        return Mutators.execute(session, Flux.just(eventFailureMutator.upsert(eventFailure)))
                .singleOrEmpty();
    }

    /**
     * Deletes an event failure
     *
     * @param eventFailure - Event failure info
     */
    public Flux<Void> delete(final EventFailure eventFailure) {
        return Mutators.execute(session, Flux.just(eventFailureMutator.delete(eventFailure)));
    }

    private EventSummary mapRowToEventSummary(Row row) {
        return new EventSummary()
                .setId(row.getUUID("id"))
                .setNamespace(row.getString("namespace"))
                .setVersion(row.getString("version"))
                .setMessageTypeCode(row.getString("message_type_code"))
                .setStreamType(row.getString("stream_type"))
                .setCreateType(row.getString("create_type"))
                .setCorrelationId(row.getString("correlation_id"))
                .setPayload(row.getString("payload"))
                .setTags(row.getMap("tags", String.class, String.class));
    }

    private EventTracking mapRowToEventTracking(Row row) {
        return new EventTracking()
                .setEventId(row.getUUID("event_id"))
                .setTrackingId(row.getUUID("tracking_id"));
    }

    private EventFailure mapRowToEventFailure(Row row) {
        return new EventFailure()
                .setEventId(row.getUUID("event_id"))
                .setFailId(row.getUUID("fail_id"))
                .setExceptionMessage(row.getString("exception_message"));
    }
}
