package com.smartsparrow.workspace.data;

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

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.NoSuchElementException;
import java.util.UUID;

@Singleton
public class AlfrescoAssetTrackGateway {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetTrackGateway.class);

    private final Session session;
    private final AlfrescoAssetSyncSummaryMaterializer alfrescoAssetSyncSummaryMaterializer;
    private final AlfrescoAssetSyncSummaryMutator alfrescoAssetSyncSummaryMutator;
    private final AlfrescoAssetSyncSummaryByCourseMaterializer alfrescoAssetSyncSummaryByCourseMaterializer;
    private final AlfrescoAssetSyncSummaryByCourseMutator alfrescoAssetSyncSummaryByCourseMutator;
    private final AlfrescoAssetSyncNotificationMaterializer alfrescoAssetSyncNotificationMaterializer;
    private final AlfrescoAssetSyncNotificationMutator alfrescoAssetSyncNotificationMutator;
    private final AlfrescoAssetSyncNotificationByReferenceMaterializer alfrescoAssetSyncNotificationByReferenceMaterializer;
    private final AlfrescoAssetSyncNotificationByReferenceMutator alfrescoAssetSyncNotificationByReferenceMutator;

    @Inject
    public AlfrescoAssetTrackGateway(final Session session,
                                     final AlfrescoAssetSyncSummaryMaterializer alfrescoAssetSyncSummaryMaterializer,
                                     final AlfrescoAssetSyncSummaryMutator alfrescoAssetSyncSummaryMutator,
                                     final AlfrescoAssetSyncSummaryByCourseMaterializer alfrescoAssetSyncSummaryByCourseMaterializer,
                                     final AlfrescoAssetSyncSummaryByCourseMutator alfrescoAssetSyncSummaryByCourseMutator,
                                     final AlfrescoAssetSyncNotificationMaterializer alfrescoAssetSyncNotificationMaterializer,
                                     final AlfrescoAssetSyncNotificationMutator alfrescoAssetSyncNotificationMutator,
                                     final AlfrescoAssetSyncNotificationByReferenceMaterializer alfrescoAssetSyncNotificationByReferenceMaterializer,
                                     final AlfrescoAssetSyncNotificationByReferenceMutator alfrescoAssetSyncNotificationByReferenceMutator) {
        this.session = session;
        this.alfrescoAssetSyncSummaryMaterializer = alfrescoAssetSyncSummaryMaterializer;
        this.alfrescoAssetSyncSummaryMutator = alfrescoAssetSyncSummaryMutator;
        this.alfrescoAssetSyncSummaryByCourseMaterializer = alfrescoAssetSyncSummaryByCourseMaterializer;
        this.alfrescoAssetSyncSummaryByCourseMutator = alfrescoAssetSyncSummaryByCourseMutator;
        this.alfrescoAssetSyncNotificationMaterializer = alfrescoAssetSyncNotificationMaterializer;
        this.alfrescoAssetSyncNotificationMutator = alfrescoAssetSyncNotificationMutator;
        this.alfrescoAssetSyncNotificationByReferenceMaterializer = alfrescoAssetSyncNotificationByReferenceMaterializer;
        this.alfrescoAssetSyncNotificationByReferenceMutator = alfrescoAssetSyncNotificationByReferenceMutator;
    }

    /**
     * Save the alfresco sync summary details
     *
     * @param summary alfresco sync summary.
     */
    public Flux<Void> persist(AlfrescoAssetSyncSummary summary) {
        return Mutators.execute(session, Flux.just(
                alfrescoAssetSyncSummaryMutator.upsert(summary),
                alfrescoAssetSyncSummaryByCourseMutator.upsert(summary)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving sync summary details %s",
                    summary), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the alfresco sync summary info by id
     *
     * @param referenceId the reference id to find the summary for
     * @return a mono of alfresco sync summary
     * @throws NoSuchElementException when the summary is not found
     */
    public Mono<AlfrescoAssetSyncSummary> findSyncSummary(final UUID referenceId) {
        return ResultSets.query(session, alfrescoAssetSyncSummaryMaterializer.findById(referenceId))
                .flatMapIterable(row -> row)
                .map(alfrescoAssetSyncSummaryMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetches all the alfresco sync summaries for a course
     *
     * @param courseId only returns the alfresco sync summaries belonging to this course
     * @return a flux of alfresco sync summaries
     */
    @Trace(async = true)
    public Flux<AlfrescoAssetSyncSummary> fetchSyncSummariesByCourse(final UUID courseId) {
        return ResultSets.query(session, alfrescoAssetSyncSummaryByCourseMaterializer.findById(courseId))
                .flatMapIterable(row -> row)
                .map(alfrescoAssetSyncSummaryByCourseMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching sync summaries for course %s", courseId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save the alfresco sync notification details
     *
     * @param notification alfresco sync notification.
     */
    public Flux<Void> persist(AlfrescoAssetSyncNotification notification) {
        return Mutators.execute(session, Flux.just(
                alfrescoAssetSyncNotificationMutator.upsert(notification),
                alfrescoAssetSyncNotificationByReferenceMutator.upsert(notification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving sync notification details %s",
                    notification), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the alfresco sync notification info by id
     *
     * @param notificationId the reference id to find the summary for
     * @return a mono of alfresco sync notification
     * @throws NoSuchElementException when the summary is not found
     */
    public Mono<AlfrescoAssetSyncNotification> findSyncNotification(final UUID notificationId) {
        return ResultSets.query(session, alfrescoAssetSyncNotificationMaterializer.findById(notificationId))
                .flatMapIterable(row -> row)
                .map(alfrescoAssetSyncNotificationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetches all the alfresco sync notifications for a reference id
     *
     * @param referenceId only returns the alfresco sync summaries belonging to this course
     * @return a flux of alfresco sync summaries
     */
    @Trace(async = true)
    public Flux<AlfrescoAssetSyncNotification> fetchSyncNotificationsByReference(final UUID referenceId) {
        return ResultSets.query(session, alfrescoAssetSyncNotificationByReferenceMaterializer.findById(referenceId))
                .flatMapIterable(row -> row)
                .map(alfrescoAssetSyncNotificationByReferenceMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching sync notifications for reference id %s", referenceId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
