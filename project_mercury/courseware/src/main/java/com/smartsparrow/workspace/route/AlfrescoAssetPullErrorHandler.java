package com.smartsparrow.workspace.route;

import com.smartsparrow.ext_http.service.ErrorNotification;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.service.AlfrescoAssetResultBroker;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.commons.collections4.map.HashedMap;

import javax.inject.Inject;

import static com.smartsparrow.workspace.route.AlfrescoCoursewareRoute.ALFRESCO_NODE_PULL_ERROR_BODY;

public class AlfrescoAssetPullErrorHandler {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetPullErrorHandler.class);

    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final AlfrescoAssetResultBroker alfrescoAssetResultBroker;

    @Inject
    public AlfrescoAssetPullErrorHandler(final AlfrescoAssetTrackService alfrescoAssetTrackService,
                                         final AlfrescoAssetResultBroker alfrescoAssetResultBroker) {
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
        // get the error notification
        ErrorNotification errorNotification = exchange.getProperty(ALFRESCO_NODE_PULL_ERROR_BODY, ErrorNotification.class);

        alfrescoAssetTrackService.getSyncNotification(errorNotification.getState().getNotificationId())
                .doOnEach(log.reactiveInfoSignal("failed to pull Alfresco asset to Bronte", syncNotification -> new HashedMap<String, Object>() {
                    {
                        put("referenceId", syncNotification.getReferenceId());
                        put("notificationId", syncNotification.getNotificationId());
                        put("assetId", syncNotification.getAssetId());
                    }
                }))
                .flatMap(syncNotification -> {
                    return alfrescoAssetTrackService.handleNotification(syncNotification, AlfrescoAssetSyncStatus.FAILED, AlfrescoAssetSyncType.PULL)
                            .map(isPullCompleted -> alfrescoAssetTrackService.handleSyncCompletion(syncNotification, isPullCompleted, AlfrescoAssetSyncType.PULL)
                                    .then(alfrescoAssetResultBroker.broadcast(syncNotification, isPullCompleted)));
                })
                .doOnEach(log.reactiveInfoSignal("alfresco asset pull error handler: broadcast", ignored -> new HashedMap<String, Object>() {
                    {
                        put("notificationId", errorNotification.getState().getNotificationId());
                        put("referenceId", errorNotification.getState().getReferenceId());
                        put("error", errorNotification.getError());
                    }
                }))
                .subscribe();
    }
}
