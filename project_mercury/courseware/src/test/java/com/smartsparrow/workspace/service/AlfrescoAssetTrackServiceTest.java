package com.smartsparrow.workspace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlfrescoAssetTrackServiceTest {

    @InjectMocks
    private AlfrescoAssetTrackService alfrescoAssetTrackService;

    @Mock
    private RedissonReactiveClient redissonReactiveClient;

    @Mock
    private RMapCacheReactive<UUID, UUID> redissonMap;

    @Mock
    private RSetCacheReactive<UUID> redissonSet;

    private AlfrescoAssetSyncType syncType =  AlfrescoAssetSyncType.PUSH;
    private UUID courseId = UUID.randomUUID();
    private UUID referenceId = UUID.randomUUID();
    private UUID notificationId = UUID.randomUUID();
    private String mapCacheName = String.format("alfresco:course2reference:map:%s", syncType.name().toLowerCase());
    private String setCacheName = String.format("alfresco:notification:set:%s:%s", syncType.name().toLowerCase(), referenceId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(redissonReactiveClient.getMapCache(any())).thenReturn((RMapCacheReactive) redissonMap);
        when(redissonMap.put(courseId, referenceId, 3600, TimeUnit.SECONDS)).thenReturn(Mono.just(referenceId));

        when(redissonReactiveClient.getSetCache(any())).thenReturn((RSetCacheReactive) redissonSet);
        when(redissonSet.add(notificationId, 3600, TimeUnit.SECONDS)).thenReturn(Mono.just(true));
    }

    @Test
    public void setReferenceId() {
        when(redissonMap.get(courseId)).thenReturn(Mono.empty());
        alfrescoAssetTrackService.setReferenceId(courseId, referenceId, syncType).block();
        verify(redissonReactiveClient, times(2)).getMapCache(mapCacheName);
        verify(redissonMap).put(courseId, referenceId, 3600, TimeUnit.SECONDS);
    }

    @Test
    public void removeReferenceId() {
        when(redissonMap.put(courseId, referenceId)).thenReturn(Mono.empty());
        when(redissonMap.containsKey(courseId)).thenReturn(Mono.just(true));
        when(redissonMap.remove(courseId)).thenReturn(Mono.just(referenceId));
        alfrescoAssetTrackService.removeReferenceId(courseId, syncType).block();
        verify(redissonReactiveClient).getMapCache(mapCacheName);
        verify(redissonMap).remove(courseId);
    }

    @Test
    public void addNotificationId() {
        alfrescoAssetTrackService.addNotificationId(referenceId, notificationId, syncType).block();
        verify(redissonReactiveClient).getSetCache(setCacheName);
        verify(redissonSet).add(notificationId, 3600, TimeUnit.SECONDS);
    }

    @Test
    public void removeNotificationId() {
        when(redissonSet.add(notificationId)).thenReturn(Mono.just(true));
        when(redissonSet.contains(notificationId)).thenReturn(Mono.just(true));
        when(redissonSet.remove(notificationId)).thenReturn(Mono.just(true));
        alfrescoAssetTrackService.removeNotificationId(referenceId, notificationId, syncType).block();
        verify(redissonReactiveClient).getSetCache(setCacheName);
        verify(redissonSet).remove(notificationId);
    }

}
