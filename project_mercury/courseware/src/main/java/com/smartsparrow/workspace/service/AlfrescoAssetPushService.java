package com.smartsparrow.workspace.service;

import com.google.common.base.Strings;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.cms.AlfrescoAssetPushRequestBuilder;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncNotification;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.data.AlfrescoAssetTrackGateway;
import org.apache.commons.collections4.map.HashedMap;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.smartsparrow.util.Warrants.affirmArgument;

@Singleton
public class AlfrescoAssetPushService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetPushService.class);

    private final AssetConfig assetConfig;
    private final BronteAssetService bronteAssetService;
    private final AlfrescoAssetService alfrescoAssetService;
    private final CoursewareAssetService coursewareAssetService;
    private final ExternalHttpRequestService httpRequestService;
    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final AlfrescoAssetTrackGateway alfrescoAssetTrackGateway;

    @Inject
    public AlfrescoAssetPushService(AssetConfig assetConfig,
                                    BronteAssetService bronteAssetService,
                                    AlfrescoAssetService alfrescoAssetService,
                                    CoursewareAssetService coursewareAssetService,
                                    ExternalHttpRequestService httpRequestService,
                                    AlfrescoAssetTrackService alfrescoAssetTrackService,
                                    AlfrescoAssetTrackGateway alfrescoAssetTrackGateway) {
        this.assetConfig = assetConfig;
        this.bronteAssetService = bronteAssetService;
        this.alfrescoAssetService = alfrescoAssetService;
        this.coursewareAssetService = coursewareAssetService;
        this.httpRequestService = httpRequestService;
        this.alfrescoAssetTrackService = alfrescoAssetTrackService;
        this.alfrescoAssetTrackGateway = alfrescoAssetTrackGateway;
    }

    /**
     * Push all Bronte course assets to Alfresco.  One request sent per asset uploaded.
     *
     * @param referenceId reference id to track results
     * @param courseId course id to query assets for
     * @param myCloudToken myCloud auth token for alfresco api auth
     * @param alfrescoNodeId destination alfresco node id uploaded assets target
     *
     * @return List of request notifications sent
     *
     */
    public Mono<List<RequestNotification>> pushCourseAssets(UUID referenceId,
                                                            UUID courseId,
                                                            String myCloudToken,
                                                            UUID alfrescoNodeId) {

        affirmArgument(referenceId != null, "referenceId is required");
        affirmArgument(courseId != null, "courseId is required");
        affirmArgument(!Strings.isNullOrEmpty(myCloudToken), "myCloudToken is required");
        affirmArgument(alfrescoNodeId != null, "alfrescoNodeId is required");

        final String alfrescoNodeUrl = assetConfig.getAlfrescoUrl()
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + alfrescoNodeId.toString()
                + "/children";

        return coursewareAssetService.getAllSummariesForRootActivity(courseId)
                .filter(summary -> summary.getProvider().equals(AssetProvider.AERO))
                .distinct()
                .delayElements(Duration.ofMillis(assetConfig.getAlfrescoPushDelayTime()))
                .flatMap(summary -> { // process each item from Flux<AssetByRootActivity>
                    UUID assetId = summary.getId();

                    return bronteAssetService.getAssetPayload(assetId)
                            .filter(assetPayload -> assetPayload.getAsset().getAssetMediaType().equals(AssetMediaType.IMAGE))
                            .flatMap(assetPayload -> {
                                // get the bronte image asset URL, filename, alt text, long description, and work urn
                                String bronteAssetUrl = "";
                                String fileName = "";
                                String altText = "";
                                String longDesc = "";
                                String workURN = "";

                                switch (summary.getProvider()) {
                                    case AERO: {
                                        final String ORIGINAL = "original";
                                        final String URL = "url";
                                        final String FILENAME = "originalAssetName";
                                        final String ALTTEXT = "altText";
                                        final String LONGDESC = "longDesc";
                                        final String WORKURN = "workURN";

                                        // use the url of the original unoptimized image
                                        Map<String, Object> payloadSource = assetPayload.getSource();

                                        if (!payloadSource.containsKey(ORIGINAL)) {
                                            return Mono.error(new UnsupportedOperationException("asset id " + assetId + " does not have an original source"));
                                        }

                                        Map<String, Object> originalData = (Map)payloadSource.get(ORIGINAL);
                                        if (!originalData.containsKey(URL)) {
                                            return Mono.error(new UnsupportedOperationException("asset id " + assetId + " does not have a source url"));
                                        }

                                        bronteAssetUrl = (String)originalData.get(URL);

                                        // alfresco needs a filename
                                        Map<String, String> payloadMetadata = assetPayload.getMetadata();
                                        if (payloadMetadata.containsKey(FILENAME)) { // use filename from asset metadata
                                            fileName = (String)payloadMetadata.get(FILENAME);
                                        } else { // use the asset id as the filename
                                            if (!bronteAssetUrl.contains(".")) {
                                                return Mono.error(new UnsupportedOperationException("bronte asset url does not have file extension"));
                                            }

                                            String fileExt = bronteAssetUrl.substring(bronteAssetUrl.lastIndexOf("."));
                                            fileName = assetId.toString() + fileExt;
                                        }

                                        // push alt text to alfresco if available
                                        if (payloadMetadata.containsKey(ALTTEXT)) {
                                            altText = payloadMetadata.get(ALTTEXT);
                                        }
                                        // push long description to alfresco if available
                                        if (payloadMetadata.containsKey(LONGDESC)) {
                                            longDesc = payloadMetadata.get(LONGDESC);
                                        }
                                        // push work urn to alfresco if available
                                        if (payloadMetadata.containsKey(WORKURN)) {
                                            workURN = payloadMetadata.get(WORKURN);
                                        }

                                            break;
                                    }
                                    default: {
                                        return Mono.error(new UnsupportedOperationException("asset id " + assetId + " with unsupported asset provider: " + summary.getProvider()));
                                    }
                                }

                                // trigger an aws lambda to upload the bronte image to alfresco
                                return pushAsset(referenceId, courseId.toString(), assetId.toString(), bronteAssetUrl, myCloudToken, alfrescoNodeUrl, fileName, altText, longDesc, workURN)
                                        .doOnEach(log.reactiveInfoSignal("started Bronte asset push to Alfresco", requestNotification -> new HashedMap<String, Object>() {
                                            {
                                                put("referenceId", referenceId);
                                                put("notificationId", requestNotification.getState().getNotificationId());
                                                put("assetId", assetId);
                                            }
                                        }))
                                        .flatMap(requestNotification -> {
                                            UUID notificationId = requestNotification.getState().getNotificationId();
                                            AlfrescoAssetSyncNotification syncNotification = new AlfrescoAssetSyncNotification()
                                                    .setNotificationId(notificationId)
                                                    .setReferenceId(referenceId)
                                                    .setCourseId(courseId)
                                                    .setAssetId(assetId)
                                                    .setSyncType(AlfrescoAssetSyncType.PUSH)
                                                    .setStatus(AlfrescoAssetSyncStatus.IN_PROGRESS);

                                             return alfrescoAssetTrackGateway.persist(syncNotification)
                                                    .then(alfrescoAssetTrackService.addNotificationId(referenceId, notificationId, AlfrescoAssetSyncType.PUSH))
                                                    .then(Mono.just(requestNotification))
                                                    .doOnEach(log.reactiveInfo(String.format("started asset push with notification id [%s]", notificationId)));
                                        });
                            })
                            .doOnError(e -> log.error(e.getMessage()));
                }).collectList();
    }

    /**
     * Use ext_http module apparatus to upload a remote Bronte S3 asset source to Alfresco.
     *
     * See https://pearsoneducationinc-my.sharepoint.com/:p:/r/personal/brian_weck_pearson_com/Documents/Engineering/Architecture%20Docs/aero%20External%20HTTP%20Push%20Events.pptx?d=w816a2f9112de42da94346832e5c512f8&csf=1&web=1&e=qlNLTi
     *
     * @param courseId bronte course id
     * @param assetId bronte asset id
     * @param bronteAssetUrl original bronte asset source url
     * @param myCloudToken myCloud auth token
     * @param alfrescoNodeUrl alfresco url to upload asset to
     * @param fileName filename to save in alfresco
     *
     * @return Request notification with relevant ids and rendered list of parameters used for request
     *
     */
     Mono<RequestNotification> pushAsset(UUID referenceId,
                                         String courseId,
                                         String assetId,
                                         String bronteAssetUrl,
                                         String myCloudToken,
                                         String alfrescoNodeUrl,
                                         String fileName,
                                         String altText,
                                         String longDesc,
                                         String workURN) {

        affirmArgument(referenceId != null, "referenceId is required");
        affirmArgument(!Strings.isNullOrEmpty(courseId), "courseId is required");
        affirmArgument(!Strings.isNullOrEmpty(assetId), "assetId is required");
        affirmArgument(!Strings.isNullOrEmpty(bronteAssetUrl), "bronteAssetUrl is required");
        affirmArgument(!Strings.isNullOrEmpty(myCloudToken), "myCloudToken is required");
        affirmArgument(!Strings.isNullOrEmpty(alfrescoNodeUrl), "alfrescoNodeUrl is required");
        affirmArgument(!Strings.isNullOrEmpty(fileName), "fileName is required");

        // prepare builder with common fields
        AlfrescoAssetPushRequestBuilder reqBuilder = new AlfrescoAssetPushRequestBuilder()
                .setUri(alfrescoNodeUrl)
                .setMyCloudToken(myCloudToken)
                .setRemoteAssetUri(bronteAssetUrl)
                .setFileName(fileName)
                .setCourseId(courseId)
                .setAssetId(assetId)
                .setAutoRename(true)
                .setAltText(altText)
                .setLongDesc(longDesc)
                .setWorkURN(workURN);

        return Mono
                // build request
                .just(reqBuilder.build())
                // send it
                .flatMap(request -> httpRequestService.submit(RequestPurpose.ALFRESCO_ASSET_PUSH, request, referenceId))
                .doOnEach(log.reactiveDebugSignal("sending Bronte asset to Alfresco"));
    }

    /**
     * Return current status of all assets pushed for an Alfresco-linked course.
     *
     * @param courseId course id to query assets for
     *
     * @return List of asset status
     *
     */
    public Mono<List<Map<String, String>>> pushAssetStatus(UUID courseId) {
        affirmArgument(courseId != null, "courseId is required");

        return coursewareAssetService.getAllSummariesForRootActivity(courseId)
                .filter(summary -> summary.getMediaType().equals(AssetMediaType.IMAGE))
                .filter(summary -> summary.getProvider().equals(AssetProvider.AERO) || summary.getProvider().equals(AssetProvider.ALFRESCO))
                .flatMap(summary -> { // process each item from Flux<AssetSummary>
                    UUID assetId = summary.getId();
                    AssetProvider provider = summary.getProvider();

                    Map<String, String> assetInfo = new HashMap<String, String>();
                    assetInfo.put("provider", provider.getLabel());

                    switch (provider) {
                        case AERO: {
                            return bronteAssetService.getAssetPayload(assetId)
                                    .flatMap(assetPayload -> {
                                        assetInfo.put("fileName", getFileName(assetPayload));
                                        return Mono.just(assetInfo);
                                    });
                        }
                        case ALFRESCO: {
                            return alfrescoAssetService.getAssetPayload(assetId)
                                    .flatMap(assetPayload -> {
                                        assetInfo.put("fileName", getFileName(assetPayload));
                                        return alfrescoAssetService.getAlfrescoAssetData(assetId)
                                                .flatMap(alfrescoAssetData -> {
                                                    assetInfo.put("alfrescoAssetId", "" + alfrescoAssetData.getAlfrescoId());
                                                    return Mono.just(assetInfo);
                                                });
                                    });
                        }
                        default: { // we should never get here b/c we filter the flux of summaries by provider
                            log.warn("ignoring \'" + provider + "\' asset \'" + assetId + "\'");
                            return Mono.empty();
                        }
                    }
                })
                .collectList();
    }

    /**
     * Return total distinct AERO image file count in the course
     *
     * @param courseId the course id
     *
     * @return total number of distinct AERO image files in the course
     */
    public Mono<Long> getAeroImageAssetCount(UUID courseId) {

        return coursewareAssetService.getAllSummariesForRootActivity(courseId)
                .filter(summary -> summary.getMediaType().equals(AssetMediaType.IMAGE) &&
                        summary.getProvider().equals(AssetProvider.AERO))
                .distinct()
                .count();
    }

    String getFileName(AssetPayload assetPayload) {
        final String FILENAME = "originalAssetName";

        // original filename or bronte asset id
        String fileName = "";
        Map<String, String> payloadMetadata = assetPayload.getMetadata();
        if (payloadMetadata.containsKey(FILENAME)) { // use filename from asset metadata
            fileName = payloadMetadata.get(FILENAME);
        } else { // use the asset id as the filename
            fileName = "asset id " + assetPayload.getAsset().getId() + " has no original file name";
        }

        return fileName;
    }
}
