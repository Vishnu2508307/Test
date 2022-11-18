package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.data.FeedbackConfig;
import com.smartsparrow.courseware.data.FeedbackGateway;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.lang.ParentInteractiveNotFoundException;
import com.smartsparrow.courseware.payload.FeedbackPayload;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

public class FeedbackServiceTest {

    @InjectMocks
    private FeedbackService feedbackService;

    @Mock
    private FeedbackGateway feedbackGateway;

    @Mock
    private PluginService pluginService;

    @Mock
    private InteractiveService interactiveService;

    @Mock
    private CoursewareAssetService coursewareAssetService;

    @Mock
    private Asset asset;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID feedbackId = UUID.randomUUID();
    private static final String pluginVersionExpr = "1.2.3";
    private static final String config = "{someConfig}";

    private Feedback feedback = new Feedback()
            .setId(feedbackId)
            .setPluginId(pluginId)
            .setPluginVersionExpr(pluginVersionExpr);

    private FeedbackConfig feedbackConfig = new FeedbackConfig()
            .setFeedbackId(feedbackId)
            .setConfig(config);

    private static final PluginSummary plugin = new PluginSummary()
            .setType(PluginType.COMPONENT)
            .setId(pluginId)
            .setName("Test Plugin");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        when(pluginService.fetchPluginFiltersByIdVersionExpr(any(), any())).thenReturn(Mono.just(pluginFilterList));
    }

    @Test
    void createFeedback_nullInteractionId() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> feedbackService.create(null, null, null));

        assertEquals(e.getMessage(), "interactive id required");
    }

    @Test
    void createFeedback_nullPluginId() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> feedbackService.create(interactiveId, null, null));

        assertEquals(e.getMessage(), "plugin id required");
    }

    @Test
    void createFeedback_nullPluginVersion() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> feedbackService.create(interactiveId, pluginId, null));

        assertEquals(e.getMessage(), "plugin version required");
    }

    @Test
    void createFeedback_valid() throws Exception {

        when(pluginService.findLatestVersion(any(UUID.class), any(String.class)))
                .thenReturn(Mono.just(pluginVersionExpr));
        when(feedbackGateway.persist(any(Feedback.class), any(UUID.class))).thenReturn(Mono.empty());
        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.empty());

        Feedback result = feedbackService.create(interactiveId, pluginId, pluginVersionExpr).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(feedback.getPluginId(), result.getPluginId());
        assertEquals(feedback.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @Test
    void replaceFeedbackConfig_nullFeedbackId() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> feedbackService.replace(null, null));

        assertEquals(e.getMessage(), "feedback id required");
    }

    @Test
    void replaceFeedbackConfig_nullConfig() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> feedbackService.replace(feedbackId, null));

        assertEquals(e.getMessage(), "config is required");
    }

    @Test
    void replaceFeedbackConfig_valid() {
        when(feedbackGateway.findByFeedbackId(any(UUID.class))).thenReturn(Mono.just(feedback));
        when(feedbackGateway.persist(any(FeedbackConfig.class))).thenReturn(Mono.empty());

        FeedbackConfig result = feedbackService
                .replace(feedbackConfig.getFeedbackId(), feedbackConfig.getConfig()).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(feedbackConfig.getFeedbackId(), result.getFeedbackId());
        assertEquals(feedbackConfig.getConfig(), result.getConfig());
    }

    @Test
    void getFeedbackPayload_byFeedback() {
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(plugin));
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.just(interactiveId));
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.just(new FeedbackConfig().setConfig("Test Config")));
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(asset)
                .putSource("original", data);
        when(coursewareAssetService.getAssetPayloads(feedbackId)).thenReturn(Mono.just(Lists.newArrayList(assetPayload)));

        FeedbackPayload payload = feedbackService.getFeedbackPayload(feedback).block();

        assertNotNull(payload);
        assertEquals(feedbackId, payload.getFeedbackId());
        assertEquals(interactiveId, payload.getInteractiveId());
        assertEquals("Test Config", payload.getConfig());
        assertEquals(pluginId, payload.getPlugin().getPluginId());
        assertEquals(PluginType.COMPONENT, payload.getPlugin().getType());
        assertEquals("Test Plugin", payload.getPlugin().getName());
        assertEquals(pluginVersionExpr, payload.getPlugin().getVersionExpr());
        assertEquals(1, payload.getAssets().size());
        assertEquals(assetPayload, payload.getAssets().get(0));
    }

    @Test
    void getFeedbackPayload_byFeedback_noConfig() {
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(plugin));
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.just(interactiveId));
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.empty());
        when(coursewareAssetService.getAssetPayloads(feedbackId)).thenReturn(Mono.empty());

        FeedbackPayload payload = feedbackService.getFeedbackPayload(feedback).block();

        assertNotNull(payload);
        assertEquals("", payload.getConfig());
        assertNull(payload.getAssets());
    }

    @Test
    void getFeedbackPayload_byFeedback_noPlugin() {
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.empty());
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.just(interactiveId));
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.getFeedbackPayload(feedback))
                .expectError(PluginNotFoundFault.class)
                .verify();
    }

    @Test
    void getFeedbackPayload_byFeedback_noInteractiveId() {
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(plugin));
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.empty());
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.getFeedbackPayload(feedback))
                .expectError(ParentInteractiveNotFoundException.class)
                .verify();
    }

    @Test
    void getFeedbackPayload_byId() {
        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(Mono.just(feedback));
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(plugin));
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.just(interactiveId));
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.empty());
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(asset)
                .putSource("original", data);
        when(coursewareAssetService.getAssetPayloads(feedbackId)).thenReturn(Mono.just(Lists.newArrayList(assetPayload)));

        FeedbackPayload payload = feedbackService.getFeedbackPayload(feedbackId).block();

        assertNotNull(payload);
        assertEquals(feedbackId, payload.getFeedbackId());
        assertEquals(interactiveId, payload.getInteractiveId());
        assertEquals(pluginId, payload.getPlugin().getPluginId());
        assertEquals(PluginType.COMPONENT, payload.getPlugin().getType());
        assertEquals("Test Plugin", payload.getPlugin().getName());
        assertEquals(pluginVersionExpr, payload.getPlugin().getVersionExpr());
        assertEquals(1, payload.getAssets().size());
        assertEquals(assetPayload, payload.getAssets().get(0));
    }

    @Test
    void getFeedbackPayload_byId_noPlugin() {
        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(Mono.just(feedback));
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.empty());
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.just(interactiveId));
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.getFeedbackPayload(feedbackId))
                .expectError(PluginNotFoundFault.class)
                .verify();
    }

    @Test
    void getFeedbackPayload_byId_noInteractiveId() {
        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(Mono.just(feedback));
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(plugin));
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.empty());
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.getFeedbackPayload(feedbackId))
                .expectError(ParentInteractiveNotFoundException.class)
                .verify();
    }

    @Test
    void getFeedbackPayload_byId_noFeedback() {
        TestPublisher<Feedback> error = TestPublisher.create();
        error.error(new FeedbackNotFoundException(feedbackId));
        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(error.mono());
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.empty());
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.getFeedbackPayload(feedbackId))
                .expectError(FeedbackNotFoundException.class)
                .verify();
    }

    @Test
    void findParentId_notFound() {
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.findParentId(feedbackId))
                .expectError(ParentInteractiveNotFoundException.class)
                .verify();
    }

    @Test
    void findParentId() {
        when(feedbackGateway.findParent(feedbackId)).thenReturn(Mono.just(interactiveId));

        StepVerifier.create(feedbackService.findParentId(feedbackId))
                .expectNext(interactiveId)
                .verifyComplete();
    }

    @Test
    void delete() {
        when(feedbackGateway.deleteRelationship(feedbackId, interactiveId)).thenReturn(Flux.empty());

        StepVerifier.create(feedbackService.delete(feedbackId, interactiveId))
                .verifyComplete();
    }

    @Test
    void findById() {
        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(Mono.just(feedback));

        StepVerifier.create(feedbackService.findById(feedbackId))
                .expectNext(feedback)
                .verifyComplete();
    }

    @Test
    void findIdsByInteractive() {
        ArrayList<UUID> ids = Lists.newArrayList(UUIDs.random(), UUIDs.random(), UUIDs.random());
        when(feedbackGateway.findByInteractive(any())).thenReturn(Mono.just(ids));

        StepVerifier.create(feedbackService.findIdsByInteractive(UUIDs.random()))
                .expectNext(ids)
                .verifyComplete();
    }

    @Test
    void duplicate() {
        UUID newInteractiveId = UUID.randomUUID();

        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(Mono.just(feedback));
        when(feedbackGateway.persist(any(), eq(newInteractiveId))).thenReturn(Mono.empty());
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.just(feedbackConfig));
        when(feedbackGateway.persist(any())).thenReturn(Mono.empty());
        when(coursewareAssetService.duplicateAssets(eq(feedbackId), any(), eq(CoursewareElementType.FEEDBACK), any(DuplicationContext.class))).thenReturn(Flux.empty());
        DuplicationContext context = new DuplicationContext();

        Feedback result = feedbackService.duplicate(feedbackId, newInteractiveId, context).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotEquals(feedback.getId(), result.getId());
        assertNotEquals(feedbackId, result.getId());
        assertEquals(feedback.getPluginId(), result.getPluginId());
        assertEquals(feedback.getPluginVersionExpr(), result.getPluginVersionExpr());
        assertEquals(1, context.getIdsMap().size());
        assertEquals(result.getId(), context.getIdsMap().get(feedbackId));
        assertTrue(context.getScenarios().isEmpty());
        verify(coursewareAssetService).duplicateAssets(eq(feedbackId), eq(result.getId()), eq(CoursewareElementType.FEEDBACK), eq(context));
    }

    @Test
    void duplicate_noConfig() {
        UUID newInteractiveId = UUID.randomUUID();

        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(Mono.just(feedback));
        when(feedbackGateway.persist(any(), eq(newInteractiveId))).thenReturn(Mono.empty());
        when(feedbackGateway.findLatestConfig(feedbackId)).thenReturn(Mono.empty());
        when(coursewareAssetService.duplicateAssets(eq(feedbackId), any(), eq(CoursewareElementType.FEEDBACK), any(DuplicationContext.class))).thenReturn(Flux.empty());

        Feedback result = feedbackService.duplicate(feedbackId, newInteractiveId, new DuplicationContext()).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotEquals(feedback.getId(), result.getId());
        assertNotEquals(feedbackId, result.getId());
        assertEquals(feedback.getPluginId(), result.getPluginId());
        assertEquals(feedback.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @Test
    void duplicate_feedbackNotFound() {
        UUID newInteractiveId = UUID.randomUUID();
        TestPublisher<Feedback> errorFeedback = TestPublisher.create();
        errorFeedback.error(new FeedbackNotFoundException(feedbackId));
        when(feedbackGateway.findByFeedbackId(feedbackId)).thenReturn(errorFeedback.mono());
        when(feedbackGateway.persist(any(), eq(newInteractiveId))).thenReturn(Mono.empty());
        DuplicationContext context = new DuplicationContext();

        assertThrows(FeedbackNotFoundException.class, () -> feedbackService.duplicate(feedbackId, newInteractiveId, context).block());
        assertTrue(context.getIdsMap().isEmpty());
    }

    @Test
    void duplicateFeedback() {
        UUID newInteractiveId = UUID.randomUUID();
        when(feedbackGateway.persist(any(), eq(newInteractiveId))).thenReturn(Mono.empty());

        Feedback result = feedbackService.duplicateFeedback(feedback, newInteractiveId).block();

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackGateway).persist(captor.capture(), eq(newInteractiveId));

        assertNotNull(captor.getValue().getId());
        assertEquals(captor.getValue(), result);
        assertNotEquals(feedback.getId(), captor.getValue().getId());
        assertNotEquals(feedbackId, captor.getValue().getId());
        assertEquals(feedback.getPluginId(), captor.getValue().getPluginId());
        assertEquals(feedback.getPluginVersionExpr(), captor.getValue().getPluginVersionExpr());
    }

    @Test
    void duplicateFeedbackConfig() {
        UUID newFeedbackId = UUID.randomUUID();
        when(feedbackGateway.persist(any())).thenReturn(Mono.empty());

        FeedbackConfig result = feedbackService.duplicateFeedbackConfig(feedbackConfig, newFeedbackId, new DuplicationContext()).block();

        verify(feedbackGateway).persist(eq(result));
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotEquals(feedbackConfig.getId(), result.getId());
        assertEquals(config, result.getConfig());
        assertEquals(newFeedbackId, result.getFeedbackId());
    }

    @Test
    void duplicateFeedbackConfig_replaceIds() {
        UUID newFeedbackId = UUID.randomUUID();
        when(feedbackGateway.persist(any())).thenReturn(Mono.empty());
        DuplicationContext context = new DuplicationContext();
        context.putIds(feedbackId, newFeedbackId);
        feedbackConfig.setConfig("{feedbackId:" + feedbackId + "}");

        FeedbackConfig result = feedbackService.duplicateFeedbackConfig(feedbackConfig, newFeedbackId, context).block();

        assertNotNull(result);
        assertEquals("{feedbackId:" + newFeedbackId + "}", result.getConfig());
    }
}
