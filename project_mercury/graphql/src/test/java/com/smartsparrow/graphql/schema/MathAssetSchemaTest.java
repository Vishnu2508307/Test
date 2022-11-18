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
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.service.LearnerAssetService;

import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;

class MathAssetSchemaTest {

    @InjectMocks
    MathAssetSchema mathAssetSchema;

    @Mock
    LearnerAssetService learnerAssetService;

    UUID activityId = UUID.randomUUID();
    UUID componentId = UUID.randomUUID();
    UUID interactiveId = UUID.randomUUID();
    UUID changeId = UUID.randomUUID();

    @Mock
    private Asset mathAsset1;

    @Mock
    private Asset mathAsset2;

    AssetPayload assetPayload1;

    AssetPayload assetPayload2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        assetPayload1 = new AssetPayload()
                .setUrn("urn1")
                .setAsset(mathAsset1);

        assetPayload2 = new AssetPayload()
                .setUrn("urn2")
                .setAsset(mathAsset2);
    }

    @Test
    void getMathAssetsActivity_NoLearnerActivity() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForWalkable(null, null, null);
        });
        assertEquals("learnerWalkable context is required", e.getMessage());
    }

    @Test
    void getMathAssetsActivity_NoChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForWalkable(new LearnerActivity(), null, null);
        });
        assertEquals("changeId is required", e.getMessage());
    }

    @Test
    void getMathAssetsActivity_NoActivityId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForWalkable(new LearnerActivity()
                                                             .setChangeId(UUID.randomUUID()), null, null);
        });
        assertEquals("ACTIVITY is required", e.getMessage());
    }

    @Test
    void getMathActivityAsset_emptyResult() {
        when(learnerAssetService.fetchMathAssetsForElementAndChangeId(activityId, changeId)).thenReturn(Flux.empty());
        Page<AssetPayload> assetsForActivity = mathAssetSchema.getMathAssetsForWalkable(new LearnerActivity()
                                                                                                .setChangeId(changeId)
                                                                                                .setId(activityId),
                                                                                        null,
                                                                                        null)
                .join();
        assertNotNull(assetsForActivity);
        assertNotNull(assetsForActivity.getEdges());
        assertEquals(0, assetsForActivity.getEdges().size());
    }

    @Test
    void getMathActivityAsset_valid() {
        when(learnerAssetService.fetchMathAssetsForElementAndChangeId(activityId, changeId))
                .thenReturn(Flux.just(assetPayload2, assetPayload1));

        Page<AssetPayload> assetsForActivity = mathAssetSchema.getMathAssetsForWalkable(new LearnerActivity()
                                                                                                .setChangeId(changeId)
                                                                                                .setId(activityId),
                                                                                        null,
                                                                                        null)
                .join();
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
    void getMathAssetsInteractive_NoLearnerActivity() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForWalkable(null, null, null);
        });
        assertEquals("learnerWalkable context is required", e.getMessage());
    }

    @Test
    void getMathAssetsInteractive_NoChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForWalkable(new LearnerInteractive(), null, null);
        });
        assertEquals("changeId is required", e.getMessage());
    }

    @Test
    void getMathAssetsInteractive_NoInteractiveId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForWalkable(new LearnerInteractive()
                                                             .setChangeId(UUID.randomUUID()), null, null);
        });
        assertEquals("INTERACTIVE is required", e.getMessage());
    }

    @Test
    void getMathInteractiveAsset_emptyResult() {
        when(learnerAssetService.fetchMathAssetsForElementAndChangeId(interactiveId,
                                                                      changeId)).thenReturn(Flux.empty());
        Page<AssetPayload> assetsForInteractive = mathAssetSchema.getMathAssetsForWalkable(new LearnerInteractive()
                                                                                                   .setChangeId(changeId)
                                                                                                   .setId(interactiveId),
                                                                                           null,
                                                                                           null)
                .join();
        assertNotNull(assetsForInteractive);
        assertNotNull(assetsForInteractive.getEdges());
        assertEquals(0, assetsForInteractive.getEdges().size());
    }

    @Test
    void getMathInteractiveAsset_valid() {
        when(learnerAssetService.fetchMathAssetsForElementAndChangeId(interactiveId, changeId))
                .thenReturn(Flux.just(assetPayload1, assetPayload2));

        Page<AssetPayload> assetsForInteractive = mathAssetSchema.getMathAssetsForWalkable(new LearnerInteractive()
                                                                                                   .setChangeId(changeId)
                                                                                                   .setId(interactiveId),
                                                                                           null,
                                                                                           null)
                .join();
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
    void getMathAssetsComponent_NoLearnerActivity() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForComponent(null, null, null);
        });
        assertEquals("learnerComponent context is required", e.getMessage());
    }

    @Test
    void getMathAssetsComponent_NoChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForComponent(new LearnerComponent(), null, null);
        });
        assertEquals("changeId is required", e.getMessage());
    }

    @Test
    void getMathAssetsComponent_NoComponentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            mathAssetSchema.getMathAssetsForComponent(new LearnerComponent()
                                                              .setChangeId(UUID.randomUUID()), null, null);
        });
        assertEquals("componentId is required", e.getMessage());
    }

    @Test
    void getMathComponentAsset_emptyResult() {
        when(learnerAssetService.fetchMathAssetsForElementAndChangeId(componentId, changeId)).thenReturn(Flux.empty());
        Page<AssetPayload> assetsForComponent = mathAssetSchema.getMathAssetsForComponent(new LearnerComponent()
                                                                                                  .setChangeId(changeId)
                                                                                                  .setId(componentId),
                                                                                          null,
                                                                                          null)
                .join();
        assertNotNull(assetsForComponent);
        assertNotNull(assetsForComponent.getEdges());
        assertEquals(0, assetsForComponent.getEdges().size());
    }

    @Test
    void getMathComponentAsset_valid() {
        when(learnerAssetService.fetchMathAssetsForElementAndChangeId(componentId, changeId))
                .thenReturn(Flux.just(assetPayload1));

        Page<AssetPayload> assetsForComponent = mathAssetSchema.getMathAssetsForComponent(new LearnerComponent()
                                                                                                  .setChangeId(changeId)
                                                                                                  .setId(componentId),
                                                                                          null,
                                                                                          null)
                .join();
        assertNotNull(assetsForComponent);
        assertNotNull(assetsForComponent.getEdges());
        assertEquals(1, assetsForComponent.getEdges().size());
        assertNotNull(assetsForComponent.getEdges().get(0));
        assertNotNull(assetsForComponent.getEdges().get(0).getNode());
        assertEquals(assetPayload1, assetsForComponent.getEdges().get(0).getNode());
    }

}
