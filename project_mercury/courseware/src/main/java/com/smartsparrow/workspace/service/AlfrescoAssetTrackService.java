package com.smartsparrow.workspace.service;

import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncNotification;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncSummary;
import com.smartsparrow.workspace.data.AlfrescoAssetTrackGateway;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RedissonReactiveClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class AlfrescoAssetTrackService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetTrackService.class);

    private final RedissonReactiveClient redissonReactiveClient;
    private final AlfrescoAssetTrackGateway alfrescoAssetTrackGateway;

    @Inject
    public AlfrescoAssetTrackService(RedissonReactiveClient redissonReactiveClient,
                                     AlfrescoAssetTrackGateway alfrescoAssetTrackGateway) {
        this.redissonReactiveClient = redissonReactiveClient;
        this.alfrescoAssetTrackGateway = alfrescoAssetTrackGateway;
    }

    /**
     * fetch the map of reference id by course id
     *
     * @param syncType the alfresco sync type
     * @return mono of RMapCacheReactive
     */
    private RMapCacheReactive<UUID, UUID> courseReferenceMap(AlfrescoAssetSyncType syncType) {
        String syncCacheName = String.format("alfresco:course2reference:map:%s", syncType.name().toLowerCase());
        return redissonReactiveClient.getMapCache(syncCacheName);
    }

    /**
     * Method to set an alfresco sync reference id per course id, but only if an association doesn't already exist.
     *
     * @param courseId       the course activity id
     * @param referenceId    the alfresco sync reference id
     * @param syncType       the alfresco sync type
     * @return mono that emits the reference id associated to the course id
     */
    public Mono<UUID> setReferenceId(UUID courseId, UUID referenceId, AlfrescoAssetSyncType syncType) {
        return courseReferenceMap(syncType).get(courseId)
                .defaultIfEmpty(referenceId)
                .flatMap(currValue -> {
                    if (currValue != referenceId) {
                        return Mono.just(currValue)
                                .doOnEach(log.reactiveDebugSignal(String.format(
                                        "Course id [%s] already has an associated reference id [%s], returning it instead",
                                        courseId, currValue)));
                    }
                    return courseReferenceMap(syncType).put(courseId, referenceId, 3600, TimeUnit.SECONDS)
                            .thenReturn(referenceId);
                });
    }

    /**
     * Method to remove alfresco sync reference id per course id
     *
     * @param courseId       the course activity id
     * @param syncType       the alfresco sync type
     * @return void mono
     */
    public Mono<Void> removeReferenceId(UUID courseId, AlfrescoAssetSyncType syncType) {
        return courseReferenceMap(syncType).remove(courseId)
                .then(Mono.empty());
    }

    /**
     * fetch the set of notifications by sync reference id
     *
     * @param referenceId the sync reference id
     * @return mono of RSetReactive
     */
    private RSetCacheReactive<UUID> notificationSet(UUID referenceId, AlfrescoAssetSyncType syncType) {
        String syncCacheName = String.format("alfresco:notification:set:%s:%s", syncType.name().toLowerCase(), referenceId);
        return redissonReactiveClient.getSetCache(syncCacheName);
    }

    /**
     * Method to add notification id for each alfresco asset process request
     *
     * @param referenceId    the alfresco sync reference id
     * @param notificationId the notification id
     * @param syncType       the alfresco sync type
     * @return mono that emits true if value was added or false if it was already present
     */
    public Mono<Boolean> addNotificationId(UUID referenceId, UUID notificationId, AlfrescoAssetSyncType syncType) {
        return notificationSet(referenceId, syncType)
                .add(notificationId, 3600, TimeUnit.SECONDS) // a single asset should not take more than this to process
                .doOnNext(isAdded -> {
                    if (!isAdded) {
                        log.reactiveDebugSignal(String.format(
                                "Notification id [%s] was already present in redis set, ignoring",
                                notificationId));
                    }
                });

    }

    /**
     * Method to remove notification from created redis set
     *
     * @param referenceId    the alfresco sync reference id
     * @param notificationId the notification id
     * @param syncType       the alfresco sync type
     * @return mono of RSetReactive
     */
    public Mono<Boolean> removeNotificationId(UUID referenceId, UUID notificationId, AlfrescoAssetSyncType syncType) {
        return notificationSet(referenceId, syncType)
                .remove(notificationId);
    }

    /**
     * Checks whether the mapping processing is completed by looking at how many notification ids are still stored
     * in the set for an alfresco sync reference id.
     *
     * @param referenceId the alfresco sync reference id to check the mapping completion for.
     * @param syncType    the alfresco sync type
     * @return true when the set is empty
     */
    public Mono<Boolean> isCompleted(final UUID referenceId, AlfrescoAssetSyncType syncType) {
        return notificationSet(referenceId, syncType)
                .size()
                .map(s -> s == 0);
    }

    /**
     * Checks whether the mapping processing is completed by looking at how many notification ids are still stored
     * in the set for an alfresco sync reference id.
     *
     * @param syncNotification the alfresco sync reference id to check the mapping completion for.
     * @param syncStatus       the alfresco sync type
     * @param syncType         the alfresco sync type
     * @return true when the sync process is completed
     */
    public Mono<Boolean> handleNotification(AlfrescoAssetSyncNotification syncNotification, AlfrescoAssetSyncStatus syncStatus, AlfrescoAssetSyncType syncType) {
        syncNotification.setStatus(syncStatus).setCompletedAt(UUIDs.timeBased());

        return alfrescoAssetTrackGateway.persist(syncNotification)
                .then(removeNotificationId(syncNotification.getReferenceId(), syncNotification.getNotificationId(), syncType))
                .then(isCompleted(syncNotification.getReferenceId(), syncType));
    }

    /**
     * Checks whether the mapping processing is completed by looking at how many notification ids are still stored
     * in the set for an alfresco sync reference id.
     *
     * @param syncNotification the alfresco sync reference id to check the mapping completion for.
     * @param isSyncComplete   is alfresco sync process completed
     * @param syncType         the alfresco sync type
     * @return true when the sync process is completed
     */
    public Mono<Boolean> handleSyncCompletion(AlfrescoAssetSyncNotification syncNotification, boolean isSyncComplete, AlfrescoAssetSyncType syncType) {
        if (isSyncComplete) {
            return alfrescoAssetTrackGateway.findSyncSummary(syncNotification.getReferenceId())
                    .flatMap(syncSummary -> {
                        // upon completion, the sync summary is always marked as completed, even if some/all notifications fail
                        syncSummary.setStatus(AlfrescoAssetSyncStatus.COMPLETED).setCompletedAt(UUIDs.timeBased());
                        return alfrescoAssetTrackGateway.persist(syncSummary)
                                .then(removeReferenceId(syncNotification.getCourseId(), syncType))
                                .then(Mono.just(true));
                    });
        }

        return Mono.just(false);
    }

    /**
     * Save the alfresco sync summary details
     *
     * @param summary alfresco sync summary.
     */
    public Flux<Void> saveAlfrescoAssetSyncSummary(AlfrescoAssetSyncSummary summary) {
        return alfrescoAssetTrackGateway.persist(summary);
    }

    /**
     * Find the alfresco sync notification info by id
     *
     * @param notificationId the reference id to find the summary for
     * @return a mono of alfresco sync notification
     */
    public Mono<AlfrescoAssetSyncNotification> getSyncNotification(final UUID notificationId) {
        return alfrescoAssetTrackGateway.findSyncNotification(notificationId);
    }

    /**
     * Is an Alfresco sync operation for a course in progress
     *
     * @param courseId course id to check.
     * @param syncType the type of sync operation.
     * @return a true if the specified sync operation for the course is in progress
     */
    public Mono<Boolean> isSyncInProgress(UUID courseId, AlfrescoAssetSyncType syncType) {
        return courseReferenceMap(syncType).containsKey(courseId);
    }

}
