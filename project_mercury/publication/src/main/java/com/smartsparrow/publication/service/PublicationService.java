package com.smartsparrow.publication.service;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.time.Duration;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.LocalDateTime;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.publication.data.ActivityByAccount;
import com.smartsparrow.publication.data.ActivityPublicationStatus;
import com.smartsparrow.publication.data.PublicationActivityPayload;
import com.smartsparrow.publication.data.PublicationByExport;
import com.smartsparrow.publication.data.PublicationGateway;
import com.smartsparrow.publication.data.PublicationMetadata;
import com.smartsparrow.publication.data.PublicationMetadataByPublishedActivity;
import com.smartsparrow.publication.data.PublicationOculusData;
import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.publication.data.PublicationPayload;
import com.smartsparrow.publication.data.PublicationSummary;
import com.smartsparrow.publication.data.PublishedActivity;
import com.smartsparrow.publication.job.data.JobByPublication;
import com.smartsparrow.publication.job.data.JobGateway;
import com.smartsparrow.publication.job.data.JobSummary;
import com.smartsparrow.publication.job.data.Notification;
import com.smartsparrow.publication.job.enums.ArtifactType;
import com.smartsparrow.publication.job.enums.JobStatus;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class PublicationService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationService.class);

    private final PublicationGateway publicationGateway;
    private final AccountService accountService;
    private final JobGateway jobGateway;
    private final PublicationBroker publicationBroker;

    @Inject
    public PublicationService(final PublicationGateway publicationGateway,
                              final AccountService accountService,
                              final JobGateway jobGateway,
                              final PublicationBroker publicationBroker) {
        this.publicationGateway = publicationGateway;
        this.accountService = accountService;
        this.jobGateway = jobGateway;
        this.publicationBroker = publicationBroker;
    }

    /**
     * Fetches a list of publication with metadata
     *
     * @return a flux of PublicationWithMetadata {@link PublicationPayload}
     */
    public Flux<PublicationPayload> fetchPublicationWithMeta() {

        return publicationGateway.fetchAllPublicationSummary().flatMap(publicationSummary -> {
            final Flux<PublicationMetadata> metadataFlux = publicationGateway.fetchMetadataByPublication(publicationSummary.getId());
            final Flux<PublishedActivity> activityFlux = publicationGateway.fetchPublishedActivityByPublication(publicationSummary.getId());

            return Flux.zip(Flux.just(publicationSummary), metadataFlux, activityFlux)
                    .flatMap(tuple3 -> {
                        PublicationSummary summary = tuple3.getT1();
                        PublicationMetadata metadata = tuple3.getT2();
                        PublishedActivity activity = tuple3.getT3();

                        return accountService.getAccountPayload(metadata.getCreatedBy())
                                .defaultIfEmpty(new AccountPayload())
                                .flux()
                                .flatMap(accountPayload -> Flux.just(new PublicationPayload().setPublicationId(summary.getId())
                                        .setTitle(summary.getTitle())
                                        .setDescription(summary.getDescription())
                                        .setConfig(summary.getConfig())
                                        .setAuthor(metadata.getAuthor())
                                        .setEtextVersion(metadata.getEtextVersion())
                                        .setBookId(metadata.getBookId())
                                        .setPublishedBy(metadata.getCreatedBy())
                                        .setPublisherFamilyName(accountPayload.getFamilyName())
                                        .setPublisherGivenName(accountPayload.getGivenName())
                                        .setUpdatedAt(metadata.getCreatedAt())
                                        .setActivityId(activity.getActivityId())))
                                .flatMap(publicationPayload -> jobGateway.fetchJobByPublication(publicationPayload.getPublicationId())
                                        .defaultIfEmpty(new JobByPublication().setPublicationId(UUIDs.timeBased()).setJobId(UUIDs.timeBased()))
                                        .flatMap(jobByPublication -> jobGateway.fetchJobSummary(jobByPublication.getJobId()))
                                        .defaultIfEmpty(new JobSummary().setId(null).setJobType(null))
                                        .flatMap(jobSummary -> Mono.just(publicationPayload.setJobId(jobSummary.getId())
                                                .setPublicationJobStatus(jobSummary.getStatus())
                                                .setStatusMessage(jobSummary.getStatusDesc()))));
                    });
        });
    }

    /**
     * persist publication along with metadata
     *
     * @param activityId
     * @param accountId
     * @param publicationTitle
     * @param description
     * @param author
     * @param exportId
     * @return mono of UUID
     */
    public Mono<UUID> createPublication(final UUID activityId,
                                        final UUID accountId,
                                        final UUID exportId,
                                        final String publicationTitle,
                                        final String description,
                                        final String author,
                                        final String version,
                                        final String config,
                                        final PublicationOutputType outputType) {
        affirmNotNull(activityId, "activityId is required");
        affirmNotNull(version, "version is required");
        affirmNotNull(outputType, "outputType is required");

        if (outputType != PublicationOutputType.BRONTE_CLASSES_ON_DEMAND
                && outputType != PublicationOutputType.BRONTE_PEARSON_PLUS) {
            affirmNotNull(exportId, "exportId is required");
        }

        final UUID publicationId = UUIDs.timeBased();
        final PublicationSummary publicationSummary = new PublicationSummary()
                .setId(publicationId)
                .setTitle(publicationTitle)
                .setDescription(description)
                .setConfig(config)
                .setOutputType(outputType);

        final PublicationMetadataByPublishedActivity publicationMetadata = (PublicationMetadataByPublishedActivity) new PublicationMetadataByPublishedActivity()
                .setActivityId(activityId)
                .setVersion(version)
                .setPublicationId(publicationId)
                .setAuthor(author)
                .setCreatedBy(accountId)
                .setCreatedAt(publicationId);

        final PublishedActivity publishedActivity = new PublishedActivity()
                .setActivityId(activityId)
                .setPublicationId(publicationId)
                .setDescription(description)
                .setTitle(publicationTitle)
                .setVersion(version)
                .setOutputType(outputType)
                .setStatus(ActivityPublicationStatus.ACTIVE);

        // there may not be an export for certain publication output types
        final PublicationByExport publicationByExport = (exportId == null) ? null : new PublicationByExport()
                .setExportId(exportId)
                .setPublicationId(publicationId);

        final ActivityByAccount activityByAccount = new ActivityByAccount()
                .setAccountId(accountId)
                .setActivityId(activityId);

        if (publicationByExport == null) {
            return publicationGateway.persist(publicationSummary)
                    .thenMany(publicationGateway.persist(publishedActivity))
                    .thenMany(publicationGateway.persist(publicationMetadata))
                    .thenMany(publicationGateway.persist(activityByAccount))
                    .then(Mono.just(publicationId));
        } else {
            return publicationGateway.persist(publicationSummary)
                    .thenMany(publicationGateway.persist(publishedActivity))
                    .thenMany(publicationGateway.persist(publicationMetadata))
                    .thenMany(publicationGateway.persist(publicationByExport))
                    .thenMany(publicationGateway.persist(activityByAccount))
                    .then(Mono.just(publicationId));
        }
    }

    /**
     * Update the etext verison of the publication with given exportId
     *
     * @param exportId
     * @param activityId
     * @param etextVersion
     * @param bookId
     * @return a flux of publication metadata
     */
    public Flux<PublicationMetadata> updatePublicationMetadata(final UUID exportId,
                                                               final UUID activityId,
                                                               final String etextVersion,
                                                               final String bookId) {
        affirmNotNull(exportId, "exportId is required");
        affirmNotNull(activityId, "activityId is required");
        affirmNotNull(etextVersion, "etextVersion is required");
        affirmNotNull(bookId, "bookId is required");

        final Flux<PublicationByExport> publicationByExportFlux = publicationGateway
                .fetchPublicationByExport(exportId);

        final Flux<PublicationMetadataByPublishedActivity> metadataFlux = publicationGateway
                .fetchMetadataByPublishedActivity(activityId);

        return Flux.zip(publicationByExportFlux, metadataFlux).flatMap(tuple2 -> {
            final PublicationByExport publicationByExport = tuple2.getT1();
            final PublicationMetadataByPublishedActivity metadata = tuple2.getT2();
            final PublicationMetadataByPublishedActivity publicationMetadata = (PublicationMetadataByPublishedActivity) new PublicationMetadataByPublishedActivity()
                    .setActivityId(metadata.getActivityId())
                    .setVersion(metadata.getVersion())
                    .setPublicationId(publicationByExport.getPublicationId())
                    .setAuthor(metadata.getAuthor())
                    .setCreatedBy(metadata.getCreatedBy())
                    .setCreatedAt(metadata.getCreatedAt())
                    .setEtextVersion(etextVersion)
                    .setBookId(bookId);

            return publicationGateway.persist(publicationMetadata)
                    .thenMany(publicationGateway.fetchMetadataByPublication(publicationByExport.getPublicationId()));
        });
    }

    /**
     * save etext notification
     *
     * @param notification
     * @param jobId
     * @return a flux of void
     */
    public Flux<Void> saveEtextNotification(Notification notification, UUID jobId) {
        affirmNotNull(notification, "notification is required");
        affirmNotNull(jobId, "jobId is required");

        return jobGateway.persist(notification, jobId);
    }



    /**
     * save job summary and artifact
     *
     * @param jobSummary
     * @param publicationId
     * @param artifactType
     * @return a flux of void
     */
    public Flux<Void> saveJobAndArtifact(final JobSummary jobSummary,
                                         final UUID publicationId,
                                         final ArtifactType artifactType) {
        affirmNotNull(jobSummary, "jobSummary is required");
        affirmNotNull(publicationId, "publicationId is required");
        affirmNotNull(artifactType, "artifactType is required");

        return jobGateway.persist(jobSummary, publicationId, artifactType);
    }

    /**
     * save job summary and mapping publication
     *
     * @param jobSummary
     * @param publicationId
     * @return a flux of void
     */
    public Flux<Void> saveJobAndPublication(final JobSummary jobSummary,
                                            final UUID publicationId) {
        affirmNotNull(jobSummary, "jobSummary is required");
        affirmNotNull(publicationId, "publicationId is required");

        return jobGateway.persist(jobSummary, publicationId);
    }

    /**
     * fetch the publication by export
     * @param exportId the export id.
     * @return flux of publication by export
     */
    public Mono<PublicationByExport> fetchPublicationByExport(UUID exportId) {
        affirmNotNull(exportId, "exportId is required");

        return publicationGateway.fetchPublicationByExport(exportId).singleOrEmpty();
    }

    /**
     * Fetches the oculus review and live status of a bookId and returns the status of higher version.
     * @param bookId
     * @return PublicationOculusData
     */
    public PublicationOculusData getOculusStatus(String bookId) {
        PublicationOculusData oculusReviewStatus = publicationBroker.getOculusStatus(bookId+"-REV").block();
        PublicationOculusData oculusLiveStatus = publicationBroker.getOculusStatus(bookId).block();

        if(oculusReviewStatus != null && oculusLiveStatus != null) {
            if (oculusReviewStatus.getOculusStatus() != null && oculusLiveStatus.getOculusStatus() == null) {
                return oculusReviewStatus;
            } else if (oculusReviewStatus.getOculusStatus() == null && oculusLiveStatus.getOculusStatus() != null) {
                return oculusLiveStatus;
            } else if(oculusReviewStatus.getOculusVersion() != null && oculusLiveStatus.getOculusVersion() != null){
                if (Integer.parseInt(oculusReviewStatus.getOculusVersion().substring(1)) >
                        Integer.parseInt(oculusLiveStatus.getOculusVersion().substring(1))) {
                    return oculusReviewStatus;
                } else {
                    return oculusLiveStatus;
                }
            }
        }
        return oculusReviewStatus;
    }

    /**
     * Fetches a list of activities
     *
     * @return a flux of P {@link PublicationActivityPayload}
     */
    public Flux<PublicationActivityPayload> fetchPublishedActivities() {

        return publicationGateway.fetchPublishedActivities()
                .flatMap(publishedActivity ->
                        publicationGateway.fetchMetadataByPublishedActivity(publishedActivity.getActivityId(), publishedActivity.getVersion())
                        .flatMap(metadata -> accountService.getAccountPayload(metadata.getCreatedBy())
                                .defaultIfEmpty(new AccountPayload())
                                .flux()
                                .flatMap(accountPayload -> {
                                    // Legacy implementation did not persist output type, so for now, if null we will assume
                                    // ePUB for eText; if we backfill this data at a later date, we can then remove this code
                                    PublicationOutputType publicationOutputType = PublicationOutputType.EPUB_ETEXT;
                                    if (publishedActivity.getOutputType() != null) {
                                        publicationOutputType = publishedActivity.getOutputType();
                                    }

                                    return Flux.just(new PublicationActivityPayload()
                                            .setActivityId(metadata.getActivityId())
                                            .setTitle(publishedActivity.getTitle())
                                            .setPublishedBy(metadata.getCreatedBy())
                                            .setPublisherGivenName(accountPayload.getGivenName())
                                            .setPublisherFamilyName(accountPayload.getFamilyName())
                                            .setAuthor(metadata.getAuthor())
                                            .setUpdatedAt(metadata.getCreatedAt())
                                            .setOutputType(publicationOutputType));
                                })
                        )
        );
    }

    /**
     * Deletes publication history entry by publication id, activityId, version
     * @param publicationId publication id
     * @param activityId activity id
     * @param version version
     * @return flux void
     */
    @Trace(async = true)
    public Flux<Void> deletePublicationHistory(final UUID publicationId, final  UUID activityId, final String version) {
        return publicationGateway.updateActivityPublicationStatus(publicationId, ActivityPublicationStatus.DELETED, activityId, version)
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
    }

    /**
     * Fetches a list of publication for the activity
     *
     * @return a flux of P {@link PublicationPayload}
     */
    public Flux<PublicationPayload> fetchPublicationForActivity(final UUID activityId) {
        return fetchPublicationForActivity(activityId, null);
    }

    public Flux<PublicationPayload> fetchPublicationForActivity(final UUID activityId,
                                                                final PublicationOutputType outputType) {

        return publicationGateway.fetchPublishedActivity(activityId)
                .filter(publishedActivity -> publishedActivity.getStatus().equals(ActivityPublicationStatus.ACTIVE))
                .flatMap(activity -> {
                    final Flux<PublicationMetadata> metadataFlux =
                            publicationGateway.fetchMetadataByPublication(activity.getPublicationId());
                    final Flux<PublicationSummary> publicationSummaryFlux =
                            publicationGateway.fetchPublicationSummary(activity.getPublicationId());

                    return Flux.zip(publicationSummaryFlux, metadataFlux)
                            .flatMap(tuple3 -> {
                                PublicationSummary summary = tuple3.getT1();
                                PublicationMetadata metadata = tuple3.getT2();

                                // filter based on publication output type (optional)
                                if (outputType != null && outputType != summary.getOutputType()) {
                                    return Mono.empty();
                                }
                                return accountService.getAccountPayload(metadata.getCreatedBy())
                                        .defaultIfEmpty(new AccountPayload())
                                        .flux()
                                        .flatMap(accountPayload ->  Flux.just(new PublicationPayload().setPublicationId(summary.getId())
                                                                         .setTitle(summary.getTitle())
                                                                         .setDescription(summary.getDescription())
                                                                         .setConfig(summary.getConfig())
                                                                         .setAuthor(metadata.getAuthor())
                                                                         .setEtextVersion(metadata.getEtextVersion())
                                                                         .setBookId(metadata.getBookId())
                                                                         .setPublishedBy(metadata.getCreatedBy())
                                                                         .setPublisherFamilyName(accountPayload.getFamilyName())
                                                                         .setPublisherGivenName(accountPayload.getGivenName())
                                                                         .setUpdatedAt(metadata.getCreatedAt())
                                                                         .setActivityId(activity.getActivityId())
                                                                         .setVersion(activity.getVersion())))
                                        .flatMap(publicationPayload -> jobGateway.fetchJobByPublication(publicationPayload.getPublicationId())
                                                .defaultIfEmpty(new JobByPublication().setPublicationId(UUIDs.timeBased()).setJobId(UUIDs.timeBased()))
                                                .flatMap(jobByPublication -> jobGateway.fetchJobSummary(jobByPublication.getJobId()))
                                                .defaultIfEmpty(new JobSummary().setId(null).setJobType(null))
                                                .flatMap(jobSummary -> {
                                                    LocalDateTime now = LocalDateTime.now();
                                                    LocalDateTime publishedTime = new LocalDateTime(UUIDs.unixTimestamp(metadata.getCreatedAt()));
                                                    long diff = Duration.between(publishedTime.toDate().toInstant(), now.toDate().toInstant()).toHours();
                                                    if(diff >= 12 && (jobSummary.getStatus() != null && jobSummary.getStatus().equals(JobStatus.STARTED))) {
                                                        jobSummary.setStatus(JobStatus.FAILED);
                                                        jobSummary.setStatusDesc("10003");
                                                        return jobGateway.persist(jobSummary, publicationPayload.getPublicationId())
                                                                .then(addJobSummaryWithPublicationPayload(publicationPayload, jobSummary));
                                                    }
                                                    return addJobSummaryWithPublicationPayload(publicationPayload, jobSummary);

                                                }));
                            });
                });
    }

    private Mono<PublicationPayload> addJobSummaryWithPublicationPayload(final PublicationPayload publicationPayload, final JobSummary jobSummary) {
        return Mono.just(
                publicationPayload.setJobId(jobSummary.getId())
                        .setPublicationJobStatus(jobSummary.getStatus())
                        .setStatusMessage(jobSummary.getStatusDesc()));
    }

    /**
     * update title for the activity ID
     * @param activityId activity ID published
     * @param title updated title
     * @return
     */
    public Mono<Void> updateTitle(final UUID activityId, final String title, final String version) {
        return publicationGateway.updateTitle(activityId, title, version);
    }
}
