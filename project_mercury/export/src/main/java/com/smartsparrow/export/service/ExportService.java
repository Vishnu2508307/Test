package com.smartsparrow.export.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareElementStructureService;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.export.data.AmbrosiaReducerErrorLog;
import com.smartsparrow.export.data.ExportAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.export.data.ExportErrorPayload;
import com.smartsparrow.export.data.ExportGateway;
import com.smartsparrow.export.data.ExportRequest;
import com.smartsparrow.export.data.ExportRequestNotification;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.data.ExportRetryNotification;
import com.smartsparrow.export.data.ExportStatus;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.export.route.CoursewareExportRoute;
import com.smartsparrow.export.wiring.ExportConfig;
import com.smartsparrow.export.wiring.Operations;
import com.smartsparrow.export.wiring.SnippetsStorage;
import com.smartsparrow.service.S3ClientService;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Singleton
public class ExportService {

    private static final String EMPTY_AMBROSIA_URL = "";

    // todo: make the thread pool size configurable in Database
    public static final ThreadPoolExecutor fixedThreadPoolBlockingS3Read = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            5 * Runtime.getRuntime().availableProcessors());

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportService.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ExportGateway exportGateway;
    private final CoursewareElementStructureService coursewareElementStructureService;
    private final AmbrosiaSnippetsReducer ambrosiaSnippetsReducer;
    private final ExportTrackService exportTrackService;
    private final S3ClientService s3ClientService;
    private final ExportConfig exportConfig;
    private final CamelReactiveStreamsService camelReactiveStreams;

    // Annotated with @Operations in the constructor to ensure we get a client for the right redis nodes
    private final RedissonReactiveClient redis;

    @Inject
    public ExportService(final ExportGateway exportGateway,
                         final CoursewareElementStructureService coursewareElementStructureService,
                         final AmbrosiaSnippetsReducer ambrosiaSnippetsReducer,
                         final ExportTrackService exportTrackService,
                         final S3ClientService s3ClientService,
                         final Provider<ExportConfig> exportConfigProvider,
                         final CamelReactiveStreamsService camelReactiveStreams,
                         final @Operations RedissonReactiveClient redis) {
        this.exportGateway = exportGateway;
        this.coursewareElementStructureService = coursewareElementStructureService;
        this.ambrosiaSnippetsReducer = ambrosiaSnippetsReducer;
        this.exportTrackService = exportTrackService;
        this.s3ClientService = s3ClientService;
        this.exportConfig = exportConfigProvider.get();
        this.camelReactiveStreams = camelReactiveStreams;
        this.redis = redis;
    }

    /**
     * Fetch courseware structure from provided element and submit a request for processing for courseware export for each element
     *
     * @param request export request notification object
     * @param exportType export type
     * @return mono of export result notification. The notification will always have a status of {@link ExportStatus#IN_PROGRESS} due to the
     *         async nature of this call. The notification is sent but the code does not wait for a reply and returns immediately.
     *         The BL should rely on the export subscription to get updates
     */
    @Trace(async = true)
    public Flux<ExportResultNotification> submit(final ExportRequest request, final ExportType exportType) {
        // create the export summary
        return createExportSummary(request, exportType)
                // fetch the complete courseware structure as a tree
                .thenMany(coursewareElementStructureService
                                  .getCoursewareElementStructure(request.getElementId(),
                                                                 request.getElementType(),
                                                                 Collections.emptyList())
                                  // traverse the tree
                                  .expand(coursewareElementNodes -> Flux.fromIterable(coursewareElementNodes.getChildren())
                                          // courseware are never too tall but can be very wide, process this in parallel
                                          .parallel()
                                          .runOn(Schedulers.parallel())
                                          .flatMap(Mono::just))
                                  .flatMap(coursewareElementNode -> sendNotificationToSNS(request,
                                                                                          coursewareElementNode))
                                  .doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Persist and process the result derived from a snippet being saved and shortcutting the sns notification,
     *
     * @param exportAmbrosiaSnippet the result notification
     * @return mono of ExportResultNotification
     */
    public Mono<ExportResultNotification> processResultSnippet(final ExportAmbrosiaSnippet exportAmbrosiaSnippet) {
        affirmNotNull(exportAmbrosiaSnippet, "exportAmbrosiaSnippet is required");

        return exportTrackService.remove(exportAmbrosiaSnippet.getNotificationId(), exportAmbrosiaSnippet.getExportId())
                .then(updateResultNotification(exportAmbrosiaSnippet.getNotificationId(), ExportStatus.COMPLETED))
                .doOnEach(log.reactiveInfo(String.format("updating export notification [%s] to completed for exportId [%s]",
                                                         exportAmbrosiaSnippet.getNotificationId(), exportAmbrosiaSnippet.getExportId())));
    }

    /**
     * Process an error notification. These are errors which are caught within the external processing.
     *
     * @param errorNotification the export error notification
     * @return a Mono of the supplied ExportErrorNotification argument
     */
    public Mono<ExportResultNotification> processErrorNotification(final ExportErrorNotification errorNotification) {
        affirmNotNull(errorNotification, "errorNotification is required");

        final ExportErrorNotification newErrorNotification = new ExportErrorNotification()
                .setErrorMessage(errorNotification.getErrorMessage())
                .setCause(errorNotification.getCause())
                .setNotificationId(errorNotification.getNotificationId())
                .setExportId(errorNotification.getExportId());
        return exportGateway.persist(newErrorNotification)
                .then(exportTrackService.remove(errorNotification.getNotificationId(), errorNotification.getExportId()))
                .then(updateResultNotification(newErrorNotification.getNotificationId(), ExportStatus.FAILED))
                .doOnEach(log.reactiveInfo(String.format("updating export notification [%s] to failed for exportId [%s]",
                                                         newErrorNotification.getNotificationId(), newErrorNotification.getExportId())));
    }

    /**
     * Process a retry notification, brokers to the proper handler based on the purpose.
     *
     * @param retryNotification the notification
     * @return a Mono of the supplied RetryNotification argument
     */
    public Mono<ExportResultNotification> processRetryNotification(final ExportRetryNotification retryNotification) {
        affirmNotNull(retryNotification, "retryNotification is required");
        return updateResultNotification(retryNotification.getNotificationId(), ExportStatus.RETRY_RECEIVED)
                .doOnEach(log.reactiveInfo(String.format("updating export notification [%s] to retry received", retryNotification.getNotificationId())));
    }

    /**
     * Process a submit dead letters notification, brokers to the proper handler based on the purpose.
     *
     * @param requestNotification the notification
     * @param messagePayload      the sns payload message
     * @return a Mono of the supplied RetryNotification argument
     */
    public Mono<ExportResultNotification> processSubmitDeadLetters(final ExportRequestNotification requestNotification,
                                                                   final String messagePayload) {
        affirmNotNull(requestNotification, "requestNotification is required");
        affirmNotNull(messagePayload, "messagePayload is required");
        ExportErrorNotification errorNotification = new ExportErrorNotification()
                .setNotificationId(requestNotification.getNotificationId())
                .setErrorMessage("submit dead-letters")
                .setCause("unknown")
                .setExportId(requestNotification.getExportId());
        return processErrorNotification(errorNotification);
    }

    /**
     * Process a retry dead letter notification, brokers to the proper handler based on the purpose.
     *
     * @param retryNotification the notification
     * @param messagePayload    the sns payload message
     * @return a Mono of the supplied RetryNotification argument
     */
    public Mono<ExportResultNotification> processRetryDeadLetters(final ExportRetryNotification retryNotification,
                                                                  final String messagePayload) {
        affirmNotNull(retryNotification, "retryNotification is required");
        affirmNotNull(messagePayload, "messagePayload is required");
        ExportErrorNotification errorNotification = new ExportErrorNotification()
                .setNotificationId(retryNotification.getNotificationId())
                .setErrorMessage("retry dead-letters")
                .setCause("unknown");
        return exportGateway.fetchExportResult(retryNotification.getNotificationId())
                .single()
                .flatMap(exportResult -> {
                    errorNotification.setExportId(exportResult.getExportId());
                    return processErrorNotification(errorNotification);
                });
    }

    /**
     * Log an event step against this export result notification
     *
     * @param exportRequest the export request object
     * @param exportStatus  the status of export
     * @return return flux of void
     */
    @Trace(async = true)
    private Flux<ExportResultNotification> createExportResultNotification(final ExportRequestNotification exportRequest,
                                                                          final ExportStatus exportStatus) {
        ExportResultNotification exportResult = new ExportResultNotification()
                .setAccountId(exportRequest.getAccountId())
                .setElementId(exportRequest.getElementId())
                .setProjectId(exportRequest.getProjectId())
                .setWorkspaceId(exportRequest.getWorkspaceId())
                .setNotificationId(exportRequest.getNotificationId())
                .setElementType(exportRequest.getElementType())
                .setStatus(exportStatus)
                .setExportId(exportRequest.getExportId())
                .setRootElementId(exportRequest.getRootElementId());

        if (ExportStatus.isCompleted(exportStatus)) {
            exportResult.setCompletedAt(UUIDs.timeBased());
        }
        return exportGateway.persist(exportResult)
                .thenMany(Flux.just(exportResult))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update the export result notifications from sns
     *
     * @param notificationId the notification id
     * @param status         status of the export
     * @return mono of ExportResultNotification
     */
    private Mono<ExportResultNotification> updateResultNotification(final UUID notificationId,
                                                                    final ExportStatus status) {
        return exportGateway.fetchExportResult(notificationId)
                .single()
                .doOnEach(log.reactiveErrorThrowable("error updating notification", throwable -> new HashMap<String, Object>() {{
                    put("notificationId", notificationId);
                    put("exportStatus", status);
                }}))
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault(String.format("could not update notification id [%s] with status [%s]. Notification not found",
                                                          notificationId, status));
                })
                .flatMap(notification -> {
                    ExportResultNotification exportResultNotification = new ExportResultNotification()
                            .setAccountId(notification.getAccountId())
                            .setElementId(notification.getElementId())
                            .setElementType(notification.getElementType())
                            .setProjectId(notification.getProjectId())
                            .setWorkspaceId(notification.getWorkspaceId())
                            .setNotificationId(notification.getNotificationId())
                            .setStatus(status)
                            .setExportId(notification.getExportId())
                            .setRootElementId(notification.getRootElementId());

                    if (ExportStatus.isCompleted(status)) {
                        exportResultNotification.setCompletedAt(UUIDs.timeBased());
                    }
                    // persist
                    return exportGateway.persist(exportResultNotification)
                            .then(Mono.just(exportResultNotification));
                });
    }

    /**
     * persist export summary details for provided courseware element
     *
     * @param exportRequestNotification the export request notification object
     * @param exportType the export type
     * @return Flux of void
     */
    @Trace(async = true)
    private Flux<Void> createExportSummary(final ExportRequest exportRequest,
                                           final ExportType exportType) {
        ExportSummary exportSummary = new ExportSummary()
                .setId(exportRequest.getExportId())
                .setAccountId(exportRequest.getAccountId())
                .setElementId(exportRequest.getElementId())
                .setElementType(exportRequest.getElementType())
                .setProjectId(exportRequest.getProjectId())
                .setWorkspaceId(exportRequest.getWorkspaceId())
                .setStatus(exportRequest.getStatus())
                .setRootElementId(exportRequest.getRootElementId())
                .setAmbrosiaUrl(EMPTY_AMBROSIA_URL)
                .setExportType(exportType != null ? exportType : ExportType.GENERIC)
                .setMetadata(exportRequest.getMetadata());

        if (ExportStatus.isCompleted(exportRequest.getStatus())) {
            exportSummary.setCompletedAt(UUIDs.timeBased());
        }

        return exportGateway.persist(exportSummary)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Send sns notification for each courseware element
     *
     * @param request export request notification object
     * @param coursewareElementNode courseware element node
     * @return flux of export result notification
     */
    @Trace(async = true)
    private Flux<ExportResultNotification> sendNotificationToSNS(final ExportRequest exportRequest,
                                                                 final CoursewareElementNode coursewareElementNode) {

        // create export notification for this elementId
        ExportRequestNotification elementExportNotification = new ExportRequestNotification(coursewareElementNode.getElementId(),
                                                                                            coursewareElementNode.getType(),
                                                                                            exportRequest);

        return Flux.from(exportTrackService.add(elementExportNotification.getNotificationId(),
                                                elementExportNotification.getExportId())
                                 .thenMany(createExportResultNotification(elementExportNotification,
                                                                          ExportStatus.IN_PROGRESS))
                                 .map(exportResultNotification -> Mono.just(exportResultNotification)
                                         // send the event to the export camel route in a reactive fashion
                                         .map(n -> {
                                             // do not wait for this to complete
                                             Mono.just(camelReactiveStreams.toStream(CoursewareExportRoute.SUBMIT_EXPORT_REQUEST,
                                                                                     elementExportNotification,
                                                                                     String.class))
                                                     .flatMap(Mono::from)
                                                     .doOnError(ex -> {
                                                         log.jsonError("error submitting export notification ",
                                                                       new HashMap<String, Object>() {
                                                                           {
                                                                               put("notificationId",
                                                                                   elementExportNotification.getNotificationId());
                                                                           }

                                                                           {
                                                                               put("exportId",
                                                                                   elementExportNotification.getExportId());
                                                                           }
                                                                       }, ex);
                                                         ExportErrorNotification errorNotification = new ExportErrorNotification()
                                                                 .setErrorMessage(ex.getMessage())
                                                                 .setExportId(elementExportNotification.getExportId())
                                                                 .setCause(ex.getCause() != null ? ex.getCause().getMessage() : "unknown cause")
                                                                 .setNotificationId(elementExportNotification.getNotificationId());
                                                         exportGateway.persist(errorNotification)
                                                                 // remove the tracking when the notification fails
                                                                 .thenMany(exportTrackService.remove(
                                                                         elementExportNotification.getNotificationId(),
                                                                         elementExportNotification.getExportId()))
                                                                 .thenMany(createExportResultNotification(
                                                                         elementExportNotification,
                                                                         ExportStatus.FAILED))
                                                                 // it's ok for this to complete in the future
                                                                 .subscribe();
                                                     })
                                                     // it's ok for this to complete in the future
                                                     .subscribe();
                                             // return the export result notification
                                             return n;
                                         })
                                         // do not interrupt this stream when an error occurs
                                         .onErrorResume(Throwable.class, throwable -> Mono.just(exportResultNotification)))
                                 .flatMap(exportResultNotification -> exportResultNotification));
    }

    /**
     * Create ambrosia snippet details
     *
     * @param exportId        the export id
     * @param notificationId  the notification id
     * @param elementId       the element id
     * @param elementType     the element type
     * @param accountId       the account id
     * @param ambrosiaSnippet the ambrosia snippet
     * @return mono of export ambrosia snippet object
     */
    @Trace(async = true)
    public Mono<ExportAmbrosiaSnippet> create(final UUID exportId,
                                              final UUID notificationId,
                                              final UUID elementId,
                                              final CoursewareElementType elementType,
                                              final UUID accountId,
                                              final String ambrosiaSnippet) {
        affirmArgument(exportId != null, "exportId is required");
        affirmArgument(notificationId != null, "notificationId is required");
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");
        affirmArgument(accountId != null, "accountId is required");
        // if the snippet is REDIS or CASSANDRA, then snippet cannot be null
        affirmArgument((ambrosiaSnippet != null || exportConfig.getSnippetsStorage() == SnippetsStorage.S3), "ambrosiaSnippet is required");

        ExportAmbrosiaSnippet exportAmbrosiaSnippet = new ExportAmbrosiaSnippet()
                .setExportId(exportId)
                .setNotificationId(notificationId)
                .setElementId(elementId)
                .setElementType(elementType)
                .setAccountId(accountId)
                .setAmbrosiaSnippet(ambrosiaSnippet);

        log.jsonInfo("creating ambrosia snippet details from SQS message", new HashMap<>(){
            { put("exportId", exportId.toString()); }
            { put("ambrosiaSnippet", ambrosiaSnippet == null ? "Not available"
                    : String.format("Snippet length %d", ambrosiaSnippet.length())); }
        });

        switch(exportConfig.getSnippetsStorage()) {
            case REDIS:{
                RBucketReactive<ExportAmbrosiaSnippet> bucket = redis.getBucket("export:" + exportId.toString() + ":"
                                                                                        + notificationId);
                return bucket.set(exportAmbrosiaSnippet, 30, TimeUnit.MINUTES)
                        .then(Mono.just(exportAmbrosiaSnippet))
                        .doOnEach(ReactiveTransaction.linkOnNext());
            }
            case S3: {
                // if S3, the ambrosia snippet will be null and not enriched
                return Mono.just(exportAmbrosiaSnippet)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            }
            case CASSANDRA:
            default: {
                return exportGateway.persist(exportAmbrosiaSnippet)
                        .then(Mono.just(exportAmbrosiaSnippet))
                        .doOnEach(ReactiveTransaction.linkOnNext());

            }
        }
    }

    /**
     * Fetch list of errors occurred in process of export
     *
     * @param exportId export reference id
     * @return returns export error payload
     */
    @Trace(async = true)
    public Flux<ExportErrorPayload> getExportErrors(final UUID exportId) {
        affirmArgument(exportId != null, "exportId is required");
        return exportGateway.exportError(exportId)
                .flatMap(exportErrorNotification -> {

                    Flux<ExportErrorNotification> fluxErrorNotification = exportGateway.fetchExportErrorLog(
                            exportErrorNotification.getNotificationId());
                    Flux<ExportAmbrosiaSnippet> fluxSnippetsByNotification = exportGateway.fetchAmbrosiaSnippetsByNotificationId(
                            exportErrorNotification.getNotificationId());

                    return Flux.zip(fluxErrorNotification, fluxSnippetsByNotification)
                            .flatMap(tuple2 -> {

                                ExportErrorNotification errorNotification = tuple2.getT1();
                                ExportAmbrosiaSnippet snippetsByNotification = tuple2.getT2();

                                ExportErrorPayload exportErrorPayload = new ExportErrorPayload();
                                exportErrorPayload.setExportId(exportId);
                                exportErrorPayload.setErrorMessage(errorNotification.getErrorMessage());
                                exportErrorPayload.setCause(errorNotification.getCause());
                                exportErrorPayload.setElementID(snippetsByNotification.getElementId());
                                exportErrorPayload.setElementType(snippetsByNotification.getElementType());

                                return Mono.just(exportErrorPayload);
                            });
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch list of errors occurred at the time of ambrosia reducer
     *
     * @param exportId export id
     * @return list of {@link AmbrosiaReducerErrorLog}
     */
    @Trace(async = true)
    public Flux<AmbrosiaReducerErrorLog> getAmbrosiaReducerErrors(final UUID exportId) {
        affirmArgument(exportId != null, "exportId is required");
        return exportGateway.getAmbrosiaReducerErrors(exportId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    private Mono<ExportAmbrosiaSnippet> getExportAmbrosiaSnippetFromS3(String bucketName, UUID exportId, String key, int totalFiles) {
        log.jsonDebug("Get export snippets from S3", new HashMap<>(){
            {put("exportId", exportId);}
            {put("bucketName", bucketName);}
            {put("S3key", key);}
        });
        return Mono.fromCallable(() -> {
            String notificationStr = s3ClientService.read(bucketName, key);
            ExportResultNotification notification = mapper
                    .readValue(notificationStr, ExportResultNotification.class);
            return new ExportAmbrosiaSnippet()
                    .setExportId(exportId)
                    .setAccountId(notification.getAccountId())
                    .setElementId(notification.getElementId())
                    .setElementType(notification.getElementType())
                    .setAmbrosiaSnippet(notification.getAmbrosiaSnippet());
        }).subscribeOn(totalFiles > 1500 ? Schedulers.fromExecutor(fixedThreadPoolBlockingS3Read) : Schedulers.elastic()); // anything more than 1000 elements, throttle it using bounded thread pool
    }

    /**
     * Generate ambrosia export. Loads Snippets, notifications for an export, then the courseware structure for the exported
     * element and finally reduce the snippets together to generate an ambrosia file.
     * The courseware structure loaded at this stage could be different and hold either more or less elements than when it was
     * originally exported. TODO store the structure at export time so that the reducing can be consistent
     * If any notification has a failed status then the ambrosia is not generated and {@link ExportStatus#FAILED} is set
     * on the summary export.
     *
     * @param exportSummary the export summary to generate the ambrosia structure for
     * @return a mono of export summary with either {@link ExportStatus#FAILED} or {@link ExportStatus#COMPLETED}
     */
    public Mono<ExportSummary> generateAmbrosia(@Nonnull final ExportSummary exportSummary) {
        final UUID exportId = exportSummary.getId();

        return exportGateway.hasNotificationError(exportId)
                .flatMap(hasErrors -> {
                    // if any error update the status to failed and return
                    if (hasErrors) {
                        final ExportSummary updatedSummary = new ExportSummary()
                                .setId(exportSummary.getId())
                                .setElementId(exportSummary.getElementId())
                                .setElementType(exportSummary.getElementType())
                                .setProjectId(exportSummary.getProjectId())
                                .setWorkspaceId(exportSummary.getWorkspaceId())
                                .setAccountId(exportSummary.getAccountId())
                                .setExportType(exportSummary.getExportType())
                                .setCompletedAt(UUIDs.timeBased())
                                .setRootElementId(exportSummary.getRootElementId())
                                .setAmbrosiaUrl(EMPTY_AMBROSIA_URL)
                                .setStatus(ExportStatus.FAILED)
                                .setMetadata(exportSummary.getMetadata());
                        return exportGateway.persist(updatedSummary)
                                .then(Mono.just(updatedSummary));
                    }

                    final Mono<Map<UUID, ExportAmbrosiaSnippet>> snippets;
                    log.jsonInfo("Getting snippets to generate ambrosia", new HashMap<String, Object>() {
                        {put("exportId", exportId);}
                        {put("snippetStorage", exportConfig.getSnippetsStorage());}
                    });

                    switch (exportConfig.getSnippetsStorage()) {
                        case S3: {
                            String snippetBucketName = exportConfig.getSnippetBucketName();
                            // in.json is the file that is created by Lambda process during the conversion of snippets. Need to configure this filename
                            List<String> exportKeyList = s3ClientService.listKeys(snippetBucketName, exportId.toString(), "in.json");
                            log.jsonInfo("Getting list of keys from S3", new HashMap<String, Object>() {
                                {put("fileCount", exportKeyList.size());}
                                {put("exportId", exportId);}
                                {put("snippetBucketName", snippetBucketName);}
                            });

                            snippets = Flux.fromIterable(exportKeyList)
                                    .flatMap(key -> getExportAmbrosiaSnippetFromS3(snippetBucketName,
                                                                                   exportId,
                                                                                   key,
                                                                                   exportKeyList.size()))
                                    .doOnNext(snippet ->
                                                      log.jsonInfo("Completed receiving notification string from S3",
                                                                   new HashMap<>() {
                                                                       {
                                                                           put("exportId", exportId);
                                                                       }

                                                                       {
                                                                           put("notificationId",
                                                                               snippet.getNotificationId());
                                                                       }

                                                                       {
                                                                           put("elementId", snippet.getElementId());
                                                                       }
                                                                   })
                                    )
                                    .collectMap(ExportAmbrosiaSnippet::getElementId)
                                    .doOnError(Exception.class, ex -> {
                                        log.jsonError("error unmarshalling notification string from S3",
                                                      new HashMap<String, Object>() {
                                                          {
                                                              put("exportId", exportId);
                                                          }
                                                      },
                                                      ex);
                                    });
                            break;
                        }

                        case REDIS: {
                            // todo: once S3 is working fine, this needs to be removed;
                            List<RBucketReactive<ExportAmbrosiaSnippet>> buckets =
                                    redis.findBuckets("export:" + exportId.toString() + ":*");

                            snippets = Flux.fromIterable(buckets)
                                    .flatMap(RBucketReactive::get)
                                    .collectMap(ExportAmbrosiaSnippet::getElementId);
                            break;
                        }

                        case CASSANDRA:
                        default: {
                            // todo: once S3 is working fine, this needs to be removed;
                            snippets = exportGateway.fetchAmbrosiaSnippets(exportId)
                                    .collectMap(ExportAmbrosiaSnippet::getElementId);
                        }
                    }

                    // find the courseware structure, keep in mind this structure could have changed since
                    // the export request was first triggered.
                    // todo: why???
                    Mono<CoursewareElementNode> coursewareStructure = coursewareElementStructureService
                            .getCoursewareElementStructure(exportSummary.getElementId(),
                                                           exportSummary.getElementType(),
                                                           Collections.emptyList());

                    return Mono.zip(snippets, coursewareStructure, Mono.just(exportSummary))
                            // reduce the snippets together into an ActivityAmbrosiaSnippet
                            .flatMap(t3 -> ambrosiaSnippetsReducer.reduce(t3.getT1(), t3.getT2(), t3.getT3())
                                    .flatMap(ambrosiaSnippet -> {
                                        log.jsonInfo("Completed reducing snippets", new HashMap<String, Object>() {
                                            {
                                                put("exportId", exportId);
                                            }

                                            {
                                                put("totalElements",
                                                    ambrosiaSnippet.getExportMetadata().getElementsExportedCount());
                                            }

                                            {
                                                put("elementId", exportSummary.getElementId());
                                            }
                                        });
                                        final String fileName = "ambrosia.json";
                                        // serialize the reduced snippet
                                        final File ambrosiaFile = ambrosiaSnippetsReducer.serialize(ambrosiaSnippet);
                                        // update the summary to completed and return
                                        final ExportSummary updatedSummary = new ExportSummary()
                                                .setId(exportSummary.getId())
                                                .setElementId(exportSummary.getElementId())
                                                .setElementType(exportSummary.getElementType())
                                                .setProjectId(exportSummary.getProjectId())
                                                .setWorkspaceId(exportSummary.getWorkspaceId())
                                                .setAccountId(exportSummary.getAccountId())
                                                // set the completed at always from the exportMetadata
                                                // this ensures the timeuuid is generated after the reducing is completed
                                                // and it is available in the snippet as a field
                                                .setCompletedAt(ambrosiaSnippet.getExportMetadata().getCompletedId())
                                                .setExportType(exportSummary.getExportType())
                                                .setRootElementId(exportSummary.getRootElementId())
                                                .setAmbrosiaUrl(String.format("%s/%s/%s",
                                                                              exportConfig.getBucketUrl(),
                                                                              exportId.toString(),
                                                                              fileName))
                                                .setStatus(ExportStatus.COMPLETED)
                                                .setMetadata(exportSummary.getMetadata());
                                        // upload to s3
                                        try {
                                            s3ClientService.upload(exportConfig.getBucketName(),
                                                                   String.format("%s/%s", exportId, "ambrosia.json"),
                                                                   ambrosiaFile);
                                        } finally {
                                            // delete the generated file.
                                            boolean wasDeleted = ambrosiaFile.delete();
                                            if (!wasDeleted) {
                                                log.warn("Failed delete ambrosia file: {}",
                                                         ambrosiaFile.getAbsolutePath());
                                            }
                                        }

                                        // persist the updated summary and return
                                        return exportGateway.persist(updatedSummary)
                                                .then(Mono.just(updatedSummary));
                                    }))
                            // persist any error to the db
                            .doOnError(exception -> {
                                exportGateway.persist(new AmbrosiaReducerErrorLog()
                                                              .setExportId(exportId)
                                                              .setCause(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception) : "unknown cause")
                                                              .setErrorMessage(exception.getMessage()))
                                        .subscribe();
                                throw Exceptions.propagate(exception);
                            });
                });
    }

    /**
     * Fetches a list of exports for a workspace
     *
     * @param workspaceId the workspace id
     * @return a flux of ExportSummary {@link ExportSummary}
     */
    @Trace(async = true)
    public Flux<ExportSummary> fetchExportSummariesForWorkspace(final UUID workspaceId) {
        affirmArgument(workspaceId != null, "workspaceId is required");
        return exportGateway.fetchExportSummariesByWorkspace(workspaceId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches a list of exports for a project
     *
     * @param projectId the project id
     * @return a flux of ExportSummary {@link ExportSummary}
     */
    @Trace(async = true)
    public Flux<ExportSummary> fetchExportSummariesForProject(final UUID projectId) {
        affirmArgument(projectId != null, "projectId is required");
        return exportGateway.fetchExportSummariesByProject(projectId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find an export summary by id
     *
     * @param exportId the export id to find the export summary for
     * @return a mono of export summary or an empty mono when not found
     */
    public Mono<ExportSummary> findById(final UUID exportId) {
        affirmArgument(exportId != null, "exportId is required");
        return exportGateway.findExportSummary(exportId);
    }

    /**
     * Find an export id by notification id
     *
     * @param notificationId the notification id to find the export id for
     * @return a mono of export id or an empty mono when not found
     */
    public Mono<UUID> findExportId(final UUID notificationId) {
        return findNotification(notificationId)
                .map(ExportResultNotification::getExportId);
    }

    /**
     * Find an export notification result by its id
     *
     * @param notificationId the id of the notification to find
     * @return a mono with the found notification or an empty mono when not found
     */
    public Mono<ExportResultNotification> findNotification(final UUID notificationId) {
        return exportGateway.fetchExportResult(notificationId);
    }
}
