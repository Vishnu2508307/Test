package com.smartsparrow.export.data;

import java.util.HashMap;
import java.util.NoSuchElementException;
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
public class ExportGateway {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportGateway.class);

    private final Session session;
    private final ExportResultNotificationMutator exportResultNotificationMutator;
    private final ExportResultNotificationMaterializer exportResultNotificationMaterializer;
    private final ErrorNotificationMutator errorNotificationMutator;
    private final ErrorNotificationMaterializer errorNotificationMaterializer;
    private final ExportSummaryMutator exportSummaryMutator;
    private final ExportSummaryMaterializer exportSummaryMaterializer;
    private final ExportSummaryByProjectMaterializer exportSummaryByProjectMaterializer;
    private final ExportSummaryByProjectMutator exportSummaryByProjectMutator;
    private final ExportSummaryByWorkspaceMaterializer exportSummaryByWorkspaceMaterializer;
    private final ExportSummaryByWorkspaceMutator exportSummaryByWorkspaceMutator;
    private final ResultNotificationByExportMaterializer resultNotificationByExportMaterializer;
    private final ResultNotificationByExportMutator resultNotificationByExportMutator;
    private final ExportRetryNotificationMutator exportRetryNotificationMutator;
    private final ExportAmbrosiaSnippetMaterializer exportAmbrosiaSnippetMaterializer;
    private final ExportAmbrosiaSnippetMutator exportAmbrosiaSnippetMutator;
    private final ErrorNotificationByExportMutator errorNotificationByExportMutator;
    private final ErrorNotificationByExportMaterializer errorNotificationByExportMaterializer;
    private final AmbrosiaReducerErrorByExportMutator ambrosiaReducerErrorByExportMutator;
    private final AmbrosiaReducerErrorByExportMaterializer ambrosiaReducerErrorByExportMaterializer;


    @Inject
    public ExportGateway(final Session session,
                         final ExportResultNotificationMutator exportResultNotificationMutator,
                         final ExportResultNotificationMaterializer exportResultNotificationMaterializer,
                         final ExportSummaryMutator exportSummaryMutator,
                         final ExportSummaryMaterializer exportSummaryMaterializer,
                         final ExportSummaryByProjectMaterializer exportSummaryByProjectMaterializer,
                         final ExportSummaryByProjectMutator exportSummaryByProjectMutator,
                         final ExportSummaryByWorkspaceMaterializer exportSummaryByWorkspaceMaterializer,
                         final ExportSummaryByWorkspaceMutator exportSummaryByWorkspaceMutator,
                         final ResultNotificationByExportMaterializer resultNotificationByExportMaterializer,
                         final ResultNotificationByExportMutator resultNotificationByExportMutator,
                         final ErrorNotificationMutator errorNotificationMutator,
                         final ErrorNotificationMaterializer errorNotificationMaterializer,
                         final ExportRetryNotificationMutator exportRetryNotificationMutator,
                         final ExportAmbrosiaSnippetMaterializer exportAmbrosiaSnippetMaterializer,
                         final ExportAmbrosiaSnippetMutator exportAmbrosiaSnippetMutator,
                         final ErrorNotificationByExportMutator errorNotificationByExportMutator,
                         final ErrorNotificationByExportMaterializer errorNotificationByExportMaterializer,
                         final AmbrosiaReducerErrorByExportMutator ambrosiaReducerErrorByExportMutator,
                         final AmbrosiaReducerErrorByExportMaterializer ambrosiaReducerErrorByExportMaterializer) {
        this.session = session;
        this.exportResultNotificationMutator = exportResultNotificationMutator;
        this.exportResultNotificationMaterializer = exportResultNotificationMaterializer;
        this.exportSummaryMutator = exportSummaryMutator;
        this.exportSummaryMaterializer = exportSummaryMaterializer;
        this.exportSummaryByProjectMaterializer = exportSummaryByProjectMaterializer;
        this.exportSummaryByProjectMutator = exportSummaryByProjectMutator;
        this.exportSummaryByWorkspaceMaterializer = exportSummaryByWorkspaceMaterializer;
        this.exportSummaryByWorkspaceMutator = exportSummaryByWorkspaceMutator;
        this.resultNotificationByExportMaterializer = resultNotificationByExportMaterializer;
        this.resultNotificationByExportMutator = resultNotificationByExportMutator;
        this.errorNotificationMutator = errorNotificationMutator;
        this.errorNotificationMaterializer = errorNotificationMaterializer;
        this.exportRetryNotificationMutator = exportRetryNotificationMutator;
        this.exportAmbrosiaSnippetMaterializer = exportAmbrosiaSnippetMaterializer;
        this.exportAmbrosiaSnippetMutator = exportAmbrosiaSnippetMutator;
        this.errorNotificationByExportMutator = errorNotificationByExportMutator;
        this.errorNotificationByExportMaterializer = errorNotificationByExportMaterializer;
        this.ambrosiaReducerErrorByExportMutator = ambrosiaReducerErrorByExportMutator;
        this.ambrosiaReducerErrorByExportMaterializer = ambrosiaReducerErrorByExportMaterializer;

    }

    /**
     * Save the courseware export result
     *
     * @param exportResult courseware export result.
     */
    @Trace(async = true)
    public Flux<Void> persist(final ExportResultNotification exportResult) {
        return Mutators.execute(session, Flux.just(
                exportResultNotificationMutator.upsert(exportResult),
                resultNotificationByExportMutator.upsert(exportResult)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving courseware export result %s",
                    exportResult), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the export summary details
     *
     * @param exportSummary courseware export summary.
     */
    @Trace(async = true)
    public Flux<Void> persist(final ExportSummary exportSummary) {
        return Mutators.execute(session, Flux.just(
                exportSummaryMutator.upsert(exportSummary),
                exportSummaryByProjectMutator.upsert(exportSummary),
                exportSummaryByWorkspaceMutator.upsert(exportSummary)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving export summary details %s",
                    exportSummary), throwable);
            throw Exceptions.propagate(throwable);
        })
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the courseware export notification error log
     *
     * @param errorNotification courseware export error
     */
    public Flux<Void> persist(final ExportErrorNotification errorNotification) {
        return Mutators.execute(session, Flux.just(
                errorNotificationMutator.upsert(errorNotification),
                errorNotificationByExportMutator.upsert(errorNotification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving courseware export error log %s",
                    errorNotification), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * fetch the export result notification
     * @param notificationId the notification id
     * @return mono of export result
     */
    public Mono<ExportResultNotification> fetchExportResult(final UUID notificationId) {
        return ResultSets.query(session, exportResultNotificationMaterializer.findById(notificationId))
                .flatMapIterable(row -> row)
                .map(exportResultNotificationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * fetch the courseware export error log
     * @param notificationId the notification id.
     * @return flux of export error log
     */
    public Flux<ExportErrorNotification> fetchExportErrorLog(final UUID notificationId) {
        return ResultSets.query(session, errorNotificationMaterializer.findById(notificationId))
                .flatMapIterable(row -> row)
                .map(errorNotificationMaterializer::fromRow);
    }

    /**
     * Save the ambrosia snippet details by export
     *
     * @param exportAmbrosiaSnippet ambrosia snippet export object
     * @deprecated to be removed when snippets storage in Cassandra is removed
     */
    @Deprecated
    @Trace(async = true)
    public Flux<Void> persist(final ExportAmbrosiaSnippet exportAmbrosiaSnippet) {
        return Mutators.execute(session, Flux.just(exportAmbrosiaSnippetMutator.upsert(exportAmbrosiaSnippet)))
                .doOnEach(log.reactiveErrorThrowable("error while saving ambrosia snippet by export",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("exportId", exportAmbrosiaSnippet.getExportId());
                                put("elementId", exportAmbrosiaSnippet.getElementId());
                            }
                        }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all the ambrosia snippets for an export
     *
     * @param exportId the export id to fetch the snippets for
     * @return a flux of ambrosia snippet
     * @deprecated to be removed when snippets storage in Cassandra is removed
     */
    @Deprecated
    public Flux<ExportAmbrosiaSnippet> fetchAmbrosiaSnippets(final UUID exportId) {
        return ResultSets.query(session, exportAmbrosiaSnippetMaterializer.findById(exportId))
                .flatMapIterable(row -> row)
                .map(exportAmbrosiaSnippetMaterializer::fromRow);
    }

    /**
     * Fetch ambrosia snippets by notification id
     * @param notificationId notification id
     * @return flux of {@link ExportAmbrosiaSnippet}
     */
    public Flux<ExportAmbrosiaSnippet> fetchAmbrosiaSnippetsByNotificationId(final UUID notificationId) {
        return ResultSets.query(session, exportAmbrosiaSnippetMaterializer.findByNotificationId(notificationId))
                .flatMapIterable(row -> row)
                .map(exportAmbrosiaSnippetMaterializer::fromRow);
    }

    /**
     * Fetch all the notification result for an export
     *
     * @param exportId the export to fetch all the result notifications for
     * @return a flux of notification result
     */
    public Flux<ExportResultNotification> fetchResultNotifications(final UUID exportId) {
        return ResultSets.query(session, resultNotificationByExportMaterializer.findById(exportId))
                .flatMapIterable(row -> row)
                .map(resultNotificationByExportMaterializer::fromRow);
    }

    /**
     * Find if there is any notification error associated with this exportId
     *
     * @param exportId the export id to find the error notifications for
     * @return a mono of true when errors found or a mono of false when there are no errors
     */
    public Mono<Boolean> hasNotificationError(final UUID exportId) {
        return ResultSets.query(session, errorNotificationByExportMaterializer.findErrors(exportId))
                .flatMapIterable(row -> row)
                .map(errorNotificationByExportMaterializer::fromRow)
                .hasElements();
    }

    /**
     * Find notification error(s) associated with this exportId
     *
     * @param exportId the export id to find the error notifications for
     * @return a mono of ExportErrorNotification
     */
    public Flux<ExportErrorNotification> exportError(final UUID exportId) {
        return ResultSets.query(session, errorNotificationByExportMaterializer.findErrors(exportId))
                .flatMapIterable(row -> row)
                .map(errorNotificationByExportMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the export summary info by id
     *
     * @param exportId the export id to find the summary for
     * @return a mono of export summary
     * @throws NoSuchElementException when the summary is not found
     */
    public Mono<ExportSummary> findExportSummary(final UUID exportId) {
        return ResultSets.query(session, exportSummaryMaterializer.findById(exportId))
                .flatMapIterable(row -> row)
                .map(exportSummaryMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetches all the exports for a workspace
     *
     * @param workspaceId only returns the exports belonging to this workspace
     * @return a flux of export summaries
     */
    @Trace(async = true)
    public Flux<ExportSummary> fetchExportSummariesByWorkspace(final UUID workspaceId) {
        return ResultSets.query(session, exportSummaryByWorkspaceMaterializer.findById(workspaceId))
                .flatMapIterable(row -> row)
                .map(exportSummaryByWorkspaceMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching exports for workspace %s", workspaceId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches all the exports for a project
     *
     * @param projectId only returns the exports belonging to this project
     * @return a flux of export summaries
     */
    @Trace(async = true)
    public Flux<ExportSummary> fetchExportSummariesByProject(final UUID projectId) {
        return ResultSets.query(session, exportSummaryByProjectMaterializer.findById(projectId))
                .flatMapIterable(row -> row)
                .map(exportSummaryByProjectMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching exports for project %s", projectId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save ambrosia snippet error logs
     *
     * @param ambrosiaSnippetErrorLog ambrosia snippet error log object
     */
    public Flux<Void> persist(final AmbrosiaReducerErrorLog ambrosiaSnippetErrorLog) {
        return Mutators.execute(session, Flux.just(ambrosiaReducerErrorByExportMutator.upsert(ambrosiaSnippetErrorLog)))
                .doOnEach(log.reactiveErrorThrowable("error while saving ambrosia snippet error log",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("exportId", ambrosiaSnippetErrorLog.getExportId());
                                                         }
                                                     }));
    }

    /**
     * Fetch ambrosia reducer error by export
     *
     * @param exportId export id
     * @return a flux of ambrosia reducer error
     */
    public Flux<AmbrosiaReducerErrorLog> getAmbrosiaReducerErrors(final UUID exportId) {
        return ResultSets.query(session, ambrosiaReducerErrorByExportMaterializer.findErrors(exportId))
                .flatMapIterable(row -> row)
                .map(ambrosiaReducerErrorByExportMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching ambrosia reducer error %s", exportId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
