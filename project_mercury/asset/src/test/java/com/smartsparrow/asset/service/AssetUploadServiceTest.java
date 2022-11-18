package com.smartsparrow.asset.service;

import static com.smartsparrow.asset.route.AssetRoute.UPLOAD_ASSET_ROUTE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetConstant;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSource;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.lang.AssetUploadException;
import com.smartsparrow.asset.lang.AssetUploadValidationException;
import com.smartsparrow.asset.lang.UnsupportedAssetException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AssetUploadServiceTest {

    @InjectMocks
    private AssetUploadService assetUploadService;

    @Mock
    private ProducerTemplate producerTemplate;

    @Mock
    private AssetGateway assetGateway;

    @Mock
    private AssetTemplate assetTemplate;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private AssetSource assetSource;

    @Mock
    private BronteAssetTypeHandler bronteAssetTypeHandler;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final AssetVisibility visibility = AssetVisibility.SUBSCRIPTION;
    private static final AssetProvider provider = AssetProvider.AERO;
    private static final String fileNameImage = "orange_diamond.png";
    private static final String fileNameImageSVG = "assetUploadSvgTest.svg";
    private static final String fileNameImageJpeg = "assetUploadJpegTest.jpg";
    private static final String fileNameDocVTT = "sampleVTT.vtt";
    private static final String fileNameDocSUBRIP = "sampleSubRip.srt";
    private static final String fileNameDocSSA = "sampleSSA.ssa";
    private static final String fileNameDocTTML = "sampleTTML.ttml";
    private static final String fileNameText = "text_asset.js";
    private static final String fileNameIconSVG = "iconUpload.svg";

    private Exchange response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        response = mock(Exchange.class);

        when(assetTemplate.getOwnerId()).thenReturn(accountId);
        when(assetTemplate.getSubscriptionId()).thenReturn(subscriptionId);
        when(assetTemplate.getProvider()).thenReturn(provider);
        when(assetTemplate.getVisibility()).thenReturn(visibility);
        when(assetTemplate.getMetadata()).thenReturn(null);

        when(producerTemplate.request(eq(UPLOAD_ASSET_ROUTE), any(Processor.class)))
                .thenReturn(response);

        when(assetGateway.persist(any(AssetSummary.class))).thenReturn(Flux.just(new Void[]{}));

        when(bronteAssetService.saveMetadata(any(UUID.class), any(Map.class)))
                .thenReturn(Flux.just(new Void[]{}).singleOrEmpty());

        when(bronteAssetService.saveAssetSource(any(AssetSummary.class), any(File.class), anyString(), anyString()))
                .thenReturn(Mono.just(assetSource));

        when(bronteAssetTypeHandler.handle(any(AssetSource.class), any(String.class),any(Map.class),  any(AssetMediaType.class)))
                .thenReturn(Mono.just(new BronteAssetEmptyResponse()));
        when(bronteAssetTypeHandler.handle(any(AssetSource.class), any(String.class),any(Map.class),  any(AssetMediaType.class)))
                .thenReturn(Mono.just(new BronteAssetEmptyResponse()));
        when(bronteAssetTypeHandler.handle(any(AssetSource.class), any(String.class),any(Map.class),  any(AssetMediaType.class)))
                .thenReturn(Mono.just(new BronteAssetEmptyResponse()));
        when(bronteAssetTypeHandler.handle(any(AssetSource.class), any(String.class),any(Map.class),  any(AssetMediaType.class)))
                .thenReturn(Mono.just(new BronteAssetImageResponse()));
        when(bronteAssetTypeHandler.handle(any(AssetSource.class), any(String.class),any(Map.class),  any(AssetMediaType.class)))
                .thenReturn(Mono.just(new BronteAssetIconResponse()));
    }

    @Test
    void save_invalidTemplate_noInputStream() {
        when(assetTemplate.getInputStream()).thenReturn(null);

        AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                ()-> assetUploadService.save(assetTemplate).block());

        assertEquals("inputStream is required", e.getMessage());
    }

    @Test
    void save_invalidTemplate_noExtension() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getFileExtension()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(assetTemplate).block());

            assertEquals("fileExtension is required", e.getMessage());
        });

    }

    @Test
    void save_invalidTemplate_noOwnerId() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getOwnerId()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(assetTemplate).block());

            assertEquals("ownerId is required", e.getMessage());
        });

    }

    @Test
    void save_invalidTemplate_noSubscriptionId() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getSubscriptionId()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(assetTemplate).block());

            assertEquals("subscriptionId is required", e.getMessage());
        });

    }

    @Test
    void save_invalidTemplate_noProvider() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getProvider()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(assetTemplate).block());

            assertEquals("assetProvider is required", e.getMessage());
        });

    }

    @Test
    void save_invalidTemplate_noVisibility() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getVisibility()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(assetTemplate).block());

            assertEquals("assetVisibility is required", e.getMessage());
        });
    }

    @Test
    void save_invalidMimeType() {
        mockAssetTemplate(fileNameText, ".js", () -> {
            UnsupportedAssetException e = assertThrows(UnsupportedAssetException.class,
                    ()-> assetUploadService.save(assetTemplate).block());

            assertEquals("File extension .js not supported", e.getMessage());
        });
    }

    @Test
    void save_camelRouteFailed() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(response.isFailed()).thenReturn(true);
            when(response.getProperty(Exchange.EXCEPTION_CAUGHT)).thenReturn(new Exception("face_palm"));

            AssetUploadException e = assertThrows(AssetUploadException.class,
                    ()-> assetUploadService.save(assetTemplate).block());

            assertEquals("face_palm", e.getMessage());
        });
    }

    @Test
    void save_image_success() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.IMAGE, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
            verify(bronteAssetTypeHandler).handle(eq(assetSource),anyString(), eq(metadata), eq(AssetMediaType.IMAGE));
        });
    }

    @Test
    void save_image_successUrnInTemplate() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);
            when(assetTemplate.getUrn()).thenReturn("urn");

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals("urn", summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.IMAGE, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
            verify(bronteAssetTypeHandler).handle(eq(assetSource), anyString(), eq(metadata), eq(AssetMediaType.IMAGE));
        });
    }

    @Test
    void saveWithAssetId_invalidTemplate_noInputStream() {
        when(assetTemplate.getInputStream()).thenReturn(null);

        AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

        assertEquals("inputStream is required", e.getMessage());
    }

    @Test
    void saveWithAssetId_invalidTemplate_noExtension() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getFileExtension()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

            assertEquals("fileExtension is required", e.getMessage());
        });

    }

    @Test
    void saveWithAssetId_invalidTemplate_noOwnerId() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getOwnerId()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

            assertEquals("ownerId is required", e.getMessage());
        });

    }

    @Test
    void saveWithAssetId_invalidTemplate_noSubscriptionId() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getSubscriptionId()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

            assertEquals("subscriptionId is required", e.getMessage());
        });

    }

    @Test
    void saveWithAssetId_invalidTemplate_noProvider() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getProvider()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

            assertEquals("assetProvider is required", e.getMessage());
        });

    }

    @Test
    void saveWithAssetId_invalidTemplate_noVisibility() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(assetTemplate.getVisibility()).thenReturn(null);

            AssetUploadValidationException e = assertThrows(AssetUploadValidationException.class,
                    ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

            assertEquals("assetVisibility is required", e.getMessage());
        });
    }

    @Test
    void saveWithAssetId_invalidMimeType() {
        mockAssetTemplate(fileNameText, ".js", () -> {
            UnsupportedAssetException e = assertThrows(UnsupportedAssetException.class,
                    ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

            assertEquals("File extension .js not supported", e.getMessage());
        });
    }

    @Test
    void saveWithAssetId_camelRouteFailed() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            when(response.isFailed()).thenReturn(true);
            when(response.getProperty(Exchange.EXCEPTION_CAUGHT)).thenReturn(new Exception("face_palm"));

            AssetUploadException e = assertThrows(AssetUploadException.class,
                    ()-> assetUploadService.save(UUID.randomUUID(), assetTemplate).block());

            assertEquals("face_palm", e.getMessage());
        });
    }

    @Test
    void saveWithAssetId_image_success() {
        mockAssetTemplate(fileNameImage, ".png", () -> {
            final UUID assetId = UUID.randomUUID();
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetId, assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertEquals(assetId, summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.IMAGE, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
        });
    }

   @Test
    void save_svg_image_success() {
        mockAssetTemplate(fileNameIconSVG, ".svg", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put(AssetConstant.ICON_MEDIA_TYPE, "ICON"); put(AssetConstant.ICON_LIBRARY, "MICROSOFT_ICON");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.ICON, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
        });
    }

    @Test
    void save_jpeg_image_success() {
        mockAssetTemplate(fileNameImageJpeg, ".jpg", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.IMAGE, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
        });
    }

    @Test
    void save_docVTT_success() {
        mockAssetTemplate(fileNameDocVTT, ".vtt", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.DOCUMENT, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
            verify(bronteAssetTypeHandler).handle(eq(assetSource), anyString(), eq(metadata), eq(AssetMediaType.DOCUMENT));
        });
    }

    @Test
    void save_docTTML_success() {
        mockAssetTemplate(fileNameDocTTML, ".ttml", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.DOCUMENT, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
            verify(bronteAssetTypeHandler).handle(eq(assetSource), anyString(), eq(metadata), eq(AssetMediaType.DOCUMENT));
        });
    }

    @Test
    void save_DocSubRip_success() {
        mockAssetTemplate(fileNameDocSUBRIP, ".srt", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.DOCUMENT, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
            verify(bronteAssetTypeHandler).handle(eq(assetSource), anyString(), eq(metadata), eq(AssetMediaType.DOCUMENT));
        });
    }

    @Test
    void save_DocSSA_success() {
        mockAssetTemplate(fileNameDocSSA, ".ssa", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.DOCUMENT, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
            verify(bronteAssetTypeHandler).handle(eq(assetSource), anyString(), eq(metadata), eq(AssetMediaType.DOCUMENT));
        });
    }

    @Test
    void test_getAssetMediaType_ICON(){
        Map<String, String> metaData = new HashMap<>();
        metaData.put("mediaType","ICON");
        metaData.put("iconLibrary","MICROSOFT_ICON");
        AssetMediaType assetMediaType = assetUploadService.getAssetMediaType(".svg", metaData);
        assertNotNull(assetMediaType);
        assertEquals(assetMediaType, AssetMediaType.ICON);
    }

    @Test
    void test_getAssetMediaType_NOT_ICON(){
        Map<String, String> metaData = new HashMap<>();
        metaData.put("mediaType","ICONS");
        metaData.put("iconLibrary","MICROSOFT_ICON");
        AssetMediaType assetMediaType = assetUploadService.getAssetMediaType(".svg", metaData);
        assertNotNull(assetMediaType);
        assertEquals(assetMediaType, AssetMediaType.IMAGE);
    }
    @Test
    void test_getAssetMediaType(){
        Map<String, String> metaData = new HashMap<>();
        AssetMediaType assetMediaType = assetUploadService.getAssetMediaType(".svg", metaData);
        assertNotNull(assetMediaType);
        assertEquals(assetMediaType, AssetMediaType.IMAGE);
    }

    @Test
    void save_icon_image_success() {
        mockAssetTemplate(fileNameImageSVG, ".svg", () -> {
            Map<String, String> metadata = new HashMap<String, String>(){
                {put("foo","bar"); put("aero", "mercury");}
            };
            when(response.isFailed()).thenReturn(false);
            when(assetTemplate.getMetadata()).thenReturn(metadata);

            ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);

            when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
            when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

            when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

            AssetPayload payload = assetUploadService.save(assetTemplate).block();

            verify(assetGateway).persist(summaryCaptor.capture());

            AssetSummary summary = summaryCaptor.getValue();

            assertNotNull(payload);

            assertAll(() -> {
                assertNotNull(summary.getUrn());
                assertEquals(AssetUtils.buildURN(summary), summary.getUrn());
                assertNotNull(summary.getId());
                assertNotNull(summary.getHash());
                assertEquals(AssetMediaType.IMAGE, summary.getMediaType());
                assertEquals(accountId, summary.getOwnerId());
                assertEquals(subscriptionId, summary.getSubscriptionId());
                assertEquals(AssetProvider.AERO, summary.getProvider());
                assertEquals(AssetVisibility.SUBSCRIPTION, summary.getVisibility());
            });

            verify(bronteAssetService).saveAssetSource(eq(summary), any(File.class), anyString(), anyString());
            verify(bronteAssetService).saveMetadata(summary.getId(), metadata);
        });
    }

    private File load(ClassLoader classLoader, String name) {
        URL url = classLoader.getResource(name);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }

    private void mockAssetTemplate(String fileName, String extension, Runnable runnable) {
        ClassLoader classLoader = getClass().getClassLoader();
        File asset = load(classLoader, fileName);

        assert asset != null;
        try (FileInputStream fileInputStream = new FileInputStream(asset)) {
            when(assetTemplate.getInputStream()).thenReturn(fileInputStream);
            when(assetTemplate.getOriginalFileName()).thenReturn(fileName);
            when(assetTemplate.getFileExtension()).thenReturn(extension);
            when(assetTemplate.getMetadata()).thenReturn(new HashMap<>());

            runnable.run();

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
