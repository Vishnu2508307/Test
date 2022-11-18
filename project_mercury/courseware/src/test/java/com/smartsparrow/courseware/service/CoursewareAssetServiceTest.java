package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.math.service.MathAssetService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CoursewareAssetServiceTest {

    @InjectMocks
    private CoursewareAssetService coursewareAssetService;

    @Mock
    private CoursewareGateway coursewareGateway;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private CoursewareAssetConfigService coursewareAssetConfigService;

    @Mock
    private MathAssetService mathAssetService;

    private static final UUID elementId = UUID.randomUUID();
    private static final UUID newElementId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final String mathML = "<math><mn>1</mn><mo>-</mo><mn>2</mn></math>";
    private static final String altText = "alt text";
    private static final UUID assetId1 = UUID.randomUUID();
    private static final UUID assetId2 = UUID.randomUUID();
    private DuplicationContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

         context = new DuplicationContext()
                .setDuplicatorAccount(accountId)
                .setDuplicatorSubscriptionId(subscriptionId);
    }

    @Test
    void addAsset() {

        CoursewareElement element = new CoursewareElement(elementId, CoursewareElementType.ACTIVITY);
        UUID assetId = UUID.randomUUID();
        String urn = "urn:aero:" + assetId;


        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(coursewareGateway.persist(element, new AssetUrn(assetId, AssetProvider.AERO), rootElementId)).thenReturn(publisher.flux());

        coursewareAssetService.addAsset(elementId, CoursewareElementType.ACTIVITY, urn, rootElementId).blockLast();

        publisher.assertWasRequested();
    }

    @Test
    void addAsset_invalidUrn() {
        String invalidUrn = "urn:aero:asdasd";

        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(coursewareGateway.persist(any(), any(), any())).thenReturn(publisher.flux());

        assertThrows(AssetURNParseException.class, () ->
                coursewareAssetService.addAsset(elementId, CoursewareElementType.ACTIVITY, invalidUrn, rootElementId).blockLast());
        publisher.assertWasNotRequested();
    }

    @Test
    void removeAsset() {
        UUID assetId = UUID.randomUUID();
        String urn = "urn:aero:" + assetId;

        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(coursewareGateway.remove(elementId, new AssetUrn(assetId, AssetProvider.AERO) , rootElementId)).thenReturn(publisher.flux());

        coursewareAssetService.removeAsset(elementId, urn, rootElementId).blockLast();

        publisher.assertWasRequested();
    }

    @Test
    void removeAsset_invalidAssetProvider() {
        UUID assetId = UUID.randomUUID();
        String invalidUrn = "urn:foo:"+ assetId;;

        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(coursewareGateway.remove(any(), any(), any())).thenReturn(publisher.flux());

        assertThrows(AssetURNParseException.class, () ->
                coursewareAssetService.removeAsset(elementId, invalidUrn, rootElementId).blockLast());
        publisher.assertWasNotRequested();
    }

    @Test
    void removeAsset_invalidUrn() {
        String invalidUrn = "urn:aero:asdasd";

        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(coursewareGateway.remove(any(), any(), any())).thenReturn(publisher.flux());

        assertThrows(AssetURNParseException.class, () ->
                coursewareAssetService.removeAsset(elementId, invalidUrn, rootElementId).blockLast());
        publisher.assertWasNotRequested();
    }

    @Test
    void duplicateAssets() {
        UUID oldElementId = UUID.randomUUID();

        UUID newRootElementId = UUID.randomUUID();
        CoursewareElementType type = CoursewareElementType.FEEDBACK;

        context.setNewRootElementId(newRootElementId)
                .setRequireNewAssetId(false);

        UUID asset1 = UUID.randomUUID();
        UUID asset2 = UUID.randomUUID();
        final String assetUrn1 = "urn:aero:" + asset1;
        final String assetUrn2 = "urn:aero:" + asset2;


        when(coursewareGateway.findAssetUrn(oldElementId))
                .thenReturn(Flux.just(assetUrn1, assetUrn2));

        when(bronteAssetService.findAssetId(assetUrn1))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(asset1)
                        .setAssetUrn(assetUrn1)));

        when(bronteAssetService.findAssetId(assetUrn2))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(asset2)
                        .setAssetUrn(assetUrn2)));

        when(bronteAssetService.getAssetSummary(asset1)).thenReturn(Mono.just(new AssetSummary()
                .setProvider(AssetProvider.EXTERNAL)
                .setId(asset1)));
        when(bronteAssetService.getAssetSummary(asset2)).thenReturn(Mono.just(new AssetSummary()
                .setProvider(AssetProvider.EXTERNAL)
                .setId(asset2)));

        when(coursewareGateway.persist(any(CoursewareElement.class),
                any(AssetUrn.class),
                any(UUID.class))).thenReturn(Flux.just(new Void[]{}));

        coursewareAssetService.duplicateAssets(oldElementId, newElementId, type, context).collectList().block();

        verify(coursewareGateway).persist(new CoursewareElement(newElementId, type), new AssetUrn(assetUrn1), newRootElementId);
        verify(coursewareGateway).persist(new CoursewareElement(newElementId, type), new AssetUrn(assetUrn2), newRootElementId);
        verify(bronteAssetService, never()).getAssetSummary(asset1);
        verify(bronteAssetService, never()).getAssetSummary(asset2);
    }


    @Test
    void getAssetFor_noElementId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> coursewareAssetService.getAssetsFor(null)
                .blockFirst());

        assertEquals("elementId is required", f.getMessage());
    }

    @Test
    void getAssetFor_noUrns() {
        when(coursewareGateway.findAssetUrn(rootElementId))
                .thenReturn(Flux.empty());

        List<AssetIdByUrn> result = coursewareAssetService.getAssetsFor(rootElementId)
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(bronteAssetService, never()).findAssetId(anyString());
    }

    @Test
    void getAssetFor() {
        final String assetUrn1 = "urn:aero:" + UUID.randomUUID();
        final String assetUrn2 = "urn:aero:" + UUID.randomUUID();
        final UUID assetId1 = UUID.randomUUID();
        final UUID assetId2 = UUID.randomUUID();

        AssetIdByUrn one = new AssetIdByUrn()
                .setAssetUrn(assetUrn1)
                .setAssetId(assetId1);

        AssetIdByUrn two = new AssetIdByUrn()
                .setAssetUrn(assetUrn2)
                .setAssetId(assetId2);

        when(coursewareGateway.findAssetUrn(rootElementId))
                .thenReturn(Flux.just(assetUrn1, assetUrn2));

        when(bronteAssetService.findAssetId(assetUrn1))
                .thenReturn(Mono.just(one));
        when(bronteAssetService.findAssetId(assetUrn2))
                .thenReturn(Mono.just(two));

        List<AssetIdByUrn> result = coursewareAssetService.getAssetsFor(rootElementId)
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(one, result.get(0));
        assertEquals(two, result.get(1));
    }

    @Test
    void duplicateAssets_noElementId() {
        CoursewareElementType type = CoursewareElementType.ACTIVITY;

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> coursewareAssetService.duplicateAssets(null, newElementId, type, context)
                        .blockFirst());

        assertEquals("elementId is required", f.getMessage());
    }

    @Test
    void duplicateAssets_noNewElementId() {
        CoursewareElementType type = CoursewareElementType.ACTIVITY;

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> coursewareAssetService.duplicateAssets(elementId, null, type, context)
                        .blockFirst());

        assertEquals("newElementId is required", f.getMessage());
    }

    @Test
    void duplicateAssets_noElementType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> coursewareAssetService.duplicateAssets(elementId, newElementId, null, context)
                        .blockFirst());

        assertEquals("elementType is required", f.getMessage());
    }

    @Test
    void duplicateAssets_noContext() {
        CoursewareElementType type = CoursewareElementType.ACTIVITY;

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> coursewareAssetService.duplicateAssets(elementId, newElementId, type, null)
                        .blockFirst());

        assertEquals("context is required", f.getMessage());
    }

    @Test
    void duplicateAssets_noRootElementId() {
        CoursewareElementType type = CoursewareElementType.ACTIVITY;

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> coursewareAssetService.duplicateAssets(elementId, newElementId, type, context)
                        .blockFirst());

        assertEquals("newRootElementId is required", f.getMessage());
    }

    @Test
    void duplicateAssets_withNewAssetId_activityType() {
        CoursewareElementType type = CoursewareElementType.ACTIVITY;
        context.setNewRootElementId(rootElementId)
                .setRequireNewAssetId(true);

        UUID asset1 = UUID.randomUUID();
        UUID asset2 = UUID.randomUUID();
        final String assetUrn1 = "urn:aero:" + asset1;
        final String assetUrn2 = "urn:aero:" + asset2;

        UUID newAsset1 = UUID.randomUUID();
        UUID newAsset2 = UUID.randomUUID();
        final String newAssetUrn1 = "urn:aero:" + newAsset1;
        final String newAssetUrn2 = "urn:aero:" + newAsset2;

        when(coursewareGateway.findAssetUrn(elementId)).thenReturn(Flux.just(assetUrn1, assetUrn2));

        when(bronteAssetService.findAssetId(assetUrn1))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(asset1)
                        .setAssetUrn(assetUrn1)));

        when(bronteAssetService.findAssetId(assetUrn2))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(asset2)
                        .setAssetUrn(assetUrn2)));

        when(bronteAssetService.duplicate(asset1, accountId, subscriptionId)).thenReturn(Mono.just(new AssetSummary()
                .setId(newAsset1).setUrn(newAssetUrn1)));

        when(bronteAssetService.duplicate(asset2, accountId, subscriptionId)).thenReturn(Mono.just(new AssetSummary()
                .setId(newAsset2).setUrn(newAssetUrn2)));

        when(coursewareAssetConfigService.updateAssetUrn(newElementId, type, assetUrn1, newAssetUrn1))
                .thenReturn(Mono.empty());

        when(coursewareAssetConfigService.updateAssetUrn(newElementId, type, assetUrn2, newAssetUrn2))
                .thenReturn(Mono.empty());

        when(coursewareGateway.persist(any(CoursewareElement.class),
                any(AssetUrn.class),
                any(UUID.class))).thenReturn(Flux.just(new Void[]{}));

        coursewareAssetService.duplicateAssets(elementId, newElementId, type, context).collectList().block();

        verify(coursewareGateway).persist(new CoursewareElement(newElementId, type), new AssetUrn(newAssetUrn1), context.getNewRootElementId());
        verify(coursewareGateway).persist(new CoursewareElement(newElementId, type), new AssetUrn(newAssetUrn2), context.getNewRootElementId());
    }

    @Test
    void duplicateAssets_withNewAssetId_interactiveType() {
        CoursewareElementType type = CoursewareElementType.INTERACTIVE;
        context.setNewRootElementId(rootElementId)
                .setRequireNewAssetId(true);

        final UUID asset1 = UUID.randomUUID();
        final String assetUrn1 = "urn:aero:" + asset1;

        final UUID newAsset1 = UUID.randomUUID();
        final String newAssetUrn1 = "urn:aero:" + newAsset1;

        when(coursewareGateway.findAssetUrn(elementId)).thenReturn(Flux.just(assetUrn1));
        when(bronteAssetService.findAssetId(assetUrn1))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(asset1)
                        .setAssetUrn(assetUrn1)));

        when(bronteAssetService.duplicate(asset1, accountId, subscriptionId)).thenReturn(Mono.just(new AssetSummary()
                .setId(newAsset1).setUrn(newAssetUrn1)));

        when(coursewareAssetConfigService.updateAssetUrn(newElementId, type, assetUrn1, newAssetUrn1))
                .thenReturn(Mono.empty());

        when(coursewareGateway.persist(any(CoursewareElement.class),
                any(AssetUrn.class),
                any(UUID.class))).thenReturn(Flux.just(new Void[]{}));

        coursewareAssetService.duplicateAssets(elementId, newElementId, type, context).collectList().block();

        verify(coursewareGateway).persist(new CoursewareElement(newElementId, type), new AssetUrn(newAssetUrn1), rootElementId);
    }

    @Test
    void duplicateAssets_withNewAssetId_noAssetFound() {
        CoursewareElementType type = CoursewareElementType.ACTIVITY;
        context.setNewRootElementId(rootElementId)
                .setRequireNewAssetId(true);

        when(coursewareGateway.findAssetUrn(elementId)).thenReturn(Flux.empty());

        coursewareAssetService.duplicateAssets(elementId, newElementId, type, context).collectList().block();

        verify(coursewareGateway, never()).persist(any(CoursewareElement.class), any(AssetUrn.class), eq(rootElementId));
    }


    @Test
    void fetchMathAssetsForElementAndChangeId_Empty() {
        when(mathAssetService.getAssetsFor(elementId))
                .thenReturn(Flux.empty());

        List<AssetPayload> mathAssetPayloads = coursewareAssetService
                .fetchMathAssetsForElement(elementId)
                .block();

        assertNotNull(mathAssetPayloads);
        assertEquals(0, mathAssetPayloads.size());
    }

    @Test
    void fetchMathAssetsForElementAndChangeId_validMultiple() {
        final String assetUrn1 = "urn:math:" + UUID.randomUUID();
        final String assetUrn2 = "urn:math:" + UUID.randomUUID();

        final com.smartsparrow.math.data.AssetIdByUrn one = new com.smartsparrow.math.data.AssetIdByUrn()
                .setAssetId(assetId1)
                .setAssetUrn(assetUrn1);

        final com.smartsparrow.math.data.AssetIdByUrn two = new com.smartsparrow.math.data.AssetIdByUrn()
                .setAssetId(assetId2)
                .setAssetUrn(assetUrn2);

        when(mathAssetService.getAssetsFor(elementId)).thenReturn(Flux.just(one, two));

        when(mathAssetService.getMathAssetPayload(eq(assetId1))).thenReturn(Mono.just(new AssetPayload().setUrn(assetUrn1)));

        when(mathAssetService.getMathAssetPayload(eq(assetId2))).thenReturn(Mono.just(new AssetPayload().setUrn(assetUrn2)));

        List<AssetPayload> mathAssetPayloads = coursewareAssetService
                .fetchMathAssetsForElement(elementId)
                .block();

        assertNotNull(mathAssetPayloads);
        assertEquals(2, mathAssetPayloads.size());
        assertNotNull(mathAssetPayloads.get(0));
    }

}
