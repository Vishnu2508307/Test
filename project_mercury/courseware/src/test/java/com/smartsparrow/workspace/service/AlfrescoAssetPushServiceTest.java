package com.smartsparrow.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.smartsparrow.asset.data.AlfrescoAssetData;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncNotification;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.data.AlfrescoAssetTrackGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.NotificationState;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AlfrescoAssetPushServiceTest {

    @InjectMocks
    private AlfrescoAssetPushService alfrescoAssetPushService;

    @Mock
    private AssetConfig assetConfig;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private AlfrescoAssetService alfrescoAssetService;

    @Mock
    private CoursewareAssetService coursewareAssetService;

    @Mock
    private ExternalHttpRequestService httpRequestService;

    @Mock
    private AlfrescoAssetTrackService alfrescoAssetTrackService;

    @Mock
    private AlfrescoAssetTrackGateway alfrescoAssetTrackGateway;

    @Mock
    private Asset asset;

    private String alfrescoUrl = "https://usppewip.cms.pearson.com";

    private UUID referenceId = UUID.randomUUID();
    private UUID courseId = UUID.randomUUID();
    private UUID assetId = UUID.randomUUID();
    private String myCloudToken = "token";
    private UUID alfrescoNodeId = UUID.randomUUID();
    private String alfrescoNodeUrl = alfrescoUrl + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + alfrescoNodeId + "/children";
    private String bronteAssetUrl = "https://some/image.jpg";
    private String fileName = assetId + ".jpg";
    private String altText = "alt_text";
    private String longDesc = "long_description";
    private String workURN = "work_urn";

    private UUID notificationId = UUID.randomUUID();
    private UUID notificationRefId = UUID.randomUUID();
    private NotificationState notificationState = new NotificationState().setNotificationId(notificationId).setReferenceId(notificationRefId);
    private RequestNotification requestNotification = new RequestNotification().setState(notificationState);

    private AlfrescoAssetSyncNotification syncNotification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(assetConfig.getAlfrescoUrl()).thenReturn(alfrescoUrl);
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);

        syncNotification = new AlfrescoAssetSyncNotification()
                .setNotificationId(notificationId)
                .setReferenceId(referenceId)
                .setCourseId(courseId)
                .setAssetId(assetId)
                .setSyncType(AlfrescoAssetSyncType.PUSH)
                .setStatus(AlfrescoAssetSyncStatus.IN_PROGRESS);
    }

    @Test
    void pushCourseAssets_success() {
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", bronteAssetUrl);
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("original", imageData);
        AssetPayload assetPayload = new AssetPayload()
                .setAsset(asset)
                .putAllSources(payloadData);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId),
                        new AssetSummary()
                                .setProvider(AssetProvider.EXTERNAL)
                                .setId(assetId)));

        when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(assetPayload));

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(requestNotification));

        when(alfrescoAssetTrackService.addNotificationId(eq(referenceId), any(UUID.class), eq(AlfrescoAssetSyncType.PUSH)))
                .thenReturn(Mono.just(true));
        when(alfrescoAssetTrackGateway.persist(any(AlfrescoAssetSyncNotification.class)))
                .thenReturn(Flux.empty());

        List<RequestNotification> requestNotifications = alfrescoAssetPushService
                .pushCourseAssets(referenceId, courseId, myCloudToken, alfrescoNodeId)
                .block();

        verify(alfrescoAssetTrackGateway).persist(syncNotification);
        verify(alfrescoAssetTrackService).addNotificationId(referenceId, notificationId, AlfrescoAssetSyncType.PUSH);

        assertNotNull(requestNotifications);
        assertEquals(1, requestNotifications.size());
    }

    @Test
    void pushCourseAssets_noAlfrescoProviders() {
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", bronteAssetUrl);
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("original", imageData);
        AssetPayload assetPayload = new AssetPayload()
                .setAsset(asset)
                .putAllSources(payloadData);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.ALFRESCO),
                        new AssetSummary()
                                .setProvider(AssetProvider.ALFRESCO)
                                .setId(assetId),
                        new AssetSummary()
                                .setProvider(AssetProvider.EXTERNAL)
                                .setId(assetId)));

        when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(assetPayload));

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(new RequestNotification()));

        List<RequestNotification> requestNotifications = alfrescoAssetPushService
                .pushCourseAssets(referenceId, courseId, myCloudToken, alfrescoNodeId)
                .block();

        assertNotNull(requestNotifications);
        assertEquals(0, requestNotifications.size());
    }

    @Test
    void pushCourseAssets_noAudioAssets() {
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.AUDIO);

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", bronteAssetUrl);
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("original", imageData);
        AssetPayload assetPayload = new AssetPayload()
                .setAsset(asset)
                .putAllSources(payloadData);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId)));

        when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(assetPayload));

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(new RequestNotification()));

        List<RequestNotification> requestNotifications = alfrescoAssetPushService
                .pushCourseAssets(referenceId, courseId, myCloudToken, alfrescoNodeId)
                .block();

        assertNotNull(requestNotifications);
        assertEquals(0, requestNotifications.size());
    }

    @Test
    void pushCourseAssets_noDocumentAssets() {
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.DOCUMENT);

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", bronteAssetUrl);
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("original", imageData);
        AssetPayload assetPayload = new AssetPayload()
                .setAsset(asset)
                .putAllSources(payloadData);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId)));

        when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(assetPayload));

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(new RequestNotification()));

        List<RequestNotification> requestNotifications = alfrescoAssetPushService
                .pushCourseAssets(referenceId, courseId, myCloudToken, alfrescoNodeId)
                .block();

        assertNotNull(requestNotifications);
        assertEquals(0, requestNotifications.size());
    }

    @Test
    void pushCourseAssets_noVideoAssets() {
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.VIDEO);

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", bronteAssetUrl);
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("original", imageData);
        AssetPayload assetPayload = new AssetPayload()
                .setAsset(asset)
                .putAllSources(payloadData);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId)));

        when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(assetPayload));

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(new RequestNotification()));

        List<RequestNotification> requestNotifications = alfrescoAssetPushService
                .pushCourseAssets(referenceId, courseId, myCloudToken, alfrescoNodeId)
                .block();

        assertNotNull(requestNotifications);
        assertEquals(0, requestNotifications.size());
    }

    @Test
    void pushCourseAssets_payloadDataNoOriginal() {
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.AUDIO);

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", bronteAssetUrl);
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("no_original", imageData);
        AssetPayload assetPayload = new AssetPayload()
                .setAsset(asset)
                .putAllSources(payloadData);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId)));

        when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(assetPayload));

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(new RequestNotification()));

        List<RequestNotification> requestNotifications = alfrescoAssetPushService
                .pushCourseAssets(referenceId, courseId, myCloudToken, alfrescoNodeId)
                .block();

        assertNotNull(requestNotifications);
        assertEquals(0, requestNotifications.size());
    }

    @Test
    void pushCourseAssets_payloadDataNoUrl() {
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.AUDIO);

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("no_url", bronteAssetUrl);
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("original", imageData);
        AssetPayload assetPayload = new AssetPayload()
                .setAsset(asset)
                .putAllSources(payloadData);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId),
                        new AssetSummary()
                                .setProvider(AssetProvider.AERO)
                                .setId(assetId)));

        when(bronteAssetService.getAssetPayload(any(UUID.class))).thenReturn(Mono.just(assetPayload));

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(new RequestNotification()));

        List<RequestNotification> requestNotifications = alfrescoAssetPushService
                .pushCourseAssets(referenceId, courseId, myCloudToken, alfrescoNodeId)
                .block();

        assertNotNull(requestNotifications);
        assertEquals(0, requestNotifications.size());
    }

    @Test
    void pushAsset_success() {
        String jsonStr = "{"
                +   "\"uri\":\"https://usppewip.cms.pearson.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + alfrescoNodeId + "/children\","
                +   "\"method\":\"POST\","
                +   "\"headers\":{"
                +     "\"X-BronteCourseId\":\"" + courseId + "\","
                +     "\"X-BronteAssetId\":\"" + assetId + "\","
                +     "\"X-MyCloudProxySession\":\"" + myCloudToken + "\""
                +   "},"
                +   "\"formData\":{"
                +     "\"name\":\"" + assetId + ".jpg\","
                +     "\"nodeType\":\"cm:content\","
                +     "\"autoRename\":\"true\","
                +     "\"include\":\"path\","
                +     "\"filedata\":\"{}\","
                +     "\"cplg:altText\":\"" + altText + "\","
                +     "\"cplg:longDescription\":\"" + longDesc + "\","
                +     "\"cp:workURN\":\"" + workURN + "\""
                +   "},"
                +   "\"remoteDataOpts\":{"
                +     "\"uri\":\"" + bronteAssetUrl + "\","
                +     "\"method\":\"GET\","
                +     "\"formDataField\":\"filedata\""
                +   "}"
                + "}";

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);

        when(httpRequestService.submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(requestNotification));

        alfrescoAssetPushService.pushAsset(referenceId, courseId.toString(), assetId.toString(), bronteAssetUrl,
                        myCloudToken, alfrescoNodeUrl, fileName, altText, longDesc, workURN).block();

        verify(httpRequestService, atLeastOnce()).submit(eq(RequestPurpose.ALFRESCO_ASSET_PUSH), captor.capture(), eq(referenceId));

        assertNotNull(captor.getValue());
        assertEquals(jsonStr, captor.getValue().getParamsAsJson());
    }

    @Test
    void pushAssetStatus_success() {
        final String FILENAME = "originalAssetName";
        final String AERO_FILENAME = "aero_image.jpg";
        final String ALFRESCO_FILENAME = "alfresco_image.jpg";

        AssetSummary aeroAssetSummary = new AssetSummary()
                .setId(UUID.randomUUID())
                .setMediaType(AssetMediaType.IMAGE)
                .setProvider(AssetProvider.AERO);
        AssetSummary alfrescoAssetSummary = new AssetSummary()
                .setId(UUID.randomUUID())
                .setMediaType(AssetMediaType.IMAGE)
                .setProvider(AssetProvider.ALFRESCO);
        AssetSummary externalAssetSummary = new AssetSummary()
                .setId(UUID.randomUUID())
                .setMediaType(AssetMediaType.IMAGE)
                .setProvider(AssetProvider.EXTERNAL);

        AssetPayload aeroAssetPayload = new AssetPayload()
                .putMetadata(FILENAME, AERO_FILENAME);
        AssetPayload alfrescoAssetPayload = new AssetPayload()
                .putMetadata(FILENAME, ALFRESCO_FILENAME);

        UUID alfrescoAssetId = UUID.randomUUID();
        AlfrescoAssetData alfrescoAssetData = new AlfrescoAssetData()
                .setAlfrescoId(alfrescoAssetId);

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(aeroAssetSummary, alfrescoAssetSummary, externalAssetSummary));

        when(bronteAssetService.getAssetPayload(eq(aeroAssetSummary.getId())))
                .thenReturn(Mono.just(aeroAssetPayload));
        when(alfrescoAssetService.getAssetPayload(eq(alfrescoAssetSummary.getId())))
                .thenReturn(Mono.just(alfrescoAssetPayload));

        when(alfrescoAssetService.getAlfrescoAssetData(eq(alfrescoAssetSummary.getId())))
                .thenReturn(Mono.just(alfrescoAssetData));

        List<Map<String, String>> assetsStatus = alfrescoAssetPushService
                .pushAssetStatus(courseId)
                .block();

        assertNotNull(assetsStatus);
        assertEquals(2, assetsStatus.size());

        Map<String, String> aeroAssetsStatus = assetsStatus.get(0);
        assertEquals(AERO_FILENAME, aeroAssetsStatus.get("fileName"));

        Map<String, String> alrescoAssetsStatus = assetsStatus.get(1);
        assertEquals(ALFRESCO_FILENAME, alrescoAssetsStatus.get("fileName"));
        assertEquals(alfrescoAssetId.toString(), alrescoAssetsStatus.get("alfrescoAssetId"));
    }

    @Test
    public void getAeroImageAssetCount_withAeroImageAsset_noDuplicated() {

        UUID assetId2 = UUID.randomUUID();
        UUID assetId3 = UUID.randomUUID();

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.IMAGE),
                        new AssetSummary()
                                .setId(assetId2)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.IMAGE),
                        new AssetSummary()
                                .setId(assetId3)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.IMAGE)));

        Long count = alfrescoAssetPushService.getAeroImageAssetCount(courseId).block();

        assertEquals(3, count);
    }

    @Test
    public void getAeroImageAssetCount_withAeroImageAsset_duplicated() {

        UUID assetId2 = UUID.randomUUID();

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.IMAGE),
                        new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.IMAGE),
                        new AssetSummary()
                                .setId(assetId2)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.IMAGE)));

        Long count = alfrescoAssetPushService.getAeroImageAssetCount(courseId).block();

        assertEquals(2, count);
    }

    @Test
    public void getAeroImageAssetCount_withALfrescoImageAsset() {

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.ALFRESCO)
                                .setMediaType(AssetMediaType.IMAGE),
                        new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.ALFRESCO)
                                .setMediaType(AssetMediaType.IMAGE),
                        new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.ALFRESCO)
                                .setMediaType(AssetMediaType.IMAGE)));

        Long count = alfrescoAssetPushService.getAeroImageAssetCount(courseId).block();

        assertEquals(0, count);
    }

    @Test
    public void getAeroImageAssetCount_withMultiTypeAeroAsset() {

        when(coursewareAssetService.getAllSummariesForRootActivity(any(UUID.class)))
                .thenReturn(Flux.just(new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.IMAGE),
                        new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.AUDIO),
                        new AssetSummary()
                                .setId(assetId)
                                .setProvider(AssetProvider.AERO)
                                .setMediaType(AssetMediaType.DOCUMENT)));

        Long count = alfrescoAssetPushService.getAeroImageAssetCount(courseId).block();

        assertEquals(1, count);
    }
}
