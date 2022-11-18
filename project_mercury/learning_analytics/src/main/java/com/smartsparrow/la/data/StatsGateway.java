package com.smartsparrow.la.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class StatsGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(StatsGateway.class);

    private final Session session;
    private final ProgressStatsByDeploymentMaterializer progressStatsByDeploymentMaterializer;
    private final ProgressStatsByDeploymentMutator progressStatsByDeploymentMutator;

    @Inject
    public StatsGateway(final Session session,
                        final ProgressStatsByDeploymentMaterializer progressStatsByDeploymentMaterializer,
                        final ProgressStatsByDeploymentMutator progressStatsByDeploymentMutator) {
        this.session = session;
        this.progressStatsByDeploymentMaterializer = progressStatsByDeploymentMaterializer;
        this.progressStatsByDeploymentMutator = progressStatsByDeploymentMutator;
    }

    /**
     * Find all progress stats for a deployment and a courseware element
     *
     * @param deploymentId - the identifier for a deployment
     * @param coursewareElementId - the courseware element id for which the stats are computed
     * @return a flux of {@link ProgressStatsByDeployment}
     */
    @Trace(async = true)
    public Flux<ProgressStatsByDeployment> findProgressStatsByDeploymentAndCoursewareElement(UUID deploymentId,
                                                                                             UUID coursewareElementId) {
        return ResultSets.query(session,
                                progressStatsByDeploymentMaterializer
                                        .findByDeploymentAndCoursewareElement(deploymentId, coursewareElementId))
                .flatMapIterable(row -> row)
                .map(progressStatsByDeploymentMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching stats for deployment %s and elementId %s",
                                            deploymentId, coursewareElementId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all progress stats for a deployment and a courseware element
     *
     * @param deploymentId - the identifier for a deployment
     * @param coursewareElementId - the courseware element id for which the stats are computed
     * @param type - the statistic type {@link StatType}
     * @return a flux of {@link ProgressStatsByDeployment}
     */
    @Trace(async = true)
    public Flux<ProgressStatsByDeployment> findProgressStatsByDeploymentCoursewareElementStatType(UUID deploymentId,
                                                                                                  UUID coursewareElementId,
                                                                                                  StatType type) {
        return ResultSets.query(session,
                                progressStatsByDeploymentMaterializer
                                        .findByDeploymentAndCoursewareElementAndStatType(deploymentId,
                                                                                         coursewareElementId,
                                                                                         type))
                .flatMapIterable(row -> row)
                .map(progressStatsByDeploymentMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching stats for deployment %s, elementId %s and statType %s",
                                            deploymentId, coursewareElementId, type), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist the progress statistics for a deployment
     *
     * @param progressStatsByDeployment the progress stats by deployment object {@link ProgressStatsByDeployment}
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(ProgressStatsByDeployment progressStatsByDeployment) {
        return Mutators.execute(session,
                                Flux.just(progressStatsByDeploymentMutator.upsert(progressStatsByDeployment)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
