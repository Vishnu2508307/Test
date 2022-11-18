package com.smartsparrow.ingestion.service;

import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_AMBROSIA_REQUEST;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.ingestion.data.IngestionEvent;
import com.smartsparrow.ingestion.data.IngestionEventType;
import com.smartsparrow.ingestion.data.IngestionGateway;
import com.smartsparrow.ingestion.data.IngestionPayload;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.data.IngestionSummaryPayload;
import com.smartsparrow.ingestion.eventmessage.IngestionSummaryEventMessage;
import com.smartsparrow.ingestion.wiring.IngestionConfig;
import com.smartsparrow.service.S3ClientService;

import junit.framework.TestCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class IngestionServiceTest {

    @InjectMocks
    private IngestionService ingestionService;

    @Mock
    private IngestionGateway ingestionGateway;

    @Mock
    private AccountService accountService;

    @Mock
    private CamelReactiveStreamsService camel;

    @Mock
    private S3ClientService s3ClientService;

    @Mock
    private IngestionConfig ingestionConfig;

    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID creatorId =  UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";
    private static final String courseName = "Test Course";
    private static final String bearerToken = "iENrnPXyR3u09i4lxKVujOnyhOlxx1aZ";
    private static final String bucketName = "bucket/ingestion";
    private static final String fileName = "content.epub";
    private static final String ingestStats = "[{}]";
    private static final UUID rootElementId = UUID.randomUUID();

    private final IngestionSummary ingestionSummary = new IngestionSummary()
            .setId(ingestionId)
            .setProjectId(projectId)
            .setWorkspaceId(workspaceId)
            .setIngestionStats(ingestStats)
            .setAmbrosiaUrl(ambrosiaUrl)
            .setStatus(IngestionStatus.COMPLETED)
            .setConfigFields(configFields)
            .setCreatorId(creatorId)
            .setActivityId(activityId)
            .setCourseName(courseName);

    private final IngestionSummaryPayload ingestionSummaryPayload = new IngestionSummaryPayload()
            .setId(ingestionId)
            .setProjectId(projectId)
            .setWorkspaceId(workspaceId)
            .setIngestionStats(ingestStats)
            .setAmbrosiaUrl(ambrosiaUrl)
            .setStatus(IngestionStatus.COMPLETED)
            .setConfigFields(configFields)
            .setCreator(new AccountPayload().setAccountId(creatorId))
            .setActivityId(activityId)
            .setCourseName(courseName);

    private final IngestionEvent ingestionEvent = new IngestionEvent()
            .setEventType(IngestionEventType.ERROR)
            .setIngestionId(ingestionId)
            .setProjectId(projectId);

    private final IngestionSummaryEventMessage ingestionSummaryEventMessage = new IngestionSummaryEventMessage(bearerToken, ingestionSummary);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(ingestionGateway.persist(any(IngestionSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(ingestionGateway.persist(any(IngestionEvent.class))).thenReturn(Flux.just(new Void[]{}));
        when(ingestionGateway.updateIngestionStatus(any(IngestionSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(ingestionGateway.updateIngestionSummary(any(IngestionSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(ingestionGateway.findIngestionSummary(eq(ingestionId))).thenReturn(Mono.just(ingestionSummary));
        when(ingestionGateway.findEventsByIngestion(eq(ingestionId))).thenReturn(Flux.just(ingestionEvent));
        when(ingestionGateway.findSummaryByProject(eq(projectId))).thenReturn(Flux.just(ingestionSummary));
        when(ingestionGateway.findSummaryByName(eq(courseName), eq(projectId), eq(rootElementId))).thenReturn(Flux.empty());
        when(ingestionGateway.delete(any(IngestionSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(ingestionConfig.getBucketName()).thenReturn(bucketName);
        when(accountService.getAccountPayload(eq(creatorId))).thenReturn(Mono.just(new AccountPayload().setAccountId(creatorId)));
    }

    @Test
    void create() {
        IngestionSummary summary = ingestionService.create(projectId, workspaceId, configFields, creatorId, ambrosiaUrl, null, courseName, rootElementId)
                .block();

        verify(ingestionGateway).persist(any(IngestionSummary.class));
        assert summary != null;
        assertNotNull(summary.getId());
        assertEquals(projectId, summary.getProjectId());
        assertEquals(workspaceId, summary.getWorkspaceId());
        assertEquals(configFields, summary.getConfigFields());
        assertEquals(creatorId, summary.getCreatorId());
        assertEquals(ambrosiaUrl, summary.getAmbrosiaUrl());
        assertEquals(IngestionStatus.UPLOADING, summary.getStatus());
        assertEquals(courseName, summary.getCourseName());
        assertNull(summary.getIngestionStats());
    }

    @Test
    void create_conflict() {
        when(ingestionGateway.findSummaryByName(eq(courseName), eq(projectId), eq(rootElementId))).thenReturn(Flux.just(ingestionSummary));
        try {
            ingestionService.create(projectId, workspaceId, configFields, creatorId, ambrosiaUrl, null, courseName, rootElementId).block();
        } catch (ConflictFault ex) {
            assertEquals("Course with that name already exists", ex.getMessage());
        }
    }

    @Test
    void findById() {
        StepVerifier.create(ingestionService.findById(ingestionId))
                .expectNext(ingestionSummary)
                .verifyComplete();
    }

    @Test
    void findById_noIngestionId() {
        IllegalArgumentFault ex =
                assertThrows(IllegalArgumentFault.class, () -> ingestionService.findById(null).block());
        assertEquals("ingestionId is required", ex.getMessage());
    }

    @Test
    void fetchLogEventsForIngestion() {
        StepVerifier.create(ingestionService.fetchLogEventsForIngestion(ingestionId))
                .expectNext(ingestionEvent)
                .verifyComplete();
    }

    @Test
    void fetchLogEventsForIngestion_noIngestionId() {
        IllegalArgumentFault ex =
                assertThrows(IllegalArgumentFault.class, () -> ingestionService.fetchLogEventsForIngestion(null).blockFirst());
        assertEquals("ingestionId is required", ex.getMessage());
    }

    @Test
    void fetchIngestionsForProject() {
        StepVerifier.create(ingestionService.fetchIngestionsForProject(projectId))
                .expectNext(ingestionSummaryPayload)
                .verifyComplete();
    }

    @Test
    void fetchIngestionsForProject_noProjectId() {
        IllegalArgumentFault ex =
                assertThrows(IllegalArgumentFault.class, () -> ingestionService.fetchIngestionsForProject(null).blockFirst());
        assertEquals("projectId is required", ex.getMessage());
    }

    @Test
    void processResultNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.processResultNotification(null, bearerToken));

        assertNotNull(f1);
        assertEquals("ingestionSummary is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.processResultNotification(ingestionSummary, null));

        assertNotNull(f2);
        assertEquals("bearerToken is required", f2.getMessage());
    }

    @Test
    void processResultNotification() {
        when(camel.toStream(eq(SUBMIT_INGESTION_AMBROSIA_REQUEST), eq(ingestionSummaryEventMessage), eq(
                IngestionSummaryEventMessage.class)))
                .thenReturn(Mono.just(ingestionSummaryEventMessage));

        ArgumentCaptor<IngestionSummary> captor = ArgumentCaptor.forClass(IngestionSummary.class);
        final IngestionSummary res = ingestionService
                .processResultNotification(ingestionSummary, bearerToken)
                .block();

        assertNotNull(res);
        assertEquals(IngestionStatus.COMPLETED, res.getStatus());
        assertEquals(ingestionSummary.getActivityId(), res.getActivityId());
        assertNotNull(res.getId());

        verify(ingestionGateway).updateIngestionSummary(captor.capture());

        final IngestionSummary captured = captor.getValue();

        assertNotNull(captured);
        assertNotEquals(res, captured);
    }

    @Test
    void processErrorNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.processErrorNotification(null));

        assertNotNull(f1);
        assertEquals("ingestionSummary is required", f1.getMessage());
    }

    @Test
    void processErrorNotification() {
        ArgumentCaptor<IngestionSummary> captor = ArgumentCaptor.forClass(IngestionSummary.class);
        IngestionSummary summary = ingestionSummary;
        summary.setStatus(IngestionStatus.FAILED);
        when(ingestionGateway.findIngestionSummary(any(UUID.class))).thenReturn(Mono.just(summary));
        final IngestionSummary res = ingestionService
                .processErrorNotification(ingestionSummary)
                .block();

        assertNotNull(res);
        assertEquals(IngestionStatus.FAILED, res.getStatus());

        verify(ingestionGateway).updateIngestionSummary(captor.capture());
        final IngestionSummary captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);
    }

    @Test
    public void publishToSQS() {
        when(camel.toStream(eq(SUBMIT_INGESTION_AMBROSIA_REQUEST), eq(ingestionSummaryEventMessage), eq(
                IngestionSummaryEventMessage.class)))
                .thenReturn(Mono.just(ingestionSummaryEventMessage));

        final IngestionSummary res = ingestionService
                .publishToSQS(ingestionSummary, SUBMIT_INGESTION_AMBROSIA_REQUEST, bearerToken)
                .block();

        assertNotNull(res);
        assertEquals(IngestionStatus.COMPLETED, res.getStatus());
        assertNotNull(res.getId());

        verify(camel).toStream(eq(SUBMIT_INGESTION_AMBROSIA_REQUEST), eq(ingestionSummaryEventMessage), eq(
                IngestionSummaryEventMessage.class));
    }

    @Test
    void processAmbrosiaResultNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.processAmbrosiaResultNotification(null));

        assertNotNull(f1);
        assertEquals("ingestionSummary is required", f1.getMessage());
    }

    @Test
    void processAmbrosiaResultNotification() {
        ArgumentCaptor<IngestionSummary> captor = ArgumentCaptor.forClass(IngestionSummary.class);
        final IngestionSummary res = ingestionService
                .processAmbrosiaResultNotification(ingestionSummary)
                .block();

        assertNotNull(res);
        assertEquals(IngestionStatus.COMPLETED, res.getStatus());
        assertNotNull(res.getId());

        verify(ingestionGateway).updateIngestionSummary(captor.capture());

        final IngestionSummary captured = captor.getValue();

        assertNotNull(captured);
        assertNotEquals(res, captured);
    }

    @Test
    void processEventLogResultNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.processEventLogResultNotification(null));

        assertNotNull(f1);
        assertEquals("ingestionEvent is required", f1.getMessage());
    }

    @Test
    void processEventLogResultNotification() {
        ArgumentCaptor<IngestionEvent> captor = ArgumentCaptor.forClass(IngestionEvent.class);
        final IngestionEvent res = ingestionService
                .processEventLogResultNotification(ingestionEvent)
                .block();

        assertNotNull(res);
        assertEquals(IngestionEventType.ERROR, res.getEventType());
        assertNotNull(res.getIngestionId());

        verify(ingestionGateway).persist(captor.capture());

        final IngestionEvent captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);
    }

    @Test
    void createSignedUrl() throws MalformedURLException {
        URL signedUrl = new URL("http://example-signed-url.com");

        when(s3ClientService.signUrl(any(String.class), any(String.class))).thenReturn(signedUrl);
        IngestionPayload payload = ingestionService.createSignedUrl(ingestionId, fileName)
                .block();

        assert payload != null;
        assertNotNull(payload.getIngestionId());
        assertEquals(signedUrl, payload.getSignedUrl());
    }

    @Test
    void createSignedUrl_noIngestionId() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.createSignedUrl(null, fileName));

        assertNotNull(f1);
        assertEquals("ingestionId is required", f1.getMessage());
    }


    @Test
    void createSignedUrl_noFileName() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.createSignedUrl(ingestionId, null));

        assertNotNull(f1);
        assertEquals("fileName is required", f1.getMessage());
    }

    @Test
    void updateIngestionStatus() {
        IngestionSummary summary = ingestionService.updateIngestionStatus(ingestionId, projectId, IngestionStatus.UPLOADING)
                .block();

        assert summary != null;
        assertNotNull(summary.getId());
        assertEquals(projectId, summary.getProjectId());
        assertEquals(workspaceId, summary.getWorkspaceId());
        assertEquals(configFields, summary.getConfigFields());
        assertEquals(creatorId, summary.getCreatorId());
        assertEquals(ambrosiaUrl, summary.getAmbrosiaUrl());
        assertEquals(IngestionStatus.COMPLETED, summary.getStatus());
        assertEquals(courseName, summary.getCourseName());
        assertEquals(ingestStats, summary.getIngestionStats());
    }

    @Test
    void updateIngestionSummary() {
        String url = "http://newAmbrosiaLocation";
        IngestionSummary returnSummary = ingestionSummary;
        returnSummary.setAmbrosiaUrl(url);
        returnSummary.setStatus(IngestionStatus.UPLOAD_FAILED);
        when(ingestionGateway.findIngestionSummary(any(UUID.class))).thenReturn(Mono.just(returnSummary));
        IngestionSummary summary = ingestionService.updateIngestionSummary(new IngestionSummary().setId(ingestionId)
                                                                                   .setProjectId(projectId)
                                                                                   .setStatus(IngestionStatus.UPLOAD_FAILED)
                                                                                   .setAmbrosiaUrl(url)
                                                                                   .setIngestionStats(ingestStats))
                .block();

        assert summary != null;
        assertNotNull(summary.getId());
        assertEquals(projectId, summary.getProjectId());
        assertEquals(workspaceId, summary.getWorkspaceId());
        assertEquals(configFields, summary.getConfigFields());
        assertEquals(creatorId, summary.getCreatorId());
        assertEquals(url, summary.getAmbrosiaUrl());
        assertEquals(IngestionStatus.UPLOAD_FAILED, summary.getStatus());
        assertEquals(courseName, summary.getCourseName());
        assertEquals(ingestStats, summary.getIngestionStats());
    }

    @Test
    void processS3UploadEvent_noIngestionId() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> ingestionService.processS3UploadEvent(null));

        assertNotNull(f1);
        assertEquals("ingestionId is required", f1.getMessage());
    }

    @Test
    void processS3UploadEvent() {
        ArgumentCaptor<IngestionSummary> captor = ArgumentCaptor.forClass(IngestionSummary.class);

        final IngestionSummary result = ingestionService.processS3UploadEvent(ingestionId)
                .block();

        assertNotNull(result);
        assertEquals(IngestionStatus.UPLOADED, result.getStatus());
        assertNotNull(result.getId());

        verify(ingestionGateway).updateIngestionStatus(captor.capture());
        final IngestionSummary captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(result, captured);
    }

    @Test
    void deleteById_noIngestionId() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> ingestionService.deleteById(null));

        assertNotNull(f1);
        assertEquals("ingestionId is required", f1.getMessage());
    }

    @Test
    void deleteById() {
        IngestionSummary summary = ingestionService.deleteById(ingestionId).block();

        verify(ingestionGateway).findIngestionSummary(ingestionId);
        assert summary != null;
        verify(ingestionGateway).delete(summary);
        assertNotNull(summary.getId());
        assertEquals(projectId, summary.getProjectId());
        assertEquals(workspaceId, summary.getWorkspaceId());
        assertEquals(configFields, summary.getConfigFields());
        assertEquals(creatorId, summary.getCreatorId());
        assertEquals(IngestionStatus.COMPLETED, summary.getStatus());
        assertEquals(courseName, summary.getCourseName());
        assertEquals(ingestStats, summary.getIngestionStats());
    }


    @Test
    void getIngestionPayload() {
        IngestionSummaryPayload summary = ingestionService.getIngestionPayload(ingestionId).block();

        verify(ingestionGateway).findIngestionSummary(ingestionId);
        assert summary != null;
        assertEquals(ingestionSummaryPayload, summary);
    }

    @Test
    void processResultNotification_noIngestion() {
        when(ingestionGateway.findIngestionSummary(ingestionId)).thenReturn(Mono.empty());
        NotFoundFault f = assertThrows(NotFoundFault.class, () ->
                ingestionService.processResultNotification(ingestionSummary, bearerToken).block());
        TestCase.assertEquals("Ingestion summary not found", f.getMessage());

    }
    @Test
    void processErrorNotification_noIngestion() {
        when(ingestionGateway.findIngestionSummary(ingestionId)).thenReturn(Mono.empty());
        NotFoundFault f = assertThrows(NotFoundFault.class, () ->
                ingestionService.processErrorNotification(ingestionSummary).block());
        TestCase.assertEquals("Ingestion summary not found", f.getMessage());

    }
    @Test
    void processAmbrosiaResultNotification_noIngestion() {
        when(ingestionGateway.findIngestionSummary(ingestionId)).thenReturn(Mono.empty());
        NotFoundFault f = assertThrows(NotFoundFault.class, () ->
                ingestionService.processAmbrosiaResultNotification(ingestionSummary).block());
        TestCase.assertEquals("Ingestion summary not found", f.getMessage());

    }
}
