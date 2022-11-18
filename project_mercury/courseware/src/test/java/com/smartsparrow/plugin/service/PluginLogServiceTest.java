package com.smartsparrow.plugin.service;

import static com.smartsparrow.plugin.data.PluginLogBucketProvider.GENERIC_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.LEARNSPACE_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.WORKSPACE_LOG_STATEMENT_BY_PLUGIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.plugin.data.BucketCollection;
import com.smartsparrow.plugin.data.BucketRetentionPolicy;
import com.smartsparrow.plugin.data.GenericLogStatement;
import com.smartsparrow.plugin.data.LearnspaceLogStatement;
import com.smartsparrow.plugin.data.LogBucketInstance;
import com.smartsparrow.plugin.data.PluginLogBucketProvider;
import com.smartsparrow.plugin.data.PluginLogContext;
import com.smartsparrow.plugin.data.PluginLogGateway;
import com.smartsparrow.plugin.data.PluginLogLevel;
import com.smartsparrow.plugin.data.WorkspaceLogStatement;
import com.smartsparrow.plugin.lang.PluginLogException;
import com.smartsparrow.plugin.wiring.BucketConfig;
import com.smartsparrow.plugin.wiring.PluginLogConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class PluginLogServiceTest {

    private static final UUID BUCKET_ID = UUIDs.timeBased();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String version = "1.2.0";
    private static final PluginLogLevel level = PluginLogLevel.INFO;
    private static final String message = "log message";
    private static final String args = "log args";
    private static final String pluginContext = "PREVIEW";
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final String tableName = GENERIC_LOG_STATEMENT_BY_PLUGIN;
    private static final Boolean enabled = true;
    private static final Long maxRecordCount = 10L;
    private static final BucketRetentionPolicy retentionPolicy = BucketRetentionPolicy.WEEK;
    private static final Long logBucketInstances = 5L;
    private static final Long MAX_COUNT = 2L;
    private static final Long NUMBER_OF_INSTANCES = 3L;
    private List<BucketConfig> bucketConfigList;
    private final LogBucketInstance genericLogBucketInstance = new LogBucketInstance()
            .setBucketId(BUCKET_ID)
            .setTableName(GENERIC_LOG_STATEMENT_BY_PLUGIN);
    private final LogBucketInstance workspaceLogBucketInstance = new LogBucketInstance()
            .setBucketId(BUCKET_ID)
            .setTableName(WORKSPACE_LOG_STATEMENT_BY_PLUGIN);
    private final LogBucketInstance learnspaceLogBucketInstance = new LogBucketInstance()
            .setBucketId(BUCKET_ID)
            .setTableName(LEARNSPACE_LOG_STATEMENT_BY_PLUGIN);
    @InjectMocks
    private PluginLogService pluginLogService;
    @Mock
    private PluginLogGateway pluginLogGateway;
    @Mock
    private PluginLogConfig pluginLogConfig;
    @Mock
    private PluginLogBucketProvider pluginLogBucketProvider;
    private List<LogBucketInstance> workspaceLogBucketInstances;
    private List<LogBucketInstance> learnspaceLogBucketInstances;
    private BucketCollection workspaceBucketCollection;
    private BucketCollection learnspaceBucketCollection;
    private GenericLogStatement genericLogStatement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        genericLogStatement = new GenericLogStatement()
                .setPluginId(pluginId)
                .setVersion(version)
                .setLevel(level)
                .setMessage(message)
                .setArgs(args)
                .setPluginContext(pluginContext)
                .setLoggingContext(PluginLogContext.LEARNSPACE);

        workspaceLogBucketInstances = new ArrayList<>();
        workspaceLogBucketInstances.add(workspaceLogBucketInstance);
        workspaceLogBucketInstances.add(genericLogBucketInstance);
        workspaceBucketCollection = new BucketCollection().setLogBucketInstances(workspaceLogBucketInstances);

        learnspaceLogBucketInstances = new ArrayList<>();
        learnspaceLogBucketInstances.add(learnspaceLogBucketInstance);
        learnspaceLogBucketInstances.add(genericLogBucketInstance);
        learnspaceBucketCollection = new BucketCollection().setLogBucketInstances(learnspaceLogBucketInstances);

        when(pluginLogConfig.getEnabled()).thenReturn(false);
        when(pluginLogGateway.persist(any(GenericLogStatement.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginLogGateway.persist(any(WorkspaceLogStatement.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginLogGateway.persist(any(LearnspaceLogStatement.class))).thenReturn(Flux.just(new Void[]{}));

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
    }

    @Test
    void logGenericPluginStatement_notEnabled() throws PluginLogException {
        GenericLogStatement genericLogStatement = pluginLogService.logGenericPluginStatement(
                workspaceLogBucketInstances, this.genericLogStatement).block();

        verify(pluginLogBucketProvider, never()).get(any(PluginLogContext.class));
        verify(pluginLogGateway, never()).persist(any(GenericLogStatement.class));
        assert genericLogStatement != null;
    }

    @Test
    void logWorkspacePluginStatement_notEnabled() throws PluginLogException {
        WorkspaceLogStatement workspaceLogStatement = pluginLogService.logWorkspacePluginStatement(pluginId,
                                                                                                   version,
                                                                                                   level,
                                                                                                   message,
                                                                                                   args,
                                                                                                   pluginContext,
                                                                                                   elementId,
                                                                                                   elementType,
                                                                                                   projectId).block();

        verify(pluginLogBucketProvider, never()).get(any(PluginLogContext.class));
        verify(pluginLogGateway, never()).persist(any(WorkspaceLogStatement.class));
        verify(pluginLogGateway, never()).persist(any(GenericLogStatement.class));
        assert workspaceLogStatement != null;
    }

    @Test
    void logLearnspacePluginStatement_notEnabled() throws PluginLogException {
        LearnspaceLogStatement learnspaceLogStatement = pluginLogService.logLearnspacePluginStatement(pluginId,
                                                                                                      version,
                                                                                                      level,
                                                                                                      message,
                                                                                                      args,
                                                                                                      pluginContext,
                                                                                                      elementId,
                                                                                                      elementType,
                                                                                                      deploymentId,
                                                                                                      cohortId).block();

        verify(pluginLogBucketProvider, never()).get(any(PluginLogContext.class));
        verify(pluginLogGateway, never()).persist(any(LearnspaceLogStatement.class));
        verify(pluginLogGateway, never()).persist(any(GenericLogStatement.class));
        assert learnspaceLogStatement != null;
    }

    @Test
    void logGenericPluginStatement_enabled() throws PluginLogException {
        when(pluginLogConfig.getEnabled()).thenReturn(true);

        ArgumentCaptor<GenericLogStatement> captor = ArgumentCaptor.forClass(GenericLogStatement.class);
        final GenericLogStatement res = pluginLogService.logGenericPluginStatement(learnspaceLogBucketInstances,
                                                                                   genericLogStatement).block();

        assertNotNull(res);
        assertEquals(BUCKET_ID, res.getBucketId());
        assertNotNull(res.getId());

        verify(pluginLogGateway).persist(captor.capture());

        final GenericLogStatement captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);
    }

    @Test
    void logWorkspacePluginStatement_enabled() throws PluginLogException {
        when(pluginLogConfig.getEnabled()).thenReturn(true);
        when(pluginLogBucketProvider.get(PluginLogContext.WORKSPACE)).thenReturn((workspaceBucketCollection));

        ArgumentCaptor<WorkspaceLogStatement> workspaceArgumentCaptor = ArgumentCaptor.forClass(WorkspaceLogStatement.class);
        ArgumentCaptor<GenericLogStatement> genericArgumentCaptor = ArgumentCaptor.forClass(GenericLogStatement.class);
        final WorkspaceLogStatement res = pluginLogService.logWorkspacePluginStatement(pluginId,
                                                                                       version,
                                                                                       level,
                                                                                       message,
                                                                                       args,
                                                                                       pluginContext,
                                                                                       elementId,
                                                                                       elementType,
                                                                                       projectId).block();

        assertNotNull(res);
        assertEquals(BUCKET_ID, res.getBucketId());
        assertNotNull(res.getId());

        verify(pluginLogBucketProvider).get(PluginLogContext.WORKSPACE);
        verify(pluginLogGateway).persist(genericArgumentCaptor.capture());
        verify(pluginLogGateway).persist(workspaceArgumentCaptor.capture());

        final WorkspaceLogStatement captured = workspaceArgumentCaptor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);

        final GenericLogStatement genericLogStatement = genericArgumentCaptor.getValue();
        assertNotNull(genericLogStatement);
    }

    @Test
    void logLearnspacePluginStatement_enabled() throws PluginLogException {
        when(pluginLogConfig.getEnabled()).thenReturn(true);
        when(pluginLogBucketProvider.get(PluginLogContext.LEARNSPACE)).thenReturn((learnspaceBucketCollection));

        ArgumentCaptor<LearnspaceLogStatement> learnspaceArgumentCaptor = ArgumentCaptor.forClass(
                LearnspaceLogStatement.class);
        ArgumentCaptor<GenericLogStatement> genericArgumentCaptor = ArgumentCaptor.forClass(GenericLogStatement.class);
        final LearnspaceLogStatement res = pluginLogService.logLearnspacePluginStatement(pluginId,
                                                                                         version,
                                                                                         level,
                                                                                         message,
                                                                                         args,
                                                                                         pluginContext,
                                                                                         elementId,
                                                                                         elementType,
                                                                                         deploymentId,
                                                                                         cohortId).block();

        assertNotNull(res);
        assertEquals(BUCKET_ID, res.getBucketId());
        assertNotNull(res.getId());

        verify(pluginLogBucketProvider).get(PluginLogContext.LEARNSPACE);
        verify(pluginLogGateway).persist(genericArgumentCaptor.capture());
        verify(pluginLogGateway).persist(learnspaceArgumentCaptor.capture());

        final LearnspaceLogStatement captured = learnspaceArgumentCaptor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);

        final GenericLogStatement genericLogStatement = genericArgumentCaptor.getValue();
        assertNotNull(genericLogStatement);
    }

    @Test
    public void updateDisablePluginLogConfig() throws PluginLogException {
        when(pluginLogBucketProvider.clearRedisBucketCache()).thenReturn(Mono.just(Boolean.TRUE));
        when(pluginLogConfig.setEnabled(false)).thenReturn(pluginLogConfig);

        final PluginLogConfig res = pluginLogService.updatePluginLogConfig(tableName,
                                                                           false,
                                                                           maxRecordCount,
                                                                           retentionPolicy,
                                                                           logBucketInstances).block();

        assertNotNull(res);
        assertFalse(res.getEnabled());
        verify(pluginLogBucketProvider).clearRedisBucketCache();
    }

    @Test
    public void updatePluginLogConfig() throws PluginLogException {
        when(pluginLogConfig.getBucketConfigs()).thenReturn(bucketConfigList);
        when(pluginLogConfig.setEnabled(enabled)).thenReturn(pluginLogConfig);
        when(pluginLogConfig.setBucketConfigs(bucketConfigList)).thenReturn(pluginLogConfig);

        final PluginLogConfig res = pluginLogService.updatePluginLogConfig(tableName,
                                                                           enabled,
                                                                           maxRecordCount,
                                                                           retentionPolicy,
                                                                           logBucketInstances).block();

        assertNotNull(res);
        assertNotNull(res.getBucketConfigs());
        verify(pluginLogBucketProvider, never()).clearRedisBucketCache();
        verify(pluginLogBucketProvider).resetRoundRobin(tableName);
    }
}