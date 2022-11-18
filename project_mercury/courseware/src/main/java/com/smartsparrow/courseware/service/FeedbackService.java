package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.data.FeedbackConfig;
import com.smartsparrow.courseware.data.FeedbackGateway;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.lang.ParentInteractiveNotFoundException;
import com.smartsparrow.courseware.payload.FeedbackPayload;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Contains operations related to a Feedback
 */
@Singleton
public class FeedbackService {

    private final FeedbackGateway feedbackGateway;
    private final PluginService pluginService;
    private final InteractiveService interactiveService;
    private final CoursewareAssetService coursewareAssetService;

    @Inject
    public FeedbackService(FeedbackGateway feedbackGateway,
                           PluginService pluginService,
                           InteractiveService interactiveService,
                           CoursewareAssetService coursewareAssetService) {
        this.feedbackGateway = feedbackGateway;
        this.pluginService = pluginService;
        this.interactiveService = interactiveService;
        this.coursewareAssetService = coursewareAssetService;
    }

    /**
     * Creates a Feedback for an Interactive with a Plugin
     *
     * @param interactiveId     the interactive id
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the plugin version expression
     * @return a Mono with created Feedback
     * @throws PluginNotFoundFault if the plugin doesn't exist
     * @throws VersionParserFault  if version expression can't be parsed
     */
    @Trace(async = true)
    public Mono<Feedback> create(final UUID interactiveId, final UUID pluginId, final String pluginVersionExpr) throws VersionParserFault {

        checkArgument(interactiveId != null, "interactive id required");
        checkArgument(pluginId != null, "plugin id required");
        checkArgument(StringUtils.isNotBlank(pluginVersionExpr), "plugin version required");

        UUID feedbackId = UUIDs.timeBased();
        Feedback feedback = new Feedback()
                .setId(feedbackId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        return pluginService.findLatestVersion(pluginId, pluginVersionExpr)
                .then(interactiveService.findById(interactiveId))
                .thenEmpty(feedbackGateway.persist(feedback, interactiveId))
                .thenReturn(feedback)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Saves a Feedback Configuration
     *
     * @param feedbackId the feedback id
     * @param config     the configuration
     * @throws FeedbackNotFoundException if feedback can't be found
     */
    @Trace(async = true)
    public Mono<FeedbackConfig> replace(final UUID feedbackId, final String config) {

        checkArgument(feedbackId != null, "feedback id required");
        checkArgument(StringUtils.isNotBlank(config), "config is required");

        UUID feedbackConfigId = UUIDs.timeBased();
        FeedbackConfig feedbackConfig = new FeedbackConfig()
                .setId(feedbackConfigId)
                .setConfig(config)
                .setFeedbackId(feedbackId);

        return feedbackGateway.findByFeedbackId(feedbackId)
                .thenEmpty(feedbackGateway.persist(feedbackConfig))
                .thenReturn(feedbackConfig)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Duplicate feedback and config and attach feedback to the given interactive.
     *
     * @param feedbackId       the feedback id to copy
     * @param newInteractiveId the new interactive id
     * @param context          keeps pairs old ids/new ids
     * @return mono with created feedback copy
     * @throws FeedbackNotFoundException if feedback is not found for given feedbackId
     */
    @Trace(async = true)
    public Mono<Feedback> duplicate(final UUID feedbackId, final UUID newInteractiveId, final DuplicationContext context) {

        return feedbackGateway.findByFeedbackId(feedbackId)
                //copy feedback object itself and attach to interactive
                .flatMap(f -> duplicateFeedback(f, newInteractiveId)
                        .doOnSuccess(newFeedback -> context.putIds(feedbackId, newFeedback.getId())))
                //copy config
                .flatMap(newFeedback -> feedbackGateway.findLatestConfig(feedbackId)
                        .flatMap(c -> duplicateFeedbackConfig(c, newFeedback.getId(), context))
                        .thenReturn(newFeedback))
                //copy assets
                .flatMap(newFeedback -> coursewareAssetService.duplicateAssets(feedbackId, newFeedback.getId(), CoursewareElementType.FEEDBACK, context)
                        .then(Mono.just(newFeedback)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an existing feedback and attach it to the given interactive
     *
     * @param feedback         the feedback to copy
     * @param newInteractiveId the new interactive id
     * @return mono with created copy
     */
    @Trace(async = true)
    Mono<Feedback> duplicateFeedback(final Feedback feedback, final UUID newInteractiveId) {
        Feedback copy = new Feedback()
                .setId(UUIDs.timeBased())
                .setPluginId(feedback.getPluginId())
                .setPluginVersionExpr(feedback.getPluginVersionExpr());

        return feedbackGateway.persist(copy, newInteractiveId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(copy);
    }

    /**
     * Duplicate a feedback config. Replace old ids with new ids from context
     *
     * @param config        the config to copy
     * @param newFeedbackId the new feedbackId
     * @param context       the duplication context which holds the mapping between old and new ids
     * @return mono with created config
     */
    @Trace(async = true)
    Mono<FeedbackConfig> duplicateFeedbackConfig(final FeedbackConfig config, final UUID newFeedbackId, final DuplicationContext context) {
        FeedbackConfig copy = new FeedbackConfig()
                .setId(UUIDs.timeBased())
                .setConfig(context.replaceIds(config.getConfig()))
                .setFeedbackId(newFeedbackId);

        return feedbackGateway.persist(copy)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(copy);
    }

    /**
     * Builds feedback payload for feedback id
     *
     * @param feedbackId the feedback id
     * @return mono with payload
     * @throws FeedbackNotFoundException          if feedback with provided id doesn't exist
     * @throws ParentInteractiveNotFoundException if the parent interactive is not found
     */
    @Trace(async = true)
    public Mono<FeedbackPayload> getFeedbackPayload(final UUID feedbackId) {
        checkArgument(feedbackId != null, "feedbackId is required");

        return feedbackGateway.findByFeedbackId(feedbackId)
                .flatMap(this::getFeedbackPayload)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Builds feedback payload for feedback
     *
     * @param feedback the feedback object
     * @return mono with payload
     * @throws ParentInteractiveNotFoundException if the parent interactive is not found
     */
    @Trace(async = true)
    public Mono<FeedbackPayload> getFeedbackPayload(final Feedback feedback) {
        checkArgument(feedback != null, "feedback is required");

        Mono<PluginSummary> plugin = pluginService.fetchById(feedback.getPluginId())
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new PluginNotFoundFault(feedback.getPluginId());
                });

        Mono<UUID> parent = findParentId(feedback.getId());

        Mono<String> config = findLatestConfig(feedback.getId()).defaultIfEmpty("");

        Mono<List<PluginFilter>> pluginFilters = pluginService.fetchPluginFiltersByIdVersionExpr(feedback.getPluginId(), feedback.getPluginVersionExpr());

        Mono<FeedbackPayload> payloadMono = Mono.zip(Mono.just(feedback), plugin, parent, config, pluginFilters)
                .map(tuple5 -> FeedbackPayload.from(tuple5.getT1(), tuple5.getT2(), tuple5.getT3(), tuple5.getT4(), tuple5.getT5()));

        payloadMono = payloadMono.flatMap(payload -> coursewareAssetService.getAssetPayloads(feedback.getId())
                .doOnSuccess(payload::setAssets)
                .thenReturn(payload))
                .doOnEach(ReactiveTransaction.linkOnNext());
        return payloadMono;
    }

    /**
     * Find the latest config for feedback
     * @param feedbackId the feedback id
     * @return mono with config or empty mono if no config
     */
    @Trace(async = true)
    public Mono<String> findLatestConfig(final UUID feedbackId) {
        return feedbackGateway.findLatestConfig(feedbackId)
                .map(FeedbackConfig::getConfig)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches an interactive id for feedback
     *
     * @param feedbackId the feedback id
     * @return mono with interactive id if found, otherwise throws exception
     * @throws ParentInteractiveNotFoundException if the parent interactive is not found
     */
    @Trace(async = true)
    public Mono<UUID> findParentId(final UUID feedbackId) {
        checkArgument(feedbackId != null, "feedbackId is required");

        return feedbackGateway.findParent(feedbackId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ParentInteractiveNotFoundException(feedbackId);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete feedback's relationships with parent interactive
     *
     * @param feedbackId    the feedback to delete
     * @param interactiveId the parent interactive id
     * @return nothing
     */
    @Trace(async = true)
    public Flux<Void> delete(final UUID feedbackId, final UUID interactiveId) {
        checkArgument(feedbackId != null, "feedbackId is required");
        checkArgument(interactiveId != null, "interactiveId is required");

        return feedbackGateway.deleteRelationship(feedbackId, interactiveId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a feedback by id
     *
     * @param feedbackId the feedback id
     * @return mono with feedback object if found, otherwise throw an error
     * @throws FeedbackNotFoundException if feedback doesn't exist
     */
    @Trace(async = true)
    public Mono<Feedback> findById(final UUID feedbackId) {
        checkArgument(feedbackId != null, "feedbackId is required");

        return feedbackGateway.findByFeedbackId(feedbackId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Finds a list of Feedback IDs for Interactive
     *
     * @param interactiveId the interactive id
     * @return Flux of Components, can be empty
     */
    @Trace(async = true)
    public Mono<List<UUID>> findIdsByInteractive(final UUID interactiveId) {
        checkArgument(interactiveId != null, "missing interactive id");
        return feedbackGateway.findByInteractive(interactiveId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
