package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.publication.data.*;
import com.smartsparrow.publication.job.enums.ArtifactType;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.UUID;

public class JobGateway {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationGateway.class);

    private final Session session;
    private final JobSummaryMutator jobSummaryMutator;
    private final JobSummaryMaterializer jobSummaryMaterializer;
    private final ArtifactMutator artifactMutator;
    private final ArtifactMaterializer artifactMaterializer;
    private final ArtifactContentMutator artifactContentMutator;
    private final ArtifactContentMaterializer artifactContentMaterializer;
    private final NotificationMutator notificationMutator;
    private final NotificationMaterializer notificationMaterializer;
    private final NotificationByJobMutator notificationByJobMutator;
    private final NotificationByJobMaterializer notificationByJobMaterializer;
    private final JobByPublicationMutator jobByPublicationMutator;
    private final JobByPublicationMaterializer jobByPublicationMaterializer;

    @Inject
    public JobGateway(final Session session,
                      final JobSummaryMutator jobSummaryMutator,
                      final JobSummaryMaterializer jobSummaryMaterializer,
                      final ArtifactMutator artifactMutator,
                      final ArtifactMaterializer artifactMaterializer,
                      final ArtifactContentMutator artifactContentMutator,
                      final ArtifactContentMaterializer artifactContentMaterializer,
                      final NotificationMutator notificationMutator,
                      final NotificationMaterializer notificationMaterializer,
                      final NotificationByJobMutator notificationByJobMutator,
                      final NotificationByJobMaterializer notificationByJobMaterializer,
                      final JobByPublicationMutator jobByPublicationMutator,
                      final JobByPublicationMaterializer jobByPublicationMaterializer) {
        this.session = session;
        this.jobSummaryMutator = jobSummaryMutator;
        this.jobSummaryMaterializer = jobSummaryMaterializer;
        this.artifactMutator = artifactMutator;
        this.artifactMaterializer = artifactMaterializer;
        this.artifactContentMutator = artifactContentMutator;
        this.artifactContentMaterializer = artifactContentMaterializer;
        this.notificationMutator = notificationMutator;
        this.notificationMaterializer = notificationMaterializer;
        this.notificationByJobMutator = notificationByJobMutator;
        this.notificationByJobMaterializer = notificationByJobMaterializer;
        this.jobByPublicationMutator = jobByPublicationMutator;
        this.jobByPublicationMaterializer = jobByPublicationMaterializer;
    }

    /**
     * Save the job summary and related artifact
     *
     * @param jobSummary  the job summary
     */
    public Flux<Void> persist(JobSummary jobSummary, UUID publicationId, ArtifactType artifactType) {
        UUID artifactId = UUIDs.timeBased();
        UUID artifactContentId = UUIDs.timeBased();
        return Mutators.execute(session, Flux.just(
                jobSummaryMutator.upsert(jobSummary),
                artifactMutator.upsert(new Artifact().setArtifactType(artifactType)
                        .setId(artifactId)
                        .setJobId(jobSummary.getId())),
                artifactContentMutator.upsert(new ArtifactContent().setId(artifactContentId)
                        .setArtifactId(artifactId)),
                jobByPublicationMutator.upsert(new JobByPublication().setJobId(jobSummary.getId())
                        .setPublicationId(publicationId))
        )).doOnError(throwable -> {
            log.error(String.format("error while saving job summary %s",
                    jobSummary), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Save/Update the job summary
     *
     * @param jobSummary  the job summary
     * @param publicationId  the publication id
     */
    public Flux<Void> persist(JobSummary jobSummary, UUID publicationId) {
        return Mutators.execute(session, Flux.just(
                jobSummaryMutator.upsert(jobSummary),
                jobByPublicationMutator.upsert(new JobByPublication().setJobId(jobSummary.getId())
                        .setPublicationId(publicationId))
        )).doOnError(throwable -> {
            log.error(String.format("error while saving job summary %s",
                    jobSummary), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetch the job summary with id
     *
     * @return mono of the job summary
     */
    public Mono<JobSummary> fetchJobSummary(UUID jobId) {
        return ResultSets.query(session, jobSummaryMaterializer.findById(jobId))
                .flatMapIterable(row -> row)
                .map(jobSummaryMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch the artifact with job_id
     *
     * @return mono of the job summary
     */
    public Mono<Artifact> fetchArtifact(UUID jobId) {
        return ResultSets.query(session, artifactMaterializer.findById(jobId))
                .flatMapIterable(row -> row)
                .map(artifactMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch the artifact content with artifact_id
     *
     * @return mono of the artifact content
     */
    public Mono<ArtifactContent> fetchArtifactContent(UUID artifactId) {
        return ResultSets.query(session, artifactContentMaterializer.findById(artifactId))
                .flatMapIterable(row -> row)
                .map(artifactContentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Save the notification & notificationByJob
     *
     * @param notification  the notification
     */
    public Flux<Void> persist(Notification notification, UUID jobId) {
        return Mutators.execute(session, Flux.just(
                notificationMutator.upsert(notification),
                notificationByJobMutator.upsert(new NotificationByJob()
                        .setJobId(jobId).setNotificationId(notification.getId()))
        )).doOnError(throwable -> {
            log.error(String.format("error while saving notification %s",
                    notification), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetch the notification with notification id
     *
     * @return mono of the notification
     */
    public Mono<Notification> fetchNotification(UUID notificationId) {
        return ResultSets.query(session, notificationMaterializer.findById(notificationId))
                .flatMapIterable(row -> row)
                .map(notificationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch the notification by job with job id
     *
     * @return mono of the NotificationByJob
     */
    public Mono<NotificationByJob> fetchNotificationByJob(UUID jobId) {
        return ResultSets.query(session, notificationByJobMaterializer.findById(jobId))
                .flatMapIterable(row -> row)
                .map(notificationByJobMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch the JobByPublication with publication id
     *
     * @return mono of the JobByPublication
     */
    public Mono<JobByPublication> fetchJobByPublication(UUID publicationId) {
        return ResultSets.query(session, jobByPublicationMaterializer.findById(publicationId))
                .flatMapIterable(row -> row)
                .map(jobByPublicationMaterializer::fromRow)
                .singleOrEmpty();
    }
}
