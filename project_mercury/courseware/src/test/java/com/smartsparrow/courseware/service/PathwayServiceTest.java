package com.smartsparrow.courseware.service;

import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.data.WalkablePathwayChildren;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.PathwayAlreadyExistsFault;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayBuilder;
import com.smartsparrow.courseware.pathway.PathwayBuilderStub;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.exception.IllegalArgumentFault;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

class PathwayServiceTest {

    @InjectMocks
    private PathwayService pathwayService;
    @Mock
    private PathwayGateway pathwayGateway;
    @Mock
    private PathwayBuilder pathwayBuilder;
    @Mock
    private ActivityService activityService;
    @Mock
    private CoursewareAssetService coursewareAssetService;
    @Mock
    private CoursewareElementDescriptionService coursewareDescriptionService;
    @Mock
    private Asset asset;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID walkableId1 = UUID.randomUUID();
    private static final UUID walkableId2 = UUID.randomUUID();
    private static final UUID walkableId3 = UUID.randomUUID();
    private static final String config = "{\"foo\":\"bar\"}";
    private static final List<UUID> walkableIds = Lists.newArrayList(walkableId1, walkableId2, walkableId3);
    private WalkablePathwayChildren walkables = buildPathwayChildren(pathwayId, walkableIds,
            Lists.newArrayList("ACTIVITY", "INTERACTIVE", "ACTIVITY"));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        PathwayBuilderStub.mock(pathwayBuilder);

        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.just(walkables));
        when(pathwayGateway.findLatestConfig(pathwayId)).thenReturn(Mono.empty());
        when(coursewareDescriptionService.fetchCoursewareDescriptionByElement(pathwayId))
                .thenReturn(Mono.just(new CoursewareElementDescription(pathwayId,CoursewareElementType.PATHWAY,"Pathway Description")));
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);
    }

    @Test
    void create() {
        UUID creatorId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(activityService.findById(eq(activityId))).thenReturn(Mono.just(new Activity()));
        when(pathwayGateway.persist(any(), any(UUID.class))).thenReturn(Flux.empty());

        Pathway pathway = pathwayService.create(creatorId, activityId, PathwayType.LINEAR, PreloadPathway.FIRST).block();

        assertNotNull(pathway);
        assertNotNull(pathway.getId());
        assertEquals(PathwayType.LINEAR, pathway.getType());
        verify(pathwayGateway, never()).findById(pathwayId);
    }

    @Test
    void create_with_pathwayId() {
        UUID creatorId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(activityService.findById(eq(activityId))).thenReturn(Mono.just(new Activity()));
        when(pathwayGateway.persist(any(), any(UUID.class))).thenReturn(Flux.empty());
        when(pathwayGateway.findById(pathwayId)).thenReturn(Mono.empty());

        Pathway pathway = pathwayService.create(creatorId, activityId, PathwayType.LINEAR, pathwayId, PreloadPathway.NONE).block();

        assertNotNull(pathway);
        assertNotNull(pathway.getId());
        assertEquals(PathwayType.LINEAR, pathway.getType());
        assertEquals(pathwayId, pathway.getId());
        verify(pathwayGateway).findById(pathwayId);
    }

    @Test
    void create_with_pathwayId_conflict() {
        UUID creatorId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        Pathway pathway = mockPathway(pathwayId);
        when(pathwayGateway.findById(eq(pathwayId))).thenReturn(Mono.just(pathway));

        assertThrows(PathwayAlreadyExistsFault.class, () -> pathwayService.create(creatorId, activityId, PathwayType.LINEAR, pathwayId, null).block());
        verify(pathwayGateway).findById(pathwayId);
        verify(activityService, never()).findById(activityId);
        verify(pathwayGateway, never()).persist(pathway, activityId);
    }

    @Test
    void create_invalidActivity() {
        UUID creatorId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(activityService.findById(eq(activityId))).thenThrow(ActivityNotFoundException.class);

        assertThrows(ActivityNotFoundException.class, () -> pathwayService.create(creatorId, activityId, PathwayType.LINEAR, PreloadPathway.ALL).block());
    }

    @SuppressWarnings("unchecked")
    @Test
    void create_exception() {
        UUID creatorId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        Flux createFlux = TestPublisher.create().error(new RuntimeException("exception on pathway create")).flux();
        when(pathwayGateway.persist(any(), any(UUID.class))).thenReturn(createFlux);
        when(activityService.findById(eq(activityId))).thenReturn(Mono.just(new Activity()));

        StepVerifier.create(pathwayService.create(creatorId, activityId, PathwayType.LINEAR, PreloadPathway.ALL)).expectError().verify();
    }

    @Test
    void findById() {
        UUID pathwayId = UUID.randomUUID();
        Pathway pathway = mockPathway(pathwayId);
        when(pathwayGateway.findById(eq(pathwayId))).thenReturn(Mono.just(pathway));

        StepVerifier.create(pathwayService.findById(pathwayId)).expectNext(pathway).verifyComplete();
        verify(pathwayGateway, times(1)).findById(eq(pathwayId));
    }

    @Test
    void findById_noPathway() {
        UUID pathwayId = UUID.randomUUID();
        when(pathwayGateway.findById(eq(pathwayId))).thenReturn(Mono.empty());

        StepVerifier.create(pathwayService.findById(pathwayId)).expectError(PathwayNotFoundException.class).verify();
    }

    @Test
    void getPathwayPayload_pathwayNotFound() {

        when(pathwayGateway.findById(pathwayId)).thenReturn(Mono.empty());
        when(pathwayGateway.findParentActivityId(pathwayId)).thenReturn(Mono.just(UUID.randomUUID()));
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.empty());

        StepVerifier.create(pathwayService.getPathwayPayload(pathwayId))
                .expectError(PathwayNotFoundException.class).verify();
    }

    @Test
    void getPathwayPayload_parentActivityIdNotFound() {
        Pathway pathway = mockPathway(pathwayId);
        when(pathwayGateway.findById(pathwayId)).thenReturn(Mono.just(pathway));
        when(pathwayGateway.findParentActivityId(pathwayId)).thenReturn(Mono.empty());
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.empty());

        StepVerifier.create(pathwayService.getPathwayPayload(pathwayId)).
                expectError(ParentActivityNotFoundException.class).verify();
    }

    @Test
    void getPathwayPaylod_noWalkableChildren() {
        UUID parentActivityId = UUID.randomUUID();
        Pathway pathway = mockPathway(pathwayId);
        when(pathwayGateway.findById(pathwayId)).thenReturn(Mono.just(pathway));
        when(pathwayGateway.findParentActivityId(pathwayId)).thenReturn(Mono.just(parentActivityId));
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.empty());
        when(coursewareAssetService.getAssetPayloads(pathwayId)).thenReturn(Mono.empty());

        PathwayPayload payload = pathwayService.getPathwayPayload(pathwayId).block();

        assertNotNull(payload);
        assertEquals(pathwayId, payload.getPathwayId());
        assertEquals(parentActivityId, payload.getParentActivityId());
        assertEquals(PathwayType.LINEAR, payload.getPathwayType());
        assertNotNull(payload.getChildren());
        assertEquals(0, payload.getChildren().size());
        assertNull(payload.getConfig());
    }

    @Test
    void getPathwayPayload_withWalkableChildren_ordered() {
        UUID parentActivityId = UUID.randomUUID();
        Pathway pathway = mockPathway(pathwayId);

        UUID one = UUID.randomUUID();
        UUID two = UUID.randomUUID();
        UUID three = UUID.randomUUID();
        UUID four = UUID.randomUUID();
        UUID five = UUID.randomUUID();

        List<UUID> walkableIds = Lists.newArrayList(one, two, three, four, five);
        Map<UUID, String> walkableTypes = new HashMap<UUID, String>() {
            {
                put(two, CoursewareElementType.ACTIVITY.name());
            }

            {
                put(one, CoursewareElementType.ACTIVITY.name());
            }

            {
                put(three, CoursewareElementType.INTERACTIVE.name());
            }

            {
                put(five, CoursewareElementType.ACTIVITY.name());
            }

            {
                put(four, CoursewareElementType.INTERACTIVE.name());
            }
        };

        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(pathwayId)
                .setWalkableIds(walkableIds)
                .setWalkableTypes(walkableTypes);

        when(pathwayGateway.findById(pathwayId)).thenReturn(Mono.just(pathway));
        when(pathwayGateway.findParentActivityId(pathwayId)).thenReturn(Mono.just(parentActivityId));
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.just(children));
        when(coursewareAssetService.getAssetPayloads(pathwayId)).thenReturn(Mono.empty());

        PathwayPayload payload = pathwayService.getPathwayPayload(pathwayId).block();

        assertNotNull(payload);
        assertEquals(pathwayId, payload.getPathwayId());
        assertEquals(parentActivityId, payload.getParentActivityId());
        assertEquals(PathwayType.LINEAR, payload.getPathwayType());
        List<WalkableChild> walkableChildren = payload.getChildren();
        assertNotNull(walkableChildren);
        assertEquals(5, walkableChildren.size());
        assertEquals(one, walkableChildren.get(0).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, walkableChildren.get(0).getElementType());
        assertEquals(two, walkableChildren.get(1).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, walkableChildren.get(1).getElementType());
        assertEquals(three, walkableChildren.get(2).getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, walkableChildren.get(2).getElementType());
        assertEquals(four, walkableChildren.get(3).getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, walkableChildren.get(3).getElementType());
        assertEquals(five, walkableChildren.get(4).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, walkableChildren.get(4).getElementType());
        assertNull(payload.getAssets());
        assertNull(payload.getConfig());

    }

    @Test
    void getPathwayPayload_withAssets() {
        PathwayConfig pathwayConfig = new PathwayConfig()
                .setPathwayId(pathwayId)
                .setId(UUID.randomUUID())
                .setConfig(config);

        when(pathwayGateway.findLatestConfig(pathwayId)).thenReturn(Mono.just(pathwayConfig));

        UUID parentActivityId = UUID.randomUUID();
        Pathway pathway = mockPathway(pathwayId);
        when(pathwayGateway.findById(pathwayId)).thenReturn(Mono.just(pathway));
        when(pathwayGateway.findParentActivityId(pathwayId)).thenReturn(Mono.just(parentActivityId));
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.empty());
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(asset)
                .putSource("original", data);
        when(coursewareAssetService.getAssetPayloads(pathwayId)).thenReturn(Mono.just(Lists.newArrayList(assetPayload)));

        PathwayPayload payload = pathwayService.getPathwayPayload(pathwayId).block();

        assertNotNull(payload);
        assertEquals(1, payload.getAssets().size());
        assertEquals(assetPayload, payload.getAssets().get(0));
        assertNotNull(payload.getConfig());
        assertEquals(pathwayConfig.getConfig(), payload.getConfig());
        assertEquals("Pathway Description", payload.getDescription());
    }

    @Test
    void findParentActivityId_notFound() {
        TestPublisher<UUID> parentActivityPublisher = TestPublisher.create();
        parentActivityPublisher.error(new NoSuchElementException());

        when(pathwayGateway.findParentActivityId(pathwayId)).thenReturn(parentActivityPublisher.mono());

        StepVerifier.create(pathwayService.findParentActivityId(pathwayId)).
                expectError(ParentActivityNotFoundException.class).verify();
    }

    @Test
    void findParentActivityId_found() {
        UUID parentActivityId = UUID.randomUUID();

        when(pathwayGateway.findParentActivityId(pathwayId)).thenReturn(Mono.just(parentActivityId));

        UUID found = pathwayService.findParentActivityId(pathwayId).block();
        assertNotNull(found);
        assertEquals(parentActivityId, found);
        verify(pathwayGateway, atLeastOnce()).findParentActivityId(pathwayId);
    }

    @Test
    void duplicatePathway() {
        UUID newActivityId = UUID.randomUUID();
        Pathway oldPathway = mockPathway(pathwayId);
        when(pathwayGateway.persist(any(), eq(newActivityId))).thenReturn(Flux.empty());
        Pathway result = pathwayService.duplicatePathway(oldPathway, newActivityId).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotEquals(pathwayId, result.getId());
        assertEquals(oldPathway.getType(), result.getType());
        verify(pathwayGateway).persist(result, newActivityId);
    }

    @Test
    void getOrderedWalkableChildren() {
        UUID child1 = UUID.randomUUID();
        UUID child2 = UUID.randomUUID();
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(pathwayId)
                .addWalkable(child1, "ACTIVITY")
                .addWalkable(child2, "INTERACTIVE");
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.just(children));

        List<WalkableChild> result = pathwayService.getOrderedWalkableChildren(pathwayId).block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new WalkableChild().setElementId(child1).setElementType(CoursewareElementType.ACTIVITY), result.get(0));
        assertEquals(new WalkableChild().setElementId(child2).setElementType(CoursewareElementType.INTERACTIVE), result.get(1));
    }

    @Test
    void getOrderedWalkableChildren_notFound() {
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.empty());

        List<WalkableChild> result = pathwayService.getOrderedWalkableChildren(pathwayId).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrderedWalkableChildren_emptyChildren() {
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(
                Mono.just(new WalkablePathwayChildren().setPathwayId(pathwayId)));

        assertThrows(IllegalArgumentFault.class, () -> pathwayService.getOrderedWalkableChildren(pathwayId).block());
    }

    @Test
    void reorder_invalidWalkableIds_pathwayDidNotHaveWalkablesBefore() {
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.empty());

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> pathwayService.reorder(pathwayId, walkableIds).blockLast());
        assertEquals("Pathway does not have walkables to reorder", f.getMessage());
    }

    @Test
    void reorder_invalidWalkableIds_LessIdsThenBefore() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                () -> pathwayService.reorder(pathwayId, Lists.newArrayList(walkableId1, walkableId2)).blockLast());
        assertEquals("Invalid walkables list", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void reorder_invalidWalkableIds_differentIdsThenBefore() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                () -> pathwayService.reorder(pathwayId, Lists.newArrayList(walkableId1, walkableId2, walkableId2)).blockLast());
        assertEquals("Invalid walkables list", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void reorder_invalidWalkableIds_differentIdsThenBefore2() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                () -> pathwayService.reorder(pathwayId, Lists.newArrayList(walkableId1, walkableId2, walkableId2, walkableId3)).blockLast());
        assertEquals("Invalid walkables list", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void reorder_invalidPathwayIds_moreIdsThenBefore() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                () -> pathwayService.reorder(pathwayId, Lists.newArrayList(UUID.randomUUID(), walkableId1, walkableId2, walkableId3)).blockLast());
        assertEquals("Invalid walkables list", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void reorder() {
        when(pathwayGateway.persist(walkables)).thenReturn(Flux.empty());

        pathwayService.reorder(pathwayId, Lists.newArrayList(walkableId3, walkableId1, walkableId2)).blockLast();

        ArgumentCaptor<WalkablePathwayChildren> captor = ArgumentCaptor.forClass(WalkablePathwayChildren.class);
        verify(pathwayGateway).persist(captor.capture());
        assertEquals(pathwayId, captor.getValue().getPathwayId());
        assertEquals(Lists.newArrayList(walkableId3, walkableId1, walkableId2), captor.getValue().getWalkableIds());
    }

    @Test
    void replaceConfig_pathwayIsNULL() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> pathwayService.replaceConfig(null, config).block());

        assertNotNull(e);
        assertEquals("pathwayId is required", e.getMessage());
    }

    @Test
    void replaceConfig_configIsNULL() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> pathwayService.replaceConfig(pathwayId, null).block());

        assertNotNull(e);
        assertEquals("config is required", e.getMessage());
    }

    @Test
    void replaceConfig() {
        when(pathwayGateway.persist(any(PathwayConfig.class))).thenReturn(Flux.just(new Void[]{}));
        ArgumentCaptor<PathwayConfig> captor = ArgumentCaptor.forClass(PathwayConfig.class);

        pathwayService.replaceConfig(pathwayId, config)
                .block();

        verify(pathwayGateway).persist(captor.capture());

        PathwayConfig replaced = captor.getValue();

        assertNotNull(replaced);
        assertNotNull(replaced.getId());
        assertEquals(pathwayId, replaced.getPathwayId());
        assertEquals(config, replaced.getConfig());
    }

    @Test
    void findLatestConfig_nullId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> pathwayService.findLatestConfig(null).block());

        assertNotNull(e);
        assertEquals("pathwayId is required", e.getMessage());

        verify(pathwayGateway, never()).findLatestConfig(any(UUID.class));
    }

    @Test
    void findLatestConfig() {

        when(pathwayGateway.findLatestConfig(pathwayId)).thenReturn(Mono.just(new PathwayConfig()));

        PathwayConfig found = pathwayService.findLatestConfig(pathwayId)
                .block();

        assertNotNull(found);
        verify(pathwayGateway).findLatestConfig(pathwayId);
    }

    private static WalkablePathwayChildren buildPathwayChildren(UUID pathwayId, List<UUID> ids, List<String> types){
        WalkablePathwayChildren children = new WalkablePathwayChildren();
        children.setPathwayId(pathwayId);
        for (int i = 0; i < ids.size(); i++) {
            children.addWalkable(ids.get(i), types.get(i));
        }
        return children;
    }
    @Test
    void testHasChildIdsInvalidId() {
        UUID pathwayId = UUID.randomUUID();
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.empty());

        StepVerifier.create(pathwayService.hasOrderedWalkableChildren(pathwayId))
                .expectNext(Boolean.FALSE)
                .verifyComplete();

    }

    @Test
    void testHasChildIdsValidId() {
        UUID pathwayId = UUID.randomUUID();
        when(pathwayGateway.findWalkableChildren(pathwayId)).thenReturn(Mono.just(walkables));
        StepVerifier.create(pathwayService.hasOrderedWalkableChildren(pathwayId))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
    }
}
