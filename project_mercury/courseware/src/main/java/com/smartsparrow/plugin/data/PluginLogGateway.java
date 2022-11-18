package com.smartsparrow.plugin.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

@Singleton
public class PluginLogGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PluginLogGateway.class);

    private final Session session;

    private final GenericLogStatementMutator genericLogStatementMutator;
    private final GenericLogStatementMaterializer genericLogStatementMaterializer;
    private final LearnspaceLogStatementMutator learnspaceLogStatementMutator;
    private final LearnspaceLogStatementMaterializer learnspaceLogStatementMaterializer;
    private final LogBucketMutator logBucketMutator;
    private final LogBucketMaterializer logBucketMaterializer;
    private final WorkspaceLogStatementMutator workspaceLogStatementMutator;
    private final WorkspaceLogStatementMaterializer workspaceLogStatementMaterializer;

    @Inject
    public PluginLogGateway(Session session,
                            GenericLogStatementMutator genericLogStatementMutator,
                            GenericLogStatementMaterializer genericLogStatementMaterializer,
                            LearnspaceLogStatementMutator learnspaceLogStatementMutator,
                            LearnspaceLogStatementMaterializer learnspaceLogStatementMaterializer,
                            LogBucketMutator logBucketMutator,
                            LogBucketMaterializer logBucketMaterializer,
                            WorkspaceLogStatementMutator workspaceLogStatementMutator,
                            WorkspaceLogStatementMaterializer workspaceLogStatementMaterializer) {
        this.session = session;
        this.genericLogStatementMutator = genericLogStatementMutator;
        this.genericLogStatementMaterializer = genericLogStatementMaterializer;
        this.learnspaceLogStatementMutator = learnspaceLogStatementMutator;
        this.learnspaceLogStatementMaterializer = learnspaceLogStatementMaterializer;
        this.logBucketMutator = logBucketMutator;
        this.logBucketMaterializer = logBucketMaterializer;
        this.workspaceLogStatementMutator = workspaceLogStatementMutator;
        this.workspaceLogStatementMaterializer = workspaceLogStatementMaterializer;
    }

    /**
     * Persist generic log statement
     *
     * @param genericLogStatement, the GenericLogStatement object
     */
    @Trace(async = true)
    public Flux<Void> persist(final GenericLogStatement genericLogStatement) {
        Flux<? extends Statement> iter = Mutators.upsert(genericLogStatementMutator,
                                                         genericLogStatement);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving generic log statement by plugin",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("pluginId",
                                                                 genericLogStatement.getPluginId());
                                                             put("version", genericLogStatement.getVersion());
                                                             put("bucketId",
                                                                 genericLogStatement.getBucketId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist learnspace log statement
     *
     * @param learnspaceLogStatement, the LearnspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<Void> persist(final LearnspaceLogStatement learnspaceLogStatement) {
        Flux<? extends Statement> iter = Mutators.upsert(learnspaceLogStatementMutator,
                                                         learnspaceLogStatement);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving learnspace log statement by plugin",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("pluginId",
                                                                 learnspaceLogStatement.getPluginId());
                                                             put("version",
                                                                 learnspaceLogStatement.getVersion());
                                                             put("bucketId",
                                                                 learnspaceLogStatement.getBucketId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist log Bucket information
     *
     * @param logBucket, the LogBucket object
     */
    @Trace(async = true)
    public Flux<Void> persist(final LogBucket logBucket) {
        Flux<? extends Statement> iter = Mutators.upsert(logBucketMutator,
                                                         logBucket);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving log bucket by day time",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("day", logBucket.getDay());
                                                             put("time", logBucket.getTime());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist workspace log statements
     *
     * @param workspaceLogStatement, the WorkspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<Void> persist(final WorkspaceLogStatement workspaceLogStatement) {
        Flux<? extends Statement> iter = Mutators.upsert(workspaceLogStatementMutator,
                                                         workspaceLogStatement);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving workspace log statement by plugin",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("pluginId",
                                                                 workspaceLogStatement.getPluginId());
                                                             put("version",
                                                                 workspaceLogStatement.getVersion());
                                                             put("bucketId",
                                                                 workspaceLogStatement.getBucketId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find LogBucket by day
     *
     * @param day
     * @return flux of LogBucket object
     */
    @Trace(async = true)
    public Flux<LogBucket> findLogBucketByDay(final LocalDate day) {
        return ResultSets.query(session,
                                logBucketMaterializer.fetchByDay(day))
                .flatMapIterable(row -> row)
                .map(logBucketMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find LogBucket by day and time
     *
     * @param day
     * @param time
     * @return flux of LogBucket object
     */
    @Trace(async = true)
    public Flux<LogBucket> findLogBucketByDayTime(final LocalDate day, final long time) {
        return ResultSets.query(session,
                                logBucketMaterializer.fetchByDayTime(day, time))
                .flatMapIterable(row -> row)
                .map(logBucketMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find GenericLogStatement by plugin id
     *
     * @param pluginId
     * @return flux of GenericLogStatement object
     */
    @Trace(async = true)
    public Flux<GenericLogStatement> findGenericLogStatementsByPluginId(final UUID pluginId) {
        return ResultSets.query(session,
                                genericLogStatementMaterializer.fetchByPluginId(pluginId))
                .flatMapIterable(row -> row)
                .map(genericLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find GenericLogStatement by plugin id and version
     *
     * @param pluginId
     * @param version
     * @return flux of GenericLogStatement object
     */
    @Trace(async = true)
    public Flux<GenericLogStatement> findGenericLogStatementsByPluginIdAndVersion(final UUID pluginId,
                                                                                  final String version) {
        return ResultSets.query(session,
                                genericLogStatementMaterializer.fetchByPluginIdAndVersion(pluginId, version))
                .flatMapIterable(row -> row)
                .map(genericLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find GenericLogStatement by plugin id and bucket id
     *
     * @param pluginId
     * @param bucketId
     * @return flux of GenericLogStatement object
     */
    @Trace(async = true)
    public Flux<GenericLogStatement> findGenericLogStatementsByPluginIdAndBucketId(final UUID pluginId,
                                                                                   final UUID bucketId) {
        return ResultSets.query(session,
                                genericLogStatementMaterializer.fetchByPluginIdAndBucketId(pluginId, bucketId))
                .flatMapIterable(row -> row)
                .map(genericLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find LearnspaceLogStatement by plugin id
     *
     * @param pluginId
     * @return flux of LearnspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<LearnspaceLogStatement> findLearnspaceLogStatementsByPluginId(final UUID pluginId) {
        return ResultSets.query(session,
                                learnspaceLogStatementMaterializer.fetchByPluginId(pluginId))
                .flatMapIterable(row -> row)
                .map(learnspaceLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find LearnspaceLogStatement by plugin id and version
     *
     * @param pluginId
     * @param version
     * @return flux of LearnspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<LearnspaceLogStatement> findLearnspaceLogStatementsByPluginIdAndVersion(final UUID pluginId,
                                                                                        final String version) {
        return ResultSets.query(session,
                                learnspaceLogStatementMaterializer.fetchByPluginIdAndVersion(pluginId,
                                                                                             version))
                .flatMapIterable(row -> row)
                .map(learnspaceLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find LearnspaceLogStatement by plugin id and bucket id
     *
     * @param pluginId
     * @param bucketId
     * @return flux of LearnspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<LearnspaceLogStatement> findLearnspaceLogStatementsByPluginIdAndBucketId(final UUID pluginId,
                                                                                         final UUID bucketId) {
        return ResultSets.query(session,
                                learnspaceLogStatementMaterializer.fetchByPluginIdAndBucketId(pluginId,
                                                                                              bucketId))
                .flatMapIterable(row -> row)
                .map(learnspaceLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find WorkspaceLogStatement by plugin id
     *
     * @param pluginId
     * @return flux of WorkspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<WorkspaceLogStatement> findWorkspaceLogStatementsByPluginId(final UUID pluginId) {
        return ResultSets.query(session,
                                workspaceLogStatementMaterializer.fetchByPluginId(pluginId))
                .flatMapIterable(row -> row)
                .map(workspaceLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find WorkspaceLogStatement by plugin id and version
     *
     * @param pluginId
     * @param version
     * @return flux of WorkspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<WorkspaceLogStatement> findWorkspaceLogStatementsByPluginIdAndVersion(final UUID pluginId,
                                                                                      final String version) {
        return ResultSets.query(session,
                                workspaceLogStatementMaterializer.fetchByPluginIdAndVersion(pluginId,
                                                                                            version))
                .flatMapIterable(row -> row)
                .map(workspaceLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * find WorkspaceLogStatement by plugin id and bucket id
     *
     * @param pluginId
     * @param bucketId
     * @return flux of WorkspaceLogStatement object
     */
    @Trace(async = true)
    public Flux<WorkspaceLogStatement> findWorkspaceLogStatementsByPluginIdAndBucketId(final UUID pluginId,
                                                                                       final UUID bucketId) {
        return ResultSets.query(session,
                                workspaceLogStatementMaterializer.fetchByPluginIdAndBucketId(pluginId,
                                                                                             bucketId))
                .flatMapIterable(row -> row)
                .map(workspaceLogStatementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
