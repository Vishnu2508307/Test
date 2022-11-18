package com.smartsparrow.ingestion.service;

import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_AMBROSIA_REQUEST;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.net.URL;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.ingestion.data.IngestionEvent;
import com.smartsparrow.ingestion.data.IngestionGateway;
import com.smartsparrow.ingestion.data.IngestionPayload;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.data.IngestionSummaryPayload;
import com.smartsparrow.ingestion.eventmessage.IngestionSummaryEventMessage;
import com.smartsparrow.ingestion.wiring.IngestionConfig;
import com.smartsparrow.service.S3ClientService;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class IngestionService {

    private static final String KEY_CONTENT = "/content/";
    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(IngestionService.class);

    private final IngestionGateway ingestionGateway;
    private final CamelReactiveStreamsService camelReactiveStreamsService;
    private final S3ClientService s3ClientService;
    private final IngestionConfig ingestionConfig;
    private final AccountService accountService;

    @Inject
    public IngestionService(IngestionGateway ingestionGateway,
                            final CamelReactiveStreamsService camelReactiveStreamsService,
                            final S3ClientService s3ClientService,
                            final AccountService accountService,
                            IngestionConfig ingestionConfig) {
        this.ingestionGateway = ingestionGateway;
        this.camelReactiveStreamsService = camelReactiveStreamsService;
        this.s3ClientService = s3ClientService;
        this.ingestionConfig = ingestionConfig;
        this.accountService = accountService;
    }

    /**
     * Persisting ingestion summary
     *
     * @param projectId the project the ingestion will belong to
     * @param workspaceId the workspace the ingestion will belong to
     * @param configFields the config for the ingestion
     * @param creatorId the user creating the ingestion
     * @param url the url location of the ambrosia being ingested
     * @param ingestStats ingestion statistics
     * @param courseName the name of the course being ingested
     *
     * @return a Mono of the ingestion summary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> create(final UUID projectId,
                                         final UUID workspaceId,
                                         final String configFields,
                                         final UUID creatorId,
                                         final String url,
                                         final String ingestStats,
                                         final String courseName,
                                         final UUID rootElementId) {

        UUID id = UUIDs.timeBased();

        IngestionSummary ingestionSummary = new IngestionSummary()
                .setId(id)
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)
                .setAmbrosiaUrl(url)
                .setConfigFields(configFields)
                .setCourseName(courseName)
                .setCreatorId(creatorId)
                .setStatus(IngestionStatus.UPLOADING)
                .setIngestionStats(ingestStats)
                .setRootElementId(rootElementId);

        logger.info("Ingestion summary" + ingestionSummary.toString());

        return ingestionGateway.findSummaryByName(courseName, projectId, rootElementId)
                .count()
                .flatMap(size -> {
                    //Inside course for lesson or unit injection name can be duplicated
                    if (size > 0 && rootElementId == null) {
                        return Mono.error(new ConflictFault("Course with that name already exists"));
                    }
                    return Mono.just(ingestionSummary);
                })
                .thenMany(ingestionGateway.persist(ingestionSummary))
                .then(Mono.just(ingestionSummary))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Send the ingestionrequest over SQS
     *
     * @param ingestionSummary the summary of the ingestion
     *
     * @param queueName
     * @param bearerToken
     * @return a Mono of the ingestion summary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> publishToSQS(final IngestionSummary ingestionSummary,
                                               final String queueName,
                                               final String bearerToken) {

        logger.info("ingestionSummary " + ingestionSummary);
        return Mono.just(new IngestionSummaryEventMessage(bearerToken, ingestionSummary))
                .map(event -> camelReactiveStreamsService.toStream(queueName, event, IngestionSummaryEventMessage.class))
                .doOnEach(logger.reactiveInfo("published message to SQS successfully"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(ex -> {
                    ex = Exceptions.unwrap(ex);
                    logger.reactiveError("exception " + ex.getMessage());
                })
                .then(Mono.just(ingestionSummary));
    }

    /**
     * Find an ingestion summary by id
     *
     * @param ingestionId the ingestion id to find the ingestion summary for
     * @return a mono of export summary or an empty mono when not found
     */
    @Trace(async = true)
    public Mono<IngestionSummary> findById(final UUID ingestionId) {
        affirmArgument(ingestionId != null, "ingestionId is required");
        return ingestionGateway.findIngestionSummary(ingestionId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find an ingestion summary payload by id
     *
     * @param ingestionId the ingestion id to find the ingestion summary for
     * @return a mono of export summary or an empty mono when not found
     */
    @Trace(async = true)
    public Mono<IngestionSummaryPayload> getIngestionPayload(final UUID ingestionId) {
        affirmArgument(ingestionId != null, "ingestionId is required");
        return findById(ingestionId)
                .flatMap(summary -> accountService.getAccountPayload(summary.getCreatorId())
                .flatMap(account -> {
                    IngestionSummaryPayload payload = new IngestionSummaryPayload()
                            .setId(summary.getId())
                            .setProjectId(summary.getProjectId())
                            .setWorkspaceId(summary.getWorkspaceId())
                            .setCourseName(summary.getCourseName())
                            .setConfigFields(summary.getConfigFields())
                            .setCreator(account)
                            .setAmbrosiaUrl(summary.getAmbrosiaUrl())
                            .setStatus(summary.getStatus())
                            .setIngestionStats(summary.getIngestionStats())
                            .setRootElementId(summary.getRootElementId())
                            .setActivityId(summary.getActivityId());
                    return Mono.just(payload);
                }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches a list of ingestion summaries for a project
     *
     * @param projectId the project id
     * @return a flux of IngestionSummaryPayload {@link IngestionSummaryPayload}
     */
    @Trace(async = true)
    public Flux<IngestionSummaryPayload> fetchIngestionsForProject(final UUID projectId) {
        affirmArgument(projectId != null, "projectId is required");
        return ingestionGateway.findSummaryByProject(projectId)
                .flatMap(summary -> accountService.getAccountPayload(summary.getCreatorId())
                .flatMap(account -> {
                    IngestionSummaryPayload payload = new IngestionSummaryPayload()
                            .setId(summary.getId())
                            .setProjectId(summary.getProjectId())
                            .setWorkspaceId(summary.getWorkspaceId())
                            .setCourseName(summary.getCourseName())
                            .setConfigFields(summary.getConfigFields())
                            .setCreator(account)
                            .setAmbrosiaUrl(summary.getAmbrosiaUrl())
                            .setStatus(summary.getStatus())
                            .setIngestionStats(summary.getIngestionStats())
                            .setRootElementId(summary.getRootElementId())
                            .setActivityId(summary.getActivityId());
                    return Mono.just(payload);
                }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches a list of log events for an ingestion
     *
     * @param ingestionId the ingestion id
     * @return a flux of IngestionEvent {@link IngestionEvent}
     */
    @Trace(async = true)
    public Flux<IngestionEvent> fetchLogEventsForIngestion(final UUID ingestionId) {
        affirmArgument(ingestionId != null, "ingestionId is required");
        return ingestionGateway.findEventsByIngestion(ingestionId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update the ingestion summary status
     *
     * @param ingestionId the ingestion id
     * @param projectId the project id
     * @param status   status of the ingestion
     * @return mono of IngestionSummary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> updateIngestionStatus(final UUID ingestionId,
                                                        final UUID projectId,
                                                        final IngestionStatus status) {
        affirmArgument(ingestionId != null, "ingestionId is required");
        affirmArgument(projectId != null, "projectId is required");
        affirmArgument(status != null, "status is required");

        return ingestionGateway.updateIngestionStatus(new IngestionSummary()
                                                              .setId(ingestionId)
                                                              .setProjectId(projectId)
                                                              .setStatus(status))
                .then(findById(ingestionId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Update the ingestion summary status
     * This function is only intended to be used in cases when we absolutely can't provide the projectId.
     *
     * @param ingestionId the ingestion id
     * @param status   status of the ingestion
     * @return mono of IngestionSummary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> updateIngestionStatus(final UUID ingestionId,
                                                        final IngestionStatus status) {
        affirmArgument(ingestionId != null, "ingestionId is required");
        affirmArgument(status != null, "status is required");

        // This does a read before write out of necessity. Avoid using this function.
        return findById(ingestionId)
               .flatMap(summary -> {
                   summary.setStatus(status);
                   ingestionGateway.updateIngestionStatus(summary);
                   return Mono.just(summary);
               })
               .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update the ingestion summary result notifications from sns
     *
     * @param ingestionSummary ingestion summary details
     * @return mono of IngestionSummary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> updateIngestionSummary(final IngestionSummary ingestionSummary) {
        affirmArgument(ingestionSummary.getId() != null, "ingestionId is required");
        affirmArgument(ingestionSummary.getProjectId() != null, "projectId is required");
        affirmArgument(ingestionSummary.getStatus() != null, "status is required");
        affirmArgument(ingestionSummary.getAmbrosiaUrl() != null, "ambrosiaUrl is required");
        return ingestionGateway.updateIngestionSummary(ingestionSummary)
                .then(findById(ingestionSummary.getId()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Process an ingestion summary notification.
     *
     * @param ingestionId the ingestionId string
     * @return a mono of the supplied IngestionSummary argument
     */
    @Trace(async = true)
    public Mono<IngestionSummary> processS3UploadEvent(final UUID ingestionId) {
        affirmNotNull(ingestionId, "ingestionId is required");

        return updateIngestionStatus(ingestionId, IngestionStatus.UPLOADED)
                .doOnEach(logger.reactiveInfo(String.format("updating project ingestion [%s] to [%s]",
                        ingestionId, IngestionStatus.UPLOADED)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Process an ingestion summary notification.
     *
     * @param ingestionSummary the ingestion summary
     * @return a mono of the supplied IngestionSummary argument
     */
    @Trace(async = true)
    public Mono<IngestionSummary> processResultNotification(final IngestionSummary ingestionSummary, final String bearerToken) {
        affirmNotNull(ingestionSummary, "ingestionSummary is required");
        affirmNotNull(bearerToken, "bearerToken is required");

        return ingestionGateway.findIngestionSummary(ingestionSummary.getId()).single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault("Ingestion summary not found");
                }).then(updateIngestionSummary(ingestionSummary.getId(),
                                               ingestionSummary.getProjectId(),
                                               ingestionSummary.getStatus(),
                                               ingestionSummary.getAmbrosiaUrl(),
                                               ingestionSummary.getIngestionStats(),
                                               ingestionSummary.getRootElementId(),
                                               ingestionSummary.getActivityId())
                                .flatMap(ingestion -> publishToSQS(ingestion, SUBMIT_INGESTION_AMBROSIA_REQUEST, bearerToken))
                                .doOnEach(logger.reactiveInfo(String.format("updating project ingestion [%s] to [%s]",
                                                                            ingestionSummary.getId(), ingestionSummary.getStatus()))))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist and process the ingestionSummary
     *
     * @param ingestionSummary the ingestion summary
     * @return a mono of the supplied IngestionSummary argument
     */
    @Trace(async = true)
    public Mono<IngestionSummary> processErrorNotification(final IngestionSummary ingestionSummary) {
        affirmNotNull(ingestionSummary, "ingestionSummary is required");
        //Since this method is for error, by default status is set as FAILED
        ingestionSummary.setStatus(IngestionStatus.FAILED);
        return ingestionGateway.findIngestionSummary(ingestionSummary.getId()).single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault("Ingestion summary not found");
                }).then(updateIngestionSummary(ingestionSummary)
                                .doOnEach(logger.reactiveInfo(String.format("updating project ingestion [%s] to failed",
                        ingestionSummary.getId()))))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist and process the ingestionSummary with ambrosiaUrl.
     *
     * @param ingestionSummary the ingestion summary
     * @return a mono of the supplied IngestionSummary argument
     */
    @Trace(async = true)
    public Mono<IngestionSummary> processAmbrosiaResultNotification(final IngestionSummary ingestionSummary) {
        affirmNotNull(ingestionSummary, "ingestionSummary is required");

        return ingestionGateway.findIngestionSummary(ingestionSummary.getId()).single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault("Ingestion summary not found");
                }).then(updateIngestionSummary(ingestionSummary.getId(),
                                               ingestionSummary.getProjectId(),
                                               ingestionSummary.getStatus(),
                                               ingestionSummary.getAmbrosiaUrl(),
                                               ingestionSummary.getIngestionStats(),
                                               ingestionSummary.getRootElementId(),
                                               ingestionSummary.getActivityId()))
                                .doOnEach(logger.reactiveInfo(String.format(
                                        "updating project ingestion [%s] to [%s] with ambrosiaUrl [%s]",
                                        ingestionSummary.getId(),
                                        ingestionSummary.getStatus(),
                                        ingestionSummary.getAmbrosiaUrl())))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Persist and process the ingestionEvent.
     *
     * @param ingestionEvent the ingestion event
     * @return a mono of the supplied IngestionEvent argument
     */
    @Trace(async = true)
    public Mono<IngestionEvent> processEventLogResultNotification(final IngestionEvent ingestionEvent) {
        affirmNotNull(ingestionEvent, "ingestionEvent is required");

        return ingestionGateway.persist(ingestionEvent)
                .then(Mono.just(ingestionEvent))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create a signed url for upload to an s3 bucket.
     *
     * @param ingestionId the ingestion id
     * @param fileName the name of the file in S3
     * @return a mono of the supplied IngestionId and the signed url
     */
    @Trace(async = true)
    public Mono<IngestionPayload> createSignedUrl(final UUID ingestionId, final String fileName) {
        affirmNotNull(ingestionId, "ingestionId is required");
        affirmNotNull(fileName, "fileName is required");

        URL signedUrl = s3ClientService.signUrl(ingestionConfig.getBucketName(), ingestionId.toString() + KEY_CONTENT + fileName);

        return Mono.just(new IngestionPayload().setIngestionId(ingestionId).setSignedUrl(signedUrl));
    }

    /**
     * Delete an ingestion from the database
     *
     * @param ingestionId the ingestion id
     * @return a mono of the deleted ingestion summary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> deleteById(final UUID ingestionId) {
        affirmNotNull(ingestionId, "ingestionId is required");

        return findById(ingestionId)
                .flatMap(summary -> ingestionGateway.delete(summary).then(Mono.just(summary)));
    }

    /**
     * Fetches a list of ingestion summaries for a project by Root element
     *
     * @param rootElementId the project id
     * @return a flux of IngestionSummaryPayload {@link IngestionSummaryPayload}
     */
    @Trace(async = true)
    public Flux<IngestionSummaryPayload> fetchIngestionForProjectByRootElement(final UUID rootElementId) {
        affirmArgument(rootElementId != null, "rootElementId is required");
        return ingestionGateway.findSummaryByRootElement(rootElementId)
                .flatMap(summary -> accountService.getAccountPayload(summary.getCreatorId())
                        .flatMap(account -> {
                            IngestionSummaryPayload payload = new IngestionSummaryPayload()
                                    .setId(summary.getId())
                                    .setProjectId(summary.getProjectId())
                                    .setWorkspaceId(summary.getWorkspaceId())
                                    .setCourseName(summary.getCourseName())
                                    .setConfigFields(summary.getConfigFields())
                                    .setCreator(account)
                                    .setAmbrosiaUrl(summary.getAmbrosiaUrl())
                                    .setStatus(summary.getStatus())
                                    .setIngestionStats(summary.getIngestionStats())
                                    .setRootElementId(summary.getRootElementId())
                                    .setActivityId(summary.getActivityId());
                            return Mono.just(payload);
                        }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update the ingestion summary result notifications from sns
     *
     * @param ingestionId the ingestion id
     * @param projectId the project id
     * @param status   status of the ingestion
     * @param ambrosiaUrl the url to the ambrosia course
     * @param ingestStats statistics for the ingestion
     * @param rootElementId the root element id
     * @return mono of IngestionSummary
     */
    @Trace(async = true)
    public Mono<IngestionSummary> updateIngestionSummary(final UUID ingestionId,
                                                         final UUID projectId,
                                                         final IngestionStatus status,
                                                         final String ambrosiaUrl,
                                                         final String ingestStats,
                                                         final UUID rootElementId,
                                                         final UUID activityId) {
        affirmArgument(ingestionId != null, "ingestionId is required");
        affirmArgument(projectId != null, "projectId is required");
        affirmArgument(status != null, "status is required");
        affirmArgument(ambrosiaUrl != null, "ambrosiaUrl is required");
        return ingestionGateway.updateIngestionSummary(new IngestionSummary()
                                                               .setId(ingestionId)
                                                               .setProjectId(projectId)
                                                               .setStatus(status)
                                                               .setAmbrosiaUrl(ambrosiaUrl)
                                                               .setIngestionStats(ingestStats)
                                                               .setRootElementId(rootElementId)
                                                               .setActivityId(activityId))
                .then(findById(ingestionId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find an ingestion summary by root element id
     *
     * @param rootElementId to find the ingestion summaries for
     * @return a flux of export summary or an empty mono when not found
     */
    @Trace(async = true)
    public Flux<IngestionSummary> findIngestionSummaryByRootElementId(final UUID rootElementId) {
        affirmArgument(rootElementId != null, "rootElementId is required");
        return ingestionGateway.findSummaryByRootElement(rootElementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
