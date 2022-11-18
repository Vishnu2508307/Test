package com.smartsparrow.ingestion.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class IngestionGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IngestionGateway.class);

    private final Session session;
    private final EventByIngestionMutator eventByIngestionMutator;
    private final EventByIngestionMaterializer eventByIngestionMaterializer;
    private final SummaryByProjectMutator summaryByProjectMutator;
    private final SummaryByProjectMaterializer summaryByProjectMaterializer;
    private final IngestionEventMutator ingestionEventMutator;
    private final IngestionEventMaterializer ingestionEventMaterializer;
    private final IngestionSummaryMutator ingestionSummaryMutator;
    private final IngestionSummaryMaterializer ingestionSummaryMaterializer;
    private final SummaryByNameMutator summaryByNameMutator;
    private final SummaryByNameMaterializer summaryByNameMaterializer;

    @Inject
    public IngestionGateway(Session session,
                            EventByIngestionMutator eventByIngestionMutator,
                            EventByIngestionMaterializer eventByIngestionMaterializer,
                            SummaryByProjectMutator summaryByProjectMutator,
                            SummaryByProjectMaterializer summaryByProjectMaterializer,
                            IngestionEventMutator ingestionEventMutator,
                            IngestionEventMaterializer ingestionEventMaterializer,
                            IngestionSummaryMutator ingestionSummaryMutator,
                            IngestionSummaryMaterializer ingestionSummaryMaterializer,
                            SummaryByNameMutator summaryByNameMutator,
                            SummaryByNameMaterializer summaryByNameMaterializer) {
        this.session = session;
        this.eventByIngestionMutator = eventByIngestionMutator;
        this.eventByIngestionMaterializer = eventByIngestionMaterializer;
        this.summaryByProjectMutator = summaryByProjectMutator;
        this.summaryByProjectMaterializer = summaryByProjectMaterializer;
        this.ingestionEventMutator = ingestionEventMutator;
        this.ingestionEventMaterializer = ingestionEventMaterializer;
        this.ingestionSummaryMutator = ingestionSummaryMutator;
        this.ingestionSummaryMaterializer = ingestionSummaryMaterializer;
        this.summaryByNameMutator = summaryByNameMutator;
        this.summaryByNameMaterializer = summaryByNameMaterializer;
    }

    /**
     * Save the ingestion summary
     *
     * @param ingestionSummary ingestion summary.
     */
    @Trace(async = true)
    public Flux<Void> persist(final IngestionSummary ingestionSummary) {
        return Mutators.execute(session, Flux.just(
                ingestionSummaryMutator.upsert(ingestionSummary),
                summaryByProjectMutator.upsert(ingestionSummary),
                summaryByNameMutator.upsert(ingestionSummary)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving ingestion summary %s",
                                    ingestionSummary), throwable);
            throw Exceptions.propagate(throwable);
        })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the ingestion summary
     *
     * @param ingestionSummary ingestion summary.
     */
    @Trace(async = true)
    public Flux<Void> updateIngestionStatus(final IngestionSummary ingestionSummary) {
        return Mutators.execute(session, Flux.just(
                ingestionSummaryMutator.updateIngestionStatus(ingestionSummary),
                summaryByProjectMutator.updateIngestionStatus(ingestionSummary)
        )).doOnError(throwable -> {
            log.error(String.format("error while updating ingestion summary %s",
                                    ingestionSummary), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the ingestion summary
     *
     * @param ingestionSummary ingestion summary.
     */
    @Trace(async = true)
    public Flux<Void> updateIngestionSummary(final IngestionSummary ingestionSummary) {
        return Mutators.execute(session, Flux.just(
                ingestionSummaryMutator.updateIngestionSummary(ingestionSummary),
                summaryByProjectMutator.updateIngestionSummary(ingestionSummary)
        )).doOnError(throwable -> {
            log.error(String.format("error while updating ingestion summary %s",
                                    ingestionSummary), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the ingestion event
     *
     * @param ingestionEvent ingestion event.
     */
    @Trace(async = true)
    public Flux<Void> persist(final IngestionEvent ingestionEvent) {
        return Mutators.execute(session, Flux.just(
                ingestionEventMutator.upsert(ingestionEvent),
                eventByIngestionMutator.upsert(ingestionEvent)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving ingestion event %s",
                                    ingestionEvent), throwable);
            throw Exceptions.propagate(throwable);
        })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * fetch the ingestion event
     * @param eventId the event id
     * @return mono of ingestion event
     */
    @Trace(async = true)
    public Mono<IngestionEvent> findIngestionEvent(final UUID eventId) {
        return ResultSets.query(session, ingestionEventMaterializer.findById(eventId))
                .flatMapIterable(row -> row)
                .map(ingestionEventMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * fetch the ingestion summary
     * @param ingestionId the ingestion id
     * @return mono of ingestion summary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> findIngestionSummary(final UUID ingestionId) {
        return ResultSets.query(session, ingestionSummaryMaterializer.findById(ingestionId))
                .flatMapIterable(row -> row)
                .map(ingestionSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches all the events for an ingestion
     *
     * @param ingestionId only returns the events belonging to this ingestion
     * @return a flux of ingestion events
     */
    @Trace(async = true)
    public Flux<IngestionEvent> findEventsByIngestion(final UUID ingestionId) {
        return ResultSets.query(session, eventByIngestionMaterializer.findEvents(ingestionId))
                .flatMapIterable(row -> row)
                .map(eventByIngestionMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches all the ingestions for a project
     *
     * @param projectId only returns the ingestions belonging to this project
     * @return a flux of ingestion summaries
     */
    @Trace(async = true)
    public Flux<IngestionSummary> findSummaryByProject(final UUID projectId) {
        return ResultSets.query(session, summaryByProjectMaterializer.findIngestions(projectId))
                .flatMapIterable(row -> row)
                .map(summaryByProjectMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * fetch the ingestion summary by course name within a project
     * @param courseName the name of the course
     * @param projectId the id of the project
     * @param rootElementId the the root element id
     * @return flux of ingestion summary
     */
    @Trace(async = true)
    public Flux<IngestionSummary> findSummaryByName(final String courseName, final UUID projectId, UUID rootElementId) {
       if(rootElementId == null){
           return ResultSets.query(session, summaryByNameMaterializer.findIngestionsByName(courseName, projectId))
                   .flatMapIterable(row -> row)
                   .map(summaryByNameMaterializer::fromRow)
                   .doOnEach(ReactiveTransaction.linkOnNext());
       }else{
        return ResultSets.query(session, summaryByNameMaterializer.findIngestionsByNameRootElment(courseName, projectId, rootElementId))
                .flatMapIterable(row -> row)
                .map(summaryByNameMaterializer::fromRowWithElement)
                .doOnEach(ReactiveTransaction.linkOnNext());}
    }


    /**
     * Delete an ingestion from the database. Delete the record from each ingestion related table
     *
     * @param ingestion the summary of the ingestion to delete
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(final IngestionSummary ingestion) {
        return Mutators.execute(session, Flux.just(
                summaryByProjectMutator.delete(ingestion),
                ingestionSummaryMutator.delete(ingestion),
                summaryByNameMutator.delete(ingestion)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting ingestion [%s]", ingestion.getId()), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches all the ingestions for a project by Root element
     *
     * @param rootElementId only returns the ingestions belonging to this project
     * @return a flux of ingestion summaries
     */
    @Trace(async = true)
    public Flux<IngestionSummary> findSummaryByRootElement(final UUID rootElementId) {
        return ResultSets.query(session, ingestionSummaryMaterializer.findByRootElementId(rootElementId))
                .flatMapIterable(row -> row)
                .map(ingestionSummaryMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());

    }
}
