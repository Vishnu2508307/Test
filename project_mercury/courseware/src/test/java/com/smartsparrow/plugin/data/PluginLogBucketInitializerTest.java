package com.smartsparrow.plugin.data;

import static com.smartsparrow.plugin.data.PluginLogBucketProvider.GENERIC_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.LEARNSPACE_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.WORKSPACE_LOG_STATEMENT_BY_PLUGIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucketReactive;

import com.smartsparrow.plugin.wiring.BucketConfig;
import com.smartsparrow.util.ClockProvider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class PluginLogBucketInitializerTest {

    private static final Long MAX_COUNT = 2L;
    private static final Long NUMBER_OF_INSTANCES = 3L;
    private static final Clock clock = Clock.systemUTC();
    private List<BucketConfig> bucketConfigList;

    @InjectMocks
    private PluginLogBucketInitializer pluginLogBucketInitializer;
    @Mock
    private PluginLogGateway pluginLogGateway;
    @Mock
    private ClockProvider clockProvider;
    @Mock
    private RBucketReactive<LogBucketInstanceCache> rBucketReactive;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        BucketConfig genericBucketConfig = new BucketConfig().setLogBucketInstances(NUMBER_OF_INSTANCES).setTableName(
                GENERIC_LOG_STATEMENT_BY_PLUGIN).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(BucketRetentionPolicy.WEEK);
        BucketConfig workspaceBucketConfig = new BucketConfig().setLogBucketInstances(NUMBER_OF_INSTANCES).setTableName(
                WORKSPACE_LOG_STATEMENT_BY_PLUGIN).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(
                BucketRetentionPolicy.WEEK);
        BucketConfig learnspaceBucketConfig = new BucketConfig().setLogBucketInstances(NUMBER_OF_INSTANCES).setTableName(
                LEARNSPACE_LOG_STATEMENT_BY_PLUGIN).setMaxRecordCount(MAX_COUNT).setRetentionPolicy(
                BucketRetentionPolicy.WEEK);
        bucketConfigList = new ArrayList<>();
        bucketConfigList.add(genericBucketConfig);
        bucketConfigList.add(workspaceBucketConfig);
        bucketConfigList.add(learnspaceBucketConfig);

        when(clockProvider.get()).thenReturn(clock);
    }

    @Test
    void initialize_empty_bucket() {
        when(pluginLogGateway.persist(any(LogBucket.class))).thenReturn(Flux.just(new Void[]{}));
        when(rBucketReactive.set(any(LogBucketInstanceCache.class))).thenReturn(Mono.empty());

        pluginLogBucketInitializer.initialize(bucketConfigList);

        verify(pluginLogGateway, times(9)).persist(any(LogBucket.class));
    }
}