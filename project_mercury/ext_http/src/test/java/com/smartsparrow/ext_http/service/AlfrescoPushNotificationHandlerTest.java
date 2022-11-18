package com.smartsparrow.ext_http.service;

import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PUSH_RESULT_HANDLER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.util.UUID;

class AlfrescoPushNotificationHandlerTest {

    private static final Logger log = MercuryLoggerFactory.getLogger(AlfrescoPushNotificationHandlerTest.class);

    @InjectMocks
    private AlfrescoPushNotificationHandler alfrescoPushNotificationHandler;

    @Mock
    private CamelReactiveStreamsService camelReactiveStreamsService;

    private UUID courseId = UUID.randomUUID();
    private UUID assetId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testAlfrescoAssetPushHandler_validateParams() {
        TestPublisher<ResultNotification> publisher = TestPublisher.create();
        publisher.complete();

        when(camelReactiveStreamsService.toStream(eq(ALFRESCO_NODE_PUSH_RESULT_HANDLER), any(ResultNotification.class), eq(ResultNotification.class)))
                .thenReturn(publisher.mono());

        String payload = responsePayload();
        ResultNotification resultNotification = readValue(payload, ResultNotification.class);
        alfrescoPushNotificationHandler.handleResultNotification(resultNotification, UUIDs.timeBased())
                .block();

        verify(camelReactiveStreamsService)
                .toStream(ALFRESCO_NODE_PUSH_RESULT_HANDLER, resultNotification, ResultNotification.class);
    }

    String responsePayload() {
        return   "{"
                + "    \"state\": {"
                + "        \"notificationId\": \"236f9ba0-5e9f-11eb-835c-172643b9b6da\","
                + "        \"purpose\": \"ALFRESCO_ASSET_PUSH\","
                + "        \"referenceId\": null"
                + "    },"
                + "    \"result\": ["
                + "        {"
                + "            \"operation\": \"request\","
                + "            \"uri\": \"https://usppewip.cms.pearson.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/58ed783e-9ecc-42e6-af74-5eca702bdb25/\","
                + "            \"method\": \"GET\","
                + "            \"headers\": {"
                + "                \"X-BronteCourseId\": \"236f9ba0-5e9f-11eb-835c-172643b9b6da\","
                + "                \"X-BronteAssetId\": \"236f9ba0-5e9f-11eb-835c-172643b9b6da\","
                + "                \"X-AlfrescoAssetId\": \"58ed783e-9ecc-42e6-af74-5eca702bdb25\","
                + "                \"X-BronteAssetModifiedTime\": \"bronteassettime123\","
                + "                \"X-BronteAssetVersion\": \"bronteversion123\","
                + "                \"X-PearsonSSOSession\": \"TEST-TOKEN\","
                + "                \"User-Agent\": \"SPR-aero/1.0\","
                + "                \"host\": \"usppewip.cms.pearson.com\""
                + "            }"
                + "        },"
                + "        {"
                + "            \"operation\": \"response\","
                + "            \"uri\": \"https://usppewip.cms.pearson.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/58ed783e-9ecc-42e6-af74-5eca702bdb25/\","
                + "            \"headers\": {"
                + "                \"cache-control\": \"no-cache\","
                + "                \"content-type\": \"application/json;charset=UTF-8\","
                + "                \"date\": \"Tue, 26 Jan 2021 02:24:13 GMT\","
                + "                \"expires\": \"Thu, 01 Jan 1970 00:00:00 GMT\","
                + "                \"pragma\": \"no-cache\","
                + "                \"server\": \"openresty\","
                + "                \"strict-transport-security\": \"max-age=31536000\","
                + "                \"x-content-type-options\": \"nosniff\","
                + "                \"x-frame-options\": \"SAMEORIGIN\","
                + "                \"x-xss-protection\": \"1; mode=block\","
                + "                \"content-length\": \"935\","
                + "                \"connection\": \"Close\""
                + "            },"
                + "            \"statusCode\": 200,"
                + "            \"body\": \"{\\\"entry\\\":{\\\"isFile\\\":true,\\\"createdByUser\\\":{\\\"id\\\":\\\"TESTID\\\",\\\"displayName\\\":\\\"TEST USER\\\"},\\\"modifiedAt\\\":\\\"2021-01-25T18:36:31.563+0000\\\",\\\"nodeType\\\":\\\"cm:content\\\",\\\"content\\\":{\\\"mimeType\\\":\\\"image/jpeg\\\",\\\"mimeTypeName\\\":\\\"JPEG Image\\\",\\\"sizeInBytes\\\":70500,\\\"encoding\\\":\\\"UTF-8\\\"},\\\"parentId\\\":\\\"c386ab9c-7b34-40ea-9c78-62fa08f46869\\\",\\\"aspectNames\\\":[\\\"rn:renditioned\\\",\\\"cm:versionable\\\",\\\"cm:titled\\\",\\\"cm:auditable\\\",\\\"cm:taggable\\\",\\\"cm:author\\\",\\\"cm:thumbnailModification\\\",\\\"exif:exif\\\"],\\\"createdAt\\\":\\\"2021-01-25T18:27:06.509+0000\\\",\\\"isFolder\\\":false,\\\"modifiedByUser\\\":{\\\"id\\\":\\\"APUVVSR\\\",\\\"displayName\\\":\\\"Sree Puvvala\\\"},\\\"name\\\":\\\"54adcff2915cc82e5d6a79e473a2fc12.jpeg\\\",\\\"id\\\":\\\"58ed783e-9ecc-42e6-af74-5eca702bdb25\\\",\\\"properties\\\":{\\\"cm:title\\\":\\\"54adcff2915cc82e5d6a79e473a2fc12.jpeg\\\",\\\"cm:versionType\\\":\\\"MAJOR\\\",\\\"cm:versionLabel\\\":\\\"1.0\\\",\\\"exif:flash\\\":false,\\\"exif:pixelXDimension\\\":370,\\\"cm:lastThumbnailModification\\\":[\\\"doclib:1611599228746\\\",\\\"imgpreview:1611599235970\\\"],\\\"exif:pixelYDimension\\\":474}}}\","
                + "            \"time\": {"
                + "                \"timingStart\": 1611627852337,"
                + "                \"timings\": {"
                + "                    \"socket\": 5.579467000003206,"
                + "                    \"lookup\": 5.848281999999017,"
                + "                    \"connect\": 204.313979999999,"
                + "                    \"response\": 819.7596279999998,"
                + "                    \"end\": 820.0167419999998"
                + "                },"
                + "                \"timingPhases\": {"
                + "                    \"wait\": 5.579467000003206,"
                + "                    \"dns\": 0.2688149999958114,"
                + "                    \"tcp\": 198.46569799999997,"
                + "                    \"firstByte\": 615.4456480000008,"
                + "                    \"download\": 0.2571140000000014,"
                + "                    \"total\": 820.0167419999998"
                + "                }"
                + "            }"
                + "        }"
                + "    ]"
                + "}";
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
}