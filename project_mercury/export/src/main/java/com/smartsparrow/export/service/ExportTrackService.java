package com.smartsparrow.export.service;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import org.redisson.api.RAtomicLongReactive;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RedissonReactiveClient;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class ExportTrackService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportTrackService.class);

    private final RedissonReactiveClient redissonReactiveClient;


    @Inject
    public ExportTrackService(RedissonReactiveClient redissonReactiveClient) {
        this.redissonReactiveClient = redissonReactiveClient;
    }

    /**
     * fetch the set of notifications by export id
     *
     * @param exportId the export id
     * @return mono of RSetReactive
     */
    public RSetCacheReactive<UUID> get(final UUID exportId) {
        String exportCacheName = String.format("export:%s", exportId);
        return redissonReactiveClient.getSetCache(exportCacheName);
    }

    /**
     * Method to add notification id for each courseware element
     *
     * @param notificationId the notification id
     * @param exportId       the export id
     * @return mono that emits true if value was added or false if it was already present
     */
    @Trace(async = true)
    public Mono<Boolean> add(final UUID notificationId, final UUID exportId) {
        return get(exportId)
                    .add(notificationId, 1800, TimeUnit.SECONDS)
                    .doOnNext(isAdded -> {
                        log.jsonInfo("Export: Adding adding notificationId to redis", new HashMap<String, Object>() {
                            {put("notificationId", notificationId);}
                            {put("exportId", exportId);}
                            {put("isAdded", isAdded);}
                        });

                        if (!isAdded) {
                            log.reactiveDebugSignal(String.format(
                                    "Notification id [%s] was already present in redis set, ignoring",
                                    notificationId));
                        }
                    })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Method to remove notification from created redis set
     *
     * @param notificationId the notification id
     * @param exportId       the export id
     * @return mono of RSetReactive
     */
    public Mono<Boolean> remove(final UUID notificationId, final UUID exportId) {
        return get(exportId)
                .remove(notificationId);
    }

    /**
     * Checks whether the mapping processing is completed by looking at how many notification ids are still stored
     * in the set for an export id.
     *
     * @param exportId the exportId to check the mapping completion for.
     * @return true when the set is empty
     */
    public Mono<Boolean> isCompleted(final UUID exportId) {
        return get(exportId)
                .size()
                .map(s -> s == 0)
                .flatMap(isListEmpty -> {
                    // prevent race condition of multiple threads/servers calling isCompleted(...) when the tracking list is empty
                    if (isListEmpty) {
                        RAtomicLongReactive redisIsCompleted = redissonReactiveClient.getAtomicLong(exportId + ":is_completed"); // initializes to 0
                        return redisIsCompleted.compareAndSet(0, 1); // returns true if was 0 and set to 1
                    } else {
                        return Mono.just(false);
                    }
                });
    }
}
