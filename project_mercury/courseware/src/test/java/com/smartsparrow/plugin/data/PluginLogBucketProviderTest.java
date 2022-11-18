package com.smartsparrow.plugin.data;

import static com.smartsparrow.plugin.data.PluginLogBucketProvider.CACHE_NAME;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.GENERIC_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.LEARNSPACE_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.WORKSPACE_LOG_STATEMENT_BY_PLUGIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.plugin.wiring.BucketConfig;
import com.smartsparrow.plugin.wiring.PluginLogConfig;

import reactor.core.publisher.Mono;

class PluginLogBucketProviderTest {

    private static final UUID BUCKET_ID = UUIDs.timeBased();
    private static final Long MAX_COUNT = 2L;
    private static final Long CURRENT_COUNT = 0L;
    private static final Long NUMBER_OF_INSTANCES = 3L;
    private static final Clock clock = Clock.systemUTC();
    private LogBucketInstanceCache logBucketInstanceCache;
    private LogBucket genericLogBucket;
    private LogBucket workspaceLogBucket;
    private LogBucket learnspaceLogBucket;
    private BucketConfig genericBucketConfig;
    private BucketConfig workspaceBucketConfig;
    private BucketConfig learnspaceBucketConfig;
    private List<BucketConfig> bucketConfigList;

    @InjectMocks
    private PluginLogBucketProvider pluginLogBucketProvider;
    @Mock
    private PluginLogConfig pluginLogConfig;
    @Mock
    private RedissonReactiveClient redissonReactiveClient;
    @Mock
    private PluginLogBucketInitializer pluginLogBucketInitializer;
    @Mock
    private RBucketReactive<LogBucketInstanceCache> rBucketReactive;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        long currentMillis = clock.millis();
        genericLogBucket = new LogBucket().setBucketId(BUCKET_ID).setTableName(GENERIC_LOG_STATEMENT_BY_PLUGIN).setDay(
                com.datastax.driver.core.LocalDate.fromMillisSinceEpoch(currentMillis))
                .setTime(currentMillis);
        LogBucketInstance genericLogBucketInstance = new LogBucketInstance().setCurrentCount(
                CURRENT_COUNT).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(BucketRetentionPolicy.WEEK).setBucketId(
                genericLogBucket.getBucketId()).setTableName(genericLogBucket.getTableName()).setDayAsString(
                genericLogBucket.getDay().toString()).setTime(
                genericLogBucket.getTime());
        CopyOnWriteArrayList<LogBucketInstance> genericLogBucketInstanceList = new CopyOnWriteArrayList<>();
        genericLogBucketInstanceList.add(genericLogBucketInstance);
        genericLogBucketInstanceList.add(genericLogBucketInstance);
        genericLogBucketInstanceList.add(genericLogBucketInstance);

        workspaceLogBucket = new LogBucket().setBucketId(BUCKET_ID).setTableName(WORKSPACE_LOG_STATEMENT_BY_PLUGIN).setDay(
                com.datastax.driver.core.LocalDate.fromMillisSinceEpoch(currentMillis))
                .setTime(currentMillis);
        LogBucketInstance workspaceLogBucketInstance = new LogBucketInstance().setCurrentCount(
                CURRENT_COUNT).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(BucketRetentionPolicy.WEEK).setBucketId(
                workspaceLogBucket.getBucketId()).setTableName(workspaceLogBucket.getTableName()).setDayAsString(
                workspaceLogBucket.getDay().toString()).setTime(
                workspaceLogBucket.getTime());
        CopyOnWriteArrayList<LogBucketInstance> workspaceLogBucketInstanceList = new CopyOnWriteArrayList<>();
        workspaceLogBucketInstanceList.add(workspaceLogBucketInstance);
        workspaceLogBucketInstanceList.add(workspaceLogBucketInstance);
        workspaceLogBucketInstanceList.add(workspaceLogBucketInstance);

        learnspaceLogBucket = new LogBucket().setBucketId(BUCKET_ID).setTableName(LEARNSPACE_LOG_STATEMENT_BY_PLUGIN).setDay(
                com.datastax.driver.core.LocalDate.fromMillisSinceEpoch(currentMillis))
                .setTime(currentMillis);
        LogBucketInstance learnspaceLogBucketInstance = new LogBucketInstance().setCurrentCount(
                CURRENT_COUNT).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(BucketRetentionPolicy.WEEK).setBucketId(
                learnspaceLogBucket.getBucketId()).setTableName(learnspaceLogBucket.getTableName()).setDayAsString(
                learnspaceLogBucket.getDay().toString()).setTime(
                learnspaceLogBucket.getTime());
        CopyOnWriteArrayList<LogBucketInstance> learnspaceLogBucketInstanceList = new CopyOnWriteArrayList<>();
        learnspaceLogBucketInstanceList.add(learnspaceLogBucketInstance);
        learnspaceLogBucketInstanceList.add(learnspaceLogBucketInstance);
        learnspaceLogBucketInstanceList.add(learnspaceLogBucketInstance);

        Map<String, CopyOnWriteArrayList<LogBucketInstance>> map = new HashMap<>();
        map.put(GENERIC_LOG_STATEMENT_BY_PLUGIN, genericLogBucketInstanceList);
        map.put(WORKSPACE_LOG_STATEMENT_BY_PLUGIN, workspaceLogBucketInstanceList);
        map.put(LEARNSPACE_LOG_STATEMENT_BY_PLUGIN, learnspaceLogBucketInstanceList);
        logBucketInstanceCache = new LogBucketInstanceCache().setCache(map);

        genericBucketConfig = new BucketConfig().setLogBucketInstances(NUMBER_OF_INSTANCES).setTableName(
                GENERIC_LOG_STATEMENT_BY_PLUGIN).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(BucketRetentionPolicy.WEEK);
        workspaceBucketConfig = new BucketConfig().setLogBucketInstances(NUMBER_OF_INSTANCES).setTableName(
                WORKSPACE_LOG_STATEMENT_BY_PLUGIN).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(
                BucketRetentionPolicy.WEEK);
        learnspaceBucketConfig = new BucketConfig().setLogBucketInstances(NUMBER_OF_INSTANCES).setTableName(
                LEARNSPACE_LOG_STATEMENT_BY_PLUGIN).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(
                BucketRetentionPolicy.WEEK);
        bucketConfigList = new ArrayList<>();
        bucketConfigList.add(genericBucketConfig);
        bucketConfigList.add(workspaceBucketConfig);
        bucketConfigList.add(learnspaceBucketConfig);

        when(pluginLogConfig.getEnabled()).thenReturn(true);
        when(pluginLogConfig.getBucketConfigs()).thenReturn(bucketConfigList);
        when(pluginLogBucketInitializer.initialize(bucketConfigList)).thenReturn(logBucketInstanceCache);
        when(pluginLogBucketInitializer.persistLogBucket(learnspaceBucketConfig)).thenReturn(Mono.just(
                learnspaceLogBucket));
        when(pluginLogBucketInitializer.persistLogBucket(genericBucketConfig)).thenReturn(Mono.just(genericLogBucket));
        when(pluginLogBucketInitializer.persistLogBucket(workspaceBucketConfig)).thenReturn(Mono.just(workspaceLogBucket));
        when(pluginLogBucketInitializer.createLogBucketInstance(learnspaceLogBucket,
                                                                learnspaceBucketConfig)).thenReturn(
                learnspaceLogBucketInstance);
        when(pluginLogBucketInitializer.createLogBucketInstance(genericLogBucket, genericBucketConfig)).thenReturn(
                genericLogBucketInstance);
        when(pluginLogBucketInitializer.createLogBucketInstance(workspaceLogBucket, workspaceBucketConfig)).thenReturn(
                workspaceLogBucketInstance);
    }

    @AfterEach
    public void tearUp() {
        pluginLogBucketProvider.genericRoundRobin.set(0);
        pluginLogBucketProvider.workspaceRoundRobin.set(0);
        pluginLogBucketProvider.learnspaceRoundRobin.set(0);
    }

    @Test
    void getBucketCollection_notEnabled() {
        when(pluginLogConfig.getEnabled()).thenReturn(false);

        BucketCollection bucketCollection = pluginLogBucketProvider.get(PluginLogContext.LEARNSPACE);

        verify(redissonReactiveClient, never()).getBucket(any(String.class));
        verify(pluginLogBucketInitializer, never()).persistLogBucket(any(BucketConfig.class));
        assert bucketCollection != null;
        assert bucketCollection.getLogBucketInstances().size() == 0;
    }

    @Test
    void getBucketCollection_workspace_enabled_empty_bucket() {
        when(pluginLogConfig.getEnabled()).thenReturn(true);

        when(redissonReactiveClient.getBucket(CACHE_NAME)).thenReturn((RBucketReactive) rBucketReactive);
        when(rBucketReactive.isExists()).thenReturn(Mono.just(Boolean.FALSE));
        when(rBucketReactive.get()).thenReturn(Mono.just(logBucketInstanceCache));
        when(rBucketReactive.set(any(LogBucketInstanceCache.class))).thenReturn(Mono.empty());

        BucketCollection bucketCollection = pluginLogBucketProvider.get(PluginLogContext.WORKSPACE);

        verify(pluginLogBucketInitializer).initialize(bucketConfigList);
        assert bucketCollection != null;
        assert bucketCollection.getLogBucketInstances().size() == 2;
        assert bucketCollection.getLogBucketInstances().get(0).getTableName().equals(GENERIC_LOG_STATEMENT_BY_PLUGIN);
        assert bucketCollection.getLogBucketInstances().get(1).getTableName().equals(WORKSPACE_LOG_STATEMENT_BY_PLUGIN);
    }

    @Test
    void getBucketCollection_learnspace_enabled_empty_bucket() {
        when(pluginLogConfig.getEnabled()).thenReturn(true);

        when(redissonReactiveClient.getBucket(CACHE_NAME)).thenReturn((RBucketReactive) rBucketReactive);
        when(rBucketReactive.isExists()).thenReturn(Mono.just(Boolean.FALSE));
        when(rBucketReactive.get()).thenReturn(Mono.just(logBucketInstanceCache));
        when(rBucketReactive.set(any(LogBucketInstanceCache.class))).thenReturn(Mono.empty());

        BucketCollection bucketCollection = pluginLogBucketProvider.get(PluginLogContext.LEARNSPACE);

        verify(pluginLogBucketInitializer).initialize(bucketConfigList);
        assert bucketCollection != null;
        assert bucketCollection.getLogBucketInstances().size() == 2;
        assert bucketCollection.getLogBucketInstances().get(0).getTableName().equals(GENERIC_LOG_STATEMENT_BY_PLUGIN);
        assert bucketCollection.getLogBucketInstances().get(1).getTableName().equals(LEARNSPACE_LOG_STATEMENT_BY_PLUGIN);
    }

    @Test
    void getBucketCollection_workspace_and_generic_enabled_bucket_exist() {
        when(pluginLogConfig.getEnabled()).thenReturn(true);

        when(redissonReactiveClient.getBucket(CACHE_NAME)).thenReturn((RBucketReactive) rBucketReactive);
        when(rBucketReactive.isExists()).thenReturn(Mono.just(Boolean.TRUE));
        when(rBucketReactive.get()).thenReturn(Mono.just(logBucketInstanceCache));
        when(rBucketReactive.set(any(LogBucketInstanceCache.class))).thenReturn(Mono.empty());

        // first time (currentCount < maxCount)
        pluginLogBucketProvider.get(PluginLogContext.WORKSPACE);

        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(workspaceBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(genericLogBucket, genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(workspaceLogBucket, workspaceBucketConfig);

        // second time (currentCount == maxCount)
        pluginLogBucketProvider.get(PluginLogContext.WORKSPACE);

        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(workspaceBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(genericLogBucket, genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(workspaceLogBucket, workspaceBucketConfig);

        // third time (currentCount > maxCount)
        pluginLogBucketProvider.get(PluginLogContext.WORKSPACE);

        verify(pluginLogBucketInitializer, times(1)).persistLogBucket(genericBucketConfig);
        verify(pluginLogBucketInitializer, times(1)).persistLogBucket(workspaceBucketConfig);
        verify(pluginLogBucketInitializer, times(1)).createLogBucketInstance(genericLogBucket, genericBucketConfig);
        verify(pluginLogBucketInitializer, times(1)).createLogBucketInstance(workspaceLogBucket, workspaceBucketConfig);
    }

    @Test
    void getBucketCollection_learnspace_and_generic_enabled_bucket_exist() {
        when(pluginLogConfig.getEnabled()).thenReturn(true);

        when(redissonReactiveClient.getBucket(CACHE_NAME)).thenReturn((RBucketReactive) rBucketReactive);
        when(rBucketReactive.isExists()).thenReturn(Mono.just(Boolean.TRUE));
        when(rBucketReactive.get()).thenReturn(Mono.just(logBucketInstanceCache));
        when(rBucketReactive.set(any(LogBucketInstanceCache.class))).thenReturn(Mono.empty());

        // first time (currentCount < maxCount)
        pluginLogBucketProvider.get(PluginLogContext.LEARNSPACE);

        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(learnspaceBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(genericLogBucket, genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(learnspaceLogBucket,
                                                                             learnspaceBucketConfig);

        // second time (currentCount == maxCount)
        pluginLogBucketProvider.get(PluginLogContext.LEARNSPACE);

        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).persistLogBucket(learnspaceBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(genericLogBucket, genericBucketConfig);
        verify(pluginLogBucketInitializer, times(0)).createLogBucketInstance(learnspaceLogBucket,
                                                                             learnspaceBucketConfig);

        // third time (currentCount > maxCount)
        pluginLogBucketProvider.get(PluginLogContext.LEARNSPACE);

        verify(pluginLogBucketInitializer, times(1)).persistLogBucket(genericBucketConfig);
        verify(pluginLogBucketInitializer, times(1)).persistLogBucket(learnspaceBucketConfig);
        verify(pluginLogBucketInitializer, times(1)).createLogBucketInstance(genericLogBucket, genericBucketConfig);
        verify(pluginLogBucketInitializer, times(1)).createLogBucketInstance(learnspaceLogBucket,
                                                                             learnspaceBucketConfig);
    }

    @Test
    public void clearRedisBucketCache_notExist() {
        when(redissonReactiveClient.getBucket(CACHE_NAME)).thenReturn((RBucketReactive) rBucketReactive);
        when(rBucketReactive.isExists()).thenReturn(Mono.just(Boolean.FALSE));

        Boolean deleted = pluginLogBucketProvider.clearRedisBucketCache().block();

        assertNotNull(deleted);
        assertTrue(deleted);
        verify(rBucketReactive, never()).delete();
    }

    @Test
    public void clearRedisBucketCache() {
        when(redissonReactiveClient.getBucket(CACHE_NAME)).thenReturn((RBucketReactive) rBucketReactive);
        when(rBucketReactive.isExists()).thenReturn(Mono.just(Boolean.TRUE));
        when(rBucketReactive.delete()).thenReturn(Mono.just(Boolean.TRUE));

        Boolean deleted = pluginLogBucketProvider.clearRedisBucketCache().block();

        assertNotNull(deleted);
        assertTrue(deleted);
        verify(rBucketReactive).delete();
    }

    @Test
    public void resetRoundRobin_generic() {
        pluginLogBucketProvider.resetRoundRobin(GENERIC_LOG_STATEMENT_BY_PLUGIN);

        assertEquals(0, pluginLogBucketProvider.genericRoundRobin.get());
    }

    @Test
    public void resetRoundRobin_workspace() {
        pluginLogBucketProvider.resetRoundRobin(WORKSPACE_LOG_STATEMENT_BY_PLUGIN);

        assertEquals(0, pluginLogBucketProvider.workspaceRoundRobin.get());
    }

    @Test
    public void resetRoundRobin_learnspace() {
        pluginLogBucketProvider.resetRoundRobin(LEARNSPACE_LOG_STATEMENT_BY_PLUGIN);

        assertEquals(0, pluginLogBucketProvider.learnspaceRoundRobin.get());
    }
}