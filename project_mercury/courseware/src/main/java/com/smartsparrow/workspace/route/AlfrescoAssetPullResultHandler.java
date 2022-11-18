package com.smartsparrow.workspace.route;

import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PULL;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.workspace.route.AlfrescoCoursewareRoute.ALFRESCO_NODE_PULL_RESULT_BODY;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.commons.collections4.map.HashedMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartsparrow.asset.data.AlfrescoImageNode;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.eventmessage.AlfrescoAssetPullEventMessage;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.asset.service.AssetUploadService;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.ext_http.service.ResultNotification;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.service.AlfrescoAssetResultBroker;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import com.smartsparrow.workspace.service.AlfrescoPullRequestParser;

import reactor.core.publisher.Mono;

public class AlfrescoAssetPullResultHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetPullResultHandler.class);

    private final CamelReactiveStreamsService camelReactiveStreamsService;
    private final AssetUploadService assetUploadService;
    private final BronteAssetService bronteAssetService;
    private final AlfrescoPullRequestParser alfrescoPullRequestParser;
    private final AlfrescoAssetService alfrescoAssetService;
    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final AlfrescoAssetResultBroker alfrescoAssetResultBroker;

    @Inject
    public AlfrescoAssetPullResultHandler(final CamelReactiveStreamsService camelReactiveStreamsService,
                                          final AssetUploadService assetUploadService,
                                          final BronteAssetService bronteAssetService,
                                          final AlfrescoPullRequestParser alfrescoPullRequestParser,
                                          final AlfrescoAssetService alfrescoAssetService,
                                          final AlfrescoAssetTrackService alfrescoAssetTrackService,
                                          final AlfrescoAssetResultBroker alfrescoAssetResultBroker) {
        this.camelReactiveStreamsService = camelReactiveStreamsService;
        this.assetUploadService = assetUploadService;
        this.bronteAssetService = bronteAssetService;
        this.alfrescoPullRequestParser = alfrescoPullRequestParser;
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
        ResultNotification resultNotification = exchange.getProperty(ALFRESCO_NODE_PULL_RESULT_BODY, ResultNotification.class);

        boolean hasError = false;

        // handle the result
        try {
            // parse the result notification and build the event message
            final AlfrescoAssetPullEventMessage eventMessage = alfrescoPullRequestParser.parse(resultNotification);

            // only perform the get content call if either an update is required or a
            // forceSync was requested
            if (eventMessage.requireUpdate() || eventMessage.isForceSync()) {
                // trigger the get content call
                Mono.just(eventMessage) //
                        .doOnEach(log.reactiveInfoSignal("Alfresco sync:  alfresco node content pull start"))
                        // perform the external call to pull the alfresco content
                        .map(event -> camelReactiveStreamsService.toStream(ALFRESCO_NODE_PULL, event, AlfrescoAssetPullEventMessage.class)) //
                        .flatMap(Mono::from)
                        .doOnEach(log.reactiveInfoSignal("Alfresco sync:  alfresco node content pull end"))
                        // fetch the summary to find the missing information
                        .flatMap(message -> bronteAssetService.getAssetSummary(message.getAssetId())
                                .doOnEach(log.reactiveInfoSignal("Alfresco sync: got asset summary"))
                                // save the alfresco image data
                                .flatMap(summary -> {
                                    //validating Alfresco node type before cast
                                    affirmArgument(message.getAlfrescoNode() instanceof AlfrescoImageNode, "Alfresco node is not image type");
                                    return alfrescoAssetService.saveAlfrescoImageData(summary.getId(), summary.getOwnerId(),
                                                                               (AlfrescoImageNode) message.getAlfrescoNode())
                                            .doOnEach(log.reactiveInfoSignal("Alfresco sync:  alfresco save asset done"))
                                            // update the bronte asset, this will trigger the optimizer as well
                                            .then(assetUploadService.save(summary.getId(), new AssetTemplate(
                                                    alfrescoPullRequestParser.getFileNameWithExtension(message.getAlfrescoNode()))
                                                    .setUrn(summary.getUrn())
                                                    .setInputStream(message.getInputStream())
                                                    .setMetadata(message.getMetadata())
                                                    .setOwnerId(summary.getOwnerId())
                                                    .setProvider(summary.getProvider())
                                                    .setSubscriptionId(summary.getSubscriptionId())
                                                    .setVisibility(summary.getVisibility())))
                                            .doOnEach(log.reactiveInfoSignal("Alfresco sync: asset upload service complete"));
                                }))
                        .doOnEach(log.reactiveInfoSignal("successfully pulled Alfresco asset to Bronte", assetPayload -> new HashedMap<String, Object>() {
                            {
                                put("referenceId", resultNotification.getState().getReferenceId());
                                put("notificationId", resultNotification.getState().getNotificationId());
                                put("assetId", assetPayload.getAsset().getId());
                            }
                        }))
                        .block();
            }

        } catch (JsonProcessingException e) {
            // log a statement and do nothing else for now
            log.jsonError("failed to pull the file content for Alfresco asset", new HashMap<String, Object>() {
                {
                    put("referenceId", resultNotification.getState().getReferenceId());
                    put("notificationId", resultNotification.getState().getNotificationId());
                }
            }, e);
            hasError = true;
        } finally {
            log.jsonInfo("alfresco pull result: finally", new HashMap<String, Object>() {
                {
                    put("notificationId", resultNotification.getState().getNotificationId());
                    put("referenceId", resultNotification.getState().getReferenceId());
                }
            });

            final AlfrescoAssetSyncStatus syncStatus = (hasError) ? AlfrescoAssetSyncStatus.FAILED : AlfrescoAssetSyncStatus.COMPLETED;

            alfrescoAssetTrackService.getSyncNotification(resultNotification.getState().getNotificationId())
                    .flatMap(syncNotification -> {
                        return alfrescoAssetTrackService.handleNotification(syncNotification, syncStatus, AlfrescoAssetSyncType.PULL)
                                .flatMap(isPullCompleted -> alfrescoAssetTrackService.handleSyncCompletion(syncNotification, isPullCompleted, AlfrescoAssetSyncType.PULL)
                                        .then(alfrescoAssetResultBroker.broadcast(syncNotification, isPullCompleted)));
                    })
                    .subscribe();
        }
    }
}
