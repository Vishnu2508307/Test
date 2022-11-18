package com.smartsparrow.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.smartsparrow.ext_http.service.NotificationState;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncNotification;
import com.smartsparrow.workspace.data.AlfrescoAssetTrackGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AlfrescoNode;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AlfrescoAssetPullServiceTest {

    @InjectMocks
    private AlfrescoAssetPullService alfrescoAssetPullService;

    @Mock
    private AssetConfig assetConfig;

    @Mock
    private ExternalHttpRequestService externalHttpRequestService;

    @Mock
    private CoursewareAssetService coursewareAssetService;

    @Mock
    private AlfrescoAssetService alfrescoAssetService;

    @Mock
    private AlfrescoAssetTrackService alfrescoAssetTrackService;

    @Mock
    private AlfrescoAssetTrackGateway alfrescoAssetTrackGateway;

    private static final AlfrescoNode alfrescoNode = new AlfrescoNode()
            .setAlfrescoId(UUID.randomUUID())
            .setName("name")
            .setVersion("version")
            .setLastModifiedDate(1L);
    private static final AssetSummary assetSummary = new AssetSummary()
            .setId(UUID.randomUUID())
            .setProvider(AssetProvider.ALFRESCO)
            .setMediaType(AssetMediaType.IMAGE)
            .setOwnerId(UUID.randomUUID());
    private static final UUID referenceId = UUID.randomUUID();
    private static final String token = "myCloudToken";

    private UUID notificationId = UUID.randomUUID();
    private UUID notificationRefId = UUID.randomUUID();
    private NotificationState notificationState = new NotificationState().setNotificationId(notificationId).setReferenceId(notificationRefId);
    private RequestNotification requestNotification = new RequestNotification().setState(notificationState);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(alfrescoAssetService.getAlfrescoImageNode(any(AssetSummary.class))).thenReturn(Mono.just(alfrescoNode));
        when(assetConfig.getAlfrescoUrl()).thenReturn("alfrescoUrl");

        when(externalHttpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PULL), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(requestNotification));

        when(alfrescoAssetTrackService.addNotificationId(eq(referenceId), any(UUID.class), eq(AlfrescoAssetSyncType.PULL)))
                .thenReturn(Mono.just(true));
        when(alfrescoAssetTrackGateway.persist(any(AlfrescoAssetSyncNotification.class)))
                .thenReturn(Flux.empty());
    }

    @Test
    void pullAsset_noReference() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetPullService.pullAsset(null, null, null, false)
                        .block());

        assertEquals("referenceId is required", f.getMessage());
    }

    @Test
    void pullAsset_noSummary() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetPullService.pullAsset(referenceId, null, null, false)
                        .block());

        assertEquals("assetSummary is required", f.getMessage());
    }

    @Test
    void pullAsset_noToken() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetPullService.pullAsset(referenceId, assetSummary, null, false)
                        .block());

        assertEquals("myCloudToken is required", f.getMessage());
    }

    @Test
    void pullAsset_invalidProvider() {
        final AssetSummary summary = new AssetSummary()
                .setProvider(AssetProvider.AERO);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetPullService.pullAsset(referenceId, summary, token, false)
                        .block());

        assertEquals("only ALFRESCO provider supported", f.getMessage());
    }

    @Test
    void pullAsset() {
        final String expected = "{" +
                "\"uri\":\"" + assetConfig.getAlfrescoUrl() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + alfrescoNode.getAlfrescoId() + "?include=path\"," +
                "\"method\":\"GET\"," +
                "\"headers\":{" +
                "\"X-BronteAsset\":\"{" +
                    "\\\"referenceId\\\":\\\"" + referenceId + "\\\"," +
                    "\\\"ownerId\\\":\\\"" + assetSummary.getOwnerId() + "\\\"," +
                    "\\\"assetId\\\":\\\"" + assetSummary.getId() + "\\\"," +
                    "\\\"version\\\":\\\"" + alfrescoNode.getVersion() + "\\\"," +
                    "\\\"alfrescoNodeId\\\":\\\"" + alfrescoNode.getAlfrescoId() + "\\\"," +
                    "\\\"lastModified\\\":" + alfrescoNode.getLastModifiedDate() + "," +
                    "\\\"forceSync\\\":" + false + "}\"," +
                "\"X-MyCloudProxySession\":\"" + token + "\"}" +
                "}";
        ArgumentCaptor<Request> requestArgumentCaptor = ArgumentCaptor.forClass(Request.class);
        final RequestNotification notification = alfrescoAssetPullService.pullAsset(referenceId, assetSummary, token, false)
                .block();

        assertNotNull(notification);

        verify(externalHttpRequestService).submit(eq(RequestPurpose.ALFRESCO_ASSET_PULL), requestArgumentCaptor.capture(),
                eq(referenceId));

        final Request request = requestArgumentCaptor.getValue();

        assertNotNull(request);
        assertEquals(expected, request.getParamsAsJson());
    }

    @Test
    void pullAssets_noReference() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetPullService.pullAssets(null, null, null, false)
                        .collectList()
                        .block());

        assertEquals("referenceId is required", f.getMessage());
    }

    @Test
    void pullAssets_noRootElementId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetPullService.pullAssets(referenceId, null, null, false)
                        .collectList()
                        .block());

        assertEquals("rootElementId is required", f.getMessage());
    }

    @Test
    void pullAssets_noToken() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetPullService.pullAssets(referenceId, UUID.randomUUID(), null, false)
                        .collectList()
                        .block());

        assertEquals("myCloudToken is required", f.getMessage());
    }

    @Test
    void pullAssets() {
        ArgumentCaptor<Request> requestArgumentCaptor = ArgumentCaptor.forClass(Request.class);
        final UUID rootElementId = UUID.randomUUID();

        final UUID assetIdTwo = UUID.randomUUID();
        final AssetSummary assetSummaryTwo = new AssetSummary()
                .setProvider(AssetProvider.ALFRESCO)
                .setId(assetIdTwo)
                .setMediaType(AssetMediaType.DOCUMENT);

        when(coursewareAssetService.getAllSummariesForRootActivity(rootElementId))
                .thenReturn(Flux.just(assetSummary, assetSummaryTwo));

        List<RequestNotification> notifications = alfrescoAssetPullService.pullAssets(referenceId, rootElementId, token, false)
                .collectList()
                .block();

        assertNotNull(notifications);
        assertEquals(1, notifications.size());

        verify(externalHttpRequestService, times(1)).submit(eq(RequestPurpose.ALFRESCO_ASSET_PULL), requestArgumentCaptor.capture(),
                eq(referenceId));

        final Request request = requestArgumentCaptor.getValue();

        assertNotNull(request);
    }
}
