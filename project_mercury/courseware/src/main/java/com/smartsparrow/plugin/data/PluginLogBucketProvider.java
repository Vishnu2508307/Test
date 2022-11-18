package com.smartsparrow.plugin.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.wiring.BucketConfig;
import com.smartsparrow.plugin.wiring.PluginLogConfig;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * The main purpose of this service is to return a BucketCollection object based on the PluginLogContext provided.
 */
@Singleton
public class PluginLogBucketProvider implements BucketProvider<PluginLogContext, BucketCollection> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(PluginLogBucketProvider.class);

    public static final String CACHE_NAME = "plugin:log";
    public static final String GENERIC_LOG_STATEMENT_BY_PLUGIN = "generic_log_statement_by_plugin";
    public static final String WORKSPACE_LOG_STATEMENT_BY_PLUGIN = "workspace_log_statement_by_plugin";
    public static final String LEARNSPACE_LOG_STATEMENT_BY_PLUGIN = "learnspace_log_statement_by_plugin";

    AtomicInteger genericRoundRobin = new AtomicInteger(0);
    AtomicInteger workspaceRoundRobin = new AtomicInteger(0);
    AtomicInteger learnspaceRoundRobin = new AtomicInteger(0);

    private final PluginLogConfig pluginLogConfig;
    private final RedissonReactiveClient redissonReactiveClient;
    private final PluginLogBucketInitializer pluginLogBucketInitializer;

    @Inject
    public PluginLogBucketProvider(PluginLogConfig pluginLogConfig,
                                   RedissonReactiveClient redissonReactiveClient,
                                   PluginLogBucketInitializer pluginLogBucketInitializer) {
        this.pluginLogConfig = pluginLogConfig;
        this.redissonReactiveClient = redissonReactiveClient;
        this.pluginLogBucketInitializer = pluginLogBucketInitializer;
    }

    /**
     * return a BucketCollection object based on the PluginLogContext provided.
     *
     * @param pluginLogContext
     * @return BucketCollection object
     */
    @Override
    @Trace(async = true)
    public BucketCollection get(final PluginLogContext pluginLogContext) {
        // initialize the list that will hold the final LogBucketInstance objects to be set in BucketCollection
        BucketCollection bucketCollection = new BucketCollection();
        List<LogBucketInstance> logBucketInstances = new ArrayList<>();

        if (pluginLogConfig.getEnabled()) {

            RBucketReactive<LogBucketInstanceCache> bucket = redissonReactiveClient.getBucket(CACHE_NAME);
            // initialize bucket if it does not exist, persist in DB and fill map with instances based on the number in the config
            bucket.isExists()
                    .flatMap(bucketExists -> {
                        if (!bucketExists) {
                            bucket.set(pluginLogBucketInitializer.initialize(pluginLogConfig.getBucketConfigs()))
                                    .doOnSuccess(value -> logger.info("{} bucket has been initialized successfully.", CACHE_NAME))
                                    .subscribe();
                        }
                        return Mono.just(bucketExists);
                    })
                    .flatMap(exists -> {
                        // get the bucketConfigs list based on pluginLogContext
                        List<BucketConfig> bucketConfigList = filterBucketConfig(pluginLogConfig.getBucketConfigs(),
                                                                                 pluginLogContext);
                        bucketConfigList.forEach(bucketConfig -> {
                            // get instances map from redis based on the tableName
                            String tableName = bucketConfig.getTableName();
                            // if map exists, then round-robin the LogBucketInstances based on the counter, which is thread safe, to get the correct LogBucketInstance
                            if (pluginLogContext == PluginLogContext.WORKSPACE && tableName.equals(
                                    WORKSPACE_LOG_STATEMENT_BY_PLUGIN)) {
                                logBucketInstances.add(getRoundRobinLogBucketInstance(bucket,
                                                                                      bucketConfig,
                                                                                      workspaceRoundRobin).block());
                            } else if (pluginLogContext == PluginLogContext.LEARNSPACE && tableName.equals(
                                    LEARNSPACE_LOG_STATEMENT_BY_PLUGIN)) {
                                logBucketInstances.add(getRoundRobinLogBucketInstance(bucket,
                                                                                      bucketConfig,
                                                                                      learnspaceRoundRobin).block());
                            } else if (bucketConfig.getTableName().equals(GENERIC_LOG_STATEMENT_BY_PLUGIN)) {
                                logBucketInstances.add(getRoundRobinLogBucketInstance(bucket,
                                                                                      bucketConfig,
                                                                                      genericRoundRobin).block());
                            }
                        });
                        return Mono.just(exists);
                    })
                    .block();
        }

        bucketCollection.setLogBucketInstances(logBucketInstances);
        return bucketCollection;
    }

    /**
     * pass the correct round-robin counter to getLogBucketInstanceCache method, and return correct logBucketInstance
     *
     * @param cachedBucket
     * @param bucketConfig
     * @param roundRobin
     * @return mono that emits the LogBucketInstance
     */
    private Mono<LogBucketInstance> getRoundRobinLogBucketInstance(final RBucketReactive<LogBucketInstanceCache> cachedBucket,
                                                                   final BucketConfig bucketConfig,
                                                                   AtomicInteger roundRobin) {
        if (roundRobin.get() > bucketConfig.getLogBucketInstances()) {
            roundRobin.set(0);
        }
        Mono<LogBucketInstanceCache> bucketInstanceCacheMono = getLogBucketInstanceCache(cachedBucket.get(),
                                                                                         bucketConfig,
                                                                                         roundRobin.get());
        return bucketInstanceCacheMono
                .flatMap(logBucketInstanceCache -> {
                    cachedBucket.set(logBucketInstanceCache).subscribe();
                    return Mono.just(logBucketInstanceCache);
                })
                .map(value -> {
                    LogBucketInstance logBucketInstance = value.getCache().get(bucketConfig.getTableName()).get(
                            roundRobin.intValue());
                    roundRobin.getAndIncrement();
                    return logBucketInstance;
                });
    }

    /**
     * extract the filtered BucketConfig list
     *
     * @param bucketConfigList
     * @param pluginLogContext
     * @return BucketConfig list
     */
    private List<BucketConfig> filterBucketConfig(final List<BucketConfig> bucketConfigList,
                                                  final PluginLogContext pluginLogContext) {
        String tableName = null;
        if (pluginLogContext == PluginLogContext.WORKSPACE) {
            tableName = WORKSPACE_LOG_STATEMENT_BY_PLUGIN;
        } else if (pluginLogContext == PluginLogContext.LEARNSPACE) {
            tableName = LEARNSPACE_LOG_STATEMENT_BY_PLUGIN;
        }
        String finalTableName = tableName;
        return bucketConfigList.stream().filter(value -> value.getTableName().equals(finalTableName) ||
                value.getTableName().equals(GENERIC_LOG_STATEMENT_BY_PLUGIN)).collect(Collectors.toList());
    }

    /**
     * Method to get LogBucketInstanceCache after setting currentCount for the correct LogBucketInstance
     *
     * @param bucketInstanceCacheMono
     * @param bucketConfig
     * @param roundRobin
     * @return mono that emits the LogBucketInstanceCache
     */
    @Trace(async = true)
    private Mono<LogBucketInstanceCache> getLogBucketInstanceCache(final Mono<LogBucketInstanceCache> bucketInstanceCacheMono,
                                                                   final BucketConfig bucketConfig,
                                                                   int roundRobin) {
        String tableName = bucketConfig.getTableName();
        return bucketInstanceCacheMono
                .flatMap(currentInstance -> {
                    CopyOnWriteArrayList<LogBucketInstance> logBucketInstances = currentInstance.getCache().get(
                            tableName);
                    LogBucketInstance logBucketInstance = logBucketInstances.get(roundRobin);
                    if (logBucketInstance != null && logBucketInstance.getCurrentCount() < logBucketInstance.getMaxRecordCount()) {
                        // if there is space then increment currentCount by 1, save back to cache list and then return logBucketInstanceCache
                        logBucketInstance.setCurrentCount(logBucketInstance.getCurrentCount() + 1);
                        logBucketInstances.set(roundRobin, logBucketInstance);
                    } else {
                        pluginLogBucketInitializer.persistLogBucket(bucketConfig).subscribe(logBucket -> {
                            LogBucketInstance instance = pluginLogBucketInitializer.createLogBucketInstance(logBucket,
                                                                                                            bucketConfig);
                            logBucketInstances.set(roundRobin, instance);
                        });
                    }
                    return bucketInstanceCacheMono;
                })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    throwable = Exceptions.unwrap(throwable);
                    logger.reactiveErrorThrowable("Exception getting LogBucketInstanceCache " + throwable);
                });
    }

    /**
     * Method to clear Redis Bucket Cache
     *
     * @return mono that emits the Boolean of deleted
     */
    @Trace(async = true)
    public Mono<Boolean> clearRedisBucketCache() {
        RBucketReactive<LogBucketInstanceCache> bucket = redissonReactiveClient.getBucket(CACHE_NAME);
        return bucket.isExists()
                .flatMap(exists -> {
                    if (exists) {
                        return bucket.delete();
                    } else {
                        return Mono.just(true);
                    }
                })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    throwable = Exceptions.unwrap(throwable);
                    logger.reactiveErrorThrowable("Exception clearing Redis Bucket Cache " + throwable);
                });
    }

    /**
     * Method to reset the round-robin counter
     *
     * @param tableName
     */
    public void resetRoundRobin(String tableName) {
        switch (tableName) {
            case GENERIC_LOG_STATEMENT_BY_PLUGIN:
                genericRoundRobin.set(0);
                break;
            case WORKSPACE_LOG_STATEMENT_BY_PLUGIN:
                workspaceRoundRobin.set(0);
                break;
            case LEARNSPACE_LOG_STATEMENT_BY_PLUGIN:
                learnspaceRoundRobin.set(0);
                break;
            default:
                throw new IllegalArgumentFault(String.format("Invalid tableName: %s", tableName));
        }
    }
}
