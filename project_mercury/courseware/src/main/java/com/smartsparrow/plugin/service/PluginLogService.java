package com.smartsparrow.plugin.service;

import static com.smartsparrow.plugin.data.PluginLogBucketProvider.GENERIC_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.LEARNSPACE_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.plugin.data.PluginLogBucketProvider.WORKSPACE_LOG_STATEMENT_BY_PLUGIN;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.newrelic.api.agent.Trace;
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
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class PluginLogService {

    private static final Logger logger = MercuryLoggerFactory.getLogger(PluginLogService.class);

    private final PluginLogGateway pluginLogGateway;
    private final PluginLogConfig pluginLogConfig;
    private final PluginLogBucketProvider pluginLogBucketProvider;

    @Inject
    public PluginLogService(PluginLogGateway pluginLogGateway,
                            PluginLogConfig pluginLogConfig,
                            PluginLogBucketProvider pluginLogBucketProvider) {
        this.pluginLogGateway = pluginLogGateway;
        this.pluginLogConfig = pluginLogConfig;
        this.pluginLogBucketProvider = pluginLogBucketProvider;
    }

    /**
     * generic log statement
     *
     * @param logBucketInstanceList
     * @param statement
     * @return mono of GenericLogStatement object
     */
    @Trace(async = true)
    public Mono<GenericLogStatement> logGenericPluginStatement(List<LogBucketInstance> logBucketInstanceList,
                                                               GenericLogStatement statement) throws PluginLogException {
        if (pluginLogConfig.getEnabled()) {
            GenericLogStatement genericLogStatement = logBucketInstanceList.stream()
                    .filter(value -> value.getTableName().equals(GENERIC_LOG_STATEMENT_BY_PLUGIN))
                    .map(value -> {
                        UUID id = UUIDs.timeBased();
                        return new GenericLogStatement()
                                .setId(id)
                                .setBucketId(value.getBucketId())
                                .setPluginId(statement.getPluginId())
                                .setVersion(statement.getVersion())
                                .setLevel(statement.getLevel())
                                .setMessage(statement.getMessage())
                                .setArgs(statement.getArgs())
                                .setPluginContext(statement.getPluginContext())
                                .setLoggingContext(statement.getLoggingContext());
                    })
                    .findAny()
                    .orElseThrow(() -> new PluginLogException(String.format(
                            "Error finding GENERIC_LOG_STATEMENT_BY_PLUGIN in list: %s",
                            logBucketInstanceList)));

            return pluginLogGateway.persist(genericLogStatement)
                    .then(Mono.just(genericLogStatement))
                    .doOnSuccess(value -> logger.info("Log GenericLogStatement: {}", value))
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } else {
            return Mono.just(new GenericLogStatement()).doOnSuccess(value -> logger.info(
                    "GenericLogStatementByPlugin is Disabled"));
        }
    }

    /**
     * workspace log statement
     *
     * @param pluginId
     * @param version
     * @param level
     * @param message
     * @param args
     * @param pluginContext
     * @param elementId
     * @param elementType
     * @param projectId
     * @return mono of WorkspaceLogStatement object
     * @throws PluginLogException if WorkspaceLogStatement object not found in the list
     */
    @Trace(async = true)
    public Mono<WorkspaceLogStatement> logWorkspacePluginStatement(final UUID pluginId,
                                                                   final String version,
                                                                   final PluginLogLevel level,
                                                                   final String message,
                                                                   final String args,
                                                                   final String pluginContext,
                                                                   final UUID elementId,
                                                                   final CoursewareElementType elementType,
                                                                   final UUID projectId) throws PluginLogException {
        if (pluginLogConfig.getEnabled()) {
            BucketCollection bucketCollection = pluginLogBucketProvider.get(PluginLogContext.WORKSPACE);
            List<LogBucketInstance> logBucketInstanceList = bucketCollection.getLogBucketInstances();

            WorkspaceLogStatement workspaceLogStatement = logBucketInstanceList.stream()
                    .filter(value -> value.getTableName().equals(WORKSPACE_LOG_STATEMENT_BY_PLUGIN))
                    .map(value -> {
                        UUID id = UUIDs.timeBased();
                        return new WorkspaceLogStatement()
                                .setElementId(elementId)
                                .setElementType(elementType)
                                .setProjectId(projectId)
                                .setId(id)
                                .setBucketId(value.getBucketId())
                                .setLoggingContext(PluginLogContext.WORKSPACE)
                                .setPluginId(pluginId)
                                .setVersion(version)
                                .setLevel(level)
                                .setMessage(message)
                                .setArgs(args)
                                .setPluginContext(pluginContext);
                    })
                    .findAny()
                    .orElseThrow(() -> new PluginLogException(String.format(
                            "Error finding WORKSPACE_LOG_STATEMENT_BY_PLUGIN in list: %s",
                            logBucketInstanceList)));

            return pluginLogGateway.persist(workspaceLogStatement)
                    .then(logGenericPluginStatement(logBucketInstanceList, workspaceLogStatement))
                    .then(Mono.just(workspaceLogStatement))
                    .doOnSuccess(value -> logger.info("Log WorkspaceLogStatement: {}", value))
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } else {
            return Mono.just(new WorkspaceLogStatement()).doOnSuccess(value -> logger.info(
                    "WorkspaceLogStatementByPlugin is Disabled"));
        }
    }

    /**
     * learnspace log statement
     *
     * @param pluginId
     * @param version
     * @param level
     * @param message
     * @param args
     * @param pluginContext
     * @param elementId
     * @param elementType
     * @param deploymentId
     * @param cohortId
     * @return mono of LearnspaceLogStatement object
     * @throws PluginLogException if LearnspaceLogStatement object not found in the list
     */
    @Trace(async = true)
    public Mono<LearnspaceLogStatement> logLearnspacePluginStatement(final UUID pluginId,
                                                                     final String version,
                                                                     final PluginLogLevel level,
                                                                     final String message,
                                                                     final String args,
                                                                     final String pluginContext,
                                                                     final UUID elementId,
                                                                     final CoursewareElementType elementType,
                                                                     final UUID deploymentId,
                                                                     final UUID cohortId) throws PluginLogException {
        if (pluginLogConfig.getEnabled()) {
            BucketCollection bucketCollection = pluginLogBucketProvider.get(PluginLogContext.LEARNSPACE);
            List<LogBucketInstance> logBucketInstanceList = bucketCollection.getLogBucketInstances();
            LearnspaceLogStatement learnspaceLogStatement = logBucketInstanceList.stream()
                    .filter(value -> value.getTableName().equals(LEARNSPACE_LOG_STATEMENT_BY_PLUGIN))
                    .map(value -> {
                        UUID id = UUIDs.timeBased();
                        return new LearnspaceLogStatement()
                                .setElementId(elementId)
                                .setElementType(elementType)
                                .setDeploymentId(deploymentId)
                                .setCohortId(cohortId)
                                .setId(id)
                                .setBucketId(value.getBucketId())
                                .setLoggingContext(PluginLogContext.LEARNSPACE)
                                .setPluginId(pluginId)
                                .setVersion(version)
                                .setLevel(level)
                                .setMessage(message)
                                .setArgs(args)
                                .setPluginContext(pluginContext);
                    })
                    .findAny()
                    .orElseThrow(() -> new PluginLogException(String.format(
                            "Error finding LEARNSPACE_LOG_STATEMENT_BY_PLUGIN in list: %s",
                            logBucketInstanceList)));

            return pluginLogGateway.persist(learnspaceLogStatement)
                    .then(logGenericPluginStatement(logBucketInstanceList, learnspaceLogStatement))
                    .then(Mono.just(learnspaceLogStatement))
                    .doOnSuccess(value -> logger.info("Log LearnspaceLogStatement: {}", value))
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } else {
            return Mono.just(new LearnspaceLogStatement()).doOnSuccess(value -> logger.info(
                    "LearnspaceLogStatementByPlugin is Disabled"));
        }
    }

    /**
     * update PluginLogConfig
     *
     * @param tableName
     * @param enabled
     * @param maxRecordCount
     * @param retentionPolicy
     * @param logBucketInstances
     * @return mono of PluginLogConfig object
     */
    @Trace(async = true)
    public Mono<PluginLogConfig> updatePluginLogConfig(final String tableName,
                                                       final Boolean enabled,
                                                       final Long maxRecordCount,
                                                       final BucketRetentionPolicy retentionPolicy,
                                                       final Long logBucketInstances) throws PluginLogException {
        if (!enabled) {
            return pluginLogBucketProvider.clearRedisBucketCache()
                    .then(Mono.just(pluginLogConfig.setEnabled(false)))
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } else {
            BucketConfig bucketConfig = pluginLogConfig.getBucketConfigs().stream()
                    .filter(value -> value.getTableName().equals(tableName))
                    .findFirst()
                    .orElseThrow(() -> new PluginLogException(String.format(
                            "Error finding tableName %s in list: %s",
                            tableName, pluginLogConfig.getBucketConfigs())));

            if (maxRecordCount != null) {
                bucketConfig.setMaxRecordCount(maxRecordCount);
            }
            if (retentionPolicy != null) {
                bucketConfig.setRetentionPolicy(retentionPolicy);
            }
            if (logBucketInstances != null) {
                bucketConfig.setLogBucketInstances(logBucketInstances);
            }

            List<BucketConfig> bucketConfigList = pluginLogConfig.getBucketConfigs().stream()
                    .map(config -> config.getTableName().equals(tableName) ? bucketConfig : config)
                    .collect(Collectors.toList());

            return Mono.just(pluginLogConfig.setEnabled(true).setBucketConfigs(bucketConfigList))
                    .doOnNext(config -> pluginLogBucketProvider.resetRoundRobin(tableName))
                    .doOnSuccess(value -> logger.info("Update PluginLogConfig: {}", value))
                    .doOnEach(ReactiveTransaction.linkOnNext());
        }
    }
}
