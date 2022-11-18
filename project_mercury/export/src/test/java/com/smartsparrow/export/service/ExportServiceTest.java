package com.smartsparrow.export.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareElementStructureService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ActivityAmbrosiaSnippet;
import com.smartsparrow.export.data.AmbrosiaReducerErrorLog;
import com.smartsparrow.export.data.AmbrosiaSnippet;
import com.smartsparrow.export.data.ExportAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.export.data.ExportErrorPayload;
import com.smartsparrow.export.data.ExportGateway;
import com.smartsparrow.export.data.ExportMetadata;
import com.smartsparrow.export.data.ExportRequestNotification;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.data.ExportRetryNotification;
import com.smartsparrow.export.data.ExportStatus;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.export.lang.AmbrosiaSnippetReducerException;
import com.smartsparrow.export.route.CoursewareExportRoute;
import com.smartsparrow.export.wiring.ExportConfig;
import com.smartsparrow.export.wiring.SnippetsStorage;
import com.smartsparrow.service.S3ClientService;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.Warrants;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ExportServiceTest {

    private ExportService exportService;

    @Mock
    private ExportGateway exportGateway;

    @Mock
    private CoursewareElementStructureService coursewareElementStructureService;

    @Mock
    ExportTrackService exportTrackService;

    @Mock
    private AmbrosiaSnippetsReducer ambrosiaSnippetsReducer;

    @Mock
    private Provider<ExportConfig> exportConfigProvider;

    @Mock
    private ExportConfig exportConfig;

    @Mock
    private S3ClientService s3ClientService;

    @Mock
    RedissonReactiveClient redisClient;

    @Mock
    RBucketReactive<Object> bucket;

    @Mock
    private CamelReactiveStreamsService camelReactiveStreamsService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID exportId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID notificationId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.COMPONENT;
    private static final List<String> configFields = Collections.emptyList();

    private static final CoursewareElement element = CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.ACTIVITY);
    private static final ExportRequestNotification exportNotification = ExportTestStub.buildRequestNotification(
            element, projectId, workspaceId, accountId, exportId
    );
    private static final ExportRequestNotification requestNotification = ExportTestStub
            .buildRequestNotification(element, projectId, workspaceId, accountId, exportId);
    private static final ExportRetryNotification retryNotification = ExportTestStub
            .buildRetryNotification(notificationId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(exportGateway.persist(any(ExportResultNotification.class))).thenReturn(Flux.just(new Void[]{}));
        when(exportGateway.persist(any(ExportSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(coursewareElementStructureService.getCoursewareElementStructure(element.getElementId(), element.getElementType(), configFields)).thenReturn(Mono.just(new CoursewareElementNode()
                .setElementId(element.getElementId())
                .setType(element.getElementType())
                .addChild(new CoursewareElementNode()
                        .setElementId(elementId)
                        .setType(elementType))));
        when(exportTrackService.add(any(),any())).thenReturn(Mono.just(true));
        when(exportTrackService.remove(any(),any())).thenReturn(Mono.just(true));
        when(exportConfigProvider.get()).thenReturn(exportConfig);

        when(exportConfig.getBucketName()).thenReturn("bucketName");
        when(exportConfig.getBucketUrl()).thenReturn("bucketurl");
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.REDIS);
        when(s3ClientService.upload(anyString(), anyString(), anyString(), anyString(), any(ContentType.class)))
                .thenReturn(new PutObjectResult());

        exportService = new ExportService(
                exportGateway,
                coursewareElementStructureService,
                ambrosiaSnippetsReducer,
                exportTrackService,
                s3ClientService,
                exportConfigProvider,
                camelReactiveStreamsService,
                redisClient);
    }

    @Test
    void submit_requestFailed() {
        ArgumentCaptor<ExportResultNotification> resultCaptor = ArgumentCaptor.forClass(ExportResultNotification.class);
        ArgumentCaptor<ExportErrorNotification> errorCaptor = ArgumentCaptor.forClass(ExportErrorNotification.class);

        when(exportGateway.persist(any(ExportErrorNotification.class))).thenReturn(Flux.just(new Void[]{}));
        TestPublisher<String> requestPublisher = TestPublisher.create();
        when(camelReactiveStreamsService.toStream(
                eq(CoursewareExportRoute.SUBMIT_EXPORT_REQUEST),
                any(ExportRequestNotification.class),
                eq(String.class))).thenReturn(requestPublisher.error(new RuntimeException("error")));
        ExportResultNotification result = exportService.submit(exportNotification, ExportType.EPUB_PREVIEW).blockFirst();

        assertNotNull(result);
        assertNotNull(result.getNotificationId());
        assertEquals(projectId, result.getProjectId());
        assertEquals(element.getElementId(), result.getElementId());
        assertEquals(element.getElementType(), result.getElementType());
        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals(accountId, result.getAccountId());
        // the status will always be in progress
        assertEquals(ExportStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getExportId());

        verify(exportGateway).persist(errorCaptor.capture());
        verify(exportGateway, atMost(2)).persist(resultCaptor.capture());

        final ExportResultNotification capturedResult = resultCaptor.getValue();

        assertNotNull(capturedResult);
        assertNotNull(capturedResult.getCompletedAt());
        assertEquals(result.getExportId(), capturedResult.getExportId());
        assertEquals(result.getAccountId(), capturedResult.getAccountId());
        assertEquals(result.getNotificationId(), capturedResult.getNotificationId());
        // the persisted notification will have status as failed
        assertEquals(ExportStatus.FAILED ,capturedResult.getStatus());
        assertEquals(result.getElementId(), capturedResult.getElementId());
        assertEquals(result.getElementType(), capturedResult.getElementType());
        assertEquals(result.getProjectId(), capturedResult.getProjectId());
        assertEquals(result.getWorkspaceId(), capturedResult.getWorkspaceId());
        assertEquals(result.getRootElementId(), capturedResult.getRootElementId());

        final ExportErrorNotification capturedError = errorCaptor.getValue();

        assertNotNull(capturedError);
        assertEquals("error", capturedError.getErrorMessage());
        assertEquals("unknown cause", capturedError.getCause());

        verify(exportTrackService, times(1)).add(any(UUID.class), eq(exportId));
        verify(exportTrackService, times(1)).remove(any(UUID.class), eq(exportId));
    }

    @Test
    void submit_success() {
        ArgumentCaptor<ExportResultNotification> resultCaptor = ArgumentCaptor.forClass(ExportResultNotification.class);

        TestPublisher<String> requestPublisher = TestPublisher.create();
        requestPublisher.emit("done").complete();
        when(camelReactiveStreamsService.toStream(
                eq(CoursewareExportRoute.SUBMIT_EXPORT_REQUEST),
                any(ExportRequestNotification.class),
                eq(String.class))).thenReturn(requestPublisher.mono());
        List<ExportResultNotification> results = exportService.submit(exportNotification, ExportType.EPUB_PREVIEW).collectList().block();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertNotNull(results.get(0).getNotificationId());
        assertEquals(projectId, results.get(0).getProjectId());
        assertEquals(element.getElementId(), results.get(0).getElementId());
        assertEquals(element.getElementType(), results.get(0).getElementType());
        assertEquals(workspaceId, results.get(0).getWorkspaceId());
        assertEquals(accountId, results.get(1).getAccountId());
        assertEquals(ExportStatus.IN_PROGRESS, results.get(0).getStatus());
        assertNotNull(results.get(0).getExportId());
        assertNull(results.get(0).getCompletedAt());


        verify(exportGateway, atMost(2)).persist(resultCaptor.capture());
        verify(exportGateway, never()).persist(any(ExportErrorNotification.class));

        final ExportResultNotification capturedResult = resultCaptor.getValue();

        assertNotNull(capturedResult);
        assertEquals(results.get(1), capturedResult);
        verify(exportTrackService, times(2)).add(any(UUID.class), eq(exportId));
    }

    @Test
    void processResultSnippet_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> exportService.processResultSnippet(null));

        assertNotNull(f1);
        assertEquals("exportAmbrosiaSnippet is required", f1.getMessage());

    }

    @Test
    void processResultSnippet() {
        when(exportGateway.fetchExportResult(any(UUID.class))).thenReturn(Mono.just(new ExportResultNotification()));
        ArgumentCaptor<ExportResultNotification> captor = ArgumentCaptor.forClass(ExportResultNotification.class);
        final String payload = "{\"foo\":\"bar\"}";
        final ExportResultNotification res = exportService
                .processResultSnippet(new ExportAmbrosiaSnippet().setNotificationId(UUID.randomUUID()))
                .block();

        assertNotNull(res);
        assertEquals(ExportStatus.COMPLETED, res.getStatus());
        assertNotNull(res.getCompletedAt());
        assertNull(res.getExportId());

        verify(exportGateway).persist(captor.capture());

        final ExportResultNotification captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);
    }

    @Test
    void processErrorNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> exportService.processErrorNotification(null));

        assertNotNull(f1);
        assertEquals("errorNotification is required", f1.getMessage());
    }

    @Test
    void processErrorNotification() {
        when(exportGateway.persist(any(ExportErrorNotification.class))).thenReturn(Flux.just(new Void[]{}));
        when(exportGateway.fetchExportResult(any(UUID.class))).thenReturn(Mono.just(new ExportResultNotification()));
        ArgumentCaptor<ExportResultNotification> captor = ArgumentCaptor.forClass(ExportResultNotification.class);
        ArgumentCaptor<ExportErrorNotification> errorCaptor = ArgumentCaptor.forClass(ExportErrorNotification.class);
        final ExportResultNotification res = exportService
                .processErrorNotification(new ExportErrorNotification()
                        .setNotificationId(UUID.randomUUID()))
                .block();

        assertNotNull(res);
        assertEquals(ExportStatus.FAILED, res.getStatus());
        assertNotNull(res.getCompletedAt());
        assertNotNull(res.getCompletedAt());

        verify(exportGateway).persist(captor.capture());
        verify(exportGateway).persist(errorCaptor.capture());
        final ExportResultNotification captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);

        final ExportErrorNotification capturedError = errorCaptor.getValue();

        assertNotNull(capturedError);
        assertNotNull(capturedError.getNotificationId());
    }

    @Test
    void processRetryNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> exportService.processRetryNotification(null));

        assertNotNull(f1);
        assertEquals("retryNotification is required", f1.getMessage());
    }

    @Test
    void processRetryNotification() {
        when(exportGateway.fetchExportResult(any(UUID.class))).thenReturn(Mono.just(new ExportResultNotification()));
        ArgumentCaptor<ExportResultNotification> captor = ArgumentCaptor.forClass(ExportResultNotification.class);
        final String payload = "{\"foo\":\"bar\"}";
        final ExportResultNotification res = exportService
                .processRetryNotification(new ExportRetryNotification().setNotificationId(UUID.randomUUID()))
                .block();

        assertNotNull(res);
        assertEquals(ExportStatus.RETRY_RECEIVED, res.getStatus());
        assertNull(res.getCompletedAt());

        verify(exportGateway).persist(captor.capture());

        final ExportResultNotification captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);
    }


    @Test
    void processSubmitDeadLetters_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> exportService.processSubmitDeadLetters(null, null));

        assertNotNull(f1);
        assertEquals("requestNotification is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                                               () -> exportService.processSubmitDeadLetters(requestNotification, null));

        assertNotNull(f2);
        assertEquals("messagePayload is required", f2.getMessage());
    }

    @Test
    void processSubmitDeadLetters() {
        when(exportGateway.persist(any(ExportErrorNotification.class))).thenReturn(Flux.just(new Void[]{}));
        when(exportGateway.fetchExportResult(any(UUID.class))).thenReturn(Mono.just(new ExportResultNotification()));
        ArgumentCaptor<ExportResultNotification> captor = ArgumentCaptor.forClass(ExportResultNotification.class);
        ArgumentCaptor<ExportErrorNotification> errorCaptor = ArgumentCaptor.forClass(ExportErrorNotification.class);
        final String payload = "{\"foo\":\"bar\"}";
        final ExportResultNotification res = exportService
                .processSubmitDeadLetters(requestNotification, payload)
                .block();

        assertNotNull(res);
        assertEquals(ExportStatus.FAILED, res.getStatus());
        assertNotNull(res.getCompletedAt());
        assertNotNull(res.getCompletedAt());

        verify(exportGateway).persist(captor.capture());
        verify(exportGateway).persist(errorCaptor.capture());
        final ExportResultNotification captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);

        final ExportErrorNotification capturedError = errorCaptor.getValue();

        assertNotNull(capturedError);
        assertNotNull(capturedError.getNotificationId());
    }

    @Test
    void processRetryDeadLetters_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> exportService.processRetryDeadLetters(null, null));

        assertNotNull(f1);
        assertEquals("retryNotification is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                                               () -> exportService.processRetryDeadLetters(retryNotification, null));

        assertNotNull(f2);
        assertEquals("messagePayload is required", f2.getMessage());
    }

    @Test
    void processRetryDeadLetters() {
        when(exportGateway.persist(any(ExportErrorNotification.class))).thenReturn(Flux.just(new Void[]{}));
        when(exportGateway.fetchExportResult(any(UUID.class))).thenReturn(Mono.just(new ExportResultNotification()));
        ArgumentCaptor<ExportResultNotification> captor = ArgumentCaptor.forClass(ExportResultNotification.class);
        ArgumentCaptor<ExportErrorNotification> errorCaptor = ArgumentCaptor.forClass(ExportErrorNotification.class);
        final String payload = "{\"foo\":\"bar\"}";
        final ExportResultNotification res = exportService
                .processRetryDeadLetters(retryNotification, payload)
                .block();

        assertNotNull(res);
        assertEquals(ExportStatus.FAILED, res.getStatus());
        assertNotNull(res.getCompletedAt());
        assertNotNull(res.getCompletedAt());

        verify(exportGateway).persist(captor.capture());
        verify(exportGateway).persist(errorCaptor.capture());
        final ExportResultNotification captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);

        final ExportErrorNotification capturedError = errorCaptor.getValue();

        assertNotNull(capturedError);
        assertNotNull(capturedError.getNotificationId());
    }

    @Test
    void create_ambrosiaSnippetSuccess() {
        when(redisClient.getBucket("export:" + exportId.toString() + ":"
                + notificationId)).thenReturn(bucket);
        when(bucket.set(any(ExportAmbrosiaSnippet.class), anyLong(), any(TimeUnit.class))).thenReturn(Mono.empty());
        ArgumentCaptor<ExportAmbrosiaSnippet> captor = ArgumentCaptor.forClass(ExportAmbrosiaSnippet.class);

        final String ambrosiaSnippet = "{\"foo\":\"bar\"}";
        final ExportAmbrosiaSnippet exportAmbrosiaSnippet = exportService.create(exportId, notificationId, elementId, elementType, accountId, ambrosiaSnippet).block();

        assertNotNull(exportAmbrosiaSnippet);

        verify(bucket).set(captor.capture(),eq(30L), eq(TimeUnit.MINUTES));
        verify(exportGateway, never()).persist(captor.capture());
        assertNotNull(captor.getValue());

        assertEquals(exportId, captor.getValue().getExportId());
        assertEquals(notificationId, captor.getValue().getNotificationId());
        assertEquals(elementId, captor.getValue().getElementId());

    }

    @Test
    void create_ambrosiaSnippetSuccessWithCassandraStorage() {
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.CASSANDRA);
        when(exportGateway.persist(any(ExportAmbrosiaSnippet.class))).thenReturn(Flux.just(new Void[]{}));
        ArgumentCaptor<ExportAmbrosiaSnippet> captor = ArgumentCaptor.forClass(ExportAmbrosiaSnippet.class);

        final String ambrosiaSnippet = "{\"foo\":\"bar\"}";
        final ExportAmbrosiaSnippet exportAmbrosiaSnippet = exportService
                .create(exportId, notificationId, elementId, elementType, accountId, ambrosiaSnippet).block();

        assertNotNull(exportAmbrosiaSnippet);

        verify(exportGateway).persist(captor.capture());
        verify(redisClient, never()).getBucket(anyString());
        assertNotNull(captor.getValue());

        assertEquals(exportId, captor.getValue().getExportId());
        assertEquals(notificationId, captor.getValue().getNotificationId());
        assertEquals(elementId, captor.getValue().getElementId());
    }

    @Test
    void create_ambrosiaSnippetSuccessWithCassandraStorageInvalidSnippet() {
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.CASSANDRA);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> exportService
                .create(exportId, notificationId, elementId, elementType, accountId, null).block());
        assertNotNull(e);
        assertEquals("ambrosiaSnippet is required", e.getMessage());
    }

    @Test
    void create_ambrosiaSnippetSuccessWithRedisStorageInvalidSnippet() {
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.REDIS);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> exportService
                .create(exportId, notificationId, elementId, elementType, accountId, null).block());
        assertNotNull(e);
        assertEquals("ambrosiaSnippet is required", e.getMessage());
    }

    @Test
    void create_ambrosiaSnippetSuccessWithS3Storage() {
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.S3);
        final ExportAmbrosiaSnippet exportAmbrosiaSnippet = exportService
                .create(exportId, notificationId, elementId, elementType, accountId, null).block();

        assertNotNull(exportAmbrosiaSnippet);
        verify(exportGateway, never()).persist(any(ExportAmbrosiaSnippet.class));
        verify(redisClient, never()).getBucket(anyString());

        assertEquals(exportId, exportAmbrosiaSnippet.getExportId());
        assertEquals(notificationId, exportAmbrosiaSnippet.getNotificationId());
        assertEquals(elementId, exportAmbrosiaSnippet.getElementId());
    }

    @Test
    void fetch_exportErrorsResult() {
        ExportErrorNotification exportErrorNotifications = new ExportErrorNotification().setExportId(exportId)
                .setNotificationId(UUID.randomUUID())
                .setErrorMessage("This is error")
                .setCause("This is cause");
        ExportAmbrosiaSnippet exportAmbrosiaSnippet = new ExportAmbrosiaSnippet()
                .setExportId(exportId)
                        .setElementType(CoursewareElementType.INTERACTIVE);

        when(exportGateway.exportError(exportId)).thenReturn(Flux.just(exportErrorNotifications));
        when(exportGateway.fetchExportErrorLog(exportErrorNotifications.getNotificationId())).thenReturn(Flux.just(exportErrorNotifications));
        when(exportGateway.fetchAmbrosiaSnippetsByNotificationId(exportErrorNotifications.getNotificationId()))
                .thenReturn(Flux.just(exportAmbrosiaSnippet));


        Flux<ExportErrorPayload> errorList = exportService.getExportErrors(exportId);


        assertNotNull(errorList);
        assertEquals(1, errorList.collectList().block().size());
        verify(exportGateway).exportError(exportId);

    }

    @Test
    void fetch_exportErrorsSizeZero() {
        when(exportGateway.exportError(exportId)).thenReturn(Flux.empty());

        Flux<ExportErrorPayload> errorList = exportService.getExportErrors(exportId);
        assertNotNull(errorList);
        assertEquals(0, errorList.collectList().block().size());
        verify(exportGateway).exportError(exportId);

    }

    @Test
    void fetch_exportError_exception() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> exportService.getExportErrors(null));

        assertEquals("exportId is required", f1.getMessage());
    }

    @Test
    void getAmbrosiaReducerErrorLog_exception() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> exportService.getAmbrosiaReducerErrors(null));

        assertEquals("exportId is required", f1.getMessage());
    }
    @Test
    void getAmbrosiaReducerErrorLog() {
        AmbrosiaReducerErrorLog ambrosiaReducerErrorLog = new AmbrosiaReducerErrorLog()
                .setErrorMessage("This is error message")
                        .setCause("This is error cause")
                                .setExportId(exportId);
        when(exportGateway.getAmbrosiaReducerErrors(exportId)).thenReturn(Flux.just(ambrosiaReducerErrorLog));
        Flux<AmbrosiaReducerErrorLog> reducerErrors = exportService.getAmbrosiaReducerErrors(exportId);

        assertNotNull(reducerErrors);
        assertEquals(1, reducerErrors.collectList().block().size());
        verify(exportGateway).getAmbrosiaReducerErrors(exportId);


    }
    @Test
    void generateAmbrosia_hasErrors() {
        when(exportGateway.hasNotificationError(exportId)).thenReturn(Mono.just(true));
        when(exportGateway.persist(any(ExportSummary.class))).thenReturn(Flux.just(new Void[]{}));
        ArgumentCaptor<ExportSummary> captor = ArgumentCaptor.forClass(ExportSummary.class);
        final ExportSummary summary = new ExportSummary()
                .setId(exportId)
                .setAccountId(accountId)
                .setElementId(elementId)
                .setElementType(elementType)
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)
                .setStatus(ExportStatus.IN_PROGRESS);

        final ExportSummary updated = exportService.generateAmbrosia(summary)
                .block();

        assertNotNull(updated);
        assertEquals(exportId, updated.getId());
        assertEquals(elementId, updated.getElementId());
        assertEquals(elementType, updated.getElementType());
        assertEquals(accountId, updated.getAccountId());
        assertEquals(projectId, updated.getProjectId());
        assertEquals(workspaceId, updated.getWorkspaceId());
        assertEquals(ExportStatus.FAILED, updated.getStatus());
        assertNotNull(updated.getCompletedAt());

        verify(exportGateway).persist(captor.capture());

        final ExportSummary persisted = captor.getValue();

        assertNotNull(persisted);
        assertEquals(updated, persisted);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateAmbrosia_success_redis() throws IOException {
        RBucketReactive<Object> bucket = mock(RBucketReactive.class);
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.REDIS);
        List<RBucketReactive<Object>> buckets = Lists.newArrayList(bucket);
        when(redisClient.findBuckets(eq("export:" + exportId.toString() + ":*"))).thenReturn(buckets);
        when(bucket.get()).thenReturn(Mono.just(new ExportAmbrosiaSnippet()
                .setElementId(elementId)));

        reduceTestHelper();
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateAmbrosia_success_cassandra() throws IOException {
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.CASSANDRA);
        when(exportGateway.fetchAmbrosiaSnippets(exportId)).thenReturn(Flux.just(new ExportAmbrosiaSnippet()
                .setElementId(elementId)));

        reduceTestHelper();
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateAmbrosia_success_S3() throws IOException {
        when(exportConfig.getSnippetsStorage()).thenReturn(SnippetsStorage.S3);

        List<String> keyListMock = Lists.newArrayList("key1");
        when(s3ClientService.listKeys(any(), eq(exportId.toString()), any())).thenReturn(keyListMock);

        ExportResultNotification mockResult = new ExportResultNotification();
        mockResult.setExportId(exportId).setElementId(elementId);
        when(s3ClientService.read(any(), anyString())).thenReturn((new ObjectMapper()).writeValueAsString(mockResult));

        reduceTestHelper();
    }

    private void reduceTestHelper() throws IOException {
        when(exportGateway.hasNotificationError(exportId)).thenReturn(Mono.just(false));

        when(coursewareElementStructureService.getCoursewareElementStructure(elementId, elementType, configFields))
                .thenReturn(Mono.just(new CoursewareElementNode()));
        when(ambrosiaSnippetsReducer.reduce(any(Map.class), any(CoursewareElementNode.class), any(ExportSummary.class)))
                .thenReturn(Mono.just(new ActivityAmbrosiaSnippet()
                        .setExportMetadata(new ExportMetadata(UUIDs.timeBased()))));
        when(ambrosiaSnippetsReducer.serialize(any(AmbrosiaSnippet.class)))
                .thenReturn(Files.createTempFile("test", "json").toFile());

        when(exportGateway.persist(any(ExportSummary.class))).thenReturn(Flux.just(new Void[]{}));
        ArgumentCaptor<ExportSummary> captor = ArgumentCaptor.forClass(ExportSummary.class);
        ArgumentCaptor<Map> snippetCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<CoursewareElementNode> nodeCaptor = ArgumentCaptor.forClass(CoursewareElementNode.class);

        final ExportSummary summary = new ExportSummary()
                .setId(exportId)
                .setAccountId(accountId)
                .setElementId(elementId)
                .setElementType(elementType)
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)
                .setStatus(ExportStatus.IN_PROGRESS);

        final ExportSummary updated = exportService.generateAmbrosia(summary)
                .block();

        assertNotNull(updated);
        assertEquals(exportId, updated.getId());
        assertEquals(elementId, updated.getElementId());
        assertEquals(elementType, updated.getElementType());
        assertEquals(accountId, updated.getAccountId());
        assertEquals(projectId, updated.getProjectId());
        assertEquals(workspaceId, updated.getWorkspaceId());
        assertEquals(ExportStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedAt());

        verify(exportGateway).persist(captor.capture());
        verify(ambrosiaSnippetsReducer).reduce(snippetCaptor.capture(), nodeCaptor.capture(), eq(summary));
        verify(s3ClientService).upload(eq(exportConfig.getBucketName()), eq(String.format("%s/%s", exportId, "ambrosia.json")),
                any(File.class));
        final ExportSummary persisted = captor.getValue();

        assertNotNull(persisted);
        assertEquals(updated, persisted);

        final Map<UUID, ExportAmbrosiaSnippet> snippets = snippetCaptor.getValue();
        final CoursewareElementNode node = nodeCaptor.getValue();

        assertNotNull(snippets);
        assertEquals(1, snippets.size());
        assertNotNull(node);
    }


    @Test
    @SuppressWarnings("unchecked")
    void generateAmbrosia_reducer_error() {
        when(exportGateway.hasNotificationError(exportId)).thenReturn(Mono.just(false));
        when(exportGateway.fetchAmbrosiaSnippets(exportId)).thenReturn(Flux.just(new ExportAmbrosiaSnippet()
                                                                                         .setElementId(elementId)));
        when(coursewareElementStructureService.getCoursewareElementStructure(elementId, elementType, configFields))
                .thenReturn(Mono.just(new CoursewareElementNode()));
        when(ambrosiaSnippetsReducer.reduce(any(Map.class), any(CoursewareElementNode.class), any(ExportSummary.class)))
                .thenThrow(new AmbrosiaSnippetReducerException(String.format("error reducing activity [%s]",
                                                                             elementId)));
        when(exportGateway.persist(any(AmbrosiaReducerErrorLog.class))).thenReturn(Flux.just(new Void[]{}));

        final ExportSummary summary = new ExportSummary()
                .setId(exportId)
                .setAccountId(accountId)
                .setElementId(elementId)
                .setElementType(elementType)
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)
                .setStatus(ExportStatus.IN_PROGRESS);

        AmbrosiaSnippetReducerException exception = assertThrows(AmbrosiaSnippetReducerException.class,
                                                                 () -> exportService.generateAmbrosia(summary).block());
        assertNotNull(exception);
        assertEquals(String.format("error reducing activity [%s]", elementId), exception.getMessage());
        verify(exportGateway).persist(any(AmbrosiaReducerErrorLog.class));
    }

}
