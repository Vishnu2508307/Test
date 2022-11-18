package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.imaging.ImageReadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.data.AudioSource;
import com.smartsparrow.asset.data.AudioSourceName;
import com.smartsparrow.asset.data.BronteAsset;
import com.smartsparrow.asset.data.DocumentSource;
import com.smartsparrow.asset.data.ExternalSource;
import com.smartsparrow.asset.data.IconAssetSummary;
import com.smartsparrow.asset.data.IconSource;
import com.smartsparrow.asset.data.IconSourceName;
import com.smartsparrow.asset.data.IconsByLibrary;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.data.VideoSource;
import com.smartsparrow.asset.data.VideoSourceName;
import com.smartsparrow.asset.data.VideoSubtitle;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.util.ImageDimensions;
import com.smartsparrow.util.Images;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class BronteAssetServiceTest {

    @InjectMocks
    private BronteAssetService bronteAssetService;

    @Mock
    private AssetGateway assetGateway;

    @Mock
    private AssetConfig assetConfig;

    @Mock
    private AssetSignatureService assetSignatureService;

    @Mock
    private AssetBuilder assetBuilder;

    @Mock
    private ExternalAssetService externalAssetService;

    private static final UUID assetId = UUID.randomUUID();
    private static final String metadataKey = "altText";
    private static final String metadataValue = "small image";

    private AssetSummary assetSummary;
    private AssetSummary assetSummary2;
    private ImageSource imageSource1;
    private ImageSource imageSource2;
    private ImageSource imageSource3;
    private AssetMetadata metadata1;
    private AssetMetadata metadata2;
    private static final Account creator = new Account()
                                    .setId(UUID.randomUUID())
                                    .setSubscriptionId(UUID.randomUUID());
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(assetConfig.getPublicUrl()).thenReturn("https://publicUrl");
        when(assetBuilder.setAssetSummary(any(AssetSummary.class)))
                .thenReturn(assetBuilder);
        when(assetBuilder.build(any(AssetProvider.class)))
                .thenReturn(new BronteAsset()
                        .setId(assetId));

        when(assetGateway.findAssetId(anyString()))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(assetId)));

        assetSummary = new AssetSummary()
                .setId(assetId)
                .setHash("hash1")
                .setProvider(AssetProvider.AERO)
                .setUrn(new AssetUrn(assetId, AssetProvider.AERO).toString())
                .setOwnerId(UUID.randomUUID())
                .setSubscriptionId(UUID.randomUUID())
                .setVisibility(AssetVisibility.GLOBAL);

        assetSummary2 = new AssetSummary()
                .setId(assetId)
                .setHash("hash1")
                .setProvider(AssetProvider.AERO)
                .setUrn(new AssetUrn(assetId, AssetProvider.AERO).toString())
                .setOwnerId(UUID.randomUUID())
                .setSubscriptionId(UUID.randomUUID())
                .setVisibility(AssetVisibility.GLOBAL)
                .setMediaType(AssetMediaType.IMAGE);

        imageSource1 = new ImageSource()
                .setAssetId(assetId)
                .setName(ImageSourceName.ORIGINAL)
                .setUrl("url for Original")
                .setHeight(100.0)
                .setWidth(100.0);

        imageSource2 = new ImageSource()
                .setAssetId(assetId)
                .setName(ImageSourceName.SMALL)
                .setUrl("url for Small")
                .setHeight(50.0)
                .setWidth(50.0);

        imageSource3 = new ImageSource()
                .setAssetId(assetId)
                .setName(ImageSourceName.SMALL)
                .setUrl("url for Small")
                .setHeight((double)0)
                .setWidth((double)0);

        metadata1 = new AssetMetadata().setAssetId(assetId).setKey("meta1").setValue("value1");
        metadata2 = new AssetMetadata().setAssetId(assetId).setKey("meta2").setValue("value2");

        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(assetSummary));
        when(assetGateway.persist(any(AssetSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(DocumentSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(VideoSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(AudioSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(IconSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persistExternal(any(AssetSummary.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getImageSourcePayload() {
        ImageSource is1 = new ImageSource()
                .setAssetId(assetId)
                .setName(ImageSourceName.ORIGINAL)
                .setUrl("url for Original")
                .setHeight(100.0)
                .setWidth(100.0);

        ImageSource is2 = new ImageSource()
                .setAssetId(assetId)
                .setName(ImageSourceName.SMALL)
                .setUrl("url for small")
                .setHeight(72.0)
                .setWidth(72.0);

        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.just(is1, is2));
        when(assetSignatureService.signUrl("https://publicUrl/url for Original"))
                .thenReturn(Mono.just("https://publicUrl/url for Original"));
        when(assetSignatureService.signUrl("https://publicUrl/url for small"))
                .thenReturn(Mono.just("https://publicUrl/url for small"));

        Map<String, Object> result = bronteAssetService.getImageSourcePayload(assetId).block();


        assertNotNull(result);
        assertEquals(2, result.size());
        Map<String, Object> original = (Map<String, Object>) result.get("original");
        assertEquals(3, original.size());
        assertEquals("https://publicUrl/url for Original", original.get("url"));
        assertEquals(100.0, original.get("height"));
        assertEquals(100.0, original.get("width"));
        Map<String, Object> small = (Map<String, Object>) result.get("small");
        assertEquals(3, small.size());
        assertEquals("https://publicUrl/url for small", small.get("url"));
        assertEquals(72.0, small.get("height"));
        assertEquals(72.0, small.get("width"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAudioSourcePayload() {
        AudioSource audioSource = new AudioSource()
                .setAssetId(assetId)
                .setName(AudioSourceName.ORIGINAL)
                .setUrl("url");

        when(assetGateway.fetchAudioSources(assetId)).thenReturn(Flux.just(audioSource));
        when(assetSignatureService.signUrl(anyString())).thenReturn(Mono.just("https://publicUrl/url"));

        Map<String, Object> result = bronteAssetService.getAudioSourcePayload(assetId).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> original = (Map<String, Object>) result.get("original");
        assertNotNull(original);
        assertEquals("https://publicUrl/url", original.get("url"));
    }

    @Test
    void getImageSourcePayload_noSources() {
        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.empty());

        Map<String, Object> result = bronteAssetService.getImageSourcePayload(assetId).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getVideoSourcePayload() {
        VideoSource vs1 = new VideoSource()
                .setAssetId(assetId)
                .setName(VideoSourceName.ORIGINAL)
                .setUrl("url for Original")
                .setResolution("720p");
        VideoSubtitle sub1 = new VideoSubtitle().setAssetId(assetId).setLang("en").setUrl("url for en");
        VideoSubtitle sub2 = new VideoSubtitle().setAssetId(assetId).setLang("ru").setUrl("url for ru");
        when(assetGateway.fetchVideoSources(assetId)).thenReturn(Flux.just(vs1));
        when(assetGateway.fetchVideoSubtitles(assetId)).thenReturn(Flux.just(sub1, sub2));
        when(assetSignatureService.signUrl("https://publicUrl/url for Original"))
                .thenReturn(Mono.just("https://publicUrl/url for Original"));
        when(assetSignatureService.signUrl("https://publicUrl/url for en"))
                .thenReturn(Mono.just("https://publicUrl/url for en"));
        when(assetSignatureService.signUrl("https://publicUrl/url for ru"))
                .thenReturn(Mono.just("https://publicUrl/url for ru"));

        Map<String, Object> result = bronteAssetService.getVideoSourcePayload(assetId).block();

        assertNotNull(result);
        assertEquals(2, result.size());
        Map<String, Object> original = (Map<String, Object>) result.get("original");
        assertEquals(2, original.size());
        assertEquals("https://publicUrl/url for Original", original.get("url"));
        assertEquals("720p", original.get("resolution"));
        Map<String, Object> subtitles = (Map<String, Object>) result.get("subtitles");
        assertEquals(2, subtitles.size());
        assertEquals("https://publicUrl/url for en", subtitles.get("en"));
        assertEquals("https://publicUrl/url for ru", subtitles.get("ru"));
    }

    @Test
    void getVideoSourcePayload_noSubtitles() {
        VideoSource vs1 = new VideoSource()
                .setAssetId(assetId)
                .setName(VideoSourceName.ORIGINAL)
                .setUrl("url for Original")
                .setResolution("720p");
        when(assetGateway.fetchVideoSources(assetId)).thenReturn(Flux.just(vs1));
        when(assetGateway.fetchVideoSubtitles(assetId)).thenReturn(Flux.empty());
        when(assetSignatureService.signUrl(anyString())).thenReturn(Mono.just("url for Original"));

        Map<String, Object> result = bronteAssetService.getVideoSourcePayload(assetId).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("original"));
    }

    @Test
    void getVideoSourcePayload_noSources() {
        VideoSubtitle sub1 = new VideoSubtitle().setAssetId(assetId).setLang("en").setUrl("url for en");
        when(assetGateway.fetchVideoSources(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchVideoSubtitles(assetId)).thenReturn(Flux.just(sub1));
        when(assetSignatureService.signUrl(anyString())).thenReturn(Mono.just("url for en"));

        Map<String, Object> result = bronteAssetService.getVideoSourcePayload(assetId).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("subtitles"));
    }

    @Test
    void getVideoSourcePayload_noSourcesAndSubtitles() {
        when(assetGateway.fetchVideoSources(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchVideoSubtitles(assetId)).thenReturn(Flux.empty());

        Map<String, Object> result = bronteAssetService.getVideoSourcePayload(assetId).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDocumentSourcePayload() {
        DocumentSource ds = new DocumentSource()
                .setAssetId(assetId)
                .setUrl("url for document");
        when(assetGateway.fetchDocumentSource(assetId)).thenReturn(Mono.just(ds));
        when(assetSignatureService.signUrl(anyString())).thenReturn(Mono.just("https://publicUrl/url for document"));
        Map<String, Object> result = bronteAssetService.getDocumentSourcePayload(assetId).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("https://publicUrl/url for document", result.get("url"));
    }

    @Test
    void getDocumentSourcePayload_noDocument() {
        when(assetGateway.fetchDocumentSource(assetId)).thenReturn(Mono.empty());

        Map<String, Object> result = bronteAssetService.getDocumentSourcePayload(assetId).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAssetPayload_image() {
        BronteAssetService bronteAssetServiceSpy = Mockito.spy(bronteAssetService);
        String urn = "urn:aero:" + assetId;
        Map<String, Object> imageSources = new HashMap<>();
        imageSources.put("original", new HashMap<>());
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(new AssetSummary().setId(assetId)
                .setProvider(AssetProvider.AERO).setMediaType(AssetMediaType.IMAGE)));
        Mockito.doReturn(Mono.just(imageSources)).when(bronteAssetServiceSpy).getImageSourcePayload(assetId);
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(new AssetMetadata().setKey("mimeType").setValue("jpeg"),
                new AssetMetadata().setKey("key").setValue("value")));

        AssetPayload result = bronteAssetServiceSpy.getAssetPayload(urn).block();

        assertNotNull(result);
        assertEquals(urn, result.getUrn());
        assertNotNull(result.getAsset());
        assertEquals(1, result.getSource().size());
        assertEquals(2, result.getMetadata().size());
        assertNotNull(result.getSource().get("original"));
        assertEquals("jpeg", result.getMetadata().get("mimeType"));
        assertEquals("value", result.getMetadata().get("key"));
    }

    @Test
    void getAssetPayload_video() {
        BronteAssetService bronteAssetServiceSpy = Mockito.spy(bronteAssetService);
        String urn = "urn:aero:" + assetId;
        Map<String, Object> videoSources = new HashMap<>();
        videoSources.put("original", new HashMap<>());
        videoSources.put("subtitles", new HashMap<>());
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(new AssetSummary().setId(assetId)
                .setProvider(AssetProvider.AERO).setMediaType(AssetMediaType.VIDEO)));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());
        Mockito.doReturn(Mono.just(videoSources)).when(bronteAssetServiceSpy).getVideoSourcePayload(assetId);

        AssetPayload result = bronteAssetServiceSpy.getAssetPayload(urn).block();

        assertNotNull(result);
        assertNotNull(result.getAsset());
        assertEquals(urn, result.getUrn());
        assertEquals(2, result.getSource().size());
        assertNotNull(result.getSource().get("original"));
        assertNotNull(result.getSource().get("subtitles"));
    }


    @Test
    void getAssetPayload_doc() {
        BronteAssetService bronteAssetServiceSpy = Mockito.spy(bronteAssetService);
        String urn = "urn:aero:" + assetId;
        Map<String, Object> docSource = new HashMap<>();
        docSource.put("url", "document url");
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(new AssetSummary().setId(assetId)
                .setProvider(AssetProvider.AERO).setMediaType(AssetMediaType.DOCUMENT)));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());
        Mockito.doReturn(Mono.just(docSource)).when(bronteAssetServiceSpy).getDocumentSourcePayload(assetId);

        AssetPayload result = bronteAssetServiceSpy.getAssetPayload(urn).block();

        assertNotNull(result);
        assertNotNull(result.getAsset());
        assertEquals(urn, result.getUrn());
        assertEquals(1, result.getSource().size());
        assertEquals("document url", result.getSource().get("url"));
    }

    @Test
    void getAssetPayload_noAsset() {
        String urn = "urn:aero:" + assetId;
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.empty());
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        AssetPayload result = bronteAssetService.getAssetPayload(urn).block();

        assertNull(result);
    }


    @Test
    void saveMetadata() {
        final UUID assetId = UUID.randomUUID();
        ArgumentCaptor<AssetMetadata> captor = ArgumentCaptor.forClass(AssetMetadata.class);

        final Map<String, String> metadata = new HashMap<String, String>() {
            {
                put("a", "1");
            }

            {
                put("b", "2");
            }
        };

        when(assetGateway.persist(any(AssetMetadata.class)))
                .thenReturn(Flux.just(new Void[]{}));

        bronteAssetService.saveMetadata(assetId, metadata)
                .block();

        verify(assetGateway).persist(captor.capture());

        List<AssetMetadata> all = captor.getAllValues();

        assertNotNull(all);
        assertEquals(2, all.size());

        assertEquals("a", all.get(0).getKey());
        assertEquals("1", all.get(0).getValue());
        assertEquals("b", all.get(1).getKey());
        assertEquals("2", all.get(1).getValue());
    }

    @Test
    void create_providerAERO() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> bronteAssetService.create("url", AssetVisibility.GLOBAL, new Account(),
                AssetMediaType.DOCUMENT, null, AssetProvider.AERO)
                .block());

        assertNotNull(f);
        assertEquals("AERO provider not supported by this service", f.getMessage());
    }

    @Test
    void create() {
        when(externalAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(new AssetPayload()));

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<ExternalSource> externalSourceCaptor = ArgumentCaptor.forClass(ExternalSource.class);

        when(assetGateway.persistExternal(any(AssetSummary.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(assetGateway.persist(any(ExternalSource.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(assetGateway.fetchAssetById(any(UUID.class)))
                .thenReturn(Mono.just(new AssetSummary()
                        .setId(UUID.randomUUID())
                        .setProvider(AssetProvider.EXTERNAL)));

        when(assetGateway.fetchMetadata(any(UUID.class))).thenReturn(Flux.empty());
        when(assetGateway.fetchExternalSource(any(UUID.class)))
                .thenReturn(Mono.just(new ExternalSource()
                        .setUrl("https://publicUrl")));

        when(assetSignatureService.signUrl("https://publicUrl")).thenReturn(Mono.just("signed-url"));

        AssetPayload created = bronteAssetService.create("https://publicUrl", AssetVisibility.GLOBAL, creator, AssetMediaType.DOCUMENT,
                null, AssetProvider.EXTERNAL)
                .block();

        assertNotNull(created);

        verify(assetGateway, never()).persist(any(AssetSummary.class));

        verify(assetGateway).persistExternal(summaryCaptor.capture());
        verify(assetGateway).persist(externalSourceCaptor.capture());

        final AssetSummary persistedSummary = summaryCaptor.getValue();

        assertAll(() -> {
            assertNotNull(persistedSummary);
            assertNotNull(persistedSummary.getId());
            assertEquals(AssetVisibility.GLOBAL, persistedSummary.getVisibility());
            assertEquals(AssetMediaType.DOCUMENT, persistedSummary.getMediaType());
            assertEquals(creator.getId(), persistedSummary.getOwnerId());
            assertEquals(creator.getSubscriptionId(), persistedSummary.getSubscriptionId());
            assertNull(persistedSummary.getHash());
        });

        final ExternalSource persistedSource = externalSourceCaptor.getValue();

        assertAll(() -> {
            assertNotNull(persistedSource);
            assertEquals(persistedSummary.getId(), persistedSource.getAssetId());
            assertEquals("https://publicUrl", persistedSource.getUrl());
        });
    }

    @Test
    void getIconSourcePayload_noSources() {
        when(assetGateway.fetchIconSources(assetId)).thenReturn(Flux.empty());

        Map<String, Object> result = bronteAssetService.getIconSourcePayload(assetId).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getIconSourcePayload() {
        IconSource is1 = new IconSource()
                .setAssetId(assetId)
                .setName(IconSourceName.ORIGINAL)
                .setUrl("url for Original")
                .setHeight(100.0)
                .setWidth(100.0);

        IconSource is2 = new IconSource()
                .setAssetId(assetId)
                .setName(IconSourceName.SMALL)
                .setUrl("url for small")
                .setHeight(72.0)
                .setWidth(72.0);

        when(assetGateway.fetchIconSources(assetId)).thenReturn(Flux.just(is1, is2));
        when(assetSignatureService.signUrl("https://publicUrl/url for Original"))
                .thenReturn(Mono.just("https://publicUrl/url for Original"));
        when(assetSignatureService.signUrl("https://publicUrl/url for small"))
                .thenReturn(Mono.just("https://publicUrl/url for small"));

        Map<String, Object> result = bronteAssetService.getIconSourcePayload(assetId).block();

        assertNotNull(result);
        assertEquals(2, result.size());
        Map<String, Object> original = (Map<String, Object>) result.get("original");
        assertEquals(3, original.size());
        assertEquals("https://publicUrl/url for Original", original.get("url"));
        assertEquals(100.0, original.get("height"));
        assertEquals(100.0, original.get("width"));
        Map<String, Object> small = (Map<String, Object>) result.get("small");
        assertEquals(3, small.size());
        assertEquals("https://publicUrl/url for small", small.get("url"));
        assertEquals(72.0, small.get("height"));
        assertEquals(72.0, small.get("width"));
    }

    @Test
    void getAssetPayload_icon() {
        BronteAssetService bronteAssetServiceSpy = Mockito.spy(bronteAssetService);
        String urn = "urn:aero:" + assetId;
        Map<String, Object> iconSources = new HashMap<>();
        iconSources.put("original", new HashMap<>());
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(new AssetSummary().setId(assetId)
                                                                                .setProvider(AssetProvider.AERO).setMediaType(AssetMediaType.ICON)));
        Mockito.doReturn(Mono.just(iconSources)).when(bronteAssetServiceSpy).getIconSourcePayload(assetId);
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(new AssetMetadata().setKey("mimeType").setValue("jpeg"),
                                                                       new AssetMetadata().setKey("key").setValue("value")));

        AssetPayload result = bronteAssetServiceSpy.getAssetPayload(urn).block();

        assertNotNull(result);
        assertEquals(urn, result.getUrn());
        assertNotNull(result.getAsset());
        assertEquals(1, result.getSource().size());
        assertEquals(2, result.getMetadata().size());
        assertNotNull(result.getSource().get("original"));
        assertEquals("jpeg", result.getMetadata().get("mimeType"));
        assertEquals("value", result.getMetadata().get("key"));
    }

    @Test
    public void test_fetchIconAssetsByLibrary() {
        String assetUrn = "urn:aero:" + UUID.randomUUID();
        List<String> iconLibraries = new ArrayList<>();
        iconLibraries.add("Microsoft icon");
        when(assetGateway.fetchAssetsByIconLibrary(any(String.class))).thenReturn(Flux.just(new IconsByLibrary()
                                                                                                    .setAssetUrn(
                                                                                                            assetUrn)));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(new AssetMetadata()
                                                                               .setAssetId(assetId)
                                                                               .setKey(metadataKey)
                                                                               .setValue(metadataValue)));

        IconAssetSummary iconAssetInfoByLibrary = bronteAssetService.fetchIconAssetsByLibrary(iconLibraries).blockFirst();
        assertNotNull(iconAssetInfoByLibrary);
        verify(assetGateway).fetchAssetsByIconLibrary(anyString());
        verify(assetGateway).fetchMetadata(assetId);
    }

    @Test
    void validate_duplicateMissAssetId(){
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> bronteAssetService
                .duplicate(null, null, null));

        assertEquals(t.getMessage(), "assetId is required");
    }

    @Test
    void validate_duplicateMissOwnerId(){
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> bronteAssetService
                .duplicate(assetId, null, null));

        assertEquals(t.getMessage(), "ownerId is required");
    }

    @Test
    void validate_duplicateMissSubscriptionId(){
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> bronteAssetService
                .duplicate(assetId, UUID.randomUUID(), null));

        assertEquals(t.getMessage(), "subscriptionId is required");
    }

    @Test
    void duplicateAeroImageAsset(){
        assetSummary.setMediaType(AssetMediaType.IMAGE);

        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.just(imageSource1, imageSource2));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(metadata1, metadata2));

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<ImageSource> imageSourceCaptor = ArgumentCaptor.forClass(ImageSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway).persist(summaryCaptor.capture());
        verify(assetGateway, times(2)).persist(imageSourceCaptor.capture());
        verify(assetGateway, times(2)).persist(metadataCaptor.capture());

        AssetSummary summary = summaryCaptor.getValue();
        List<ImageSource> imageSources = imageSourceCaptor.getAllValues();
        List<AssetMetadata> metadatas = metadataCaptor.getAllValues();

        assertAll(() -> {
            assertNotNull(summary);
            assertNotEquals(assetSummary.getId(), summary.getId());
            assertNotEquals(assetSummary.getOwnerId(), summary.getOwnerId());
            assertNotEquals(assetSummary.getSubscriptionId(), summary.getSubscriptionId());
            assertNotEquals(assetSummary.getUrn(), summary.getUrn());

            assertEquals(assetSummary.getHash(), summary.getHash());
            assertEquals(assetSummary.getProvider(), summary.getProvider());
            assertEquals(assetSummary.getMediaType(), summary.getMediaType());
            assertEquals(assetSummary.getVisibility(), summary.getVisibility());

            assertEquals(summary.getOwnerId(), creator.getId());
            assertEquals(summary.getSubscriptionId(), creator.getSubscriptionId());

            assertEquals(summary.getId(), newAssetSummary.getId());
            assertEquals(summary.getUrn(), newAssetSummary.getUrn());

            assertNotNull(imageSources);
            assertEquals(2, imageSources.size());
            assertNotEquals(imageSource2.getAssetId(), imageSources.get(1).getAssetId());

            assertEquals(imageSource2.getName(), imageSources.get(1).getName());
            assertEquals(imageSource2.getUrl(), imageSources.get(1).getUrl());
            assertEquals(imageSource2.getHeight(), imageSources.get(1).getHeight());
            assertEquals(imageSource2.getWidth(), imageSources.get(1).getWidth());
            assertEquals(imageSources.get(1).getAssetId(), newAssetSummary.getId());

            assertNotNull(metadatas);
            assertEquals(2, metadatas.size());
            assertEquals(metadatas.get(0).getAssetId(), metadatas.get(1).getAssetId());
            assertEquals(metadatas.get(0).getAssetId(), newAssetSummary.getId());

            assertNotEquals(metadata1.getAssetId(), metadatas.get(0).getAssetId());

            assertEquals(metadata1.getKey(), metadatas.get(0).getKey());
            assertEquals(metadata1.getValue(), metadatas.get(0).getValue());
        });
    }

    @Test
    void duplicateAlfrescoImageAsset(){
        assetSummary.setMediaType(AssetMediaType.IMAGE)
                    .setProvider(AssetProvider.ALFRESCO)
                    .setUrn(new AssetUrn(assetId, AssetProvider.ALFRESCO).toString());

        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.just(imageSource1));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(metadata1));

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<ImageSource> imageSourceCaptor = ArgumentCaptor.forClass(ImageSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway).persist(summaryCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(imageSourceCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(metadataCaptor.capture());

        AssetSummary summary = summaryCaptor.getValue();
        List<ImageSource> imageSources = imageSourceCaptor.getAllValues();
        List<AssetMetadata> metadatas = metadataCaptor.getAllValues();

        assertAll(() -> {
            assertNotNull(summary);
            assertNotEquals(assetSummary.getId(), summary.getId());

            assertEquals(assetSummary.getHash(), summary.getHash());
            assertEquals(assetSummary.getMediaType(), summary.getMediaType());
            assertEquals(assetSummary.getVisibility(), summary.getVisibility());

            assertNotEquals(assetSummary.getProvider(), summary.getProvider());
            assertEquals(summary.getProvider(), AssetProvider.AERO);
            assertEquals(summary.getOwnerId(), creator.getId());
            assertEquals(summary.getSubscriptionId(), creator.getSubscriptionId());
            assertEquals(summary.getId(), newAssetSummary.getId());
            assertEquals(summary.getUrn(), newAssetSummary.getUrn());

            assertNotNull(imageSources);
            assertEquals(1, imageSources.size());
            assertNotEquals(imageSource1.getAssetId(), imageSources.get(0).getAssetId());

            assertEquals(imageSource1.getName(), imageSources.get(0).getName());
            assertEquals(imageSource1.getUrl(), imageSources.get(0).getUrl());
            assertEquals(imageSource1.getHeight(), imageSources.get(0).getHeight());
            assertEquals(imageSource1.getWidth(), imageSources.get(0).getWidth());
            assertEquals(imageSources.get(0).getAssetId(), newAssetSummary.getId());

            assertNotNull(metadatas);
            assertEquals(1, metadatas.size());

            assertNotEquals(metadata1.getAssetId(), metadatas.get(0).getAssetId());

            assertEquals(metadata1.getKey(), metadatas.get(0).getKey());
            assertEquals(metadata1.getValue(), metadatas.get(0).getValue());
            assertEquals(metadatas.get(0).getAssetId(), newAssetSummary.getId());
        });
    }

    @Test
    void duplicateAlfrescoImageAsset_noMetadata(){
        AssetProvider provider = AssetProvider.ALFRESCO;

        assetSummary.setProvider(provider)
                .setMediaType(AssetMediaType.IMAGE)
                .setUrn(new AssetUrn(assetId, provider).toString());

        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.just(imageSource1));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<ImageSource> imageSourceCaptor = ArgumentCaptor.forClass(ImageSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway).persist(summaryCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(imageSourceCaptor.capture());
        verify(assetGateway, never()).persist(metadataCaptor.capture());
    }

    @Test
    void duplicateDocumentAsset_noMetadata(){
        assetSummary.setMediaType(AssetMediaType.DOCUMENT);

        DocumentSource documentSource = new DocumentSource()
                                            .setAssetId(assetId)
                                            .setUrl("url for document");

        when(assetGateway.fetchDocumentSource(assetId)).thenReturn(Mono.just(documentSource));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<DocumentSource> documentCaptor = ArgumentCaptor.forClass(DocumentSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway).persist(summaryCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(documentCaptor.capture());
        verify(assetGateway, never()).persist(metadataCaptor.capture());

        DocumentSource newDocumentSource = documentCaptor.getValue();

        assertAll(() -> {
            assertNotNull(newDocumentSource);
            assertEquals(newAssetSummary.getId(), newDocumentSource.getAssetId());
            assertEquals(documentSource.getUrl(), newDocumentSource.getUrl());
        });
    }

    @Test
    void duplicateVideoAsset_noMetadata(){
        assetSummary.setMediaType(AssetMediaType.VIDEO);

        VideoSource videoSource = new VideoSource()
                                    .setAssetId(assetId)
                                    .setName(VideoSourceName.ORIGINAL)
                                    .setResolution("720p")
                                    .setUrl("url for video");

        when(assetGateway.fetchVideoSources(assetId)).thenReturn(Flux.just(videoSource));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<VideoSource> videoCaptor = ArgumentCaptor.forClass(VideoSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway).persist(summaryCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(videoCaptor.capture());
        verify(assetGateway, never()).persist(metadataCaptor.capture());

        VideoSource newVideoSource = videoCaptor.getValue();

        assertAll(() -> {
            assertNotNull(newVideoSource);
            assertEquals(newAssetSummary.getId(), newVideoSource.getAssetId());
            assertEquals(videoSource.getName(), newVideoSource.getName());
            assertEquals(videoSource.getResolution(), newVideoSource.getResolution());
            assertEquals(videoSource.getUrl(), newVideoSource.getUrl());
        });
    }

    @Test
    void duplicateAudioAsset_noMetadata(){
        assetSummary.setMediaType(AssetMediaType.AUDIO);

        AudioSource audioSource = new AudioSource()
                                    .setAssetId(assetId)
                                    .setName(AudioSourceName.ORIGINAL)
                                    .setUrl("url");

        when(assetGateway.fetchAudioSources(assetId)).thenReturn(Flux.just(audioSource));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<AudioSource> audioCaptor = ArgumentCaptor.forClass(AudioSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway).persist(summaryCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(audioCaptor.capture());
        verify(assetGateway, never()).persist(metadataCaptor.capture());

        AudioSource newAudioSource = audioCaptor.getValue();

        assertAll(() -> {
            assertNotNull(newAudioSource);
            assertEquals(newAssetSummary.getId(), newAudioSource.getAssetId());
            assertEquals(audioSource.getName(), newAudioSource.getName());
            assertEquals(audioSource.getUrl(), newAudioSource.getUrl());
        });
    }

    @Test
    void duplicateIconAsset_noMetadata(){
        assetSummary.setMediaType(AssetMediaType.ICON);

        IconSource iconSource1 = new IconSource()
                                .setAssetId(assetId)
                                .setName(IconSourceName.SMALL)
                                .setUrl("url for small")
                                .setHeight(72.0)
                                .setWidth(72.0);

        IconSource iconSource2 = new IconSource()
                                .setAssetId(assetId)
                                .setName(IconSourceName.ORIGINAL)
                                .setUrl("url for Original")
                                .setHeight(100.0)
                                .setWidth(100.0);

        when(assetGateway.fetchIconSources(assetId)).thenReturn(Flux.just(iconSource1, iconSource2));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<IconSource> iconCaptor = ArgumentCaptor.forClass(IconSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway, atMostOnce()).persist(summaryCaptor.capture());
        verify(assetGateway, times(2)).persist(iconCaptor.capture());
        verify(assetGateway, never()).persist(metadataCaptor.capture());

        List<IconSource> newIconSource = iconCaptor.getAllValues();

        assertAll(() -> {
            assertNotNull(newIconSource);
            assertEquals(2, newIconSource.size());
            assertEquals(newIconSource.get(0).getAssetId(), newIconSource.get(1).getAssetId());
            assertEquals(newAssetSummary.getId(), newIconSource.get(0).getAssetId());
            assertEquals(iconSource1.getName(), newIconSource.get(0).getName());
            assertEquals(iconSource1.getUrl(), newIconSource.get(0).getUrl());
            assertEquals(iconSource1.getWidth(), newIconSource.get(0).getWidth());
            assertEquals(iconSource1.getHeight(), newIconSource.get(0).getHeight());
            assertEquals(iconSource2.getName(), newIconSource.get(1).getName());
            assertEquals(iconSource2.getUrl(), newIconSource.get(1).getUrl());
            assertEquals(iconSource2.getWidth(), newIconSource.get(1).getWidth());
            assertEquals(iconSource2.getHeight(), newIconSource.get(1).getHeight());
        });
    }

    @Test
    void duplicateExternalAsset(){
        assetSummary.setMediaType(AssetMediaType.IMAGE)
                .setProvider(AssetProvider.EXTERNAL)
                .setUrn(new AssetUrn(assetId, AssetProvider.EXTERNAL).toString());

        ExternalSource externalSource = new ExternalSource()
                .setAssetId(assetId)
                .setUrl("url for external source");

        when(assetGateway.fetchExternalSource(assetId)).thenReturn(Mono.just(externalSource));
        when(assetGateway.persist(any(ExternalSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(metadata1, metadata2));

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<ExternalSource> externalSourceCaptor = ArgumentCaptor.forClass(ExternalSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway, atMostOnce()).persist(summaryCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(externalSourceCaptor.capture());
        verify(assetGateway, times(2)).persist(metadataCaptor.capture());

        ExternalSource newExternalSource = externalSourceCaptor.getValue();
        List<AssetMetadata> metadatas = metadataCaptor.getAllValues();

        assertAll(() -> {
            assertNotNull(newExternalSource);
            assertEquals(newExternalSource.getAssetId(), newAssetSummary.getId());

            assertNotEquals(externalSource, newExternalSource);
            assertEquals(externalSource.getUrl(), newExternalSource.getUrl());

            assertNotNull(metadatas);
            assertEquals(2, metadatas.size());

            assertNotEquals(metadata1.getAssetId(), metadatas.get(0).getAssetId());

            assertEquals(metadatas.get(0).getAssetId(), metadatas.get(1).getAssetId());
            assertEquals(metadatas.get(0).getAssetId(), newAssetSummary.getId());
            assertEquals(metadata1.getKey(), metadatas.get(0).getKey());
            assertEquals(metadata1.getValue(), metadatas.get(0).getValue());
        });
    }

    @Test
    void duplicateExternalAsset_noMetadata(){
        assetSummary.setMediaType(AssetMediaType.IMAGE)
                .setProvider(AssetProvider.EXTERNAL)
                .setUrn(new AssetUrn(assetId, AssetProvider.EXTERNAL).toString());

        ExternalSource externalSource = new ExternalSource()
                                        .setAssetId(assetId)
                                        .setUrl("url for external source");

        when(assetGateway.fetchExternalSource(assetId)).thenReturn(Mono.just(externalSource));
        when(assetGateway.persist(any(ExternalSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<ExternalSource> externalSourceCaptor = ArgumentCaptor.forClass(ExternalSource.class);
        ArgumentCaptor<AssetMetadata> metadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);

        AssetSummary newAssetSummary = bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block();

        assertNotNull(newAssetSummary);

        verify(assetGateway, atMostOnce()).persist(summaryCaptor.capture());
        verify(assetGateway, atMostOnce()).persist(externalSourceCaptor.capture());
        verify(assetGateway, never()).persist(metadataCaptor.capture());

        ExternalSource newExternalSource = externalSourceCaptor.getValue();

        assertAll(() -> {
            assertNotNull(newExternalSource);
            assertEquals(newExternalSource.getAssetId(), newAssetSummary.getId());
            assertNotEquals(externalSource, newExternalSource);
            assertEquals(externalSource.getUrl(), newExternalSource.getUrl());
        });
    }

    @Test
    void duplicateAeroImageAsset_noImageSource(){
        assetSummary.setMediaType(AssetMediaType.IMAGE);

        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(metadata1, metadata2));

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block());

        assertNotNull(f);
        assertEquals("cannot find image source by asset id: " + assetId,
                f.getMessage());
    }

    @Test
    void duplicateAlfrescoImageAsset_noImageSource(){
        assetSummary.setMediaType(AssetMediaType.IMAGE)
                .setProvider(AssetProvider.ALFRESCO)
                .setUrn(new AssetUrn(assetId, AssetProvider.ALFRESCO).toString());

        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.just(metadata1, metadata2));

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block());

        assertNotNull(f);
        assertEquals("cannot find image source by asset id: " + assetId,
                f.getMessage());
    }

    @Test
    void duplicateDocumentAsset_noDocumentSource(){
        assetSummary.setMediaType(AssetMediaType.DOCUMENT);

        when(assetGateway.fetchDocumentSource(assetId)).thenReturn(Mono.empty());
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block());

        assertNotNull(f);
        assertEquals("cannot find document source by asset id: " + assetId,
               f.getMessage());
    }

    @Test
    void duplicateVideoAsset_noVideoSource(){
        assetSummary.setMediaType(AssetMediaType.VIDEO);

        when(assetGateway.fetchVideoSources(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block());

        assertNotNull(f);
        assertEquals("cannot find video source by asset id: " + assetId,
                f.getMessage());
    }

    @Test
    void duplicateAudioAsset_noAudioSource(){
        assetSummary.setMediaType(AssetMediaType.AUDIO);

        when(assetGateway.fetchAudioSources(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block());

        assertNotNull(f);
        assertEquals("cannot find audio source by asset id: " + assetId,
                f.getMessage());
    }

    @Test
    void duplicateIconAsset_noIconSource(){
        assetSummary.setMediaType(AssetMediaType.ICON);

        when(assetGateway.fetchIconSources(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block());

        assertNotNull(f);
        assertEquals("cannot find icon source by asset id: " + assetId,
                f.getMessage());
    }

    @Test
    void duplicateExternalAsset_noExternalSource(){
        assetSummary.setProvider(AssetProvider.EXTERNAL)
                    .setUrn(new AssetUrn(assetId, AssetProvider.EXTERNAL).toString());

        when(assetGateway.fetchExternalSource(assetId)).thenReturn(Mono.empty());
        when(assetGateway.persist(any(ExternalSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.fetchMetadata(assetId)).thenReturn(Flux.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> bronteAssetService
                .duplicate(assetId, creator.getId(), creator.getSubscriptionId()).block());

        assertNotNull(f);
        assertEquals("cannot find external source by asset id: " + assetId,
                f.getMessage());
    }

    @Test
    void test_deleteIconAssetsByLibrary() {
        String iconLibrary = "Microsoft Icon";
        when(assetGateway.deleteIconAssets(any(IconsByLibrary.class))).thenReturn(Flux.just(new Void[]{}));

        bronteAssetService.deleteIconAssetsByLibrary(iconLibrary);
        verify(assetGateway).deleteIconAssets(any(IconsByLibrary.class));
    }

    @Test
    void test_saveAssetSource_svg_with_0_dimensions() throws IOException, ImageReadException {
        BronteAssetService bronteAssetServiceSpy = Mockito.spy(bronteAssetService);
        String mimeType = "svg";
        String assetUrl = "url";
        ImageDimensions imageDimensions = new ImageDimensions();
        imageDimensions.setHeight((double) 0);
        imageDimensions.setWidth((double) 0);
        File file1 = mock(File.class);
        try (MockedStatic<Images> mockImages = Mockito.mockStatic(Images.class)) {
            mockImages.when(() -> {
                Images.readSVG(any(File.class));
            }).thenThrow(IOException.class);

            mockImages.when(() -> {
                Images.getImageDimensions(file1, mimeType);
            }).thenReturn(imageDimensions);

            when(assetGateway.persist(imageSource3)).thenReturn(Flux.just(new Void[]{}));
            bronteAssetServiceSpy.saveAssetSource(assetSummary2, file1, assetUrl, mimeType).block();
        }
        verify(bronteAssetServiceSpy, atLeastOnce()).saveAssetSource(assetSummary2, file1, assetUrl, mimeType);
    }
}
