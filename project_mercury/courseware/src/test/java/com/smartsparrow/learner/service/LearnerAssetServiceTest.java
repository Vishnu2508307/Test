package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.ExternalSource;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.data.MathAssetData;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerAssetGateway;
import com.smartsparrow.math.service.MathAssetService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LearnerAssetServiceTest {

    @InjectMocks
    private LearnerAssetService learnerAssetService;

    @Mock
    private CoursewareAssetService coursewareAssetService;

    @Mock
    private LearnerAssetGateway learnerAssetGateway;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private AssetBuilder assetBuilder;

    @Mock
    private Asset asset;

    @Mock
    private Deployment deployment;

    @Mock
    private MathAssetService mathAssetService;

    private static final UUID elementId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID assetId1 = UUID.randomUUID();
    private static final UUID assetId2 = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();

    private static final String mathML = "<math><mn>1</mn><mo>-</mo><mn>2</mn></math>";
    private static final String altText = "alt text";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(assetBuilder.build(any(AssetProvider.class))).thenReturn(asset);
        when(asset.getAssetProvider()).thenReturn(AssetProvider.AERO);
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);
        when(assetBuilder.setAssetSummary(any(AssetSummary.class)))
                .thenReturn(assetBuilder);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);
    }

    @Test
    void fetchAssetsForElementAndChangeId_Empty() {
        when(learnerAssetGateway.findAssetsUrn(elementId, changeId))
                .thenReturn(Flux.empty());
        when(learnerAssetGateway.findAssets(elementId, changeId))
                .thenReturn(Flux.empty());

        List<AssetPayload> assetPayloads = learnerAssetService
                .fetchAssetsForElementAndChangeId(elementId, changeId)
                .collectList()
                .block();

        assertNotNull(assetPayloads);
        assertEquals(0, assetPayloads.size());
    }

    @Test
    void fetchAssetsForElementAndChangeId_validMultiple() {
        final String urn1 = "urn:aero:" + UUID.randomUUID();
        when(learnerAssetGateway.findAssetsUrn(elementId, changeId))
                .thenReturn(Flux.just(urn1));

        when(learnerAssetGateway.findAssetId(urn1))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(assetId1)));

        when(learnerAssetGateway.findAssets(elementId, changeId))
                .thenReturn(Flux.just(assetId1, assetId2));

        when(learnerAssetGateway.fetchAssetById(assetId1)).thenReturn(Mono.just(new AssetSummary()
                .setId(assetId1)
                .setProvider(AssetProvider.AERO)
                .setMediaType(AssetMediaType.IMAGE)));

        when(learnerAssetGateway.fetchMetadata(assetId1)).thenReturn(Flux.empty());

        when(learnerAssetGateway.fetchImageSources(assetId1))
                .thenReturn(Flux.just(new ImageSource()
                        .setName(ImageSourceName.ORIGINAL)
                        .setUrl("aurl")));

        when(bronteAssetService.buildPublicUrl("aurl"))
                .thenReturn(Mono.just("aresolvedUrl"));

        List<AssetPayload> assetPayloads = learnerAssetService
                .fetchAssetsForElementAndChangeId(elementId, changeId)
                .collectList()
                .block();

        assertNotNull(assetPayloads);
        assertEquals(1, assetPayloads.size());
        assertNotNull(assetPayloads.get(0));
    }

    @Test
    void publishAssetsFor() {
        final String assetUrn1 = "urn:aero:" + UUID.randomUUID();
        final String assetUrn2 = "urn:aero:" + UUID.randomUUID();
        final CoursewareElementType type = CoursewareElementType.ACTIVITY;

        final AssetIdByUrn one = new AssetIdByUrn()
                .setAssetId(assetId1)
                .setAssetUrn(assetUrn1);

        final AssetIdByUrn two = new AssetIdByUrn()
                .setAssetId(assetId2)
                .setAssetUrn(assetUrn2);

        when(coursewareAssetService.getAssetsFor(elementId))
                .thenReturn(Flux.just(one, two));

        when(learnerAssetGateway.persist(any(AssetIdByUrn.class), eq(CoursewareElement.from(elementId, type)), eq(deployment)))
                .thenReturn(Flux.just(new Void[]{}));

        when(bronteAssetService.getAssetSummary(assetId1))
                .thenReturn(Mono.just(new AssetSummary()
                        .setProvider(AssetProvider.EXTERNAL)
                        .setMediaType(AssetMediaType.IMAGE)
                        .setId(assetId1)));
        when(bronteAssetService.getAssetSummary(assetId2))
                .thenReturn(Mono.just(new AssetSummary()
                        .setProvider(AssetProvider.EXTERNAL)
                        .setMediaType(AssetMediaType.ICON)
                        .setId(assetId2)));
        when(learnerAssetGateway.persist(any(AssetSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(bronteAssetService.getExternalSourceRecord(any(UUID.class)))
                .thenReturn(Mono.just(new ExternalSource()
                        .setUrl("yo!")));
        when(learnerAssetGateway.persist(any(ExternalSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(bronteAssetService.getAssetMetadata(any(UUID.class))).thenReturn(Flux.empty());


        learnerAssetService.publishAssetsFor(deployment, elementId, type)
                .blockFirst();

        verify(learnerAssetGateway).persist(eq(one), eq(CoursewareElement.from(elementId, type)), eq(deployment));
        verify(learnerAssetGateway).persist(eq(two), eq(CoursewareElement.from(elementId, type)), eq(deployment));

    }

    @Test
    void publishAssetsFor_Alfresco() {
        final String assetUrn1 = "urn:aero:" + UUID.randomUUID();
        final CoursewareElementType type = CoursewareElementType.ACTIVITY;

        final AssetIdByUrn one = new AssetIdByUrn()
                .setAssetId(assetId1)
                .setAssetUrn(assetUrn1);

        final AssetSummary assetSummary1 = new AssetSummary()
                .setProvider(AssetProvider.ALFRESCO)
                .setMediaType(AssetMediaType.IMAGE)
                .setId(assetId1);

        when(coursewareAssetService.getAssetsFor(elementId))
                .thenReturn(Flux.just(one));

        when(learnerAssetGateway.persist(any(AssetIdByUrn.class), eq(CoursewareElement.from(elementId, type)), eq(deployment)))
                .thenReturn(Flux.just(new Void[]{}));

        when(bronteAssetService.getAssetSummary(assetId1))
                .thenReturn(Mono.just(assetSummary1));
        when(learnerAssetGateway.persist(any(AssetSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(bronteAssetService.getImageSource(any(UUID.class)))
                .thenReturn(Flux.just(new ImageSource().setUrl("yo!")));
        when(learnerAssetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(bronteAssetService.getAssetMetadata(any(UUID.class))).thenReturn(Flux.empty());

        learnerAssetService.publishAssetsFor(deployment, elementId, type)
                .blockFirst();

        assertEquals(AssetProvider.AERO,assetSummary1.getProvider());

        verify(learnerAssetGateway).persist(eq(one), eq(CoursewareElement.from(elementId, type)), eq(deployment));
    }

    @Test
    void publishMathAssetsFor() {
        final String assetUrn1 = "urn:math:" + UUID.randomUUID();
        final String assetUrn2 = "urn:math:" + UUID.randomUUID();
        final CoursewareElementType type = CoursewareElementType.ACTIVITY;

        final com.smartsparrow.math.data.AssetIdByUrn one = new com.smartsparrow.math.data.AssetIdByUrn()
                .setAssetId(assetId1)
                .setAssetUrn(assetUrn1);

        final com.smartsparrow.math.data.AssetIdByUrn two = new com.smartsparrow.math.data.AssetIdByUrn()
                .setAssetId(assetId2)
                .setAssetUrn(assetUrn2);

        final com.smartsparrow.math.data.AssetSummary assetSummary1 = new com.smartsparrow.math.data.AssetSummary()
                .setId(assetId1)
                .setMathML(mathML)
                .setAltText(altText);

        final com.smartsparrow.math.data.AssetSummary assetSummary2 = new com.smartsparrow.math.data.AssetSummary()
                .setId(assetId2)
                .setMathML(mathML)
                .setAltText(altText);

        when(mathAssetService.getAssetsFor(elementId)).thenReturn(Flux.just(one, two));

        when(learnerAssetGateway.persist(any(com.smartsparrow.math.data.AssetIdByUrn.class), eq(CoursewareElement.from(elementId, type)), eq(deployment)))
                .thenReturn(Flux.just(new Void[]{}));

        when(mathAssetService.getMathAssetSummaryById(one.getAssetId())).thenReturn(Mono.just(assetSummary1));

        when(mathAssetService.getMathAssetSummaryById(two.getAssetId())).thenReturn(Mono.just(assetSummary2));

        when(learnerAssetGateway.persist(any(com.smartsparrow.math.data.AssetSummary.class))).thenReturn(Flux.just(new Void[]{}));

        learnerAssetService.publishMathAssetsFor(deployment, elementId, type)
                .blockFirst();

        verify(learnerAssetGateway).persist(eq(one), eq(CoursewareElement.from(elementId, type)), eq(deployment));
        verify(learnerAssetGateway).persist(eq(two), eq(CoursewareElement.from(elementId, type)), eq(deployment));
        verify(learnerAssetGateway).persist(eq(assetSummary1));
        verify(learnerAssetGateway).persist(eq(assetSummary2));
    }

    @Test
    void fetchMathAssetsForElementAndChangeId_Empty() {
        when(learnerAssetGateway.findMathAssetsUrn(elementId, changeId))
                .thenReturn(Flux.empty());

        List<AssetPayload> assetPayloads = learnerAssetService
                .fetchMathAssetsForElementAndChangeId(elementId, changeId)
                .collectList()
                .block();

        assertNotNull(assetPayloads);
        assertEquals(0, assetPayloads.size());
    }

    @Test
    void fetchMathAssetsForElementAndChangeId_validMultiple() {
        final String urn1 = "urn:math:" + UUID.randomUUID();

        when(assetBuilder.setMathAssetData(any(MathAssetData.class))).thenReturn(assetBuilder);
        when(asset.getAssetProvider()).thenReturn(AssetProvider.MATH);
        when(learnerAssetGateway.findMathAssetsUrn(elementId, changeId))
                .thenReturn(Flux.just(urn1));
        when(learnerAssetGateway.findMathAssetId(urn1))
                .thenReturn(Mono.just(new com.smartsparrow.math.data.AssetIdByUrn()
                                              .setAssetId(assetId1)
                                              .setAssetUrn(urn1)));
        when(learnerAssetGateway.fetchMathAssetById(assetId1)).thenReturn(Mono.just(new com.smartsparrow.math.data.AssetSummary()
                                                                                            .setId(assetId1)
                                                                                            .setAltText(altText)
                                                                                            .setMathML(mathML)
                                                                                            .setHash("Hash")
                                                                                            .setSvgText("SvgText")
                                                                                            .setSvgShape("SvgShape")));

        List<AssetPayload> assetPayloads = learnerAssetService
                .fetchMathAssetsForElementAndChangeId(elementId, changeId)
                .collectList()
                .block();

        assertNotNull(assetPayloads);
        assertEquals(1, assetPayloads.size());
        assertNotNull(assetPayloads.get(0));
    }

    @Test
    void getMathAssetPayload_noAssetId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> learnerAssetService.getMathAssetPayload(null));
        assertEquals("assetId is required", ex.getMessage());
    }

    @Test
    void getMathAssetPayload() {
        final com.smartsparrow.math.data.AssetSummary assetSummary = new com.smartsparrow.math.data.AssetSummary()
                .setId(assetId1)
                .setMathML(mathML)
                .setAltText(altText);
        when(learnerAssetGateway.fetchMathAssetById(assetId1)).thenReturn(Mono.just(assetSummary));
        when(assetBuilder.setMathAssetData(any(MathAssetData.class))).thenReturn(assetBuilder);
        when(asset.getAssetProvider()).thenReturn(AssetProvider.MATH);

        AssetPayload assetPayload = learnerAssetService.getMathAssetPayload(assetId1).block();

        assertNotNull(assetPayload);
        assertNotNull(assetPayload.getAsset());
        assertNotNull(assetPayload.getSource());
        assertEquals(mathML, assetPayload.getSource().get("mathML"));
        assertEquals(altText, assetPayload.getSource().get("altText"));
        assertEquals(String.format("urn:%s:%s", AssetProvider.MATH.getLabel(), assetSummary.getId()), assetPayload.getUrn());
    }
}
