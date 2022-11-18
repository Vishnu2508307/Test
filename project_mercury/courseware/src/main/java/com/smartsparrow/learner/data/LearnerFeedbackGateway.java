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
public class LearnerFeedbackGateway {

    private static final Logger log = LoggerFactory.getLogger(LearnerFeedbackGateway.class);

    private final Session session;

    private final LearnerFeedbackMutator learnerFeedbackMutator;
    private final LearnerFeedbackMaterializer learnerFeedbackMaterializer;
    private final ParentInteractiveByLearnerFeedbackMutator parentInteractiveByLearnerFeedbackMutator;
    private final ParentInteractiveByLearnerFeedbackMaterializer parentInteractiveByLearnerFeedbackMaterializer;
    private final LearnerElementMutator learnerElementMutator;

    @Inject
    public LearnerFeedbackGateway(Session session,
                                  LearnerFeedbackMutator learnerFeedbackMutator,
                                  LearnerFeedbackMaterializer learnerFeedbackMaterializer,
                                  ParentInteractiveByLearnerFeedbackMutator parentInteractiveByLearnerFeedbackMutator,
                                  ParentInteractiveByLearnerFeedbackMaterializer parentInteractiveByLearnerFeedbackMaterializer,
                                  LearnerElementMutator learnerElementMutator) {
        this.session = session;
        this.learnerFeedbackMutator = learnerFeedbackMutator;
        this.learnerFeedbackMaterializer = learnerFeedbackMaterializer;
        this.parentInteractiveByLearnerFeedbackMutator = parentInteractiveByLearnerFeedbackMutator;
        this.parentInteractiveByLearnerFeedbackMaterializer = parentInteractiveByLearnerFeedbackMaterializer;
        this.learnerElementMutator = learnerElementMutator;
    }

    /**
     * Persist a learner feedback
     *
     * @param learnerFeedback the object to persist
     * @return a flux of void
     */
    public Flux<Void> persist(LearnerFeedback learnerFeedback) {
        LearnerCoursewareElement element = new LearnerCoursewareElement()
                .setId(learnerFeedback.getId())
                .setDeploymentId(learnerFeedback.getDeploymentId())
                .setChangeId(learnerFeedback.getChangeId())
                .setElementType(learnerFeedback.getElementType());

        return Mutators.execute(session, Flux.just(
                learnerFeedbackMutator.upsert(learnerFeedback),
                learnerElementMutator.upsert(element)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find a learner feedback based on the latest change id
     *
     * @param feedbackId the feedback to find
     * @param deploymentId the deployment id associated with the feedback
     * @return a mono of learner feedback
     */
    public Mono<LearnerFeedback> findLatestDeployed(UUID feedbackId, UUID deploymentId) {
        return ResultSets.query(session, learnerFeedbackMaterializer.findLatestDeployed(feedbackId, deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerFeedbackMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the parent relationship between a learner feedback and its parent interactive
     *
     * @param learnerParentElement the object that describes the relationship
     * @return a flux of void
     */
    public Flux<Void> persistParentInteractive(LearnerParentElement learnerParentElement) {
        return Mutators.execute(session, Flux.just(
                parentInteractiveByLearnerFeedbackMutator.upsert(learnerParentElement)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the parent interactive for a learner feedback based on the latest change id
     *
     * @param feedbackId the feedback to find the parent interactive for
     * @param deploymentId the deployment associated with the feedback
     * @return a mono of uuid representing the parent interactive id
     */
    @Trace(async = true)
    public Mono<UUID> findParentInteractive(UUID feedbackId, UUID deploymentId) {
        return ResultSets.query(session,
                parentInteractiveByLearnerFeedbackMaterializer.findLatestDeployed(feedbackId, deploymentId))
                .flatMapIterable(row -> row)
                .map(parentInteractiveByLearnerFeedbackMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
