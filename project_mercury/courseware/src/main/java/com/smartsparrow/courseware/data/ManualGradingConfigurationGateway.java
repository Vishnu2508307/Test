package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.learner.data.LearnerManualGradingComponentByWalkable;
import com.smartsparrow.learner.data.LearnerManualGradingComponentByWalkableMaterializer;
import com.smartsparrow.learner.data.LearnerManualGradingComponentByWalkableMutator;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ManualGradingConfigurationGateway {

    private final static Logger log = LoggerFactory.getLogger(ManualGradingConfigurationGateway.class);

    private final Session session;

    private final ManualGradingConfigurationMaterializer manualGradingConfigurationMaterializer;
    private final ManualGradingConfigurationMutator manualGradingConfigurationMutator;
    private final LearnerManualGradingConfigurationMaterializer learnerManualGradingConfigurationMaterializer;
    private final LearnerManualGradingConfigurationMutator learnerManualGradingConfigurationMutator;
    private final ManualGradingComponentByWalkableMaterializer manualGradingComponentByWalkableMaterializer;
    private final ManualGradingComponentByWalkableMutator manualGradingComponentByWalkableMutator;
    private final LearnerManualGradingComponentByWalkableMaterializer learnerManualGradingComponentByWalkableMaterializer;
    private final LearnerManualGradingComponentByWalkableMutator learnerManualGradingComponentByWalkableMutator;

    @Inject
    public ManualGradingConfigurationGateway(Session session,
                                             ManualGradingConfigurationMaterializer manualGradingConfigurationMaterializer,
                                             ManualGradingConfigurationMutator manualGradingConfigurationMutator,
                                             LearnerManualGradingConfigurationMaterializer learnerManualGradingConfigurationMaterializer,
                                             LearnerManualGradingConfigurationMutator learnerManualGradingConfigurationMutator,
                                             ManualGradingComponentByWalkableMaterializer manualGradingComponentByWalkableMaterializer,
                                             ManualGradingComponentByWalkableMutator manualGradingComponentByWalkableMutator,
                                             LearnerManualGradingComponentByWalkableMaterializer learnerManualGradingComponentByWalkableMaterializer,
                                             LearnerManualGradingComponentByWalkableMutator learnerManualGradingComponentByWalkableMutator) {
        this.session = session;
        this.manualGradingConfigurationMaterializer = manualGradingConfigurationMaterializer;
        this.manualGradingConfigurationMutator = manualGradingConfigurationMutator;
        this.learnerManualGradingConfigurationMaterializer = learnerManualGradingConfigurationMaterializer;
        this.learnerManualGradingConfigurationMutator = learnerManualGradingConfigurationMutator;
        this.manualGradingComponentByWalkableMaterializer = manualGradingComponentByWalkableMaterializer;
        this.manualGradingComponentByWalkableMutator = manualGradingComponentByWalkableMutator;
        this.learnerManualGradingComponentByWalkableMaterializer = learnerManualGradingComponentByWalkableMaterializer;
        this.learnerManualGradingComponentByWalkableMutator = learnerManualGradingComponentByWalkableMutator;
    }

    /**
     * Persist a manual grading configuration obj to the database
     *
     * @param manualGradingConfiguration the obj to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ManualGradingConfiguration manualGradingConfiguration) {
        return Mutators.execute(session, Flux.just(
                manualGradingConfigurationMutator.upsert(manualGradingConfiguration)
        )).doOnEach(ReactiveTransaction.linkOnNext())
        .doOnError(throwable -> {
            log.error("error persisting the manual grading configuration", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Delete a manual grading configuration obj from the database
     *
     * @param manualGradingConfiguration the obj to delete
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(final ManualGradingConfiguration manualGradingConfiguration) {
        return Mutators.execute(session, Flux.just(
                manualGradingConfigurationMutator.delete(manualGradingConfiguration)
        )).doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error("error deleting the manual grading configuration", throwable);
                    throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the manual grading configuration for a component
     *
     * @param componentId the component id to find the manual grading configurations for
     * @return a mono of manual grading configuration or an empty mono when the configurations are not found
     */
    @Trace(async = true)
    public Mono<ManualGradingConfiguration> find(final UUID componentId) {
        return ResultSets.query(session, manualGradingConfigurationMaterializer.find(componentId))
                .flatMapIterable(row -> row)
                .map(manualGradingConfigurationMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a learner manual grading configuration
     *
     * @param manualGradingConfiguration the manual grading configuration to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final LearnerManualGradingConfiguration manualGradingConfiguration) {
        return Mutators.execute(session, Flux.just(
                learnerManualGradingConfigurationMutator.upsert(manualGradingConfiguration)
        ));
    }

    /**
     * Find all the manual grading configuration in a deployment
     *
     * @param deploymentId the deployment id to find the manual grading configurations for
     * @return a flux of manual grading configurations
     */
    @Trace(async = true)
    public Flux<LearnerManualGradingConfiguration> findAll(final UUID deploymentId) {
        return ResultSets.query(session, learnerManualGradingConfigurationMaterializer.findAll(deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerManualGradingConfigurationMaterializer::fromRow);
    }

    /**
     * Find a manual grading configuration for a component in a deployment
     *
     * @param deploymentId the deployment the component belongs to
     * @param componentId the component id to find the configurations for
     * @return a mono of learner manual grading configuration or empty mono when not found
     */
    @Trace(async = true)
    public Mono<LearnerManualGradingConfiguration> find(final UUID deploymentId, final UUID componentId) {
        return ResultSets.query(session, learnerManualGradingConfigurationMaterializer.find(deploymentId, componentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerManualGradingConfigurationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist tracking of a manual grading component by an ancestor walkable (courseware context)
     *
     * @param manualGradingComponentByWalkable the relationship to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ManualGradingComponentByWalkable manualGradingComponentByWalkable) {
        return Mutators.execute(session, Flux.just(
                manualGradingComponentByWalkableMutator.upsert(manualGradingComponentByWalkable)
        )).doOnEach(ReactiveTransaction.linkOnNext())
        .doOnError(throwable -> {
            log.error("error persisting the manual grading component by walkable", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Delete tracking of a manual grading component by an ancestor walkable (courseware context)
     *
     * @param manualGradingComponentByWalkable the relationship to track
     * @return a flux of void
     */
    public Flux<Void> delete(final ManualGradingComponentByWalkable manualGradingComponentByWalkable) {
        return Mutators.execute(session, Flux.just(
                manualGradingComponentByWalkableMutator.delete(manualGradingComponentByWalkable)
        )).doOnError(throwable -> {
            log.error("error deleting the manual grading component by walkable", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find all manual grading component descendants from a walkable
     *
     * @param walkableId the walkable to find the descendants component for
     * @return a flux of descendant manual grading components
     */
    @Trace(async = true)
    public Flux<ManualGradingComponentByWalkable> findManualComponentsByWalkable(final UUID walkableId) {
        return ResultSets.query(session, manualGradingComponentByWalkableMaterializer.findAll(walkableId))
                .flatMapIterable(row -> row)
                .map(manualGradingComponentByWalkableMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist tracking of a manual grading component by an ancestor walkable (learner context)
     *
     * @param learnerManualGradingComponentByWalkable the relationship to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final LearnerManualGradingComponentByWalkable learnerManualGradingComponentByWalkable) {
        return Mutators.execute(session, Flux.just(
                learnerManualGradingComponentByWalkableMutator.upsert(learnerManualGradingComponentByWalkable)
        )).doOnError(throwable -> {
            log.error("error persisting the learner manual grading component by walkable", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find all the manual grading component descendants from a walkable
     *
     * @param deploymentId the deployment the walkable belongs to
     * @param walkableId the id of the walkable to find the descendants manual components for
     * @return a flux of manual grading components
     */
    @Trace(async = true)
    public Flux<LearnerManualGradingComponentByWalkable> findManualGradingComponentsByWalkable(final UUID deploymentId, final UUID walkableId) {
        return ResultSets.query(session, learnerManualGradingComponentByWalkableMaterializer.findAll(deploymentId, walkableId))
                .flatMapIterable(row -> row)
                .map(learnerManualGradingComponentByWalkableMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
