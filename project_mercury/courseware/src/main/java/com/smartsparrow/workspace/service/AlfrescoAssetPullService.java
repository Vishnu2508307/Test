package com.smartsparrow.workspace.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.courseware.service.CoursewareAssetService;
// import com.smartsparrow.ext_http.service.AlfrescoSyncTrackService;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.cms.AlfrescoAssetPullRequestBuilder;
import com.smartsparrow.workspace.cms.AlfrescoAssetPullRequestMessage;

import com.smartsparrow.workspace.data.AlfrescoAssetSyncNotification;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.data.AlfrescoAssetTrackGateway;
import org.apache.commons.collections4.map.HashedMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AlfrescoAssetPullService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetPullService.class);

    private final ExternalHttpRequestService externalHttpRequestService;
    private final AssetConfig assetConfig;
    private final CoursewareAssetService coursewareAssetService;
    private final AlfrescoAssetService alfrescoAssetService;
    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final AlfrescoAssetTrackGateway alfrescoAssetTrackGateway;

    @Inject
    public AlfrescoAssetPullService(final ExternalHttpRequestService externalHttpRequestService,
                                    final AssetConfig assetConfig,
                                    final CoursewareAssetService coursewareAssetService,
                                    final AlfrescoAssetService alfrescoAssetService,
                                    AlfrescoAssetTrackService alfrescoAssetTrackService,
                                    AlfrescoAssetTrackGateway alfrescoAssetTrackGateway) {
        this.externalHttpRequestService = externalHttpRequestService;
        this.assetConfig = assetConfig;
        this.coursewareAssetService = coursewareAssetService;
        this.alfrescoAssetService = alfrescoAssetService;
        this.alfrescoAssetTrackService = alfrescoAssetTrackService;
        this.alfrescoAssetTrackGateway = alfrescoAssetTrackGateway;
    }

    /**
     * Sync a single asset with alfresco by pulling the updated information
     *
     * @param referenceId the external request reference id
     * @param assetSummary the asset summary to sync
     * @param myCloudToken the myCloud authentication token
     * @return a mono of request notification
     */
    public Mono<RequestNotification> pullAsset(final UUID referenceId, final AssetSummary assetSummary,
                                               final String myCloudToken, final boolean forceSync) {
        affirmArgument(referenceId != null, "referenceId is required");
        affirmArgument(assetSummary != null, "assetSummary is required");
        affirmArgument(myCloudToken != null, "myCloudToken is required");
        affirmArgument(assetSummary.getProvider().equals(AssetProvider.ALFRESCO), "only ALFRESCO provider supported");

        // find the alfresco image node
        return alfrescoAssetService.getAlfrescoImageNode(assetSummary)
                .flatMap(alfrescoNode -> {
                    // build the message request
                    final AlfrescoAssetPullRequestMessage assetSyncRequestMessage = new AlfrescoAssetPullRequestMessage()
                            .setAssetId(assetSummary.getId())
                            .setOwnerId(assetSummary.getOwnerId())
                            .setVersion(alfrescoNode.getVersion())
                            .setLastModified(alfrescoNode.getLastModifiedDate())
                            .setAlfrescoNodeId(alfrescoNode.getAlfrescoId())
                            .setReferenceId(referenceId)
                            .setForceSync(forceSync);

                    // prepare the request builder
                    final AlfrescoAssetPullRequestBuilder requestBuilder = new AlfrescoAssetPullRequestBuilder()
                            .setUri(assetConfig.getAlfrescoUrl() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/")
                            .setMessage(assetSyncRequestMessage)
                            .setMyCloudToken(myCloudToken);

                    // submit the request
                    return externalHttpRequestService.submit(RequestPurpose.ALFRESCO_ASSET_PULL, requestBuilder.build(),
                            referenceId);
                })
                // log a debug line with the referenceId
                .doOnEach(log.reactiveDebugSignal("pulling asset", requestNotification -> new HashMap<String, Object>() {
                    {put("referenceId", referenceId);}
                    {put("assetId", assetSummary.getId());}
                }));
    }

    /**
     * For a rootElementId sync all the alfresco asset of type image with a pull action
     *
     * @param referenceId the reference id
     * @param rootElementId the root element id to find all the asset to sync for
     * @param myCloudToken the myCloud authentication token
     * @param forceSync will force to get the content from alfresco even when there are no updates
     * @return a flux of request notification
     */
    public Flux<RequestNotification> pullAssets(final UUID referenceId, final UUID rootElementId, final String myCloudToken,
                                                final boolean forceSync) {
        affirmArgument(referenceId != null, "referenceId is required");
        affirmArgument(rootElementId != null, "rootElementId is required");
        affirmArgument(myCloudToken != null, "myCloudToken is required");
        //TODO: Below filtering by Alfresco asset type will be enabled upon completion of BRNT-2792
        //return coursewareAssetService.getAssetsByRootActivityAndProvider(rootElementId, AssetProvider.ALFRESCO)
        return coursewareAssetService.getAllSummariesForRootActivity(rootElementId)
                // only sync images for now
                .filter(summary -> summary.getProvider().equals(AssetProvider.ALFRESCO) && summary.getMediaType().equals(AssetMediaType.IMAGE))
                // finally pull the asset
                .flatMap(assetSummary -> {
                    return pullAsset(referenceId, assetSummary, myCloudToken, forceSync)
                            .doOnEach(log.reactiveInfoSignal("started Alfresco asset pull to Bronte", requestNotification -> new HashedMap<String, Object>() {
                                {
                                    put("referenceId", referenceId);
                                    put("notificationId", requestNotification.getState().getNotificationId());
                                    put("assetId", assetSummary.getId());
                                }
                            }))
                            .map(requestNotification -> {
                                UUID notificationId = requestNotification.getState().getNotificationId();
                                AlfrescoAssetSyncNotification syncNotification = new AlfrescoAssetSyncNotification()
                                        .setNotificationId(notificationId)
                                        .setReferenceId(referenceId)
                                        .setCourseId(rootElementId)
                                        .setAssetId(assetSummary.getId())
                                        .setSyncType(AlfrescoAssetSyncType.PULL)
                                        .setStatus(AlfrescoAssetSyncStatus.IN_PROGRESS);

                                return alfrescoAssetTrackGateway.persist(syncNotification)
                                        .then(alfrescoAssetTrackService.addNotificationId(referenceId, notificationId, AlfrescoAssetSyncType.PULL))
                                        .then(Mono.just(requestNotification))
                                        .doOnEach(log.reactiveInfo(String.format("started asset pull with notification id [%s]", notificationId)))
                                        .block();
                            });
                })
                // log a debug line with the referenceId
                .doOnEach(log.reactiveDebugSignal("pulling all assets", requestNotification -> new HashMap<String, Object>() {
                    {put("referenceId", referenceId);}
                    {put("rootElementId", rootElementId);}
                }));
    }

}
