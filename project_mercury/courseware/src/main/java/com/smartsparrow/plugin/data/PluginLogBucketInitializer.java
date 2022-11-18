package com.smartsparrow.plugin.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.plugin.wiring.BucketConfig;
import com.smartsparrow.util.ClockProvider;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * The main purpose of this service is to initialize a RMapCacheReactive object based on the PluginLogContext provided.
 */
@Singleton
public class PluginLogBucketInitializer {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(PluginLogBucketInitializer.class);

    private final PluginLogGateway pluginLogGateway;
    private final ClockProvider clockProvider;

    @Inject
    public PluginLogBucketInitializer(PluginLogGateway pluginLogGateway,
                                      ClockProvider clockProvider) {
        this.pluginLogGateway = pluginLogGateway;
        this.clockProvider = clockProvider;
    }

    /**
     * initialize LogBucketInstanceCache if the RBucketReactive bucket does not exist
     *
     * @param bucketConfigList
     * @return LogBucketInstanceCache
     */
    @Trace(async = true)
    public LogBucketInstanceCache initialize(final List<BucketConfig> bucketConfigList) {
        Map<String, CopyOnWriteArrayList<LogBucketInstance>> map = new HashMap<>();
        bucketConfigList.forEach(bucketConfig -> {
            CopyOnWriteArrayList<LogBucketInstance> bucketInstanceList = new CopyOnWriteArrayList<>();
            for (int counter = 0; counter < bucketConfig.getLogBucketInstances(); counter++) {
                persistLogBucket(bucketConfig)
                        .doOnSuccess(logBucket -> {
                            LogBucketInstance logBucketInstance = createLogBucketInstance(logBucket, bucketConfig);
                            bucketInstanceList.add(logBucketInstance);
                        })
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .block();
            }
            map.put(bucketConfig.getTableName(), bucketInstanceList);
        });

        return new LogBucketInstanceCache().setCache(map);
    }

    /**
     * Method to persist LogBucket
     *
     * @param bucketConfig
     * @return mono of LogBucket object
     */
    @Trace(async = true)
    public Mono<LogBucket> persistLogBucket(final BucketConfig bucketConfig) {
        LogBucket logBucket = createLogBucket(bucketConfig);
        logger.info("LogBucket " + logBucket.toString());

        return pluginLogGateway.persist(logBucket)
                .then(Mono.just(logBucket))
                .doOnEach(logger.reactiveInfo(String.format(
                        "Persisted logBucket with tableName [%s] and bucketId [%s]",
                        logBucket.getTableName(),
                        logBucket.getBucketId().toString())))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(ex -> {
                    ex = Exceptions.unwrap(ex);
                    logger.reactiveErrorThrowable("Exception persisting LogBucket " + ex);
                });
    }

    /**
     * Method to prepare a new LogBucket
     *
     * @param bucketConfig
     * @return LogBucket object
     */
    private LogBucket createLogBucket(final BucketConfig bucketConfig) {
        UUID bucketId = UUIDs.timeBased();
        long currentMillis = clockProvider.get().millis();
        return new LogBucket()
                .setBucketId(bucketId)
                .setTableName(bucketConfig.getTableName())
                .setDay(com.datastax.driver.core.LocalDate.fromMillisSinceEpoch(currentMillis))
                .setTime(currentMillis);
    }

    /**
     * Method to prepare a new LogBucketInstance
     *
     * @param bucketConfig
     * @return LogBucketInstance object
     */
    public LogBucketInstance createLogBucketInstance(final LogBucket logBucket,
                                                     final BucketConfig bucketConfig) {
        return new LogBucketInstance()
                .setMaxRecordCount(bucketConfig.getMaxRecordCount())
                .setCurrentCount(1L)
                .setRetentionPolicy(bucketConfig.getRetentionPolicy())
                .setDayAsString(logBucket.getDay().toString()) // LocalDate will be set in yyyy-mm-dd format
                .setTime(logBucket.getTime())
                .setTableName(logBucket.getTableName())
                .setBucketId(logBucket.getBucketId());
    }
}
