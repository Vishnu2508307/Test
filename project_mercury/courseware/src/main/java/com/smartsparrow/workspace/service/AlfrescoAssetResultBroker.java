package com.smartsparrow.workspace.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus.COMPLETED;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate.ActivityAlfrescoAssetsUpdateRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncNotification;

import reactor.core.publisher.Mono;

@Singleton
public class AlfrescoAssetResultBroker {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetResultBroker.class);

    private final CoursewareService coursewareService;
    private final ActivityAlfrescoAssetsUpdateRTMProducer activityAlfrescoAssetsUpdateRTMProducer;

    @Inject
    public AlfrescoAssetResultBroker(final CoursewareService coursewareService,
                                     ActivityAlfrescoAssetsUpdateRTMProducer activityAlfrescoAssetsUpdateRTMProducer) {
        this.coursewareService = coursewareService;
        this.activityAlfrescoAssetsUpdateRTMProducer = activityAlfrescoAssetsUpdateRTMProducer;
    }

    /**
     * Broadcast to the export subscription when the mapping is completed and the ambrosia file is generated
     *
     * @param syncNotification the sync notification to trigger the broadcast for
     * @param isSyncComplete is the sync operation complete
     * @return a mono of export summary for the provided export id
     */
    public Mono<Void> broadcast(final AlfrescoAssetSyncNotification syncNotification, boolean isSyncComplete) {
        return coursewareService.getRootElementId(syncNotification.getCourseId(), ACTIVITY)
                .flatMap(rootElementId -> {
                    if (log.isDebugEnabled()) {
                        log.debug("AlfrescoAssetResultBroker broadcast: {}", rootElementId.toString());
                    }
                    activityAlfrescoAssetsUpdateRTMProducer.buildActivityAlfrescoAssetsUpdateRTMConsumable(rootElementId,
                                                                                                           syncNotification.getCourseId(),
                                                                                                           ACTIVITY,
                                                                                                           syncNotification.getAssetId(),
                                                                                                           syncNotification.getSyncType(),
                                                                                                           syncNotification.getStatus() == COMPLETED,
                                                                                                           isSyncComplete).produce();
                    return Mono.just(rootElementId);
                })
                .doOnEach(log.reactiveInfoSignal("alfresco asset result broker: broadcast",
                                                 ignored -> new HashedMap<String, Object>() {
                                                     {
                                                         put("notificationId", syncNotification.getNotificationId());
                                                         put("referenceId", syncNotification.getReferenceId());
                                                     }
                                                 }))
                .then(Mono.empty());
    }
}
