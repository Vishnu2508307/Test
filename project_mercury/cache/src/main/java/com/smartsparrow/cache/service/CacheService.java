package com.smartsparrow.cache.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;

import com.smartsparrow.cache.config.CacheConfig;
import com.smartsparrow.dataevent.eventmessage.EventMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Contains methods for working with Redis Cache.
 *
 * Methods prefixed with:
 *  - <code>map</code>: operate on redis Hash type, equivalent to <code>hget, hset, hdel</code> etc.
 */
@Singleton
public class CacheService {

    private final static MercuryLogger log = MercuryLoggerFactory.getLogger(CacheService.class);

    private final RedissonReactiveClient redissonReactiveClient;
    private final CacheConfig cacheConfig;

    @Inject
    public CacheService(RedissonReactiveClient redissonReactiveClient,
                        CacheConfig cacheConfig) {
        this.redissonReactiveClient = redissonReactiveClient;
        this.cacheConfig = cacheConfig;
    }

    /**
     * Retrieves a value from external redis cache.
     *
     * If cache.isEnableLearnerCache config setting is false, bypasses the whole cache system and just returns
     * the same publisher mono.
     *
     * If cache.isEnableLearnerCache config setting is true, queries redis for the provided cacheKey key. If key is
     * not found in redis, calls the {@link CacheService#set(String, Mono, long, TimeUnit)} method to store the value in
     * redis and then returns it back to caller
     *
     * It this signature is used instead of the one that specifies time to live, TTL will default to 1 hour.
     *
     * @param cacheKey the redis key of cached value
     * @param type type of cached value, must match the type of mono in publisher
     * @param publisher Mono that emits the value that should be cached if not cached already
     * @return either the cached value matching cacheKey key or publisher if it wasn't cached. if publisher emits no items,
     * returns Mono.empty()
     *
     */
    public <T> Mono<T> computeIfAbsent(String cacheKey, Class<T> type, Mono<T> publisher) {
        return computeIfAbsent(cacheKey, type, publisher, 1, TimeUnit.HOURS);
    }

    /**
     * Retrieves a value from external redis cache.
     *
     * If cache.isEnableLearnerCache config setting is false, bypasses the whole cache system and just returns
     * the same publisher mono.
     *
     * If cache.isEnableLearnerCache config setting is true, queries redis for the provided cacheKey key. If key is
     * not found in redis, calls the {@link CacheService#set(String, Mono, long, TimeUnit)} method to store the
     * value in redis and then returns it back to caller
     *
     *
     * @param cacheKey the redis key of cached value
     * @param type type of cached value, must match the type of mono in publisher
     * @param publisher Mono that emits the value that should be cached if not cached already
     * @param ttl time to live in cache
     * @param timeUnit unit for time to live value
     * @return either the cached value matching cacheKey key or publisher if it wasn't cached. if publisher emits no items,
     * returns Mono.empty()
     *
     */
    public <T> Mono<T> computeIfAbsent(String cacheKey, Class<T> type, Mono<T> publisher, long ttl, TimeUnit timeUnit) {

        if(cacheConfig.isEnableLearnerCache()) {
            // Get redis bucket for given cacheKey key
            RBucketReactive<CachedMonoWrapper<T>> bucket = redissonReactiveClient.getBucket(cacheKey);

            return bucket.get()
                    // get the wrapped value from redis
                    .flatMap(cached -> {
                        // if publisher originally emitted no items, wrapped cached value is an empty husk to avoid
                        // cache misses
                        if (cached.isEmptyValue()) {
                            return Mono.empty();
                        }
                        // if there was a value, return it in a new Mono
                        return Mono.just(cached.getValue());
                    })
                    // no cached items match the cacheKey key, so cache it from publisher emitted item
                    .switchIfEmpty(set(cacheKey, publisher, ttl, timeUnit))
                    // cast result back to original type
                    .map(type::cast)
                    // if casting serialized value from cache back to expected type fails, it means the cache might
                    // be stale and the class type for a given key changed, so clear the cache and return the original
                    // publisher without interrupting the chain
                    .doOnError(e -> {
                        log.warn("failed to deserialize {} to {} - cleaning it from cache. Message: {}",
                                cacheKey, type.getName(), e.getMessage());
                        bucket.delete().subscribe();
                    })
                    .onErrorResume(e -> publisher);
        } else {
            // if caching is disabled, just return publisher back to caller.
            return publisher;
        }
    }

    /**
     * Stores item emitted by value and returns it back to caller
     * @param cacheKey the redis key to store the value under
     * @param value mono that emits item that will be cached
     * @param ttl time to live in cache
     * @param timeUnit unit for time to live value
     * @return same as value parameter
     *
     */
    public <T> Mono<T> set(String cacheKey, Mono<T> value, long ttl, TimeUnit timeUnit) {

        // Get redis bucket for given cacheKey key
        RBucketReactive<CachedMonoWrapper<T>> bucket = redissonReactiveClient.getBucket(cacheKey);

        // turns value into a hot publisher. Necessary so it can be subscribed so the value can be cached,
        // but the original caller still needs to subscribe too
        return value.cache()
                // check if it emits anything
                .hasElement()
                .doOnNext(hasElement -> {

                    // prep a wrapper for the value
                    CachedMonoWrapper<T> wrap = new CachedMonoWrapper<T>();

                    if(hasElement) {
                        // if it emits something, subscribe to loader value and store emitted item in redis, wrapped
                        // in a MonoCached instance
                        value.subscribe(v ->
                                bucket.set(wrap.setValue(v).setEmptyValue(false), ttl, timeUnit)
                                        .subscribe()
                        );
                    } else {
                        // If loader value doesn't emit anything, store an empty wrapper to avoid further cache misses
                        bucket.set(wrap.setEmptyValue(true), ttl, timeUnit)
                                .subscribe();
                    }

                })
                // once caching work is done, return the original mono so caller can subscribe too
                .then(value);
    }

    /**
     * Deletes a value from external redis cache.
     *
     * If cache.isEnableLearnerCache config setting is false, bypasses the whole cache system and just returns.
     *
     * If cache.isEnableLearnerCache config setting is true, queries redis for the provided cacheKey key. If key is
     * not found in redis, just returns
     *
     * @param cacheKey the redis key of cached value
     * @return true if successful, else false
     * returns Mono.empty()
     *
     */
    public Mono<Boolean> clearIfPresent(String cacheKey) {
        AtomicBoolean error = new AtomicBoolean(false);

        if (cacheConfig.isEnableLearnerCache()) {
            // Get redis bucket for given cacheKey key
            List<RBucketReactive<CachedMonoWrapper<Object>>> buckets = redissonReactiveClient.findBuckets(cacheKey);

            buckets.forEach(bucket -> {
                bucket.get()
                        // get the wrapped value from redis
                        .flatMap(cached -> {
                            // Delete the cached key value pair
                            return bucket.delete();
                        })
                        .thenReturn(Mono.just(true))
                        .doOnError(e -> {
                            log.warn("failed to delete entries from cache for deployment: {}. Message: {}",
                                     cacheKey, e.getMessage());
                            error.set(true);
                        }).subscribe();
            });
        }
        if (error.get()) {
            return Mono.just(false);
        }
        return Mono.just(true);

    }

    /**
     * Checks if cached Hash contains a value for the key in a given map associated to keyspace
     *
     * @param keySpace the hash keyspace name
     * @param key the key
     * @return <code>true</code> if map contains given key
     */
    public Mono<Boolean> mapContainsKey(String keySpace, String key) {
        return Mono.from(redissonReactiveClient.getMapCache(keySpace).containsKey(key));
    }

    /**
     * Fetches a value from cache for the key from the map associated to keyspace
     * @param keySpace the hash keyspace
     * @param key the key
     * @return Mono with a value, returns empty Mono if value is not stored in cache
     */
    public Mono<String> mapGet(String keySpace, String key) {
        RMapCacheReactive<String, String> mapCache = redissonReactiveClient.getMapCache(keySpace);
        return Mono.from(mapCache.get(key));
    }

    /**
     * Puts the value into the cache for the key and keyspace
     * @param keySpace the hash keyspace name
     * @param key the key
     * @param value the value
     * @return <code>true</code> if key is a new key in cache and value was set.
     *         <code>false</code> if key already exists in cache and the value was updated
     */
    public Mono<Boolean> mapPut(String keySpace, String key, String value) {
        return mapPut(keySpace, key, value, 0);
    }

    /**
     * Puts the value into the cache for the key and keyspace
     * @param keySpace the hash keyspace name
     * @param key the key
     * @param value the value cannot be null
     * @param ttl time to live for key/value entry in minutes
     *              If <code>0</code> then stores infinitely
     * @return <code>true</code> if key is a new key in cache and value was set
     *         <code>false</code> if key already exists in cache and the value was updated
     */
    public Mono<Boolean> mapPut(String keySpace, String key, String value, long ttl) {
        checkNotNull(value);
        if (log.isDebugEnabled()) {
            log.debug("Caching key='{}' value='{}' for keyspace='{}' with ttl={}", key, value, keySpace, ttl);
        }
        return Mono.from(redissonReactiveClient.getMapCache(keySpace).fastPut(key, value, ttl, TimeUnit.MINUTES));
    }

    /**
     * General method to publish {@link EventMessage} to redis topic
     *
     * This method is also used by Camel to publish events from the routes, hence the @Handler and @Body annotations
     *
     * @param event EventMessage received from dataevents
     */
    @Handler
    public void publishEvents(@Body EventMessage event) {

        RTopicReactive topic = redissonReactiveClient.getTopic(event.getName());

        // publish the whole event message obj to redis and log how many subscribers got it
        topic.publish(event) //
                .publishOn(Schedulers.elastic())
                .subscribe(i -> log.debug("Producer clientId {} published to channel: {}, clients: {}",
                        event.getProducingClientId(), event.getName(), i));

    }

}
