package com.smartsparrow.learner.data;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DeploymentGateway {

    private static final Logger log = LoggerFactory.getLogger(DeploymentGateway.class);

    private final Session session;

    private final DeploymentMutator deploymentMutator;
    private final DeploymentMaterializer deploymentMaterializer;
    private final DeploymentByActivityMutator deploymentByActivityMutator;
    private final DeploymentByActivityMaterializer deploymentByActivityMaterializer;
    private final DeploymentByCohortMutator deploymentByCohortMutator;
    private final DeploymentByCohortMaterializer deploymentByCohortMaterializer;
    private final DeploymentByProductMutator deploymentByProductMutator;
    private final DeploymentByProductMaterializer deploymentByProductMaterializer;
    private final DeploymentMetadataMaterializer deploymentMetadataMaterializer;

    @Inject
    public DeploymentGateway(Session session,
                             DeploymentMutator deploymentMutator,
                             DeploymentMaterializer deploymentMaterializer,
                             DeploymentByActivityMutator deploymentByActivityMutator,
                             DeploymentByActivityMaterializer deploymentByActivityMaterializer,
                             DeploymentByCohortMutator deploymentByCohortMutator,
                             DeploymentByCohortMaterializer deploymentByCohortMaterializer,
                             DeploymentByProductMutator deploymentByProductMutator,
                             DeploymentByProductMaterializer deploymentByProductMaterializer,
                             final DeploymentMetadataMaterializer deploymentMetadataMaterializer) {
        this.session = session;
        this.deploymentMutator = deploymentMutator;
        this.deploymentMaterializer = deploymentMaterializer;
        this.deploymentByActivityMutator = deploymentByActivityMutator;
        this.deploymentByActivityMaterializer = deploymentByActivityMaterializer;
        this.deploymentByCohortMutator = deploymentByCohortMutator;
        this.deploymentByCohortMaterializer = deploymentByCohortMaterializer;
        this.deploymentByProductMutator = deploymentByProductMutator;
        this.deploymentByProductMaterializer = deploymentByProductMaterializer;
        this.deploymentMetadataMaterializer = deploymentMetadataMaterializer;
    }

    /**
     * Persist a deployment
     *
     * @param deployment the deployment to persist
     * @return a flux of void
     */
    public Flux<Void> persist(DeployedActivity deployment) {
        return Mutators.execute(session, Flux.just(
                deploymentMutator.upsert(deployment),
                deploymentByActivityMutator.upsert(deployment),
                deploymentByCohortMutator.insert(deployment.getCohortId(), deployment.getId())
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Persist product id to deployment id
     *
     * @param productId the product id to persist
     * @param deploymentId the deployment id to persist
     * @return a flux of void
     */
    public Flux<Void> persist(String productId, UUID deploymentId) {
        return Mutators.execute(session, Flux.just(
                deploymentByProductMutator.upsert(productId, deploymentId)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Persist a cohort to deployment association
     *
     * @param cohortId the deployment to persist
     * @param deploymentId the deployment to persist
     * @return a flux of void
     */
    public Flux<Void> persist(UUID cohortId, UUID deploymentId) {
        return Mutators.execute(session, Flux.just(
                deploymentByCohortMutator.insert(cohortId, deploymentId)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the latest version of a deployed activity
     *
     * @param id the deployment id
     * @param activityId the activity id
     * @return a mono of deployment
     */
    public Mono<DeployedActivity> findLatest(UUID id, UUID activityId) {
        return ResultSets.query(session, deploymentMaterializer.findLatestByActivity(id, activityId))
                .flatMapIterable(row -> row)
                .map(deploymentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the latest deployment by id
     *
     * @param id the deployment id
     * @return a mono of deployment
     */
    @Trace(async = true)
    public Mono<DeployedActivity> findLatest(UUID id) {
        return ResultSets.query(session, deploymentMaterializer.findLatest(id))
                .flatMapIterable(row -> row)
                .map(deploymentMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    public Mono<UUID> findLatestChangeId(UUID id) {
        return ResultSets.query(session, deploymentMaterializer.findLatestChangeIds(id, 1))
                .flatMapIterable(row -> row)
                .map(row -> row.getUUID("change_id"))
                .singleOrEmpty();
    }

    /**
     * Find the most recent changeIds for a deployment, ordered by most recent first
     *
     * @param id the deployment id
     * @param limit limit of how many changeIds to return
     */
    public Flux<UUID> findLatestChangeIds(UUID id, int limit) {
        return ResultSets.query(session, deploymentMaterializer.findLatestChangeIds(id, limit))
                .flatMapIterable(row -> row)
                .map(row -> row.getUUID("change_id"));
    }

    /**
     * Find all the deployments for an activity
     *
     * @param activityId the activity to find the deployments for
     * @return a flux of deployment
     */
    public Flux<DeployedActivity> findByActivity(UUID activityId) {
        return ResultSets.query(session, deploymentByActivityMaterializer.findByActivity(activityId))
                .flatMapIterable(row -> row)
                .map(deploymentByActivityMaterializer::fromRow);
    }

    /**
     * Find all the deployment ids for a cohort
     *
     * @param cohortId the cohort to find the deployment for
     * @return a flux of deployment ids
     */
    @Trace(async = true)
    public Flux<UUID> findByCohort(UUID cohortId) {
        return ResultSets.query(session, deploymentByCohortMaterializer.findDeployments(cohortId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row->row)
                .map(deploymentByCohortMaterializer::fromRow);
    }

    /**
     * Find the deployment id for a given product id.
     *
     * @param productId the product id to search the deployment id for
     * @return a deployment id
     * @throws NoSuchElementException if product id is found
     */
    @Trace(async = true)
    public Mono<UUID> findDeploymentId(String productId) {
        return ResultSets.query(session, deploymentByProductMaterializer.findDeployment(productId))
                .flatMapIterable(row -> row)
                .map(deploymentByProductMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     *  Get the gradepassback count by deploymentId and changeId
     *  @param deploymentId the deployment id
     * @param changeId the change id
     * @return Gradepassback questions count
     */
    public Mono<Long> findGradepassbackCount(UUID deploymentId, UUID changeId) {
        return ResultSets.query(session, deploymentMetadataMaterializer.findCountById(deploymentId, changeId))
                .flatMapIterable(row -> row)
                .map(deploymentMetadataMaterializer::fromRow)
                .singleOrEmpty();

    }

    /**
     * Find activity by deployment id
     * @param deploymentId the deployment id
     * @return mono of deployed activity object
     */
    public Mono<DeployedActivity> findActivityByDeployment(UUID deploymentId) {
        return ResultSets.query(session, deploymentMaterializer.findActivityByDeployment(deploymentId))
                .flatMapIterable(row -> row)
                .map(deploymentMaterializer::fromRow)
                .singleOrEmpty();
    }
}
