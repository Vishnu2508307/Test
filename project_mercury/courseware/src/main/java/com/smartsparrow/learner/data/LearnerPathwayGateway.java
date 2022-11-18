package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerPathwayGateway {

    private static final Logger log = LoggerFactory.getLogger(LearnerPathwayGateway.class);

    private final Session session;

    private final LearnerPathwayMutator learnerPathwayMutator;
    private final LearnerPathwayMaterializer learnerPathwayMaterializer;
    private final ChildWalkableByLearnerPathwayMutator childWalkableByLearnerPathwayMutator;
    private final ChildWalkableByLearnerPathwayMaterializer childWalkableByLearnerPathwayMaterializer;
    private final ParentActivityByLearnerPathwayMutator parentActivityByLearnerPathwayMutator;
    private final ParentActivityByLearnerPathwayMaterializer parentActivityByLearnerPathwayMaterializer;
    private final LearnerElementMutator learnerElementMutator;

    @Inject
    public LearnerPathwayGateway(Session session,
                                 LearnerPathwayMutator learnerPathwayMutator,
                                 LearnerPathwayMaterializer learnerPathwayMaterializer,
                                 ChildWalkableByLearnerPathwayMutator childWalkableByLearnerPathwayMutator,
                                 ChildWalkableByLearnerPathwayMaterializer childWalkableByLearnerPathwayMaterializer,
                                 ParentActivityByLearnerPathwayMutator parentActivityByLearnerPathwayMutator,
                                 ParentActivityByLearnerPathwayMaterializer parentActivityByLearnerPathwayMaterializer,
                                 LearnerElementMutator learnerElementMutator) {
        this.session = session;
        this.learnerPathwayMutator = learnerPathwayMutator;
        this.learnerPathwayMaterializer = learnerPathwayMaterializer;
        this.childWalkableByLearnerPathwayMutator = childWalkableByLearnerPathwayMutator;
        this.childWalkableByLearnerPathwayMaterializer = childWalkableByLearnerPathwayMaterializer;
        this.parentActivityByLearnerPathwayMutator = parentActivityByLearnerPathwayMutator;
        this.parentActivityByLearnerPathwayMaterializer = parentActivityByLearnerPathwayMaterializer;
        this.learnerElementMutator = learnerElementMutator;
    }

    /**
     * Persist a learner pathway
     *
     * @param learnerPathway the object to persist
     * @return a flux of void
     */
    public Flux<Void> persist(LearnerPathway learnerPathway) {
        LearnerCoursewareElement element = new LearnerCoursewareElement()
                .setId(learnerPathway.getId())
                .setDeploymentId(learnerPathway.getDeploymentId())
                .setChangeId(learnerPathway.getChangeId())
                .setElementType(learnerPathway.getElementType());

        return Mutators.execute(session, Flux.just(
                learnerPathwayMutator.upsert(learnerPathway),
                learnerElementMutator.upsert(element)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find a deployed pathway based on the latest change id
     *
     * @param pathwayId the pathway id to search for
     * @param deploymentId the deployment id associated with the pathway
     * @return a mono of learner pathway
     */
    @Trace(async = true)
    public Mono<LearnerPathway> findLatestDeployed(UUID pathwayId, UUID deploymentId) {
        return ResultSets.query(session, learnerPathwayMaterializer.findLatestDeployed(pathwayId, deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerPathwayMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find a deployed pathway based on the latest change id
     *
     * @param pathwayId the pathway id to search for
     * @param deploymentId the deployment id associated with the pathway
     * @return a mono of learner pathway
     */
    @Trace(async = true)
    public Flux<LearnerPathway> findPathwaysByLatestDeployed(UUID pathwayId, UUID deploymentId) {
        return ResultSets.query(session, learnerPathwayMaterializer.findPathwaysByLatestDeployed(pathwayId, deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerPathwayMaterializer::fromRow);
    }

    /**
     * Persist the relationship between a deployed pathway and its walkable children
     *
     * @param learnerWalkablePathwayChildren the object that represent the relationship
     * @return a flux of void
     */
    public Flux<Void> persistChildWalkable(LearnerWalkablePathwayChildren learnerWalkablePathwayChildren) {
        return Mutators.execute(session, Flux.just(
                childWalkableByLearnerPathwayMutator.addWalkable(learnerWalkablePathwayChildren)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the walkable children for a deployed pathway based on the latest change id
     *
     * @param pathwayId the pathway id to search the children for
     * @param deploymentId the deployment associated with the pathway
     * @return a mono containing the object that describes the walkable children
     */
    @Trace(async = true)
    public Mono<LearnerWalkablePathwayChildren> findWalkableChildren(UUID pathwayId, UUID deploymentId) {
        return ResultSets.query(session,
                childWalkableByLearnerPathwayMaterializer.findLatestByDeployment(pathwayId, deploymentId))
                .flatMapIterable(row -> row)
                .map(childWalkableByLearnerPathwayMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the relationship between a deployed pathway and its parent learner activity
     *
     * @param learnerParentElement the object that represent the relationship
     * @return a flux of void
     */
    public Flux<Void> persistParentActivity(LearnerParentElement learnerParentElement) {
        return Mutators.execute(session, Flux.just(
                parentActivityByLearnerPathwayMutator.upsert(learnerParentElement)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the parent learner activity for a deployed pathway based on the latest change id
     *
     * @param pathwayId the pathway id to find the parent for
     * @param deploymentId the deployment id associated with the pathway
     * @return a mono of uuid representing the parent activity id
     */
    @Trace(async = true)
    public Mono<UUID> findParentActivityId(UUID pathwayId, UUID deploymentId) {
        return ResultSets.query(session,
                parentActivityByLearnerPathwayMaterializer.findByLatestDeployment(pathwayId, deploymentId))
                .flatMapIterable(row -> row)
                .map(parentActivityByLearnerPathwayMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
