package com.smartsparrow.cache.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;

import com.smartsparrow.cache.config.CacheConfig;
import com.smartsparrow.dataevent.eventmessage.EventMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CacheServiceTest {

    @Mock
    private RedissonReactiveClient redissonReactiveClient;

    @Mock
    private CacheConfig cacheConfig;

    @InjectMocks
    private CacheService cacheService;
    @Mock
    private RMapCacheReactive<String, String> mapCache;

    private static final String KEYSPACE = "keyspace";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String NULL_KEY = "key1";
    private static final String NULL_VALUE = "NULL";
    private static final String NO_KEY = "key2";

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(redissonReactiveClient.getMapCache(eq(KEYSPACE))).thenReturn((RMapCacheReactive)mapCache);
        when(mapCache.get(KEY)).thenReturn(Mono.just(VALUE));
        when(mapCache.containsKey(KEY)).thenReturn(Mono.just(true));
        when(mapCache.get(NULL_KEY)).thenReturn(Mono.just(NULL_VALUE));
        when(mapCache.containsKey(NULL_KEY)).thenReturn(Mono.just(true));
        when(mapCache.get(NO_KEY)).thenReturn(Mono.empty());
        when(mapCache.containsKey(NO_KEY)).thenReturn(Mono.just(false));
    }

    @Test
    void computeIfAbsent_hit() {
        EventMessage<String> event = getEventMessage();
        RBucketReactive bucket = mock(RBucketReactive.class);

        when(cacheConfig.isEnableLearnerCache()).thenReturn(true);
        when(redissonReactiveClient.getBucket(event.getName())).thenReturn(bucket);

        Mono<CachedMonoWrapper> cachedValue = Mono.just(new CachedMonoWrapper().setValue(event)
                .setEmptyValue(false));
        when(bucket.get()).thenReturn(cachedValue);

        Mono<EventMessage> value = cacheService.computeIfAbsent(event.getName(), EventMessage.class, Mono.just(event));

        EventMessage res = value.block();
        assertEquals(event, res);

        verify(bucket, never()).set(any(),anyLong(),any());
    }

    @Test
    void computeIfAbsent_emptyHit() {
        EventMessage<String> event = getEventMessage();
        RBucketReactive bucket = mock(RBucketReactive.class);

        when(cacheConfig.isEnableLearnerCache()).thenReturn(true);
        when(redissonReactiveClient.getBucket(event.getName())).thenReturn(bucket);

        Mono<CachedMonoWrapper> cachedValue = Mono.empty();
        when(bucket.get()).thenReturn(cachedValue);

        Mono<EventMessage> value = cacheService.computeIfAbsent(event.getName(), EventMessage.class, Mono.empty());

        boolean res = Boolean.TRUE.equals(value.hasElement().block());
        assertFalse(res);

        // FixMe: this is actually a bug that slipped original implementation.
        // Address in https://agile-jira.pearson.com/browse/BRNT-10072
        // It should not call set() in this path where an an empty wrapper was already found, but it does because
        //
        //                         if (cached.isEmptyValue()) {
        //                            return Mono.empty();
        //                        }
        // happens just above in the chain.
        // Preferably eliminate all this CachedWrapper nonsense and let misses be misses, then we can find
        // the spurious queries and fix those instead.
        verify(bucket, times(1)).set(any(),anyLong(),any());

    }

    @Test
    void computeIfAbsent_miss() {
        EventMessage<String> event = getEventMessage();
        RBucketReactive bucket = mock(RBucketReactive.class);

        when(cacheConfig.isEnableLearnerCache()).thenReturn(true);
        when(redissonReactiveClient.getBucket(event.getName())).thenReturn(bucket);
        when(bucket.set(any(), anyLong(), any())).thenReturn(Mono.empty());


                Mono<CachedMonoWrapper> cachedValue = Mono.empty();
        when(bucket.get()).thenReturn(cachedValue);

        Mono<EventMessage> value = cacheService.computeIfAbsent(event.getName(), EventMessage.class, Mono.just(event));

        EventMessage res = value.block();
        assertEquals(event, res);

        verify(bucket).set(any(),anyLong(),any());
    }

    @Test
    void computeIfAbsent_deserializationFail() {
        EventMessage<String> event = getEventMessage();
        RBucketReactive bucket = mock(RBucketReactive.class);
        when(bucket.delete()).thenReturn(Mono.empty());

        when(cacheConfig.isEnableLearnerCache()).thenReturn(true);
        when(redissonReactiveClient.getBucket(event.getName())).thenReturn(bucket);

        when(bucket.get()).thenReturn(Mono.error(new RuntimeException("Massive Failure!!!")));

        Mono<EventMessage> value = cacheService.computeIfAbsent(event.getName(), EventMessage.class, Mono.just(event));

        EventMessage res = value.block();
        assertEquals(event, res);

        verify(bucket).delete();
    }
    @Test
    void mapContainsKey() {
        Boolean result = cacheService.mapContainsKey(KEYSPACE, KEY).block();
        assertEquals(true, result);
    }

    @Test
    void mapContainsKeyNoValue() {
        Boolean result = cacheService.mapContainsKey(KEYSPACE, NO_KEY).block();
        assertEquals(false, result);
    }

    @Test
    void mapGet() {
        String result = cacheService.mapGet(KEYSPACE, KEY).block();
        assertEquals(VALUE, result);
    }

    @Test
    void mapGetNullValue() {
        String result = cacheService.mapGet(KEYSPACE, NULL_KEY).block();
        assertEquals(NULL_VALUE, result);
    }

    @Test
    void mapGetNotExistedValue() {
        String result = cacheService.mapGet(KEYSPACE, NO_KEY).block();
        assertEquals(null, result);
    }

    @Test
    void mapPut() {
        when(mapCache.fastPut("new_key", VALUE, 0, TimeUnit.MINUTES)).thenReturn(Mono.just(true));

        Boolean result = cacheService.mapPut(KEYSPACE, "new_key", VALUE).block();

        verify(mapCache).fastPut("new_key", VALUE, 0, TimeUnit.MINUTES);
        assertEquals(true, result);
    }

    @Test
    void mapPutWithTTL() {
        when(mapCache.fastPut(KEY, VALUE, 10, TimeUnit.MINUTES)).thenReturn(Mono.just(false));

        Boolean result = cacheService.mapPut(KEYSPACE, KEY, VALUE, 10).block();

        verify(mapCache).fastPut(KEY, VALUE, 10, TimeUnit.MINUTES);
        assertEquals(false, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void publishEvents() {

        EventMessage<String> event = getEventMessage();
        TestPublisher publisher = TestPublisher.create();
        RTopicReactive rTopic = mock(RTopicReactive.class);
        when(redissonReactiveClient.getTopic("some.redis.topic/router/someId")).thenReturn(rTopic);
        when(rTopic.publish(event)).thenReturn(publisher.mono());

        cacheService.publishEvents(event);

        publisher.assertSubscribers(1);
        publisher.emit(1L);
        publisher.assertNoSubscribers();
    }

    // Utils
    private EventMessage<String> getEventMessage() {
        return new EventMessage<String>() {

            private String channelName = "some.redis.topic/router/someId";

            @Override
            public String getName() {
                return channelName;
            }

            @Override
            public String getProducingClientId() {
                return "camel-test";
            }

            @Override
            public String getContent() {
                return "Content";
            }

            @Override
            public String buildChannelName(String s) {
                return channelName;
            }
        };

    }

}
