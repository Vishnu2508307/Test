package com.smartsparrow.asset.service;

import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_CONTENT;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_INFO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smartsparrow.asset.data.AlfrescoAsset;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.eventmessage.AlfrescoNodeContentEventMessage;
import com.smartsparrow.asset.eventmessage.AlfrescoNodeInfoEventMessage;
import com.smartsparrow.iam.service.Account;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AlfrescoAssetData;
import com.smartsparrow.asset.data.AlfrescoImageNode;
import com.smartsparrow.asset.data.AlfrescoNode;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.util.ClockProvider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AlfrescoAssetServiceTest {

    @InjectMocks
    private AlfrescoAssetService alfrescoAssetService;

    @Mock
    private AssetGateway assetGateway;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private AssetUploadService assetUploadService;

    @Mock
    private CamelReactiveStreamsService camelReactiveStreamsService;

    @Mock
    private AssetBuilder assetBuilder;

    @Mock
    private ClockProvider clockProvider;

    private static final Clock clock = Clock.systemUTC();
    private static final UUID assetId = UUID.randomUUID();
    private static final UUID ownerId = UUID.randomUUID();
    private static final String alfrescoPath = "bronte/documentLibrary/folder";
    private static final String workURN = "urn:pearson:work:" + UUID.randomUUID();

    private static final AlfrescoImageNode node = new AlfrescoImageNode()
            .setAlfrescoId(UUID.randomUUID())
            .setAltText("altText")
            .setHeight(1.0)
            .setWidth(2.0)
            .setLastModifiedDate(1L)
            .setLongDescription("longDesc")
            .setMimeType("mimeType")
            .setName("name")
            .setSource("source")
            .setVersion("version")
            .setPath(alfrescoPath)
            .setWorkURN(workURN);

    private static final AssetSummary assetSummary = new AssetSummary()
            .setId(assetId)
            .setProvider(AssetProvider.ALFRESCO)
            .setMediaType(AssetMediaType.IMAGE);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(clockProvider.get()).thenReturn(clock);
    }

    @Test
    void saveAlfrescoImageData_noAssetId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetService.saveAlfrescoImageData(null, null, null)
                        .block());

        assertEquals("assetId is required", f.getMessage());
    }

    @Test
    void saveAlfrescoImageData_noOwnerId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetService.saveAlfrescoImageData(assetId, null, null)
                        .block());

        assertEquals("ownerId is required", f.getMessage());
    }

    @Test
    void saveAlfrescoImageData_noImageNode() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetService.saveAlfrescoImageData(assetId, ownerId, null)
                        .block());

        assertEquals("alfrescoImageNode is required", f.getMessage());
    }

    @Test
    void saveAlfrescoImageData_assetNotFound() {
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.empty());
        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class,
                () -> alfrescoAssetService.saveAlfrescoImageData(assetId, ownerId, node)
                        .block());

        assertEquals(String.format("cannot find asset summary by asset id: %s", assetId), f.getMessage());
    }

    @Test
    void saveAlfrescoImageData_originalSourceNotFound() {
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(new AssetSummary()));
        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class,
                () -> alfrescoAssetService.saveAlfrescoImageData(assetId, ownerId, node)
                        .block());

        assertEquals(String.format("cannot find image source by asset id: %s", assetId), f.getMessage());
    }

    @Test
    void saveAlfrescoImageData() {
        when(assetGateway.fetchAssetById(assetId)).thenReturn(Mono.just(new AssetSummary()
                .setProvider(AssetProvider.AERO)
                .setOwnerId(UUID.randomUUID())));
        when(assetGateway.fetchImageSources(assetId)).thenReturn(Flux.just(new ImageSource()
                .setWidth(10.0)
                .setHeight(20.0)
                .setUrl("aUrl")
                .setName(ImageSourceName.ORIGINAL)));

        // mock persistence
        when(assetGateway.persist(any(AssetSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.just(new Void[]{}));
        when(assetGateway.persist(any(AlfrescoAssetData.class))).thenReturn(Flux.just(new Void[]{}));
        when(bronteAssetService.saveMetadata(eq(assetId), any(Map.class))).thenReturn(Flux.just()
                .singleOrEmpty());

        ArgumentCaptor<AssetSummary> summaryCaptor = ArgumentCaptor.forClass(AssetSummary.class);
        ArgumentCaptor<ImageSource> imageSourceCaptor = ArgumentCaptor.forClass(ImageSource.class);
        ArgumentCaptor<AlfrescoAssetData> assetDataCaptor = ArgumentCaptor.forClass(AlfrescoAssetData.class);
        final Map<String, String> expectedMetadata = new HashMap<String, String>() {
            {
                put("altText", node.getAltText());
            }

            {
                put("longDesc", node.getLongDescription());
            }

            {
                put("alfrescoPath", node.getPath());
            }

            {
                put("workURN", workURN);
            }
        };

        alfrescoAssetService.saveAlfrescoImageData(assetId, ownerId, node)
                .block();

        verify(assetGateway).persist(summaryCaptor.capture());
        verify(assetGateway).persist(imageSourceCaptor.capture());
        verify(assetGateway).persist(assetDataCaptor.capture());
        verify(bronteAssetService).saveMetadata(eq(assetId), eq(expectedMetadata));

        final AssetSummary persistedSummary = summaryCaptor.getValue();

        assertNotNull(persistedSummary);
        assertEquals(ownerId, persistedSummary.getOwnerId());
        assertEquals(AssetProvider.ALFRESCO, persistedSummary.getProvider());

        final ImageSource persistedImageSource = imageSourceCaptor.getValue();

        assertNotNull(persistedImageSource);
        assertEquals(node.getSource(), persistedImageSource.getUrl());
        assertEquals(node.getWidth(), persistedImageSource.getWidth());
        assertEquals(node.getHeight(), persistedImageSource.getHeight());

        final AlfrescoAssetData persistedData = assetDataCaptor.getValue();

        assertNotNull(persistedData);
        assertEquals(assetId, persistedData.getAssetId());
        assertEquals(node.getAlfrescoId(), persistedData.getAlfrescoId());
        assertEquals(node.getLastModifiedDate(), persistedData.getLastModifiedDate());
        assertNotNull(persistedData.getLastSyncDate());
        assertEquals(node.getName(), persistedData.getName());
        assertEquals(node.getVersion(), persistedData.getVersion());
    }

    @Test
    void getAlfrescoAssetData_noAssetId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetService.getAlfrescoAssetData(null));

        assertEquals("assetId is required", f.getMessage());

        verify(assetGateway, never()).fetchAlfrescoAssetById(any(UUID.class));
    }

    @Test
    void getAlfrescoAssetData() {
        when(assetGateway.fetchAlfrescoAssetById(assetId)).thenReturn(Mono.just(new AlfrescoAssetData()
                .setAssetId(assetId)));

        final AlfrescoAssetData result = alfrescoAssetService.getAlfrescoAssetData(assetId)
                .block();

        assertNotNull(result);
        assertEquals(assetId, result.getAssetId());

        verify(assetGateway).fetchAlfrescoAssetById(assetId);
    }

    @Test
    void getAlfrescoImageNode_noSummary() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetService.getAlfrescoImageNode(null)
                .block());

        assertEquals("assetSummary is required", f.getMessage());

        verify(bronteAssetService, never()).getImageSource(any(UUID.class));
        verify(bronteAssetService, never()).getAssetMetadata(any(UUID.class));
    }

    @Test
    void getAlfrescoImageNode_invalidProvider() {
        assetSummary.setProvider(AssetProvider.AERO);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetService.getAlfrescoImageNode(assetSummary)
                .block());

        assertEquals("assetSummary with ALFRESCO provider required", f.getMessage());

        verify(bronteAssetService, never()).getImageSource(any(UUID.class));
        verify(bronteAssetService, never()).getAssetMetadata(any(UUID.class));
    }

    @Test
    void getAlfrescoImageNode_sourceNotFound() {
        when(bronteAssetService.getImageSource(assetId)).thenReturn(Flux.empty());
        when(bronteAssetService.getAssetMetadata(assetId)).thenReturn(Flux.empty());
        when(assetGateway.fetchAlfrescoAssetById(assetId)).thenReturn(Mono.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class,
                () -> alfrescoAssetService.getAlfrescoImageNode(assetSummary)
                        .block());

        assertEquals(String.format("cannot find image source by asset id: %s", assetId), f.getMessage());
    }

    @Test
    void getAlfrescoImageNode() {
        when(bronteAssetService.getImageSource(assetId)).thenReturn(Flux.just(new ImageSource()
                .setWidth(10.0)
                .setHeight(20.0)
                .setUrl("aUrl")
                .setName(ImageSourceName.ORIGINAL)));
        when(bronteAssetService.getAssetMetadata(assetId)).thenReturn(Flux.just(new AssetMetadata()));
        when(assetGateway.fetchAlfrescoAssetById(assetId)).thenReturn(Mono.just(new AlfrescoAssetData()));

        final AlfrescoNode found = alfrescoAssetService.getAlfrescoImageNode(assetSummary)
                .block();

        assertNotNull(found);

        verify(bronteAssetService).getImageSource(assetId);
        verify(bronteAssetService).getAssetMetadata(assetId);
        verify(assetGateway).fetchAlfrescoAssetById(assetId);
    }

    @Test
    void save_success() {
        String alfrescoNodeId = UUID.randomUUID().toString();
        Account account = new Account().setId(UUID.randomUUID()).setSubscriptionId(UUID.randomUUID());
        String myCloudToken = "token";
        String filename = "filename.jpg";
        long modifiedAt = 1614191720;
        String version = "1.0";
        String altText = "some alt text";
        String longDesc = "some long desc";
        String pathName = "/Company Home/Sites/" + alfrescoPath;

        AlfrescoNodeInfo alfrescoNodeInfo = new AlfrescoNodeInfo()
                .setId(alfrescoNodeId)
                .setName(filename)
                .setModifiedAt(modifiedAt)
                .setVersion(version)
                .setAltText(altText)
                .setLongDesc(longDesc)
                .setPathName(pathName);

        InputStream is = new ByteArrayInputStream("some data".getBytes(StandardCharsets.UTF_8));

        AlfrescoNodeInfoEventMessage alfrescoNodeInfoEventMessage = new AlfrescoNodeInfoEventMessage(alfrescoNodeId, myCloudToken)
                .setAlfrescoNodeInfo(alfrescoNodeInfo);

        AlfrescoNodeContentEventMessage alfrescoNodeContentEventMessage = new AlfrescoNodeContentEventMessage(alfrescoNodeId, myCloudToken)
                .setContentStream(is);

        AssetSummary assetSummary = new AssetSummary()
                .setId(assetId)
                .setProvider(AssetProvider.ALFRESCO)
                .setOwnerId(account.getId())
                .setSubscriptionId(account.getSubscriptionId())
                .setMediaType(AssetMediaType.IMAGE)
                .setVisibility(AssetVisibility.GLOBAL);

        AlfrescoAssetData alfrescoAssetData = new AlfrescoAssetData()
                .setAlfrescoId(UUID.fromString(alfrescoNodeId))
                .setName(filename)
                .setLastModifiedDate(modifiedAt)
                .setLastSyncDate(modifiedAt)
                .setVersion(version);

        AssetMetadata altTextMetadata = new AssetMetadata().setAssetId(assetId).setKey("altText").setValue(altText);
        AssetMetadata longDescMetadata = new AssetMetadata().setAssetId(assetId).setKey("longDesc").setValue(longDesc);
        AssetMetadata alfrescoPathMetadata = new AssetMetadata().setAssetId(assetId).setKey("alfrescoPath").setValue(alfrescoPath);
        AssetMetadata workURNMetadata = new AssetMetadata().setAssetId(assetId).setKey("workURN").setValue(workURN);

        Map<String, Object> originalSource = new HashMap<>();
        originalSource.put("url", "public/url");
        originalSource.put("width", 256.0);
        originalSource.put("height", 256.0);
        Map<String, Object> imageSources = new HashMap<>();
        imageSources.put("original", originalSource);

        AlfrescoAsset alfrescoAsset = new AlfrescoAsset()
                .setId(assetSummary.getId())
                .setOwnerId(assetSummary.getOwnerId())
                .setSubscriptionId(assetSummary.getSubscriptionId())
                .setAssetMediaType(assetSummary.getMediaType())
                .setAssetVisibility(assetSummary.getVisibility())
                .setAlfrescoId(alfrescoAssetData.getAlfrescoId())
                .setName(alfrescoAssetData.getName())
                .setLastModifiedDate(alfrescoAssetData.getLastModifiedDate())
                .setLastSyncDate(alfrescoAssetData.getLastSyncDate())
                .setVersion(alfrescoAssetData.getVersion());

        when(assetUploadService.create(any(UUID.class), any(AssetTemplate.class))).thenReturn(Mono.just(new AssetSummary()));

        when(assetGateway.persist(any(AlfrescoAssetData.class))).thenReturn(Flux.empty());

        when(camelReactiveStreamsService.toStream(eq(ALFRESCO_NODE_INFO), any(AlfrescoNodeInfoEventMessage.class), eq(AlfrescoNodeInfoEventMessage.class)))
                .thenReturn(Mono.just(alfrescoNodeInfoEventMessage));
        when(camelReactiveStreamsService.toStream(eq(ALFRESCO_NODE_CONTENT), any(AlfrescoNodeContentEventMessage.class), eq(AlfrescoNodeContentEventMessage.class)))
                .thenReturn(Mono.just(alfrescoNodeContentEventMessage));

        when(assetGateway.fetchAssetById(any(UUID.class))).thenReturn(Mono.just(assetSummary));
        when(assetGateway.fetchAlfrescoAssetById(any(UUID.class))).thenReturn(Mono.just(alfrescoAssetData));
        when(assetGateway.fetchMetadata(any(UUID.class))).thenReturn(Flux.just(altTextMetadata, longDescMetadata, alfrescoPathMetadata, workURNMetadata));
        when(bronteAssetService.getImageSourcePayload(any(UUID.class))).thenReturn(Mono.just(imageSources));

        when(assetBuilder.setAssetSummary(any(AssetSummary.class))).thenReturn(assetBuilder);
        when(assetBuilder.setAlfrescoAssetData(any(AlfrescoAssetData.class))).thenReturn(assetBuilder);
        when(assetBuilder.build(eq(AssetProvider.ALFRESCO))).thenReturn(alfrescoAsset);

        AssetPayload payload = alfrescoAssetService.save(alfrescoNodeId, account, myCloudToken).block();

        assertNotNull(payload);
        Map<String, String>  payloadMetadata = payload.getMetadata();

        assertEquals(altText, payloadMetadata.get("altText"));
        assertEquals(longDesc, payloadMetadata.get("longDesc"));
        assertEquals(alfrescoPath, payloadMetadata.get("alfrescoPath"));
        assertEquals(workURN, payloadMetadata.get("workURN"));
    }

    @Test
    void getAlfrescoPath() {

        String pathName = "/Company Home/Sites/" + alfrescoPath;

        String path = alfrescoAssetService.getAlfrescoPath(pathName);

        assertEquals(alfrescoPath, path);
    }

    @Test
    void getAlfrescoPath_emptyString() {

        String pathName = "";

        String path = alfrescoAssetService.getAlfrescoPath(pathName);

        assertEquals("", path);
    }

    @Test
    void getAlfrescoPath_nullString() {

        String pathName = null;

        String path = alfrescoAssetService.getAlfrescoPath(pathName);

        assertEquals("", path);
    }
}