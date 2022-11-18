package com.smartsparrow.math.service;

import static com.smartsparrow.asset.data.AssetProvider.MATH;
import static com.smartsparrow.dataevent.RouteUri.MATH_ASSET_GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.data.MathAssetData;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.math.config.MathConfig;
import com.smartsparrow.math.data.AssetByHash;
import com.smartsparrow.math.data.AssetIdByUrn;
import com.smartsparrow.math.data.AssetSummary;
import com.smartsparrow.math.data.MathAssetErrorNotification;
import com.smartsparrow.math.data.MathAssetGateway;
import com.smartsparrow.math.data.MathAssetRequestNotification;
import com.smartsparrow.math.data.MathAssetResultNotification;
import com.smartsparrow.math.data.MathAssetRetryNotification;
import com.smartsparrow.math.data.UsageByAssetUrn;
import com.smartsparrow.math.event.MathAssetEventMessage;
import com.smartsparrow.math.route.MathRoute;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class MathAssetServiceTest {

    @InjectMocks
    private MathAssetService mathAssetService;

    @Mock
    private MathAssetGateway mathAssetGateway;

    @Mock
    private ProducerTemplate producerTemplate;

    @Mock
    private Exchange response;

    @Mock
    private CamelReactiveStreamsService camelReactiveStreamsService;

    @Mock
    private AssetBuilder assetBuilder;

    @Mock
    private Asset asset;

    @Mock
    private MathConfig mathConfig;

    private static final String mathML = "<math><mn>1</mn><mo>-</mo><mn>2</mn></math>";
    private static final String altText = "alt text";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID assetId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final String hash = "e3b0c44298fc1c149afbf4c";
    private MathAssetEventMessage eventMessage;
    private AssetUrn assetUrn;
    private AssetByHash assetByHash;
    private List<String> elementIds;
    private UsageByAssetUrn usageByAssetUrn;
    private AssetIdByUrn assetIdByUrn;
    private AssetSummary assetSummary;

    private static final MathAssetRequestNotification requestNotification = MathAssetTestStub.buildRequestNotification(
            assetId, mathML);
    private static final MathAssetResultNotification resultNotification = MathAssetTestStub
            .buildResultNotification(requestNotification, "<svg></svg>");
    private static final MathAssetErrorNotification errorNotification = MathAssetTestStub
            .buildErrorNotification("cause", "error", requestNotification.getNotificationId(), assetId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        eventMessage = new MathAssetEventMessage(mathML);

        assetUrn = new AssetUrn(assetId, MATH);

        assetByHash = new AssetByHash()
                .setAssetId(assetId)
                .setOwnerId(accountId)
                .setHash(hash);

        elementIds = new ArrayList<>();
        elementIds.add(elementId.toString());

        assetSummary = new AssetSummary();
        assetSummary
                .setId(assetId)
                .setMathML(mathML)
                .setAltText(altText);

        usageByAssetUrn = new UsageByAssetUrn()
                .setAssetId(assetId)
                .setAssetUrn(assetUrn.toString())
                .setElementId(elementIds);

        assetIdByUrn = new AssetIdByUrn()
                .setAssetId(assetId)
                .setAssetUrn(assetUrn.toString());
        when(mathAssetGateway.fetchAssetIdByUrn(assetUrn.toString())).thenReturn(Mono.just(assetIdByUrn));
        when(mathAssetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(assetSummary));
    }

    @Test
    void createNewMathAsset() {
        when(mathAssetGateway.findByHash(anyString())).thenReturn(Mono.empty());
        when(camelReactiveStreamsService.toStream(eq(MATH_ASSET_GET),
                                                  any(MathAssetEventMessage.class),
                                                  eq(MathAssetEventMessage.class)))
                .thenReturn(Mono.just(eventMessage));
        when(mathAssetGateway.findByUrn(anyString())).thenReturn(Mono.just(usageByAssetUrn));
        when(mathAssetGateway.persist(eq(eventMessage),
                                      anyString(),
                                      any(AssetUrn.class),
                                      eq(elementId),
                                      eq(accountId),
                                      eq(elementIds))).thenReturn(Mono.just(assetUrn));
        when(producerTemplate.request(eq(MathRoute.SUBMIT_MATH_RESOLVER_REQUEST), any(Processor.class)))
                .thenReturn(response);
        when(mathAssetGateway.persist(any(MathAssetRequestNotification.class))).thenReturn(Flux.just(new Void[]{}));

        AssetUrn assetUrn = mathAssetService.createMathAsset(mathML,
                                                             altText,
                                                             elementId,
                                                             accountId).block();

        assertNotNull(assetUrn);
        assertEquals(assetId, assetUrn.getAssetId());
        assertEquals(MATH, assetUrn.getAssetProvider());

        verify(mathAssetGateway, never()).update(elementId, assetUrn);
        verify(camelReactiveStreamsService, atLeastOnce()).toStream(eq(MATH_ASSET_GET),
                                                                    any(MathAssetEventMessage.class),
                                                                    eq(MathAssetEventMessage.class));
        verify(mathAssetGateway, atLeastOnce()).findByUrn(anyString());
        verify(mathAssetGateway, atLeastOnce()).persist(eq(eventMessage),
                                                        anyString(),
                                                        any(AssetUrn.class),
                                                        eq(elementId),
                                                        eq(accountId),
                                                        eq(elementIds));
    }

    @Test
    void fetchExistingMathAsset() {
        when(mathAssetGateway.findByHash(anyString())).thenReturn(Mono.just(assetByHash));
        when(producerTemplate.request(eq(MathRoute.SUBMIT_MATH_RESOLVER_REQUEST), any(Processor.class)))
                .thenReturn(response);
        when(mathAssetGateway.persist(any(MathAssetRequestNotification.class))).thenReturn(Flux.just(new Void[]{}));
        when(mathAssetGateway.update(elementId, assetUrn)).thenReturn(Mono.just(assetUrn));

        AssetUrn assetUrn = mathAssetService.createMathAsset(mathML,
                                                             altText,
                                                             elementId,
                                                             accountId).block();

        assertNotNull(assetUrn);
        assertEquals(assetId, assetUrn.getAssetId());
        assertEquals(MATH, assetUrn.getAssetProvider());

        verify(mathAssetGateway).update(elementId, assetUrn);
        verify(camelReactiveStreamsService, never()).toStream(eq(MATH_ASSET_GET),
                                                              any(MathAssetEventMessage.class),
                                                              eq(MathAssetEventMessage.class));
        verify(mathAssetGateway, never()).findByUrn(assetUrn.toString());
        verify(mathAssetGateway, never()).persist(eq(eventMessage),
                                                  anyString(),
                                                  eq(assetUrn),
                                                  eq(elementId),
                                                  eq(accountId),
                                                  eq(elementIds));
    }

    @Test
    void getAssetIdByUrnNull() {
        Throwable e = assertThrows(IllegalArgumentFault.class,
                                   () -> mathAssetService.getAssetIdByUrn(null));
        assertEquals(e.getMessage(), "assetUrn is required");
    }

    @Test
    void getAssetIdByUrn() {
        when(mathAssetGateway.fetchAssetIdByUrn(anyString())).thenReturn(Mono.just(assetIdByUrn));

        AssetIdByUrn assetIdByUrn1 = mathAssetService.getAssetIdByUrn(assetIdByUrn.toString()).block();

        assertNotNull(assetIdByUrn1);
        assertEquals(assetId, assetIdByUrn1.getAssetId());
        assertEquals(assetUrn.toString(), assetIdByUrn1.getAssetUrn());
    }

    @Test
    void getAssetsForNull() {
        Throwable e = assertThrows(IllegalArgumentFault.class,
                                   () -> mathAssetService.getAssetsFor(null));
        assertEquals(e.getMessage(), "elementId is required");
    }

    @Test
    void getAssetsFor() {
        String mathAssetUrn = "urn:math:d2f78174-e273-4d84-88bd-24e186869da1";
        when(mathAssetGateway.findAssetUrn(elementId)).thenReturn(Flux.just(mathAssetUrn));
        when(mathAssetGateway.fetchAssetIdByUrn(anyString())).thenReturn(Mono.just(assetIdByUrn));

        AssetIdByUrn assetIdByUrn1 = mathAssetService.getAssetsFor(elementId).blockFirst();

        assertNotNull(assetIdByUrn1);
        assertEquals(assetId, assetIdByUrn1.getAssetId());
        assertEquals(assetUrn.toString(), assetIdByUrn1.getAssetUrn());
    }

    @Test
    void getMathAssetSummary() {
        AssetSummary summary = mathAssetService.getMathAssetSummary(assetUrn.toString()).block();

        assertNotNull(summary);
        assertEquals(assetId, summary.getId());
        assertEquals(mathML, summary.getMathML());
        assertEquals(altText, summary.getAltText());
    }

    @Test
    void processResultNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> mathAssetService.processResultNotification(null));

        assertNotNull(f1);
        assertEquals("resultNotification is required", f1.getMessage());
    }

    @Test
    void processResultNotification() {
        when(mathAssetGateway.persist(any(MathAssetResultNotification.class))).thenReturn(Flux.empty());
        when(mathAssetGateway.persist(any(UUID.class), any(String.class))).thenReturn(Flux.empty());
        ArgumentCaptor<MathAssetResultNotification> captor = ArgumentCaptor.forClass(MathAssetResultNotification.class);
        final MathAssetResultNotification res = mathAssetService
                .processResultNotification(resultNotification)
                .block();

        assertNotNull(res);
        assertNotNull(res.getMathML());
        assertNotNull(res.getSvgShape());
        assertNotNull(res.getAssetId());

        verify(mathAssetGateway).persist(captor.capture());

        final MathAssetResultNotification captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);
    }

    @Test
    void processErrorNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> mathAssetService.processErrorNotification(null));

        assertNotNull(f1);
        assertEquals("errorNotification is required", f1.getMessage());
    }

    @Test
    void processErrorNotification() {
        when(mathAssetGateway.persist(any(MathAssetErrorNotification.class))).thenReturn(Flux.just(new Void[]{}));
        ArgumentCaptor<MathAssetErrorNotification> errorCaptor = ArgumentCaptor.forClass(MathAssetErrorNotification.class);
        final MathAssetErrorNotification res = mathAssetService
                .processErrorNotification(new MathAssetErrorNotification()
                                                  .setNotificationId(UUID.randomUUID()))
                .block();

        assertNotNull(res);
        assertNull(res.getAssetId());

        verify(mathAssetGateway).persist(errorCaptor.capture());

        final MathAssetErrorNotification capturedError = errorCaptor.getValue();

        assertNotNull(capturedError);
        assertNotNull(capturedError.getNotificationId());
    }

    @Test
    void processRetryNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> mathAssetService.processRetryNotification(null));

        assertNotNull(f1);
        assertEquals("retryNotification is required", f1.getMessage());
    }

    @Test
    void processRetryNotification() {
        when(mathAssetGateway.persist(any(MathAssetRetryNotification.class))).thenReturn(Flux.empty());
        ArgumentCaptor<MathAssetRetryNotification> captor = ArgumentCaptor.forClass(MathAssetRetryNotification.class);

        final String payload = "{\"foo\":\"bar\"}";
        final MathAssetRetryNotification res = mathAssetService
                .processRetryNotification(new MathAssetRetryNotification()
                                                  .setNotificationId(UUID.randomUUID()))
                .block();

        assertNotNull(res);
        assertNotNull(res.getNotificationId());

        verify(mathAssetGateway).persist(captor.capture());
    }

    @Test
    void validate_noElementId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> mathAssetService.removeMathAsset(null, assetUrn.toString()));
        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void validate_noAssetUrn() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> mathAssetService.removeMathAsset(elementId, null));
        assertEquals("assetUrn is required", ex.getMessage());
    }

    @Test
    void removeAsset() {
        when(mathAssetGateway.findByUrn(assetUrn.toString())).thenReturn(Mono.just(usageByAssetUrn));
        when(mathAssetGateway.remove(elementId, assetUrn)).thenReturn(Flux.empty());

        mathAssetService.removeMathAsset(elementId, assetUrn.toString()).blockLast();

        verify(mathAssetGateway, atLeastOnce()).remove(elementId, assetUrn);
    }

    @Test
    void removeAsset_invalidAssetProvider() {
        UUID assetId = UUID.randomUUID();
        String invalidUrn = "urn:foo:" + assetId;

        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(mathAssetGateway.findByUrn(anyString())).thenReturn(Mono.just(usageByAssetUrn));
        when(mathAssetGateway.remove(any(), any())).thenReturn(publisher.flux());

        assertThrows(AssetURNParseException.class, () ->
                mathAssetService.removeMathAsset(elementId, invalidUrn).blockLast());
        publisher.assertWasNotRequested();
    }

    @Test
    void removeAsset_invalidUrn() {
        String invalidUrn = "urn:aero:asdasd";

        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(mathAssetGateway.findByUrn(anyString())).thenReturn(Mono.just(usageByAssetUrn));
        when(mathAssetGateway.remove(any(), any())).thenReturn(publisher.flux());

        assertThrows(AssetURNParseException.class, () ->
                mathAssetService.removeMathAsset(elementId, invalidUrn).blockLast());
        publisher.assertWasNotRequested();
    }


    @Test
    void getMathAssetPayload_noAssetId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> mathAssetService.getMathAssetPayload(null));
        assertEquals("assetId is required", ex.getMessage());
    }

    @Test
    void getMathAssetPayload() {
        final AssetSummary assetSummary = new AssetSummary()
                .setId(assetId)
                .setMathML(mathML)
                .setAltText(altText);
        when(mathAssetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(assetSummary));
        when(asset.getAssetProvider()).thenReturn(AssetProvider.MATH);
        when(assetBuilder.setMathAssetData(any(MathAssetData.class))).thenReturn(assetBuilder);
        when(assetBuilder.setAssetSummary(any(com.smartsparrow.asset.data.AssetSummary.class)))
                .thenReturn(assetBuilder);
        when(assetBuilder.build(any(AssetProvider.class))).thenReturn(asset);

        AssetPayload assetPayload = mathAssetService.getMathAssetPayload(assetId).block();

        assertNotNull(assetPayload);
        assertNotNull(assetPayload.getAsset());
        assertEquals(String.format("urn:%s:%s", AssetProvider.MATH.getLabel(), assetSummary.getId()), assetPayload.getUrn());
    }

    @Test
    void isFeatureEnabled() {
        when(mathConfig.isEnabled()).thenReturn(true);
        assertTrue(mathAssetService.isFeatureEnabled());

        when(mathConfig.isEnabled()).thenReturn(false);
        assertFalse(mathAssetService.isFeatureEnabled());
    }
}
