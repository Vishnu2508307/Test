package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class FeedbackGateway {

    private final static Logger log = LoggerFactory.getLogger(FeedbackGateway.class);

    private final Session session;

    private final FeedbackMutator feedbackMutator;
    private final FeedbackByInteractiveMutator feedbackByInteractiveMutator;
    private final ParentInteractiveByFeedbackMutator parentInteractiveByFeedbackMutator;
    private final FeedbackConfigMutator feedbackConfigMutator;
    private final FeedbackMaterializer feedbackMaterializer;
    private final FeedbackConfigMaterializer feedbackConfigMaterializer;
    private final FeedbackByInteractiveMaterializer feedbackByInteractiveMaterializer;
    private final ParentInteractiveByFeedbackMaterializer parentInteractiveByFeedbackMaterializer;
    private final ElementMutator elementMutator;


    @Inject
    public FeedbackGateway(Session session,
                           FeedbackMutator feedbackMutator,
                           FeedbackByInteractiveMutator feedbackByInteractiveMutator,
                           ParentInteractiveByFeedbackMutator parentInteractiveByFeedbackMutator,
                           FeedbackConfigMutator feedbackConfigMutator,
                           FeedbackMaterializer feedbackMaterializer,
                           FeedbackConfigMaterializer feedbackConfigMaterializer,
                           FeedbackByInteractiveMaterializer feedbackByInteractiveMaterializer,
                           ParentInteractiveByFeedbackMaterializer parentInteractiveByFeedbackMaterializer,
                           ElementMutator elementMutator) {
        this.session = session;
        this.feedbackMutator = feedbackMutator;
        this.feedbackByInteractiveMutator = feedbackByInteractiveMutator;
        this.parentInteractiveByFeedbackMutator = parentInteractiveByFeedbackMutator;
        this.feedbackConfigMutator = feedbackConfigMutator;
        this.feedbackMaterializer = feedbackMaterializer;
        this.feedbackConfigMaterializer = feedbackConfigMaterializer;
        this.feedbackByInteractiveMaterializer = feedbackByInteractiveMaterializer;
        this.parentInteractiveByFeedbackMaterializer = parentInteractiveByFeedbackMaterializer;
        this.elementMutator = elementMutator;
    }

    private Feedback toFeedback(Row row) {
        return new Feedback()
                .setId(row.getUUID("id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersionExpr(row.getString("plugin_version_expr"));
    }

    private FeedbackConfig toFeedbackConfig(Row row) {
        return new FeedbackConfig()
                .setId(row.getUUID("id"))
                .setFeedbackId(row.getUUID("feedback_id"))
                .setConfig(row.getString("config"));
    }

    /**
     * Persists Feedback object and links it to the interactive
     * @param feedback the feedback to persist
     * @param interactiveId the interactive which the feedback should be attached
     */
    @Trace(async = true)
    public Mono<Void> persist(final Feedback feedback, final UUID interactiveId) {
        CoursewareElement feedbackElement = new CoursewareElement()
                .setElementId(feedback.getId())
                .setElementType(CoursewareElementType.FEEDBACK);

        return Mutators.execute(session, Flux.just(feedbackMutator.upsert(feedback),
                feedbackByInteractiveMutator.addFeedback(feedback.getId(), interactiveId),
                parentInteractiveByFeedbackMutator.upsert(feedback.getId(), interactiveId),
                elementMutator.upsert(feedbackElement)
        ))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error("Error: persist, with feedback {}", feedback, e);
                    throw Exceptions.propagate(e);
                })
                .singleOrEmpty();
    }

    /**
     * Persists Feedback Configuration
     * @param feedbackConfig the configuration obejct to persist
     */
    @Trace(async = true)
    public Mono<Void> persist(final FeedbackConfig feedbackConfig) {
        return Mutators.execute(session, Flux.just(feedbackConfigMutator.upsert(feedbackConfig)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error("Error: persist, with feedbackConfig {}", feedbackConfig, e);
                    throw Exceptions.propagate(e);
                })
                .singleOrEmpty();
    }

    /**
     * Find a Feedback by id
     * @param feedbackId the feedback id
     * @return Mono with Feedback
     * @throws FeedbackNotFoundException if feedback not found
     */
    @Trace(async = true)
    public Mono<Feedback> findByFeedbackId(final UUID feedbackId) {
        return ResultSets.query(session, feedbackMaterializer.fetchById(feedbackId))
                .flatMapIterable(row -> row)
                .map(this::toFeedback)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new FeedbackNotFoundException(feedbackId);
                });
    }

    /**
     * Find a parent interactive for feedback
     * @param feedbackId the feedback id
     * @return the interactive id or empty mono if no parent
     */
    public Mono<UUID> findParent(final UUID feedbackId) {
        return ResultSets.query(session, parentInteractiveByFeedbackMaterializer.fetchById(feedbackId))
                .flatMapIterable(row -> row)
                .map(parentInteractiveByFeedbackMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find a list of feedbacks for interactive
     * @param interactiveId the interactive id
     * @return list of feedback ids attached to the interactive, can be empty list
     */
    @Trace(async = true)
    public Mono<List<UUID>> findByInteractive(final UUID interactiveId) {
        return ResultSets.query(session, feedbackByInteractiveMaterializer.fetchAll(interactiveId))
                .flatMapIterable(row -> row)
                .map(feedbackByInteractiveMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the latest Feedback Configuration
     * @param feedbackId the feedback id
     * @return mono with configuration if exists, otherwise empty mono
     */
    @Trace(async = true)
    public Mono<FeedbackConfig> findLatestConfig(final UUID feedbackId) {
        return ResultSets.query(session, feedbackConfigMaterializer.fetchLatestConfig(feedbackId))
                .flatMapIterable(row -> row)
                .map(this::toFeedbackConfig)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete relationships between feedback and its parent interactive.
     * @param feedbackId the feedback id to delete
     * @param interactiveId the interactive id
     */
    @Trace(async = true)
    public Flux<Void> deleteRelationship(final UUID feedbackId, final UUID interactiveId) {
        return Mutators.execute(session, Flux.just(
                feedbackByInteractiveMutator.removeFeedback(feedbackId, interactiveId),
                parentInteractiveByFeedbackMutator.delete(feedbackId))
                .doOnEach(ReactiveTransaction.linkOnNext()));
    }

}
