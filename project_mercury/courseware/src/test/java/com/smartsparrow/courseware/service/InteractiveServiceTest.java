package com.smartsparrow.courseware.service;

import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_B;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.google.common.collect.Sets;
import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.MathAsset;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.FeedbackGateway;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.InteractiveGateway;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.courseware.data.WalkablePathwayChildren;
import com.smartsparrow.courseware.lang.InteractiveAlreadyExistsFault;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

class InteractiveServiceTest {

    @InjectMocks
    private InteractiveService interactiveService;
    @Mock
    private InteractiveGateway interactiveGateway;
    @Mock
    private PluginService pluginService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private ComponentGateway componentGateway;
    @Mock
    private FeedbackGateway feedbackGateway;
    @Mock
    private CoursewareAssetService coursewareAssetService;
    @Mock
    private PathwayGateway pathwayGateway;
    @Mock
    private DocumentItemService documentItemService;
    @Mock
    private CoursewareElementDescriptionService descriptionService;
    @Mock
    private Asset asset;
    @Mock
    private MathAsset mathAsset;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersion = "1.*";
    private static final String config = "configuration";
    private final UUID parentPathwayId = UUIDs.random();

    private final Interactive interactive = new Interactive()
            .setId(interactiveId)
            .setPluginId(pluginId)
            .setPluginVersionExpr("1.0");

    private final PluginSummary pluginSummary = new PluginSummary()
            .setId(pluginId)
            .setLatestVersion("2.0")
            .setType(PluginType.SCREEN)
            .setName("Int plugin");

    private final InteractiveConfig interactiveConfig = new InteractiveConfig()
            .setConfig(config)
            .setId(UUID.randomUUID())
            .setInteractiveId(interactiveId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(interactiveGateway.findById(eq(interactiveId))).thenReturn(Mono.just(interactive));
        when(interactiveGateway.findLatestConfig(interactiveId)).thenReturn(Mono.just(interactiveConfig));
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(pluginSummary));
        when(interactiveGateway.findParent(interactiveId)).thenReturn(Mono.just(parentPathwayId));
        when(componentGateway.findComponentIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackGateway.findByInteractive(interactiveId)).thenReturn(Mono.empty());

        when(documentItemService.findAllLinked(interactiveId)).thenReturn(Flux.empty());
        when(coursewareAssetService.getAssetPayloads(interactiveId)).thenReturn(Mono.empty());
        when(coursewareAssetService
                     .fetchMathAssetsForElement(interactiveId)).thenReturn(Mono.empty());
        when(descriptionService.fetchCoursewareDescriptionByElement(interactiveId))
                .thenReturn(Mono.just(new CoursewareElementDescription(interactiveId, CoursewareElementType.INTERACTIVE, "Interactive Description")));

        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        when(pluginService.fetchPluginFiltersByIdVersionExpr(any(), any())).thenReturn(Mono.just(pluginFilterList));
    }

    @Test
    void create_success() throws VersionParserFault {
        UUID creatorId = UUID.randomUUID();
        UUID pathwayId = UUID.randomUUID();
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersion))).thenReturn(Mono.just("1.0.0"));
        when(interactiveGateway.persist(any(Interactive.class))).thenReturn(Mono.empty());
        when(interactiveGateway.persistParent(any(UUID.class), eq(pathwayId))).thenReturn(Flux.empty());
        when(pathwayGateway.persistChild(any(UUID.class), eq(CoursewareElementType.INTERACTIVE), eq(pathwayId))).thenReturn(Flux.empty());
        when(pathwayService.findById(eq(pathwayId))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.create(creatorId, pathwayId, pluginId, pluginVersion))
                .expectNextMatches(interactive -> {
                    assertNotNull(interactive.getId());
                    assertEquals(pluginId, interactive.getPluginId());
                    assertEquals(pluginVersion, interactive.getPluginVersionExpr());
                    assertNotNull(interactive.getStudentScopeURN());
                    assertEquals(EvaluationMode.DEFAULT, interactive.getEvaluationMode());
                    return true;
                })
                .verifyComplete();

        ArgumentCaptor<Interactive> captor = ArgumentCaptor.forClass(Interactive.class);
        verify(interactiveGateway).persist(captor.capture());
        assertNotNull(captor.getValue().getId());
        assertEquals(pluginId, captor.getValue().getPluginId());
        assertEquals(pluginVersion, captor.getValue().getPluginVersionExpr());
        verify(interactiveGateway).persistParent(captor.getValue().getId(), pathwayId);
        verify(pathwayGateway).persistChild(captor.getValue().getId(), CoursewareElementType.INTERACTIVE, pathwayId);
    }

    @Test
    void create_successIdProvided() throws VersionParserFault {
        UUID creatorId = UUID.randomUUID();
        UUID pathwayId = UUID.randomUUID();
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersion))).thenReturn(Mono.just("1.0.0"));
        when(interactiveGateway.findById(eq(interactiveId))).thenReturn(Mono.empty());
        when(interactiveGateway.persist(any(Interactive.class))).thenReturn(Mono.empty());
        when(interactiveGateway.persistParent(any(UUID.class), eq(pathwayId))).thenReturn(Flux.empty());
        when(pathwayGateway.persistChild(any(UUID.class), eq(CoursewareElementType.INTERACTIVE), eq(pathwayId))).thenReturn(Flux.empty());
        when(pathwayService.findById(eq(pathwayId))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.create(creatorId, pathwayId, pluginId, pluginVersion, interactiveId))
                .expectNextMatches(interactive -> {
                    assertNotNull(interactive.getId());
                    assertEquals(pluginId, interactive.getPluginId());
                    assertEquals(pluginVersion, interactive.getPluginVersionExpr());
                    assertNotNull(interactive.getStudentScopeURN());
                    return true;
                })
                .verifyComplete();

        ArgumentCaptor<Interactive> captor = ArgumentCaptor.forClass(Interactive.class);
        verify(interactiveGateway).persist(captor.capture());
        assertNotNull(captor.getValue().getId());
        assertEquals(pluginId, captor.getValue().getPluginId());
        assertEquals(pluginVersion, captor.getValue().getPluginVersionExpr());
        verify(interactiveGateway).persistParent(captor.getValue().getId(), pathwayId);
        verify(pathwayGateway).persistChild(captor.getValue().getId(), CoursewareElementType.INTERACTIVE, pathwayId);
    }

    @Test
    void create_IdProvidedInteractiveFound() throws VersionParserFault {
        UUID creatorId = UUID.randomUUID();
        UUID pathwayId = UUID.randomUUID();
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersion))).thenReturn(Mono.just("1.0.0"));
        when(interactiveGateway.persist(any(Interactive.class))).thenReturn(Mono.empty());
        when(interactiveGateway.persistParent(any(UUID.class), eq(pathwayId))).thenReturn(Flux.empty());
        when(pathwayGateway.persistChild(any(UUID.class), eq(CoursewareElementType.INTERACTIVE), eq(pathwayId))).thenReturn(Flux.empty());
        when(pathwayService.findById(eq(pathwayId))).thenReturn(Mono.empty());

        assertThrows(InteractiveAlreadyExistsFault.class, () -> interactiveService.create(creatorId, pathwayId, pluginId, pluginVersion, interactiveId).block());
        verify(interactiveGateway).findById(interactiveId);
        verify(interactiveGateway, never()).persist(any(Interactive.class));
    }


    @SuppressWarnings("unchecked")
    @Test
    void create_pluginNotFound() throws VersionParserFault {
        UUID creatorId = UUID.randomUUID();
        UUID pathwayId = UUID.randomUUID();

        Mono mono = TestPublisher.create().error(new PluginNotFoundFault("PluginNotFoundFault")).mono();

        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersion))).thenReturn(mono);
        when(interactiveGateway.persist(any(Interactive.class))).thenReturn(Mono.empty());
        when(pathwayService.findById(eq(pathwayId))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.create(creatorId, pathwayId, pluginId, pluginVersion))
                .expectError(PluginNotFoundFault.class)
                .verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    void create_versionParserException() throws VersionParserFault {
        UUID creatorId = UUID.randomUUID();
        UUID pathwayId = UUID.randomUUID();

        Mono mono = TestPublisher.create().error(new VersionParserFault("VersionParserFault")).mono();

        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersion))).thenReturn(mono);
        when(interactiveGateway.persist(any(Interactive.class))).thenReturn(Mono.empty());
        when(pathwayService.findById(eq(pathwayId))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.create(creatorId, pathwayId, pluginId, pluginVersion))
                .expectError(VersionParserFault.class)
                .verify();
    }

    @Test
    void findById() {

    }

    @Test
    void findById_noInteractive() {
        when(interactiveGateway.findById(eq(interactiveId))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.findById(interactiveId))
                .expectError(InteractiveNotFoundException.class)
                .verify();
    }

    @Test
    void replaceConfig() {
        UUID creatorId = UUID.randomUUID();
        when(interactiveGateway.persist(any(InteractiveConfig.class))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.replaceConfig(creatorId, interactiveId, config))
                .expectNextMatches(interactiveConfig -> {
                    assertNotNull(interactiveConfig.getId());
                    assertEquals(config, interactiveConfig.getConfig());
                    assertEquals(interactiveId, interactiveConfig.getInteractiveId());
                    return true;
                })
                .verifyComplete();

        ArgumentCaptor<InteractiveConfig> captor = ArgumentCaptor.forClass(InteractiveConfig.class);
        verify(interactiveGateway).persist(captor.capture());
        assertNotNull(captor.getValue().getId());
        assertEquals(config, captor.getValue().getConfig());
        assertEquals(interactiveId, captor.getValue().getInteractiveId());
    }

    @Test
    void replaceConfig_noInteractive() {
        UUID creatorId = UUID.randomUUID();
        when(interactiveGateway.findById(eq(interactiveId))).thenReturn(Mono.empty());
        when(interactiveGateway.persist(any(InteractiveConfig.class))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.replaceConfig(creatorId, interactiveId, config))
                .expectError(InteractiveNotFoundException.class)
                .verify();
    }

    @Test
    void findLatestConfig() {
        StepVerifier.create(interactiveService.findLatestConfig(interactiveId)).expectNext(interactiveConfig).verifyComplete();
    }

    @Test
    void findLatestConfig_noConfig() {
        when(interactiveGateway.findLatestConfig(eq(interactiveId))).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.findLatestConfig(interactiveId)).verifyComplete();
    }

    @Test
    void duplicateInteractive() {
        Interactive oldInteractive = new Interactive()
                .setEvaluationMode(EvaluationMode.COMBINED)
                .setId(interactiveId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersion);
        when(interactiveGateway.persist(any(Interactive.class))).thenReturn(Mono.empty());

        Interactive result = interactiveService.duplicateInteractive(oldInteractive).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotEquals(interactiveId, result.getId());
        assertEquals(pluginId, result.getPluginId());
        assertEquals(pluginVersion, result.getPluginVersionExpr());
        assertEquals(oldInteractive.getEvaluationMode(), result.getEvaluationMode());
        verify(interactiveGateway).persist(eq(result));
    }

    @Test
    void duplicateInteractiveConfig() {
        UUID newInteractiveId = UUID.randomUUID();
        when(interactiveGateway.persist(any(InteractiveConfig.class))).thenReturn(Mono.empty());

        InteractiveConfig result = interactiveService.duplicateInteractiveConfig(interactiveId, newInteractiveId, new DuplicationContext()).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotEquals(interactiveConfig.getId(), result.getId());
        assertEquals(config, result.getConfig());
        assertEquals(newInteractiveId, result.getInteractiveId());
        verify(interactiveGateway).persist(eq(result));
    }

    @Test
    void duplicateInteractiveConfig_noLatestConfig() {
        UUID newInteractiveId = UUID.randomUUID();
        when(interactiveGateway.findLatestConfig(interactiveId)).thenReturn(Mono.empty());

        InteractiveConfig result = interactiveService.duplicateInteractiveConfig(interactiveId, newInteractiveId, new DuplicationContext()).block();

        assertNull(result);
        verify(interactiveGateway, never()).persist(eq(result));
    }

    @Test
    void duplicateInteractiveConfig_replaceIds() {
        interactiveConfig.setConfig("{interactiveId:" + interactiveId + "}");
        UUID newInteractiveId = UUID.randomUUID();
        when(interactiveGateway.persist(any(InteractiveConfig.class))).thenReturn(Mono.empty());
        DuplicationContext context = new DuplicationContext();
        context.putIds(interactiveId, newInteractiveId);

        InteractiveConfig result = interactiveService.duplicateInteractiveConfig(interactiveId, newInteractiveId, context).block();

        assertNotNull(result);
        assertEquals("{interactiveId:" + newInteractiveId + "}", result.getConfig());
    }

    @Test
    void getInteractivePayload_success() {
        UUID studentScopeURN = UUIDs.random();
        Interactive interactive = new Interactive()
                .setId(interactiveId)
                .setPluginId(pluginId)
                .setPluginVersionExpr("1.0")
                .setStudentScopeURN(studentScopeURN);
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(asset)
                .putSource("original", data);
        List<UUID> components = Lists.newArrayList(UUIDs.random(), UUIDs.random());
        List<UUID> feedbacks = Lists.newArrayList(UUIDs.random(), UUIDs.random());
        List<AssetPayload> assets = Lists.newArrayList(assetPayload);

        when(componentGateway.findComponentIdsByInteractive(eq(interactiveId))).thenReturn(Flux.fromIterable(components));
        when(feedbackGateway.findByInteractive(eq(interactiveId))).thenReturn(Mono.just(feedbacks));
        when(coursewareAssetService.getAssetPayloads(interactiveId)).thenReturn(Mono.just(assets));

        StepVerifier.create(interactiveService.getInteractivePayload(interactive))
                .assertNext(payload -> {
                    assertPayload(payload, interactive, pluginSummary, parentPathwayId, interactiveConfig, components,
                            feedbacks, assets);
                    assertEquals(studentScopeURN, payload.getStudentScopeURN());
                })
                .verifyComplete();

    }

    @Test
    void getInteractivePayload_byInteractiveId_success() {
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(asset)
                .putSource("original", data);
        List<UUID> components = Lists.newArrayList(UUIDs.random(), UUIDs.random());
        List<UUID> feedbacks = Lists.newArrayList(UUIDs.random(), UUIDs.random());
        List<AssetPayload> assets = Lists.newArrayList(assetPayload);

        when(componentGateway.findComponentIdsByInteractive(eq(interactiveId))).thenReturn(Flux.fromIterable(components));
        when(feedbackGateway.findByInteractive(eq(interactiveId))).thenReturn(Mono.just(feedbacks));
        when(coursewareAssetService.getAssetPayloads(interactiveId)).thenReturn(Mono.just(assets));

        StepVerifier.create(interactiveService.getInteractivePayload(interactiveId))
                .assertNext(payload -> assertPayload(payload, interactive, pluginSummary, parentPathwayId, interactiveConfig, components,
                        feedbacks, assets))
                .verifyComplete();
    }

    private void assertPayload(InteractivePayload actualPayload, Interactive interactive, PluginSummary pluginSummary, UUID parentPathwayId,
                               InteractiveConfig config, List<UUID> components, List<UUID> feedbacks, List<AssetPayload> assets) {
        assertEquals(interactive.getId(), actualPayload.getInteractiveId());
        assertEquals(config.getConfig(), actualPayload.getConfig());
        assertEquals(interactive.getPluginId(), actualPayload.getPlugin().getPluginId());
        assertEquals(interactive.getPluginVersionExpr(), actualPayload.getPlugin().getVersionExpr());
        assertEquals(pluginSummary.getName(), actualPayload.getPlugin().getName());
        assertEquals(pluginSummary.getType(), actualPayload.getPlugin().getType());
        assertEquals(parentPathwayId, actualPayload.getParentPathwayId());
        assertEquals(components, actualPayload.getComponents());
        assertEquals(feedbacks, actualPayload.getFeedbacks());
        assertEquals(assets, actualPayload.getAssets());
        assertEquals("Interactive Description", actualPayload.getDescription());
    }

    @Test
    void getInteractivePayload_noInteractiveId() {
        assertThrows(IllegalArgumentException.class, () -> interactiveService.getInteractivePayload((UUID) null));
    }

    @Test
    void getInteractivePayload_noInteractive() {
        assertThrows(IllegalArgumentException.class, () -> interactiveService.getInteractivePayload((Interactive) null));
    }

    @Test
    void getInteractivePayload_noParentPathwayId() {
        when(interactiveGateway.findParent(any())).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.getInteractivePayload(interactive))
                .expectError(ParentPathwayNotFoundException.class)
                .verify();
    }

    @Test
    void getInteractivePayload_pluginNotFound() {
        when(pluginService.fetchById(any())).thenReturn(Mono.empty());

        StepVerifier.create(interactiveService.getInteractivePayload(interactive))
                .expectError(PluginNotFoundFault.class)
                .verify();
    }

    @Test
    void getInteractivePayload_noAsset() {
        when(coursewareAssetService.getAssetPayloads(interactiveId)).thenReturn(Mono.empty());

        InteractivePayload result = interactiveService.getInteractivePayload(interactive).block();

        assertNotNull(result);
        assertNull(result.getAssets());
    }

    @Test
    void getInteractivePayload_byInteractiveId_noAsset() {
        when(coursewareAssetService.getAssetPayloads(interactiveId)).thenReturn(Mono.empty());

        InteractivePayload result = interactiveService.getInteractivePayload(interactiveId).block();

        assertNotNull(result);
        assertNull(result.getAssets());
    }

    @Test
    void getInteractivePayload_byInteractiveId_withLinkedDocumentItems() {
        when(documentItemService.findAllLinked(any())).thenReturn(Flux.just(ITEM_A, ITEM_B));

        InteractivePayload result = interactiveService.getInteractivePayload(interactiveId).block();

        assertNotNull(result);
        assertEquals(2, result.getLinkedDocumentItems().size());
        assertEquals(Sets.newHashSet(DocumentItemPayload.from(ITEM_A), DocumentItemPayload.from(ITEM_B)),
                Sets.newHashSet(result.getLinkedDocumentItems()));
    }

    @Test
    void delete_interactiveIdNotSupplied() {
        UUID parentPathwayId = UUID.randomUUID();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                interactiveService.delete(null, parentPathwayId));
        assertEquals("interactiveId is required", e.getMessage());
    }

    @Test
    void delete_parentPathwayIdNotSupplied() {
        UUID interactiveId = UUIDs.random();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                interactiveService.delete(interactiveId, null));
        assertEquals("parentPathwayId is required", e.getMessage());
    }

    @Test
    void delete_success() {
        UUID interactiveId = UUIDs.random();
        UUID parentPathwayId = UUID.randomUUID();
        when(interactiveGateway.removeParent(interactiveId)).thenReturn(Flux.just(new Void[]{}));
        when(pathwayGateway.removeChild(interactiveId, CoursewareElementType.INTERACTIVE, parentPathwayId)).thenReturn(Flux.empty());
        interactiveService.delete(interactiveId, parentPathwayId);
        verify(interactiveGateway).removeParent(interactiveId);
        verify(pathwayGateway).removeChild(interactiveId, CoursewareElementType.INTERACTIVE, parentPathwayId);
    }

    @Test
    void move_success() {
        UUID interactiveId = UUIDs.random();
        UUID parentPathwayId = UUID.randomUUID();
        int index = 1;
        UUID destinationPathwayId = UUID.randomUUID();
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(destinationPathwayId);

        when(interactiveGateway.findById(eq(interactiveId))).thenReturn(Mono.just(interactive));
        when(interactiveGateway.removeParent(eq(interactiveId))).thenReturn(Flux.just(new Void[]{}));
        when(pathwayGateway.removeChild(eq(interactiveId), eq(CoursewareElementType.INTERACTIVE),
                                        eq(parentPathwayId))).thenReturn(Flux.empty());
        when(interactiveGateway.persistParent(eq(interactiveId), eq(destinationPathwayId))).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(eq(destinationPathwayId))).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        interactiveService.move(interactiveId, destinationPathwayId, index, parentPathwayId);

        verify(pathwayGateway).removeChild(interactiveId, CoursewareElementType.INTERACTIVE, parentPathwayId);
        verify(pathwayGateway).findWalkableChildren(destinationPathwayId);
    }

    @Test
    void saveRelationship_withIndex() {
        UUID oldChildId = UUID.randomUUID();
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 0;
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(parentPathwayId)
                .addWalkable(oldChildId, "ACTIVITY");
        when(interactiveGateway.persistParent(interactiveId, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        interactiveService.saveToPathway(interactiveId, parentPathwayId, index).blockLast();

        ArgumentCaptor<WalkablePathwayChildren> captor = ArgumentCaptor.forClass(WalkablePathwayChildren.class);
        verify(pathwayGateway).persist(captor.capture());
        assertEquals(2, captor.getValue().getWalkableIds().size());
        assertEquals(interactiveId, captor.getValue().getWalkableIds().get(0));
        assertEquals(oldChildId, captor.getValue().getWalkableIds().get(1));
        assertEquals(2, captor.getValue().getWalkableTypes().size());
        assertEquals("INTERACTIVE", captor.getValue().getWalkableTypes().get(interactiveId));
    }

    @Test
    void saveRelationship_indexOutOfBoundsException() {
        UUID oldChildId = UUID.randomUUID();
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 2;
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(parentPathwayId)
                .addWalkable(oldChildId, "ACTIVITY");
        when(interactiveGateway.persistParent(interactiveId, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        assertThrows(IndexOutOfBoundsException.class,
                () -> interactiveService.saveToPathway(interactiveId, parentPathwayId, index).blockLast());
    }

    @Test
    void saveRelationship_emptyChildren() {
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 0;
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(parentPathwayId);
        when(interactiveGateway.persistParent(interactiveId, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        interactiveService.saveToPathway(interactiveId, parentPathwayId, index).blockLast();

        ArgumentCaptor<WalkablePathwayChildren> captor = ArgumentCaptor.forClass(WalkablePathwayChildren.class);
        verify(pathwayGateway).persist(captor.capture());
        assertEquals(1, captor.getValue().getWalkableIds().size());
        assertEquals(interactiveId, captor.getValue().getWalkableIds().get(0));
        assertEquals(1, captor.getValue().getWalkableTypes().size());
    }

    @Test
    void saveRelationship_childrenNotFound() {
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 0;
        when(interactiveGateway.persistParent(interactiveId, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.empty());
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        interactiveService.saveToPathway(interactiveId, parentPathwayId, index).blockLast();

        ArgumentCaptor<WalkablePathwayChildren> captor = ArgumentCaptor.forClass(WalkablePathwayChildren.class);
        verify(pathwayGateway).persist(captor.capture());
        assertEquals(1, captor.getValue().getWalkableIds().size());
        assertEquals(interactiveId, captor.getValue().getWalkableIds().get(0));
        assertEquals(1, captor.getValue().getWalkableTypes().size());
    }

    @Test
    void findInteractiveIds_noPluginId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                                  () -> interactiveService.findInteractiveIds(null).block());

        assertEquals("plugin Id is required", e.getMessage());
    }

    @Test
    void findInteractiveIds_success() {
        when(interactiveGateway.findInteractiveIdsByPluginId(pluginId)).thenReturn(Flux.just(interactiveId,interactiveId));

        List<UUID> interactiveList = interactiveService.findInteractiveIds(pluginId).block();

        assertNotNull(interactiveList);

        verify(interactiveGateway).findInteractiveIdsByPluginId(pluginId);
    }

    @Test
    void testHasChildIdsInvalidId() {
        UUID interactiveId = UUID.randomUUID();
        when(componentGateway.findComponentIdsByInteractive(interactiveId)).thenReturn(Flux.empty());

        StepVerifier.create(interactiveService.hasChildComponentIds(interactiveId))
                .expectNext(Boolean.FALSE)
                .verifyComplete();

    }

    @Test
    void testHasChildIdsValidId() {
        UUID interactive = UUID.randomUUID();
        UUID componentIdTwo = UUID.randomUUID();
        when(componentGateway.findComponentIdsByInteractive(interactive)).thenReturn(Flux.just(componentIdTwo));
        StepVerifier.create(interactiveService.hasChildComponentIds(interactive))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
    }

    @Test
    void updateEvaluationMode_nullInteractiveId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> interactiveService.updateEvaluationMode(null, EvaluationMode.DEFAULT));

        assertEquals("interactiveId is required", f.getMessage());
    }

    @Test
    void updateEvaluationMode_nullEvalMode() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> interactiveService.updateEvaluationMode(interactiveId, null));

        assertEquals("evaluationMode is required", f.getMessage());
    }

    @Test
    void updateEvaluationMode() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();

        when(interactiveGateway.updateEvaluationMode(interactiveId, EvaluationMode.COMBINED))
                .thenReturn(publisher.mono());

        interactiveService.updateEvaluationMode(interactiveId, EvaluationMode.COMBINED)
                .block();

        verify(interactiveGateway).updateEvaluationMode(interactiveId, EvaluationMode.COMBINED);
    }

    @Test
    void getInteractivePayload_withMathAssets() {
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(mathAsset)
                .putSource("original", data);
        when(coursewareAssetService.fetchMathAssetsForElement(interactiveId)).thenReturn(Mono.just(Lists.newArrayList(assetPayload)));

        InteractivePayload payload = interactiveService.getInteractivePayload(interactiveId).block();

        assertNotNull(payload);
        assertEquals(1, payload.getMathAssets().size());
        assertEquals(assetPayload, payload.getMathAssets().get(0));
    }
}
