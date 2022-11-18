package com.smartsparrow.learner.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerActivityGateway {

    private static final Logger log = LoggerFactory.getLogger(LearnerActivityGateway.class);

    private final Session session;

    private final LearnerActivityByDeploymentMutator learnerActivityByDeploymentMutator;
    private final LearnerActivityByDeploymentMaterializer learnerActivityByDeploymentMaterializer;
    private final ParentPathwayByLearnerActivityMutator parentPathwayByLearnerActivityMutator;
    private final ParentPathwayByLearnerActivityMaterializer parentPathwayByLearnerActivityMaterializer;
    private final ChildPathwayByLearnerActivityMutator childPathwayByLearnerActivityMutator;
    private final ChildPathwayByLearnerActivityMaterializer childPathwayByLearnerActivityMaterializer;
    private final ChildComponentByLearnerActivityMutator childComponentByLearnerActivityMutator;
    private final ChildComponentByLearnerActivityMaterializer childComponentByLearnerActivityMaterializer;
    private final LearnerWalkableByStudentScopeMutator learnerWalkableByStudentScopeMutator;
    private final LearnerElementMutator learnerElementMutator;

    @Inject
    public LearnerActivityGateway(Session session,
                                  LearnerActivityByDeploymentMutator learnerActivityByDeploymentMutator,
                                  LearnerActivityByDeploymentMaterializer learnerActivityByDeploymentMaterializer,
                                  ParentPathwayByLearnerActivityMutator parentPathwayByLearnerActivityMutator,
                                  ParentPathwayByLearnerActivityMaterializer parentPathwayByLearnerActivityMaterializer,
                                  ChildPathwayByLearnerActivityMutator childPathwayByLearnerActivityMutator,
                                  ChildPathwayByLearnerActivityMaterializer childPathwayByLearnerActivityMaterializer,
                                  ChildComponentByLearnerActivityMutator childComponentByLearnerActivityMutator,
                                  ChildComponentByLearnerActivityMaterializer childComponentByLearnerActivityMaterializer,
                                  LearnerWalkableByStudentScopeMutator learnerWalkableByStudentScopeMutator,
                                  LearnerElementMutator learnerElementMutator) {
        this.session = session;
        this.parentPathwayByLearnerActivityMutator = parentPathwayByLearnerActivityMutator;
        this.parentPathwayByLearnerActivityMaterializer = parentPathwayByLearnerActivityMaterializer;
        this.childPathwayByLearnerActivityMutator = childPathwayByLearnerActivityMutator;
        this.childPathwayByLearnerActivityMaterializer = childPathwayByLearnerActivityMaterializer;
        this.childComponentByLearnerActivityMutator = childComponentByLearnerActivityMutator;
        this.childComponentByLearnerActivityMaterializer = childComponentByLearnerActivityMaterializer;
        this.learnerWalkableByStudentScopeMutator = learnerWalkableByStudentScopeMutator;
        this.learnerElementMutator = learnerElementMutator;
        this.learnerActivityByDeploymentMutator = learnerActivityByDeploymentMutator;
        this.learnerActivityByDeploymentMaterializer = learnerActivityByDeploymentMaterializer;
    }

    /**
     * Persist a learner activity
     *
     * @param learnerActivity the object to persist
     * @return a flux of void
     */
    @SuppressWarnings("Duplicates")
    public Flux<Void> persist(LearnerActivity learnerActivity, Deployment deployment) {
        LearnerCoursewareElement activityElement = new LearnerCoursewareElement()
                .setId(learnerActivity.getId())
                .setDeploymentId(deployment.getId())
                .setChangeId(deployment.getChangeId())
                .setElementType(CoursewareElementType.ACTIVITY);

        return Mutators.execute(session, Flux.just(
                learnerActivityByDeploymentMutator.upsert(learnerActivity),
                learnerWalkableByStudentScopeMutator.persist(learnerActivity, deployment),
                learnerElementMutator.upsert(activityElement)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the latest deployed activity by id
     *
     * @param activityId the activity id to find
     * @param deploymentId the activity deployment id
     * @return a mono of learner activity
     */
    public Mono<LearnerActivity> findLatest(UUID activityId, UUID deploymentId) {
        return ResultSets.query(session, learnerActivityByDeploymentMaterializer.findByDeployment(activityId, deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerActivityByDeploymentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the relationship between a deployed activity and its parent pathway
     *
     * @param learnerParentElement the object including the relationship information
     * @return a flux of void
     */
    public Flux<Void> persistParentPathway(LearnerParentElement learnerParentElement) {
        return Mutators.execute(session, Flux.just(
                parentPathwayByLearnerActivityMutator.upsert(learnerParentElement)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the parent pathway id for a deployed activity based on the latest change id
     *
     * @param activityId the activity id to find the parent pathway id for
     * @param deploymentId the deployment id associated to the activity
     * @return a mono of uuid
     */
    @Trace(async = true)
    public Mono<UUID> findParentPathwayId(UUID activityId, UUID deploymentId) {
        return ResultSets.query(session,
                parentPathwayByLearnerActivityMaterializer.findLatestByDeployment(activityId, deploymentId))
                .flatMapIterable(row -> row)
                .map(parentPathwayByLearnerActivityMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the child relationship between a deployed activity and its children pathway
     *
     * @return a flux of void
     */
    public Flux<Void> persistChildPathway(UUID activityId, UUID deploymentId, UUID changeId, UUID pathwayId) {
        return Mutators.execute(session, Flux.just(
                childPathwayByLearnerActivityMutator.addChild(
                        activityId,
                        deploymentId,
                        changeId,
                        pathwayId
                )
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the children pathway ids for a deployed activity based on the latest change id
     *
     * @param activityId the activity to find the children pathway for
     * @param deploymentId the deployment id associated with the activity
     * @return a mono list of uuid representing the pathway ids
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildPathwayIds(UUID activityId, UUID deploymentId) {
        return ResultSets.query(session,
                childPathwayByLearnerActivityMaterializer.findLatestByDeployment(activityId, deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(childPathwayByLearnerActivityMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the child relationship between a deployed activity and its component children
     *
     * @param activityId the parent activity id
     * @param deploymentId the deployment the activity has been published to
     * @param changeId the change id of the deployment
     * @param componentId the component id to save as child
     * @return a flux of void
     */
    public Flux<Void> persistChildComponent(UUID activityId, UUID deploymentId, UUID changeId, UUID componentId) {
        return Mutators.execute(session, Flux.just(
                childComponentByLearnerActivityMutator.addChild(
                        activityId,
                        deploymentId,
                        changeId,
                        componentId
                )
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the children component ids for a deployed activity
     *
     * @param activityId the activity to search the children component for
     * @param deploymentId the deployment id associated with the activity
     * @return a mono list of uuid representing the component ids
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildComponents(UUID activityId, UUID deploymentId) {
        return ResultSets.query(session,
                childComponentByLearnerActivityMaterializer.findLatestChangeFor(activityId, deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(childComponentByLearnerActivityMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find an activity by id and deploymentId
     * @param activityId the activity id
     * @param deploymentId the deployment id
     * @return a mono with LearnerActivity, empty mono if activity not found
     */
    @Trace(async = true)
    public Mono<LearnerActivity> findActivityByDeployment(UUID activityId, UUID deploymentId) {
        return ResultSets.query(session, learnerActivityByDeploymentMaterializer.findByDeployment(activityId, deploymentId))
                .flatMapIterable(row -> row)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(learnerActivityByDeploymentMaterializer::fromRow)
                .singleOrEmpty();
    }
}
