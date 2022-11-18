package com.smartsparrow.workspace.service;

import static com.smartsparrow.data.Headers.BRONTE_ASSET_HEADER;
import static com.smartsparrow.data.Headers.ALFRESCO_AZURE_AUTHORIZATION_HEADER;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartsparrow.asset.data.AlfrescoImageNode;
import com.smartsparrow.asset.eventmessage.AlfrescoAssetPullEventMessage;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ext_http.service.HttpEvent;
import com.smartsparrow.ext_http.service.NotificationState;
import com.smartsparrow.ext_http.service.ResultNotification;
import com.smartsparrow.util.Json;
import com.smartsparrow.workspace.cms.AlfrescoAssetPullRequestMessage;

class AlfrescoPullRequestParserTest {

    @InjectMocks
    private AlfrescoPullRequestParser alfrescoPullRequestParser;

    @Mock
    private AssetConfig assetConfig;

    @Mock
    private AlfrescoAssetService alfrescoAssetService;

    @Mock
    private ResultNotification resultNotification;

    @Mock
    private HttpEvent requestEvent;

    @Mock
    private HttpEvent responseEvent;

    @Mock
    private AlfrescoImageNode alfrescoImageNode;

    private static final UUID referenceId = UUID.randomUUID();
    private static final UUID assetId = UUID.randomUUID();
    private static final UUID ownerId = UUID.randomUUID();
    private static final UUID alfrescoId = UUID.randomUUID();
    private static final String token = "token";

    private static final String alfrescoPath = "bronte---sf-test/documentLibrary/Book3";
    private static final String pathName = "/Company Home/Sites/" + alfrescoPath;

    private String alfrescoRequestMessageString;

    private static final String responseBody = "{\n" +
            "{\"entry\": {\n" +
            "    \"isFile\": true,\n" +
            "    \"createdByUser\": {\n" +
            "      \"id\": \"HMRSPSON\",\n" +
            "      \"displayName\": \"Homer Simpson\"\n" +
            "    },\n" +
            "    \"modifiedAt\": \"2021-01-25T18:36:31.563+0000\",\n" +
            "    \"nodeType\": \"cm:content\",\n" +
            "    \"content\": {\n" +
            "      \"mimeType\": \"image/jpeg\",\n" +
            "      \"mimeTypeName\": \"JPEG Image\",\n" +
            "      \"sizeInBytes\": 70500,\n" +
            "      \"encoding\": \"UTF-8\"\n" +
            "    },\n" +
            "    \"parentId\": \"c386ab9c-7b34-40ea-9c78-62fa08f46869\",\n" +
            "    \"aspectNames\": [\n" +
            "      \"rn:renditioned\",\n" +
            "      \"cm:versionable\",\n" +
            "      \"cm:titled\",\n" +
            "      \"cm:auditable\",\n" +
            "      \"cm:taggable\",\n" +
            "      \"cm:author\",\n" +
            "      \"cm:thumbnailModification\",\n" +
            "      \"exif:exif\"\n" +
            "    ],\n" +
            "    \"createdAt\": \"2021-01-25T18:27:06.509+0000\",\n" +
            "    \"path\": {\n" +
            "      \"name\": \"" + pathName + "\",\n" +
            "      \"isComplete\": true\n" +
            "    },\n" +
            "    \"isFolder\": false,\n" +
            "    \"modifiedByUser\": {\n" +
            "      \"id\": \"HMRSPSON\",\n" +
            "      \"displayName\": \"Homer Simpson\"\n" +
            "    },\n" +
            "    \"name\": \"54adcff2915cc82e5d6a79e473a2fc12.jpeg\",\n" +
            "    \"id\": \"58ed783e-9ecc-42e6-af74-5eca702bdb25\",\n" +
            "    \"properties\": {\n" +
            "      \"cm:title\": \"54adcff2915cc82e5d6a79e473a2fc12.jpeg\",\n" +
            "      \"cm:versionType\": \"MAJOR\",\n" +
            "      \"cm:versionLabel\": \"1.0\",\n" +
            "      \"exif:flash\": false,\n" +
            "      \"exif:pixelXDimension\": 370,\n" +
            "      \"cm:lastThumbnailModification\": [\n" +
            "        \"doclib:1611599228746\",\n" +
            "        \"imgpreview:1611599235970\"\n" +
            "      ],\n" +
            "      \"exif:pixelYDimension\": 474\n" +
            "    }\n" +
            "  }\n" +
            "}}";

    private static final String responseBodyNoPath = "{\n" +
            "{\"entry\": {\n" +
            "    \"isFile\": true,\n" +
            "    \"createdByUser\": {\n" +
            "      \"id\": \"HMRSPSON\",\n" +
            "      \"displayName\": \"Homer Simpson\"\n" +
            "    },\n" +
            "    \"modifiedAt\": \"2021-01-25T18:36:31.563+0000\",\n" +
            "    \"nodeType\": \"cm:content\",\n" +
            "    \"content\": {\n" +
            "      \"mimeType\": \"image/jpeg\",\n" +
            "      \"mimeTypeName\": \"JPEG Image\",\n" +
            "      \"sizeInBytes\": 70500,\n" +
            "      \"encoding\": \"UTF-8\"\n" +
            "    },\n" +
            "    \"parentId\": \"c386ab9c-7b34-40ea-9c78-62fa08f46869\",\n" +
            "    \"createdAt\": \"2021-01-25T18:27:06.509+0000\",\n" +
            "    \"isFolder\": false,\n" +
            "    \"modifiedByUser\": {\n" +
            "      \"id\": \"HMRSPSON\",\n" +
            "      \"displayName\": \"Homer Simpson\"\n" +
            "    },\n" +
            "    \"name\": \"54adcff2915cc82e5d6a79e473a2fc12.jpeg\",\n" +
            "    \"id\": \"58ed783e-9ecc-42e6-af74-5eca702bdb25\",\n" +
            "    \"properties\": {\n" +
            "      \"cm:title\": \"54adcff2915cc82e5d6a79e473a2fc12.jpeg\",\n" +
            "      \"cm:versionType\": \"MAJOR\",\n" +
            "      \"cm:versionLabel\": \"1.0\",\n" +
            "      \"exif:flash\": false,\n" +
            "      \"exif:pixelXDimension\": 370,\n" +
            "      \"cm:lastThumbnailModification\": [\n" +
            "        \"doclib:1611599228746\",\n" +
            "        \"imgpreview:1611599235970\"\n" +
            "      ],\n" +
            "      \"exif:pixelYDimension\": 474\n" +
            "    }\n" +
            "  }\n" +
            "}}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        NotificationState notificationState = new NotificationState()
                .setReferenceId(referenceId);

        when(responseEvent.getOperation()).thenReturn(HttpEvent.Operation.response);
        when(requestEvent.getOperation()).thenReturn(HttpEvent.Operation.request);

        when(resultNotification.getState()).thenReturn(notificationState);
        when(assetConfig.getAlfrescoUrl()).thenReturn("alfrescoUrl");

        when(resultNotification.getResult()).thenReturn(Lists.newArrayList(requestEvent, responseEvent));
        alfrescoRequestMessageString = Json.stringify(new AlfrescoAssetPullRequestMessage()
                .setAssetId(assetId)
                .setAlfrescoNodeId(alfrescoId)
                .setForceSync(false)
                .setLastModified(System.currentTimeMillis())
                .setOwnerId(ownerId)
                .setReferenceId(referenceId)
                .setVersion("1.0"));

        when(responseEvent.getBody()).thenReturn(responseBody);
        when(alfrescoImageNode.getName()).thenReturn("Test Image");
        when(alfrescoImageNode.getMimeType()).thenReturn("image/jpeg");

        when(alfrescoAssetService.getAlfrescoPath(pathName)).thenReturn(alfrescoPath);
    }

    @Test
    void parse_nullNotification() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoPullRequestParser.parse(null));

        assertEquals("resultNotification is required", f.getMessage());
    }

    @Test
    void parse_noResults() {
        when(resultNotification.getResult()).thenReturn(new ArrayList<>());
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoPullRequestParser.parse(resultNotification));

        assertEquals("results are required", f.getMessage());
    }

    @Test
    void parse_missingResponseOrRequesteEvents() {
        when(resultNotification.getResult()).thenReturn(Lists.newArrayList(requestEvent));
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoPullRequestParser.parse(resultNotification));

        assertEquals("responseEvent missing", f.getMessage());
    }

    @Test
    void parse_responseNotOkStatus() {
        when(responseEvent.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoPullRequestParser.parse(resultNotification));

        assertEquals("response had a non 200 status", f.getMessage());
    }

    @Test
    void parse() throws JsonProcessingException {
        when(responseEvent.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Map<String, List<String>> headers = new HashMap<String, List<String>>() {
            {put(BRONTE_ASSET_HEADER, Lists.newArrayList(alfrescoRequestMessageString));}
            {put(ALFRESCO_AZURE_AUTHORIZATION_HEADER, Lists.newArrayList(token));}
        };

        Map<String, String> metadata = new HashMap<String, String>() {
            {put("altText", "");}
            {put("longDesc", "");}
            {put("workURN", "");}
            {put("alfrescoPath", alfrescoPath);}
        };

        when(requestEvent.getHeaders()).thenReturn(headers);

        AlfrescoAssetPullEventMessage eventMessage = alfrescoPullRequestParser.parse(resultNotification);

        assertNotNull(eventMessage);
        assertEquals(token, eventMessage.getMyCloudToken());
        assertNotNull(eventMessage.getAlfrescoUrl());
        assertNotNull(eventMessage.getAlfrescoNode());
        assertEquals(metadata, eventMessage.getMetadata());
        assertEquals(referenceId, eventMessage.getReferenceId());
        assertNull(eventMessage.getInputStream());
        assertFalse(eventMessage.isForceSync());
    }

    @Test
    void fileNameWithExtension() {
        assertEquals("Test_Image.jpeg", alfrescoPullRequestParser.getFileNameWithExtension(alfrescoImageNode));
    }

    @Test
    void parse_missingPathInResponse() {
        when(responseEvent.getBody()).thenReturn(responseBodyNoPath);
        when(responseEvent.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        Map<String, List<String>> headers = new HashMap<String, List<String>>() {
            {put(BRONTE_ASSET_HEADER, Lists.newArrayList(alfrescoRequestMessageString));}
            {put(ALFRESCO_AZURE_AUTHORIZATION_HEADER, Lists.newArrayList(token));}
        };
        when(requestEvent.getHeaders()).thenReturn(headers);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoPullRequestParser.parse(resultNotification));

        assertEquals("missing required path info", f.getMessage());
    }
}
