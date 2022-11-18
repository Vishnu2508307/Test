package com.smartsparrow.export.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RedissonReactiveClient;

import reactor.core.publisher.Mono;

public class ExportTrackServiceTest {

    @InjectMocks
    private ExportTrackService exportTrackService;
    @Mock
    RedissonReactiveClient redissonReactiveClient;
    @Mock
    RSetCacheReactive<UUID> redissonSet;


    private static final UUID exportId = UUID.randomUUID();
    UUID notificationIdOne = UUID.randomUUID();
    UUID notificationIdTwo = UUID.randomUUID();
    String exportCacheName = String.format("export:%s", exportId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(redissonReactiveClient.getSetCache(any())).thenReturn((RSetCacheReactive) redissonSet);
        when(redissonSet.add(notificationIdOne, 1800, TimeUnit.SECONDS)).thenReturn(Mono.just(true));
    }

    @Test
    public void add() {
        exportTrackService.add(notificationIdOne, exportId).block();
        verify(redissonReactiveClient).getSetCache(exportCacheName);
        verify(redissonSet).add(notificationIdOne, 1800, TimeUnit.SECONDS);
    }

    @Test
    public void remove() {
        when(redissonSet.add(notificationIdTwo)).thenReturn(Mono.just(true));
        when(redissonSet.contains(notificationIdTwo)).thenReturn(Mono.just(true));
        when(redissonSet.remove(notificationIdTwo)).thenReturn(Mono.just(true));
        exportTrackService.remove(notificationIdTwo, exportId).block();
        verify(redissonReactiveClient).getSetCache(exportCacheName);
        verify(redissonSet).remove(notificationIdTwo);
    }
}
