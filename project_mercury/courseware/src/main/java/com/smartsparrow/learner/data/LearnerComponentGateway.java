package com.smartsparrow.learner.data;

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
public class LearnerComponentGateway {

    private static final Logger log = LoggerFactory.getLogger(LearnerComponentGateway.class);

    private final Session session;

    private final LearnerComponentMutator learnerComponentMutator;
    private final LearnerComponentMaterializer learnerComponentMaterializer;
    private final ParentByLearnerComponentMutator parentByLearnerComponentMutator;
    private final ParentByLearnerComponentMaterializer parentByLearnerComponentMaterializer;
    private final LearnerElementMutator learnerElementMutator;

    @Inject
    public LearnerComponentGateway(Session session,
                                   LearnerComponentMutator learnerComponentMutator,
                                   LearnerComponentMaterializer learnerComponentMaterializer,
                                   ParentByLearnerComponentMutator parentByLearnerComponentMutator,
                                   ParentByLearnerComponentMaterializer parentByLearnerComponentMaterializer,
                                   LearnerElementMutator learnerElementMutator) {
        this.session = session;
        this.learnerComponentMutator = learnerComponentMutator;
        this.learnerComponentMaterializer = learnerComponentMaterializer;
        this.parentByLearnerComponentMutator = parentByLearnerComponentMutator;
        this.parentByLearnerComponentMaterializer = parentByLearnerComponentMaterializer;
        this.learnerElementMutator = learnerElementMutator;
    }

    /**
     * Persist a deployed component
     *
     * @param learnerComponent the object to persist
     * @return a flux of void
     */
    public Flux<Void> persist(LearnerComponent learnerComponent) {
        LearnerCoursewareElement element = new LearnerCoursewareElement()
                .setId(learnerComponent.getId())
                .setDeploymentId(learnerComponent.getDeploymentId())
                .setChangeId(learnerComponent.getChangeId())
                .setElementType(learnerComponent.getElementType());

        return Mutators.execute(session, Flux.just(
                learnerComponentMutator.upsert(learnerComponent),
                learnerElementMutator.upsert(element)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find a deployed component based on the latest change id
     *
     * @param componentId the component to find
     * @param deploymentId the deployment associated with the component
     * @return a mono of learner component
     */
    @Trace(async = true)
    public Mono<LearnerComponent> findLatestDeployed(UUID componentId, UUID deploymentId) {
        return ResultSets.query(session, learnerComponentMaterializer.findLatestDeployed(componentId, deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerComponentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the relationship between a deployed component and its parent
     *
     * @param parentByLearnerComponent the object representing the parent relationship
     * @return a flux of void
     */
    public Flux<Void> persistParent(ParentByLearnerComponent parentByLearnerComponent) {
        return Mutators.execute(session, Flux.just(
                parentByLearnerComponentMutator.upsert(parentByLearnerComponent)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the parent for a deployed component based on the latest change id
     *
     * @param componentId the component to find the parent for
     * @param deploymentId the deployment associated with the component
     * @return a mono of parent by learner component object
     */
    @Trace(async = true)
    public Mono<ParentByLearnerComponent> findParent(UUID componentId, UUID deploymentId) {
        return ResultSets.query(session,
                parentByLearnerComponentMaterializer.findByLatestDeployment(componentId, deploymentId))
                .flatMapIterable(row -> row)
                .map(parentByLearnerComponentMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
