package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.lang.ParentInteractiveNotFoundException;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerFeedback;
import com.smartsparrow.learner.data.LearnerFeedbackGateway;
import com.smartsparrow.learner.data.LearnerInteractiveGateway;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishFeedbackException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerFeedbackService {

    private final LearnerFeedbackGateway learnerFeedbackGateway;
    private final LearnerInteractiveGateway learnerInteractiveGateway;
    private final FeedbackService feedbackService;
    private final LearnerAssetService learnerAssetService;
    private final DeploymentLogService deploymentLogService;
    private final PluginService pluginService;

    @Inject
    public LearnerFeedbackService(LearnerFeedbackGateway learnerFeedbackGateway,
                                  LearnerInteractiveGateway learnerInteractiveGateway,
                                  FeedbackService feedbackService,
                                  LearnerAssetService learnerAssetService,
                                  DeploymentLogService deploymentLogService,
                                  PluginService pluginService) {
        this.learnerFeedbackGateway = learnerFeedbackGateway;
        this.learnerInteractiveGateway = learnerInteractiveGateway;
        this.feedbackService = feedbackService;
        this.learnerAssetService = learnerAssetService;
        this.deploymentLogService = deploymentLogService;
        this.pluginService = pluginService;
    }

    /**
     * Publish all feedback children for a parent interactive. Find all feedback, convert them to learner feedback,
     * persist each one to the database along with their parent/child relationship with the parent interactive.
     *
     * @param interactiveId the interactive to deploy the feedback for
     * @param deployment the deployment to deploy the feedback to
     * @return a flux of learner feedback
     * @throws PublishFeedbackException when either:
     * <br> interactiveId is <code>null</code>
     * <br> deployment is <code>null</code>
     * <br> feedback is not found
     */
    public Flux<LearnerFeedback> publish(UUID interactiveId, DeployedActivity deployment, boolean lockPluginVersionEnabled) {

        try {
            checkArgument(interactiveId != null, "interactiveId is required");
            checkArgument(deployment != null, "deployment is required");
        } catch (IllegalArgumentException e) {
            throw new PublishFeedbackException(interactiveId, e.getMessage());
        }

        return feedbackService.findIdsByInteractive(interactiveId)
                .flatMapIterable(one -> one)
                .collectList()
                .flatMapMany(feedbackIds -> publish(interactiveId, deployment, feedbackIds, lockPluginVersionEnabled));
    }

    /**
     * Build and persist each learner feedback.
     *
     * @param parentInteractiveId the feedback parent interactive
     * @param deployment the deployment to deploy the feedback to
     * @param feedbackIds the list of feedback ids to convert to learner feedback and save
     * @return a flux of learner feedback
     */
    private Flux<LearnerFeedback> publish(UUID parentInteractiveId, DeployedActivity deployment, List<UUID> feedbackIds,
                                          boolean lockPluginVersionEnabled) {

        if (feedbackIds.isEmpty()) {
            return Flux.empty();
        }

        return feedbackIds.stream()
                .map(feedbackId -> build(feedbackId, deployment, parentInteractiveId, lockPluginVersionEnabled)
                        .flux()
                        .doOnError(throwable -> {
                            deploymentLogService.logFailedStep(deployment, feedbackId, CoursewareElementType.FEEDBACK,
                                    "[learnerFeedbackService] " + Arrays.toString(throwable.getStackTrace()))
                                    .subscribe();
                            throw Exceptions.propagate(throwable);
                        }))
                .reduce(Flux::concat)
                .orElse(Flux.empty())
                .concatMap(learnerFeedback -> persist(learnerFeedback, parentInteractiveId, deployment));
    }

    /**
     * Find feedback and feedback config then build a learner feedback object.
     *
     * @param feedbackId the feedback id to build the learner feedback from
     * @param deployment the deployment to deploy the learner feedback to
     * @return a mono of learner feedback
     * @throws PublishFeedbackException when the feedback is not found
     */
    private Mono<LearnerFeedback> build(final UUID feedbackId, final DeployedActivity deployment, UUID interactiveId,
                                        boolean lockPluginVersionEnabled) {
        Mono<Feedback> feedbackMono = feedbackService.findById(feedbackId);
        Mono<String> feedbackConfigMono = feedbackService.findLatestConfig(feedbackId)
                .defaultIfEmpty("");

        return Mono.zip(feedbackMono, feedbackConfigMono)
                .flatMap(tuple2 -> {
                    return deploymentLogService.logProgressStep(deployment, feedbackId, CoursewareElementType.FEEDBACK,
                            "[learnerFeedbackService] staring publishing feedback")
                            .thenReturn(tuple2);
                })
                .doOnError(FeedbackNotFoundException.class, ex -> {
                    throw new PublishFeedbackException(interactiveId, ex.getMessage());
                })
                .map(tuple -> new LearnerFeedback()
                        .setId(feedbackId)
                        .setDeploymentId(deployment.getId())
                        .setChangeId(deployment.getChangeId())
                        .setPluginId(tuple.getT1().getPluginId())
                        .setPluginVersionExpr(pluginService.resolvePluginVersion(tuple.getT1().getPluginId(),
                                tuple.getT1().getPluginVersionExpr(), lockPluginVersionEnabled))
                        .setConfig((tuple.getT2().isEmpty() ? null : tuple.getT2())));
    }

    /**
     * Persist a learner feedback to the database along with the parent/child relationship with its parent interactive.
     *
     * @param learnerFeedback the learner feedback to persist
     * @param parentInteractiveId the interactive id to save as learner feedback parent
     * @return a mono of learner feedback
     */
    private Mono<LearnerFeedback> persist(final LearnerFeedback learnerFeedback, final UUID parentInteractiveId,
                                          Deployment deployment) {
        final UUID learnerFeedbackId = learnerFeedback.getId();
        final UUID deploymentId = learnerFeedback.getDeploymentId();
        final UUID changeId = learnerFeedback.getChangeId();

        return learnerFeedbackGateway.persist(learnerFeedback)
                .thenMany(learnerFeedbackGateway.persistParentInteractive(new LearnerParentElement()
                        .setElementId(learnerFeedbackId)
                        .setDeploymentId(deploymentId)
                        .setChangeId(changeId)
                        .setParentId(parentInteractiveId)))
                .thenMany(learnerInteractiveGateway.persistChildFeedback(
                        learnerFeedbackId,
                        parentInteractiveId,
                        deploymentId,
                        changeId
                ))
                .then(deploymentLogService.logProgressStep(deployment, learnerFeedbackId, CoursewareElementType.FEEDBACK,
                        "[learnerFeedbackService] finished persisting parent/child relationship"))
                .thenMany(learnerAssetService.publishAssetsFor(deployment, learnerFeedbackId, CoursewareElementType.FEEDBACK))
                .then(deploymentLogService.logProgressStep(deployment, learnerFeedbackId, CoursewareElementType.FEEDBACK,
                        "[learnerFeedbackService] finished publishing assets"))
                .thenMany(learnerAssetService.publishMathAssetsFor(deployment, learnerFeedbackId, CoursewareElementType.FEEDBACK))
                .then(deploymentLogService.logProgressStep(deployment, learnerFeedbackId, CoursewareElementType.FEEDBACK,
                        "[learnerFeedbackService] finished publishing math assets"))
                .then(Mono.just(learnerFeedback));
    }

    @Trace(async = true)
    public Mono<UUID> findParentId(final UUID feedbackId, final UUID deploymentId) {
        checkArgument(feedbackId != null, "feedbackId is required");
        checkArgument(deploymentId != null, "deploymentId is required");

        return learnerFeedbackGateway.findParentInteractive(feedbackId, deploymentId)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ParentInteractiveNotFoundException(feedbackId);
                });
    }

    /**
     * Find the learner feedback by id
     *
     * @param feedbackId the feedback id
     * @param deploymentId the deployment id
     * @return a mono with the learner feedback or an empty mono when not found
     */
    public Mono<LearnerFeedback> findFeedback(UUID feedbackId, UUID deploymentId) {
        return learnerFeedbackGateway.findLatestDeployed(feedbackId, deploymentId);
    }
}
