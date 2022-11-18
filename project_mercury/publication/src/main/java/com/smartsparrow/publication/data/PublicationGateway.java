package com.smartsparrow.publication.data;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
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

import java.util.UUID;

@Singleton
public class PublicationGateway {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationGateway.class);

    private final Session session;
    private final PublicationSummaryMutator publicationSummaryMutator;
    private final PublicationSummaryMaterializer publicationSummaryMaterializer;
    private final PublicationByActivityMutator publicationByActivityMutator;
    private final PublicationByActivityMaterializer publicationByActivityMaterializer;
    private final PublishedActivityByPublicationMutator publishedActivityByPublicationMutator;
    private final PublishedActivityByPublicationMaterializer publishedActivityByPublicationMaterializer;
    private final PublishedActivityMutator publishedActivityMutator;
    private final PublishedActivityMaterializer publishedActivityMaterializer;
    private final PublicationMetadataMutator publicationMetadataMutator;
    private final PublicationMetadataMaterializer publicationMetadataMaterializer;
    private final PublicationMetadataByPublishedActivityMutator publicationMetadataByPublishedActivityMutator;
    private final PublicationMetadataByPublishedActivityMaterializer publicationMetadataByPublishedActivityMaterializer;
    private final PublicationByExportMaterializer publicationByExportMaterializer;
    private final PublicationByExportMutator publicationByExportMutator;
    private final ActivityByAccountMutator activityByAccountMutator;
    private final ActivityByAccountMaterializer activityByAccountMaterializer;

    @Inject
    public PublicationGateway(final Session session,
                              final PublicationSummaryMutator publicationSummaryMutator,
                              final PublicationSummaryMaterializer publicationSummaryMaterializer,
                              final PublishedActivityByPublicationMutator publishedActivityByPublicationMutator,
                              final PublishedActivityByPublicationMaterializer publishedActivityByPublicationMaterializer,
                              final PublicationByActivityMutator publicationByActivityMutator,
                              final PublicationByActivityMaterializer publicationByActivityMaterializer,
                              final PublishedActivityMutator publishedActivityMutator,
                              final PublishedActivityMaterializer publishedActivityMaterializer,
                              final PublicationMetadataMutator publicationMetadataMutator,
                              final PublicationMetadataMaterializer publicationMetadataMaterializer,
                              final PublicationMetadataByPublishedActivityMutator publicationMetadataByPublishedActivityMutator,
                              final PublicationMetadataByPublishedActivityMaterializer publicationMetadataByPublishedActivityMaterializer,
                              final PublicationByExportMaterializer publicationByExportMaterializer,
                              final PublicationByExportMutator publicationByExportMutator,
                              final ActivityByAccountMutator activityByAccountMutator,
                              final ActivityByAccountMaterializer activityByAccountMaterializer) {
        this.session = session;
        this.publicationSummaryMutator = publicationSummaryMutator;
        this.publicationSummaryMaterializer = publicationSummaryMaterializer;
        this.publishedActivityByPublicationMutator = publishedActivityByPublicationMutator;
        this.publishedActivityByPublicationMaterializer = publishedActivityByPublicationMaterializer;
        this.publicationByActivityMutator = publicationByActivityMutator;
        this.publicationByActivityMaterializer = publicationByActivityMaterializer;
        this.publishedActivityMaterializer = publishedActivityMaterializer;
        this.publishedActivityMutator = publishedActivityMutator;
        this.publicationMetadataMutator = publicationMetadataMutator;
        this.publicationMetadataMaterializer = publicationMetadataMaterializer;
        this.publicationMetadataByPublishedActivityMutator = publicationMetadataByPublishedActivityMutator;
        this.publicationMetadataByPublishedActivityMaterializer = publicationMetadataByPublishedActivityMaterializer;
        this.publicationByExportMaterializer = publicationByExportMaterializer;
        this.publicationByExportMutator = publicationByExportMutator;
        this.activityByAccountMutator = activityByAccountMutator;
        this.activityByAccountMaterializer = activityByAccountMaterializer;
    }

    /**
     * Save the publication summary
     *
     * @param publicationSummary  the publication summary
     */
    public Flux<Void> persist(PublicationSummary publicationSummary) {
        return Mutators.execute(session, Flux.just(
                publicationSummaryMutator.upsert(publicationSummary)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving publication summary %s",
                    publicationSummary), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetch all publication summary
     *
     * @return all the publication summaries
     */
    public Flux<PublicationSummary> fetchAllPublicationSummary() {
        return ResultSets.query(session, publicationSummaryMaterializer.findAll())
                .flatMapIterable(row -> row)
                .map(publicationSummaryMaterializer::fromRow);
    }

    /**
     * Fetch publication summary with id
     *
     * @return the publication summary with the id
     */
    public Flux<PublicationSummary> fetchPublicationSummary(UUID publicationId) {
        return ResultSets.query(session, publicationSummaryMaterializer.findById(publicationId))
                .flatMapIterable(row -> row)
                .map(publicationSummaryMaterializer::fromRow);
    }

    /**
     * fetch the publication by activity
     * @param activityId the activity id.
     * @return flux of publication by activity
     */
    public Flux<PublicationByActivity> fetchPublicationByActivity(UUID activityId) {
        return ResultSets.query(session, publicationByActivityMaterializer.findById(activityId))
                .flatMapIterable(row -> row)
                .map(publicationByActivityMaterializer::fromRow);
    }

    /**
     * fetch the publication by activity
     * @param activityId the activity id.
     * @return flux of publication by activity
     */
    public Flux<PublishedActivity> fetchPublishedActivity(UUID activityId) {
        return ResultSets.query(session, publishedActivityByPublicationMaterializer.findByActivityId(activityId))
                .flatMapIterable(row -> row)
                .map(publishedActivityByPublicationMaterializer::fromRow);
    }

    /**
     * Save the published activity
     *
     * @param publishedActivity  the published activity
     */
    public Flux<Void> persist(PublishedActivity publishedActivity) {
        return Mutators.execute(session, Flux.just(
                publishedActivityMutator.upsert(publishedActivity),
                publishedActivityByPublicationMutator.upsert(publishedActivity),
                publicationByActivityMutator.upsert(new PublicationByActivity()
                        .setActivityId(publishedActivity.getActivityId())
                        .setPublicationId(publishedActivity.getPublicationId()))
        )).doOnError(throwable -> {
            log.error(String.format("error while saving published activity %s",
                    publishedActivity), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * fetch the published activity
     * @param activityId the activity id.
     * @return flux of published activity
     */
    public Flux<PublishedActivity> fetchPublishedActivityWithId(UUID activityId) {
        return ResultSets.query(session, publishedActivityMaterializer.findById(activityId))
                .flatMapIterable(row -> row)
                .map(publishedActivityMaterializer::fromRow);
    }

    /**
     * fetch a list of published activity
     * @return flux of published activity
     */
    public Flux<PublishedActivity> fetchPublishedActivities() {
        return ResultSets.query(session, publishedActivityMaterializer.findAll())
                .flatMapIterable(row -> row)
                .map(publishedActivityMaterializer::fromRow);
    }

    /**
     * fetch the published activity
     * @param publicationId the publication id.
     * @return flux of published activity
     */
    public Flux<PublishedActivity> fetchPublishedActivityByPublication(UUID publicationId) {
        return ResultSets.query(session, publishedActivityByPublicationMaterializer.findById(publicationId))
                .flatMapIterable(row -> row)
                .map(publishedActivityByPublicationMaterializer::fromRow);
    }

    /**
     * Save the publication metadata
     *
     * @param publicationMetadata  the publication metadata
     */
    public Flux<Void> persist(PublicationMetadataByPublishedActivity publicationMetadata) {
        return Mutators.execute(session, Flux.just(
                publicationMetadataMutator.upsert(publicationMetadata),
                publicationMetadataByPublishedActivityMutator.upsert(publicationMetadata)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving metadata of publication %s",
                    publicationMetadata), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * fetch the metadata by publication
     * @param publicationId the publication id
     * @return flux of metadata by publication
     */
    public Flux<PublicationMetadata> fetchMetadataByPublication(UUID publicationId) {
        return ResultSets.query(session, publicationMetadataMaterializer.findById(publicationId))
                .flatMapIterable(row -> row)
                .map(publicationMetadataMaterializer::fromRow);
    }

    @Trace(async = true)
    public Flux<Void> updateActivityPublicationStatus(final UUID publicationId, final ActivityPublicationStatus activityPublicationStatus, final  UUID activityId, final String version) {
        return Mutators.execute(session, Flux.just(
                        publishedActivityByPublicationMaterializer.updateActivityPublicationStatus(publicationId, activityPublicationStatus, activityId, version)
                )).doOnError(throwable -> {
                    log.error(String.format("error while updating activity publication status %s",
                                            publicationId), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * fetch the publication metadata by published activity
     * @param activityId the published activity id
     * @return flux of publication metadata by published activity
     */
    public Flux<PublicationMetadataByPublishedActivity> fetchMetadataByPublishedActivity(UUID activityId) {
        return ResultSets.query(session, publicationMetadataByPublishedActivityMaterializer.findById(activityId))
                .flatMapIterable(row -> row)
                .map(publicationMetadataByPublishedActivityMaterializer::fromRow);
    }

    /**
     * fetch the publication metadata by published activity
     * @param activityId the published activity id
     * @return flux of publication metadata by published activity
     */
    public Flux<PublicationMetadataByPublishedActivity> fetchMetadataByPublishedActivity(UUID activityId, String version) {
        return ResultSets.query(session, publicationMetadataByPublishedActivityMaterializer.findByIdAndVersion(activityId, version))
                .flatMapIterable(row -> row)
                .map(publicationMetadataByPublishedActivityMaterializer::fromRow);
    }

    /**
     * Save the publication by export
     *
     * @param publicationByExport  the publication by export
     */
    public Flux<Void> persist(PublicationByExport publicationByExport) {
        return Mutators.execute(session, Flux.just(
                publicationByExportMutator.upsert(publicationByExport)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving publication export %s",
                    publicationByExport), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * fetch the publication by export
     * @param exportId the export id.
     * @return flux of publication by export
     */
    public Flux<PublicationByExport> fetchPublicationByExport(UUID exportId) {
        return ResultSets.query(session, publicationByExportMaterializer.findById(exportId))
                .flatMapIterable(row -> row)
                .map(publicationByExportMaterializer::fromRow);
    }

    /**
     * Save the avtivity by account
     *
     * @param activityByAccount  the activity by account
     */
    public Flux<Void> persist(ActivityByAccount activityByAccount) {
        return Mutators.execute(session, Flux.just(
                activityByAccountMutator.upsert(activityByAccount)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving activity by account %s",
                    activityByAccount), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * fetch the activity by account
     * @param accountId the account id.
     * @return flux of activity by account
     */
    public Flux<ActivityByAccount> fetchActivityByAccount(UUID accountId) {
        return ResultSets.query(session, activityByAccountMaterializer.findById(accountId))
                .flatMapIterable(row -> row)
                .map(activityByAccountMaterializer::fromRow);
    }

    /**
     * Update a title for published activity
     *
     * @param activityId     the cohort id to set the finished date for
     * @param title the finished date to set to the cohort
     */
    public Mono<Void> updateTitle(UUID activityId, String title,  String version) {
        return Mutators.execute(session, Flux.just(publishedActivityMutator.setTitle(activityId,
                                                                                     title, version))).singleOrEmpty();
    }
}
