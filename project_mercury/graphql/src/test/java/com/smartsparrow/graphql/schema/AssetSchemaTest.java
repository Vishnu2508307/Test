package com.smartsparrow.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.service.LearnerAssetService;

import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;

public class AssetSchemaTest {

    @InjectMocks
    AssetSchema assetSchema;

    @Mock
    LearnerAssetService learnerAssetService;

    UUID activityId = UUID.randomUUID();
    UUID componentId = UUID.randomUUID();
    UUID interactiveId = UUID.randomUUID();
    UUID changeId = UUID.randomUUID();

    @Mock
    private Asset imageAsset;

    @Mock
    private Asset videoAsset;

    AssetPayload assetPayload1;

    AssetPayload assetPayload2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(imageAsset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);
        when(videoAsset.getAssetMediaType()).thenReturn(AssetMediaType.VIDEO);

        assetPayload1 = new AssetPayload()
                .setUrn("urn1")
                .setAsset(imageAsset);

        assetPayload2 = new AssetPayload()
                .setUrn("urn2")
                .setAsset(videoAsset);
    }

    @Test
    void getAssetsActivity_NoLearnerActivity() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForWalkable(null, null, null).join();
        });
        assertEquals("learnerWalkable context is required", e.getMessage());
    }

    @Test
    void getAssetsActivity_NoChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForWalkable(new LearnerActivity(), null, null).join();
        });
        assertEquals("changeId is required", e.getMessage());
    }

    @Test
    void getAssetsActivity_NoActivityId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForWalkable(new LearnerActivity()
                    .setChangeId(UUID.randomUUID()), null, null).join();
        });
        assertEquals("ACTIVITY is required", e.getMessage());
    }

    @Test
    void getActivityAsset_emptyResult() {
        when(learnerAssetService.fetchAssetsForElementAndChangeId(activityId, changeId)).thenReturn(Flux.empty());
        Page<AssetPayload> assetsForActivity = assetSchema
                .getAssetsForWalkable(new LearnerActivity().setId(activityId).setChangeId(changeId),
                                      null,
                                      null)
                .join();
        assertNotNull(assetsForActivity);
        assertNotNull(assetsForActivity.getEdges());
        assertEquals(0, assetsForActivity.getEdges().size());
    }

    @Test
    void getActivityAsset_valid() {
        when(learnerAssetService.fetchAssetsForElementAndChangeId(activityId, changeId))
                .thenReturn(Flux.just(assetPayload2, assetPayload1));

        Page<AssetPayload> assetsForActivity = assetSchema.getAssetsForWalkable(new LearnerActivity()
                .setChangeId(changeId)
                .setId(activityId), null, null).join();
        assertNotNull(assetsForActivity);
        assertNotNull(assetsForActivity.getEdges());
        assertEquals(2, assetsForActivity.getEdges().size());
        assertNotNull(assetsForActivity.getEdges().get(0));
        assertNotNull(assetsForActivity.getEdges().get(0).getNode());
        assertEquals(assetPayload2, assetsForActivity.getEdges().get(0).getNode());
        assertNotNull(assetsForActivity.getEdges().get(1).getNode());
        assertEquals(assetPayload1, assetsForActivity.getEdges().get(1).getNode());
    }

    @Test
    void getAssetsInteractive_NoLearnerActivity() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForWalkable(null, null, null).join();
        });
        assertEquals("learnerWalkable context is required", e.getMessage());
    }

    @Test
    void getAssetsInteractive_NoChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForWalkable(new LearnerInteractive(), null, null).join();
        });
        assertEquals("changeId is required", e.getMessage());
    }

    @Test
    void getAssetsInteractive_NoInteractiveId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForWalkable(new LearnerInteractive()
                    .setChangeId(UUID.randomUUID()), null, null).join();
        });
        assertEquals("INTERACTIVE is required", e.getMessage());
    }

    @Test
    void getInteractiveAsset_emptyResult() {
        when(learnerAssetService.fetchAssetsForElementAndChangeId(interactiveId, changeId)).thenReturn(Flux.empty());
        Page<AssetPayload> assetsForInteractive = assetSchema.getAssetsForWalkable(new LearnerInteractive()
                .setChangeId(changeId)
                .setId(interactiveId), null, null).join();
        assertNotNull(assetsForInteractive);
        assertNotNull(assetsForInteractive.getEdges());
        assertEquals(0, assetsForInteractive.getEdges().size());
    }

    @Test
    void getInteractiveAsset_valid() {
        when(learnerAssetService.fetchAssetsForElementAndChangeId(interactiveId, changeId))
                .thenReturn(Flux.just(assetPayload1, assetPayload2));

        Page<AssetPayload> assetsForInteractive = assetSchema.getAssetsForWalkable(new LearnerInteractive()
                .setChangeId(changeId)
                .setId(interactiveId), null, null).join();
        assertNotNull(assetsForInteractive);
        assertNotNull(assetsForInteractive.getEdges());
        assertEquals(2, assetsForInteractive.getEdges().size());
        assertNotNull(assetsForInteractive.getEdges().get(0));
        assertNotNull(assetsForInteractive.getEdges().get(0).getNode());
        assertEquals(assetPayload1, assetsForInteractive.getEdges().get(0).getNode());
        assertNotNull(assetsForInteractive.getEdges().get(1).getNode());
        assertEquals(assetPayload2, assetsForInteractive.getEdges().get(1).getNode());
    }

    @Test
    void getAssetsComponent_NoLearnerActivity() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForComponent(null, null, null).join();
        });
        assertEquals("learnerComponent context is required", e.getMessage());
    }

    @Test
    void getAssetsComponent_NoChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForComponent(new LearnerComponent(), null, null).join();
        });
        assertEquals("changeId is required", e.getMessage());
    }

    @Test
    void getAssetsComponent_NoComponentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            assetSchema.getAssetsForComponent(new LearnerComponent()
                    .setChangeId(UUID.randomUUID()), null, null).join();
        });
        assertEquals("componentId is required", e.getMessage());
    }

    @Test
    void getComponentAsset_emptyResult() {
        when(learnerAssetService.fetchAssetsForElementAndChangeId(componentId, changeId)).thenReturn(Flux.empty());
        Page<AssetPayload> assetsForComponent = assetSchema.getAssetsForComponent(new LearnerComponent()
                .setChangeId(changeId)
                .setId(componentId), null, null).join();
        assertNotNull(assetsForComponent);
        assertNotNull(assetsForComponent.getEdges());
        assertEquals(0, assetsForComponent.getEdges().size());
    }

    @Test
    void getComponentAsset_valid() {
        when(learnerAssetService.fetchAssetsForElementAndChangeId(componentId, changeId))
                .thenReturn(Flux.just(assetPayload1));

        Page<AssetPayload> assetsForComponent = assetSchema.getAssetsForComponent(new LearnerComponent()
                .setChangeId(changeId)
                .setId(componentId), null, null).join();
        assertNotNull(assetsForComponent);
        assertNotNull(assetsForComponent.getEdges());
        assertEquals(1, assetsForComponent.getEdges().size());
        assertNotNull(assetsForComponent.getEdges().get(0));
        assertNotNull(assetsForComponent.getEdges().get(0).getNode());
        assertEquals(assetPayload1, assetsForComponent.getEdges().get(0).getNode());
    }

}
