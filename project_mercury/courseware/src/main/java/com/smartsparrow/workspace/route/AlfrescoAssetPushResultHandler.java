package com.smartsparrow.workspace.route;

import com.smartsparrow.asset.data.AlfrescoImageNode;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.ext_http.service.HttpEvent;
import com.smartsparrow.ext_http.service.ResultNotification;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.service.AlfrescoAssetResultBroker;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;
import reactor.core.publisher.Mono;

import javax.inject.Inject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.smartsparrow.workspace.route.AlfrescoCoursewareRoute.ALFRESCO_NODE_PUSH_RESULT_BODY;

public class AlfrescoAssetPushResultHandler {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetPushResultHandler.class);

    private final String COURSE_ID_HEADER = "X-BronteCourseId";
    private final String ASSET_ID_HEADER = "X-BronteAssetId";

    private final AlfrescoAssetService alfrescoAssetService;
    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final AlfrescoAssetResultBroker alfrescoAssetResultBroker;

    @Inject
    public AlfrescoAssetPushResultHandler(final AlfrescoAssetService alfrescoAssetService,
                                          final AlfrescoAssetTrackService alfrescoAssetTrackService,
                                          final AlfrescoAssetResultBroker alfrescoAssetResultBroker) {
        this.alfrescoAssetService = alfrescoAssetService;
        this.alfrescoAssetTrackService = alfrescoAssetTrackService;
        this.alfrescoAssetResultBroker = alfrescoAssetResultBroker;
    }

    /**
     * Handle the alfresco update pull external call result. Performs an additional external http call via camel
     * to get the file content. This call is only performed when either there is an update available or a forceSync was issued
     *
     * @param exchange the camel exchange
     */
    @Handler
    public void handle(Exchange exchange) {
        // get the result notification
        ResultNotification resultNotification = exchange.getProperty(ALFRESCO_NODE_PUSH_RESULT_BODY, ResultNotification.class);

        UUID courseId = null;
        UUID assetId = null;
        AlfrescoImageNode alfrescoImageNode = new AlfrescoImageNode();
        boolean hasError = false;

        try {
            // parse result into AlfrescoImageNode
            List<HttpEvent> httpEvents = resultNotification.getResult();
            for (HttpEvent httpEvent : httpEvents) {
                switch (httpEvent.getOperation()) {
                    case request: { // need some values from the request
                        Map<String, List<String>> headers = httpEvent.getHeaders();

                        // grab course id
                        if (headers.containsKey(COURSE_ID_HEADER)) {
                            String courseIdStr = headers.get(COURSE_ID_HEADER).get(0);
                            courseId = UUID.fromString(courseIdStr);
                        } else {
                            throw new BadRequestException("missing expected header: " + COURSE_ID_HEADER);
                        }

                        // grab asset id
                        if (headers.containsKey(ASSET_ID_HEADER)) {
                            String assetIdStr = headers.get(ASSET_ID_HEADER).get(0);
                            assetId = UUID.fromString(assetIdStr);
                        } else {
                            throw new BadRequestException("missing expected header: " + ASSET_ID_HEADER);
                        }

                        break;
                    }
                    case response: { // need some values from the response
                        String bodyStr = httpEvent.getBody();
                        bodyStr = StringEscapeUtils.unescapeJava(bodyStr); // need to unescape raw body string
                        bodyStr = bodyStr.substring(1, bodyStr.length() - 1); // and remove leading, trailing quotes

                        JSONObject body = new JSONObject(bodyStr);

                        JSONObject entry = body.getJSONObject("entry");

                        String id = entry.getString("id");
                        String name = entry.getString("name");
                        String modifiedAt = entry.getString("modifiedAt");


                        modifiedAt = modifiedAt.substring(0, modifiedAt.indexOf('.')); // '2021-02-03T02:05:47.372+0000' -> '2021-02-03T02:05:47'
                        long modifiedAtEpoch = LocalDateTime.parse(modifiedAt).toEpochSecond(ZoneOffset.UTC);

                        // get path name from the response
                        String pathName = entry.getJSONObject("path").getString("name");
                        String alfrescoPath = alfrescoAssetService.getAlfrescoPath(pathName);

                        alfrescoImageNode.setAlfrescoId(UUID.fromString(id));
                        alfrescoImageNode.setName(name);
                        alfrescoImageNode.setVersion("1.0"); // new alfresco image is always first version
                        alfrescoImageNode.setLastModifiedDate(modifiedAtEpoch);
                        alfrescoImageNode.setPath(alfrescoPath);

                        break;
                    }
                    case redirect: {
                        // ignore
                        break;
                    }
                    case error: {
                        log.jsonInfo("alfresco push result: error", new HashMap<String, Object>() {
                            {
                                put("notificationId", resultNotification.getState().getNotificationId());
                                put("referenceId", resultNotification.getState().getReferenceId());
                            }
                        });
                        // todo review what HttpEvent error value should we return here
                        throw new BadRequestException("an error occurred: " + httpEvent.getBody());
                    }
                }
            }

            Map<String, UUID> idMap = new HashMap<>();
            idMap.put(COURSE_ID_HEADER, courseId);
            idMap.put(ASSET_ID_HEADER, assetId);
            Mono<Map<String, UUID>> idMapMono = Mono.just(idMap);

            Mono<AssetSummary> assetSummaryMono = alfrescoAssetService.getAssetById(assetId)
                    .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find asset summary by asset id: %s", assetId))));

            Mono<ImageSource> imageSourceMono = alfrescoAssetService.getImageSources(assetId)
                    .filter(image -> image.getName().equals(ImageSourceName.ORIGINAL)).singleOrEmpty()
                    .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find image source by asset id: %s", assetId))));

            Mono.zip(idMapMono, assetSummaryMono, imageSourceMono)
                    .flatMap(tuple3 -> {

                        final Map<String, UUID> uidMap = tuple3.getT1();
                        final AssetSummary assetSummary = tuple3.getT2();
                        final ImageSource imageSource = tuple3.getT3();

                        UUID assetUid = uidMap.get(ASSET_ID_HEADER);
                        UUID ownerId = assetSummary.getOwnerId();

                        alfrescoImageNode.setSource(imageSource.getUrl());
                        alfrescoImageNode.setWidth(imageSource.getWidth());
                        alfrescoImageNode.setHeight(imageSource.getHeight());

                        return alfrescoAssetService.saveAlfrescoImageData(assetUid, ownerId, alfrescoImageNode)
                                .then(Mono.just(1));
                    })
                    .doOnEach(log.reactiveInfoSignal("successfully pushed Bronte asset to Alfresco", ignored -> new HashedMap<String, Object>() {
                        {
                            put("referenceId", resultNotification.getState().getReferenceId());
                            put("notificationId", resultNotification.getState().getNotificationId());
                            put("assetId", idMap.get(ASSET_ID_HEADER));
                        }
                    }))
                    .block();
        } catch (BadRequestException ex) {
            log.jsonError("failed to push the file content for Bronte asset", new HashMap<String, Object>() {
                {
                    put("referenceId", resultNotification.getState().getReferenceId());
                    put("notificationId", resultNotification.getState().getNotificationId());
                }
            }, ex);
            hasError = true;
        } finally {
            log.jsonInfo("alfresco push result: finally", new HashMap<String, Object>() {
                {
                    put("notificationId", resultNotification.getState().getNotificationId());
                    put("referenceId", resultNotification.getState().getReferenceId());
                }
            });

            final AlfrescoAssetSyncStatus syncStatus = (hasError) ? AlfrescoAssetSyncStatus.FAILED : AlfrescoAssetSyncStatus.COMPLETED;

            alfrescoAssetTrackService.getSyncNotification(resultNotification.getState().getNotificationId())
                    .flatMap(syncNotification -> {
                        return alfrescoAssetTrackService.handleNotification(syncNotification, syncStatus, AlfrescoAssetSyncType.PUSH)
                                .flatMap(isPushCompleted -> alfrescoAssetTrackService.handleSyncCompletion(syncNotification, isPushCompleted, AlfrescoAssetSyncType.PUSH)
                                        .then(alfrescoAssetResultBroker.broadcast(syncNotification, isPushCompleted)));
                    })
                    .subscribe();
        }
    }
}
