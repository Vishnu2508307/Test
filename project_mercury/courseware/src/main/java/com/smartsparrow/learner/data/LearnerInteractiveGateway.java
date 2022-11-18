package com.smartsparrow.learner.data;

import java.util.List;
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
public class LearnerInteractiveGateway {

    private static final Logger log = LoggerFactory.getLogger(LearnerInteractiveGateway.class);

    private final Session session;

    private final LearnerInteractiveMutator learnerInteractiveMutator;
    private final LearnerInteractiveMaterializer learnerInteractiveMaterializer;
    private final FeedbackByLearnerInteractiveMutator feedbackByLearnerInteractiveMutator;
    private final FeedbackByLearnerInteractiveMaterializer feedbackByLearnerInteractiveMaterializer;
    private final ParentPathwayByLearnerInteractiveMutator parentPathwayByLearnerInteractiveMutator;
    private final ParentPathwayByLearnerInteractiveMaterializer parentPathwayByLearnerInteractiveMaterializer;
    private final ChildComponentByLearnerInteractiveMutator childComponentByLearnerInteractiveMutator;
    private final ChildComponentByLearnerInteractiveMaterializer childComponentByLearnerInteractiveMaterializer;
    private final LearnerWalkableByStudentScopeMutator learnerWalkableByStudentScopeMutator;
    private final LearnerElementMutator learnerElementMutator;

    @Inject
    public LearnerInteractiveGateway(Session session,
                                     LearnerInteractiveMutator learnerInteractiveMutator,
                                     LearnerInteractiveMaterializer learnerInteractiveMaterializer,
                                     FeedbackByLearnerInteractiveMutator feedbackByLearnerInteractiveMutator,
                                     FeedbackByLearnerInteractiveMaterializer feedbackByLearnerInteractiveMaterializer,
                                     ParentPathwayByLearnerInteractiveMutator parentPathwayByLearnerInteractiveMutator,
                                     ParentPathwayByLearnerInteractiveMaterializer parentPathwayByLearnerInteractiveMaterializer,
                                     ChildComponentByLearnerInteractiveMutator childComponentByLearnerInteractiveMutator,
                                     ChildComponentByLearnerInteractiveMaterializer childComponentByLearnerInteractiveMaterializer,
                                     LearnerWalkableByStudentScopeMutator learnerWalkableByStudentScopeMutator,
                                     LearnerElementMutator learnerElementMutator) {
        this.session = session;
        this.learnerInteractiveMutator = learnerInteractiveMutator;
        this.learnerInteractiveMaterializer = learnerInteractiveMaterializer;
        this.feedbackByLearnerInteractiveMutator = feedbackByLearnerInteractiveMutator;
        this.feedbackByLearnerInteractiveMaterializer = feedbackByLearnerInteractiveMaterializer;
        this.parentPathwayByLearnerInteractiveMutator = parentPathwayByLearnerInteractiveMutator;
        this.parentPathwayByLearnerInteractiveMaterializer = parentPathwayByLearnerInteractiveMaterializer;
        this.childComponentByLearnerInteractiveMutator = childComponentByLearnerInteractiveMutator;
        this.childComponentByLearnerInteractiveMaterializer = childComponentByLearnerInteractiveMaterializer;
        this.learnerWalkableByStudentScopeMutator = learnerWalkableByStudentScopeMutator;
        this.learnerElementMutator = learnerElementMutator;
    }

    /**
     * Persist a learner interactive
     *
     * @param learnerInteractive the object to persist
     * @return a flux of void
     */
    @SuppressWarnings("Duplicates")
    public Flux<Void> persist(LearnerInteractive learnerInteractive, Deployment deployment) {
        LearnerCoursewareElement element = new LearnerCoursewareElement()
                .setId(learnerInteractive.getId())
                .setDeploymentId(deployment.getId())
                .setChangeId(deployment.getChangeId())
                .setElementType(learnerInteractive.getElementType());

        return Mutators.execute(session, Flux.just(
                learnerInteractiveMutator.upsert(learnerInteractive),
                learnerWalkableByStudentScopeMutator.persist(learnerInteractive, deployment),
                learnerElementMutator.upsert(element)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find a deployed interactive based on the latest change id
     *
     * @param interactiveId the interactive to find
     * @param deploymentId the deployment id associated with the interactive
     * @return a mono of learner interactive
     */
    @Trace(async = true)
    public Mono<LearnerInteractive> findLatestDeployed(UUID interactiveId, UUID deploymentId) {
        return ResultSets.query(session,
                learnerInteractiveMaterializer.findByLatestDeployment(interactiveId, deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerInteractiveMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the child relationship between an interactive and its children feedback
     *
     * @param feedbackId the child feedback id
     * @param parentInteractiveId the feedback parent interactive id
     * @param deploymentId the deployment the feedback has been deployed to
     * @param changeId the deployment change id
     * @return a flux of void
     */
    public Flux<Void> persistChildFeedback(UUID feedbackId, UUID parentInteractiveId, UUID deploymentId, UUID changeId) {
        return Mutators.execute(session, Flux.just(
                feedbackByLearnerInteractiveMutator.addChild(feedbackId, parentInteractiveId, deploymentId, changeId)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the children feedback ids for a deployed interactive based on the latest change id
     *
     * @param interactiveId the interactive id to search the children for
     * @param deploymentId the deployment associated with the interactive
     * @return a list of feedback ids
     */
    public Mono<List<UUID>> findChildrenFeedback(UUID interactiveId, UUID deploymentId) {
        return ResultSets.query(session,
                feedbackByLearnerInteractiveMaterializer.findByLatestDeployment(interactiveId, deploymentId))
                .flatMapIterable(row -> row)
                .map(feedbackByLearnerInteractiveMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the parent relationship between a learner interactive and its parent pathway
     *
     * @param learnerParentElement the object representing the parent relationship
     * @return a flux of void
     */
    public Flux<Void> persistParentPathway(LearnerParentElement learnerParentElement) {
        return Mutators.execute(session, Flux.just(
                parentPathwayByLearnerInteractiveMutator.upsert(learnerParentElement)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the parent pathway id for a deployed interactive based on the latest change id
     *
     * @param interactiveId the interactive to search the parent for
     * @param deploymentId the deployment associated with the interactive
     * @return a mono of uuid representing the parent pathway id
     */
    @Trace(async = true)
    public Mono<UUID> findParentPathway(UUID interactiveId, UUID deploymentId) {
        return ResultSets.query(session,
                parentPathwayByLearnerInteractiveMaterializer.findLatestDeployed(interactiveId, deploymentId))
                .flatMapIterable(row -> row)
                .map(parentPathwayByLearnerInteractiveMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the child relationship between a learner interactive and its component children
     *
     * @param interactiveId the interactive id to save the child component for
     * @param deploymentId the deployment the interactive has been published to
     * @param changeId the change id of the deployment
     * @param componentId the component to save has child
     * @return a flux of void
     */
    public Flux<Void> persistChildComponent(UUID interactiveId, UUID deploymentId, UUID changeId, UUID componentId) {
        return Mutators.execute(session, Flux.just(
                childComponentByLearnerInteractiveMutator.addChild(
                        interactiveId,
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
     * Find the children component for a deployed interactive based on the latest change id
     *
     * @param interactiveId the interactive to search the children component for
     * @param deploymentId the deployment associated with the interactive
     * @return a mono list of uuid representing the children component ids
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildrenComponent(UUID interactiveId, UUID deploymentId) {
        return ResultSets.query(session,
                childComponentByLearnerInteractiveMaterializer.findLatestChangeFor(interactiveId, deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(childComponentByLearnerInteractiveMaterializer::fromRow)
                .singleOrEmpty();
    }

}
