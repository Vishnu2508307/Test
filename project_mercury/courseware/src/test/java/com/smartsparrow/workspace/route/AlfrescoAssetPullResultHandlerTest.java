package com.smartsparrow.workspace.route;

import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PULL;
import static com.smartsparrow.workspace.route.AlfrescoCoursewareRoute.ALFRESCO_NODE_PULL_RESULT_BODY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.asset.data.AlfrescoAsset;
import com.smartsparrow.asset.data.AlfrescoImageNode;
import com.smartsparrow.asset.data.AlfrescoNode;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.eventmessage.AlfrescoAssetPullEventMessage;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetUploadService;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ext_http.service.ResultNotification;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncNotification;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.service.AlfrescoAssetResultBroker;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import com.smartsparrow.workspace.service.AlfrescoPullRequestParser;

import reactor.core.publisher.Mono;

class AlfrescoAssetPullResultHandlerTest {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetPullResultHandlerTest.class);

    @InjectMocks
    private AlfrescoAssetPullResultHandler alfrescoAssetPullResultHandler;

    @Mock
    private CamelReactiveStreamsService camelReactiveStreamsService;

    @Mock
    private AssetUploadService assetUploadService;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private AlfrescoPullRequestParser alfrescoPullRequestParser;

    @Mock
    private AlfrescoAssetService alfrescoAssetService;

    @Mock
    private Exchange exchange;

    private AlfrescoAssetPullEventMessage eventMessage;

    @Mock
    private AlfrescoAssetPullEventMessage responseMessage;

    @Mock
    private AlfrescoAssetTrackService alfrescoAssetTrackService;

    @Mock
    private AlfrescoAssetResultBroker alfrescoAssetResultBroker;

    private static final UUID assetId = UUID.randomUUID();
    private static final UUID ownerId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);

        AlfrescoImageNode alfrescoNode = new AlfrescoImageNode()
                .setAlfrescoId(UUID.randomUUID())
                .setName("name.jpg");

        eventMessage = new AlfrescoAssetPullEventMessage()
                .setAssetId(assetId)
                .setRequireUpdate(false)
                .setForceSync(false);

        responseMessage = new AlfrescoAssetPullEventMessage()
                .setAlfrescoNode(alfrescoNode)
                .setInputStream(null)
                .setMetadata(new HashMap<>())
                .setAssetId(assetId);

        AssetSummary assetSummary = new AssetSummary()
                .setUrn("urn")
                .setProvider(AssetProvider.ALFRESCO)
                .setOwnerId(ownerId)
                .setId(assetId)
                .setVisibility(AssetVisibility.GLOBAL);

        String payload = "{    \"state\": {        \"notificationId\": \"236f9ba0-5e9f-11eb-835c-172643b9b6da\",        \"purpose\": \"ALFRESCO_ASSET_PULL\",        \"referenceId\": null    },    \"result\": [        {            \"operation\": \"request\",            \"uri\": \"https:g/usppewip.cms.pearson.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/58ed783e-9ecc-42e6-af74-5eca702bdb25/\",            \"method\": \"GET\",            \"headers\": {                \"X-BronteAssetId\": \"bronteasset123\",                \"X-AlfrescoAssetId\": \"58ed783e-9ecc-42e6-af74-5eca702bdb25\",                \"X-BronteAssetModifiedTime\": \"bronteassettime123\",                \"X-BronteAssetVersion\": \"bronteversion123\",                \"X-PearsonSSOSession\": \"TEST-TOKEN\",                \"User-Agent\": \"SPR-aero/1.0\",                \"host\": \"usppewip.cms.pearson.com\"            }        },        {            \"operation\": \"response\",            \"uri\": \"https://usppewip.cms.pearson.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/58ed783e-9ecc-42e6-af74-5eca702bdb25/\",            \"headers\": {                \"cache-control\": \"no-cache\",                \"content-type\": \"application/json;charset=UTF-8\",                \"date\": \"Tue, 26 Jan 2021 02:24:13 GMT\",                \"expires\": \"Thu, 01 Jan 1970 00:00:00 GMT\",                \"pragma\": \"no-cache\",                \"server\": \"openresty\",                \"strict-transport-security\": \"max-age=31536000\",                \"x-content-type-options\": \"nosniff\",                \"x-frame-options\": \"SAMEORIGIN\",                \"x-xss-protection\": \"1; mode=block\",                \"content-length\": \"935\",                \"connection\": \"Close\"            },            \"statusCode\": 200,            \"body\": \"{\\\"entry\\\":{\\\"isFile\\\":true,\\\"createdByUser\\\":{\\\"id\\\":\\\"TESTID\\\",\\\"displayName\\\":\\\"TEST USER\\\"},\\\"modifiedAt\\\":\\\"2021-01-25T18:36:31.563+0000\\\",\\\"nodeType\\\":\\\"cm:content\\\",\\\"content\\\":{\\\"mimeType\\\":\\\"image/jpeg\\\",\\\"mimeTypeName\\\":\\\"JPEG Image\\\",\\\"sizeInBytes\\\":70500,\\\"encoding\\\":\\\"UTF-8\\\"},\\\"parentId\\\":\\\"c386ab9c-7b34-40ea-9c78-62fa08f46869\\\",\\\"aspectNames\\\":[\\\"rn:renditioned\\\",\\\"cm:versionable\\\",\\\"cm:titled\\\",\\\"cm:auditable\\\",\\\"cm:taggable\\\",\\\"cm:author\\\",\\\"cm:thumbnailModification\\\",\\\"exif:exif\\\"],\\\"createdAt\\\":\\\"2021-01-25T18:27:06.509+0000\\\",\\\"isFolder\\\":false,\\\"modifiedByUser\\\":{\\\"id\\\":\\\"APUVVSR\\\",\\\"displayName\\\":\\\"Sree Puvvala\\\"},\\\"name\\\":\\\"54adcff2915cc82e5d6a79e473a2fc12.jpeg\\\",\\\"id\\\":\\\"58ed783e-9ecc-42e6-af74-5eca702bdb25\\\",\\\"properties\\\":{\\\"cm:title\\\":\\\"54adcff2915cc82e5d6a79e473a2fc12.jpeg\\\",\\\"cm:versionType\\\":\\\"MAJOR\\\",\\\"cm:versionLabel\\\":\\\"1.0\\\",\\\"exif:flash\\\":false,\\\"exif:pixelXDimension\\\":370,\\\"cm:lastThumbnailModification\\\":[\\\"doclib:1611599228746\\\",\\\"imgpreview:1611599235970\\\"],\\\"exif:pixelYDimension\\\":474}}}\",            \"time\": {                \"timingStart\": 1611627852337,                \"timings\": {                    \"socket\": 5.579467000003206,                    \"lookup\": 5.848281999999017,                    \"connect\": 204.313979999999,                    \"response\": 819.7596279999998,                    \"end\": 820.0167419999998                },                \"timingPhases\": {                    \"wait\": 5.579467000003206,                    \"dns\": 0.2688149999958114,                    \"tcp\": 198.46569799999997,                    \"firstByte\": 615.4456480000008,                    \"download\": 0.2571140000000014,                    \"total\": 820.0167419999998                }            }        }    ]}";
        ResultNotification resultNotification = readValue(payload, ResultNotification.class);

        when(exchange.getProperty(ALFRESCO_NODE_PULL_RESULT_BODY, ResultNotification.class))
                .thenReturn(resultNotification);

        when(alfrescoPullRequestParser.parse(any(ResultNotification.class)))
                .thenReturn(eventMessage);

        when(camelReactiveStreamsService.toStream(ALFRESCO_NODE_PULL, eventMessage, AlfrescoAssetPullEventMessage.class))
                .thenReturn(Mono.just(responseMessage));

        when(bronteAssetService.getAssetSummary(assetId)).thenReturn(Mono.just(assetSummary));

        when(assetUploadService.save(eq(assetId), any(AssetTemplate.class)))
                .thenReturn(Mono.just(new AssetPayload().setAsset(new AlfrescoAsset().setId(assetId))));

        when(alfrescoAssetService.saveAlfrescoImageData(eq(assetId), eq(ownerId), any(AlfrescoImageNode.class)))
                .thenReturn(Mono.empty());

        when(alfrescoPullRequestParser.getFileNameWithExtension(any(AlfrescoImageNode.class))).thenReturn("name.jpg");

        when(alfrescoAssetTrackService.getSyncNotification(any(UUID.class)))
                .thenReturn(Mono.just(new AlfrescoAssetSyncNotification()));

        when(alfrescoAssetTrackService.handleNotification(any(AlfrescoAssetSyncNotification.class), any(AlfrescoAssetSyncStatus.class), eq(AlfrescoAssetSyncType.PULL)))
                .thenReturn(Mono.just(true));

        when(alfrescoAssetTrackService.handleSyncCompletion(any(AlfrescoAssetSyncNotification.class), eq(true), eq(AlfrescoAssetSyncType.PULL)))
                .thenReturn(Mono.just(true));

        when(alfrescoAssetResultBroker.broadcast(any(AlfrescoAssetSyncNotification.class), anyBoolean()))
                .thenReturn(Mono.empty());
    }

    @Test
    void handle_requireUpdate() throws JsonProcessingException {
        eventMessage.setRequireUpdate(true);
        ArgumentCaptor<AssetTemplate> templateCaptor = ArgumentCaptor.forClass(AssetTemplate.class);

        alfrescoAssetPullResultHandler.handle(exchange);

        verify(alfrescoPullRequestParser).parse(any(ResultNotification.class));
        verify(camelReactiveStreamsService).toStream(ALFRESCO_NODE_PULL, eventMessage, AlfrescoAssetPullEventMessage.class);
        verify(bronteAssetService).getAssetSummary(assetId);
        verify(assetUploadService).save(eq(assetId), templateCaptor.capture());
        verify(alfrescoAssetService).saveAlfrescoImageData(eq(assetId), eq(ownerId), any(AlfrescoImageNode.class));

        AssetTemplate template = templateCaptor.getValue();

        assertNotNull(template);
        assertEquals("name.jpg", template.getOriginalFileName());
        assertEquals("urn", template.getUrn());
    }

    @Test
    void handle_forceSync() throws JsonProcessingException {
        eventMessage.setForceSync(true);
        ArgumentCaptor<AssetTemplate> templateCaptor = ArgumentCaptor.forClass(AssetTemplate.class);

        alfrescoAssetPullResultHandler.handle(exchange);

        verify(alfrescoPullRequestParser).parse(any(ResultNotification.class));
        verify(camelReactiveStreamsService).toStream(ALFRESCO_NODE_PULL, eventMessage, AlfrescoAssetPullEventMessage.class);
        verify(bronteAssetService).getAssetSummary(assetId);
        verify(assetUploadService).save(eq(assetId), templateCaptor.capture());
        verify(alfrescoAssetService).saveAlfrescoImageData(eq(assetId), eq(ownerId), any(AlfrescoImageNode.class));

        AssetTemplate template = templateCaptor.getValue();

        assertNotNull(template);
        assertEquals("name.jpg", template.getOriginalFileName());
        assertEquals("urn", template.getUrn());
    }

    @Test
    void handle_noUpdate() throws JsonProcessingException {
        alfrescoAssetPullResultHandler.handle(exchange);

        verify(alfrescoPullRequestParser).parse(any(ResultNotification.class));
        verify(camelReactiveStreamsService, never()).toStream(ALFRESCO_NODE_PULL, eventMessage, AlfrescoAssetPullEventMessage.class);
        verify(bronteAssetService, never()).getAssetSummary(assetId);
        verify(assetUploadService, never()).save(eq(assetId), any(AssetTemplate.class));
        verify(alfrescoAssetService, never()).saveAlfrescoImageData(eq(assetId), eq(ownerId), any(AlfrescoImageNode.class));
    }

    /**
     * Helper to centrally deal with deserializing the SNS message bodies.
     *
     * @param content   the message content
     * @param valueType the type to convert to
     * @param <T>       the return class
     * @return a hydrated content object
     */
    <T> T readValue(String content, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, valueType);
        } catch (IOException e) {
            log.error("Unable to deserialize payload", e);
            throw new BadRequestException("Unable to parse message content");
        }
    }

    @Test
    void handle_validate_alfrescoNodetype() throws JsonProcessingException {
        AlfrescoNode alfrescoNode = new AlfrescoNode()
                .setAlfrescoId(UUID.randomUUID())
                .setName("name")
                .setVersion("version")
                .setLastModifiedDate(1L);

        responseMessage = new AlfrescoAssetPullEventMessage()
                .setAlfrescoNode(alfrescoNode)
                .setInputStream(null)
                .setMetadata(new HashMap<>())
                .setAssetId(assetId);
        when(camelReactiveStreamsService.toStream(ALFRESCO_NODE_PULL, eventMessage, AlfrescoAssetPullEventMessage.class))
                .thenReturn(Mono.just(responseMessage));
        eventMessage.setRequireUpdate(true);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                                              () -> alfrescoAssetPullResultHandler.handle(exchange));

        assertEquals("Alfresco node is not image type", f.getMessage());
    }

}
