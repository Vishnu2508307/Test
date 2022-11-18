package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class InteractiveGateway {

    private final Session session;

    //
    private final ElementMutator elementMutator;
    private final InteractiveMutator interactiveMutator;
    private final InteractiveByPluginMutator interactiveByPluginMutator;
    private final InteractiveConfigLatestMutator interactiveConfigLatestMutator;
    private final InteractiveConfigIdMutator interactiveConfigIdMutator;
    private final ParentPathwayByInteractiveMutator parentPathwayByInteractiveMutator;
    private final WalkableByStudentScopeMutator walkableByStudentScopeMutator;

    //
    private final InteractiveMaterializer interactiveMaterializer;
    private final InteractiveByPluginMaterializer interactiveByPluginMaterializer;
    private final InteractiveConfigIdMaterializer interactiveConfigIdMaterializer;
    private final InteractiveConfigLatestMaterializer interactiveConfigLatestMaterializer;
    private final ParentPathwayByInteractiveMaterializer parentPathwayByInteractiveMaterializer;

    @Inject
    public InteractiveGateway(Session session,
                              ElementMutator elementMutator,
                              InteractiveMutator interactiveMutator,
                              InteractiveByPluginMutator interactiveByPluginMutator,
                              InteractiveConfigLatestMutator interactiveConfigLatestMutator,
                              InteractiveConfigIdMutator interactiveConfigIdMutator,
                              ParentPathwayByInteractiveMutator parentPathwayByInteractiveMutator,
                              WalkableByStudentScopeMutator walkableByStudentScopeMutator,
                              InteractiveMaterializer interactiveMaterializer,
                              InteractiveByPluginMaterializer interactiveByPluginMaterializer,
                              InteractiveConfigIdMaterializer interactiveConfigIdMaterializer,
                              InteractiveConfigLatestMaterializer interactiveConfigLatestMaterializer,
                              ParentPathwayByInteractiveMaterializer parentPathwayByInteractiveMaterializer) {
        this.session = session;
        this.elementMutator = elementMutator;
        this.interactiveMutator = interactiveMutator;
        this.interactiveByPluginMutator = interactiveByPluginMutator;
        this.interactiveConfigLatestMutator = interactiveConfigLatestMutator;
        this.interactiveConfigIdMutator = interactiveConfigIdMutator;
        this.parentPathwayByInteractiveMutator = parentPathwayByInteractiveMutator;
        this.walkableByStudentScopeMutator = walkableByStudentScopeMutator;
        this.interactiveMaterializer = interactiveMaterializer;
        this.interactiveByPluginMaterializer = interactiveByPluginMaterializer;
        this.interactiveConfigIdMaterializer = interactiveConfigIdMaterializer;
        this.interactiveConfigLatestMaterializer = interactiveConfigLatestMaterializer;
        this.parentPathwayByInteractiveMaterializer = parentPathwayByInteractiveMaterializer;
    }

    /**
     * Create a new interactive
     *
     * @param interactive the new interactive
     */
    @Trace(async = true)
    public Mono<Void> persist(final Interactive interactive) {
        CoursewareElement interactiveElement = new CoursewareElement()
                .setElementId(interactive.getId())
                .setElementType(CoursewareElementType.INTERACTIVE);

        return Mutators.execute(session, Flux.just(
                interactiveMutator.upsert(interactive),
                interactiveByPluginMutator.upsert(interactive),
                walkableByStudentScopeMutator.upsert(interactive.getStudentScopeURN(), interactiveElement),
                elementMutator.upsert(interactiveElement)
        )).singleOrEmpty()
        .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the reference to the parent pathway for the interactive.
     *
     * @param interactiveId the interactive id
     * @param pathwayId     the parentPathwayId
     */
    @Trace(async = true)
    public Flux<Void> persistParent(final UUID interactiveId, final UUID pathwayId) {
        return Mutators.execute(session, Flux.just(
                parentPathwayByInteractiveMutator.insert(interactiveId, pathwayId)
        ).doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Remove the reference to the parent pathway for the interactive.
     *
     * @param interactiveId the interactive id to detach from the pathway
     */
    @Trace(async = true)
    public Flux<Void> removeParent(final UUID interactiveId) {
        return Mutators.execute(session, Flux.just(
                parentPathwayByInteractiveMutator.deleteBy(interactiveId)
        ).doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Persists an Interactive Configuration
     *
     * @param configuration the configuration to persist
     */
    @Trace(async = true)
    public Mono<Void> persist(final InteractiveConfig configuration) {
        return Mutators.execute(session, Flux.just(interactiveConfigLatestMutator.upsert(configuration),
                        interactiveConfigIdMutator.upsert(configuration)))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Finds an Interactive by id
     *
     * @param interactiveId the interactive id
     * @return Mono of Interactive, returns empty Mono if interactive does not exist
     */
    @Trace(async = true)
    public Mono<Interactive> findById(final UUID interactiveId) {
        return ResultSets.query(session, interactiveMaterializer.findById(interactiveId))
                .flatMapIterable(row -> row)
                .map(interactiveMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches all interactive Ids for plugin id
     *
     * @param pluginId the plugin id to find interactive ids for
     */
    public Flux<UUID> findInteractiveIdsByPluginId(final UUID pluginId) {
        return ResultSets.query(session, interactiveByPluginMaterializer.fetchAllBy(pluginId))
                .flatMapIterable(row -> row)
                .map(interactiveByPluginMaterializer::fromRow);
    }

    /**
     * Fetches all interactive Ids for plugin id and version
     *
     * @param pluginId the plugin id to find interactive ids for
     * @param pluginVersion the plugin version to find interactive ids for
     */
    @Trace(async = true)
    public Flux<UUID> findInteractiveIdsByPluginIdAndVersion(final UUID pluginId, final String pluginVersion) {
        return ResultSets.query(session, interactiveByPluginMaterializer.fetchAllBy(pluginId))
                .flatMapIterable(row -> row)
                .map(interactiveByPluginMaterializer::fromRowToInteractive)
                .filter(interactive -> pluginVersion.equalsIgnoreCase(interactive.getPluginVersionExpr()))
                .map(Interactive::getId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a parent pathway for the interactive
     * @param interactiveId the interactive id
     * @return the parent pathway id , can be empty mono if interactive isn't added to any pathway
     */
    @Trace(async = true)
    public Mono<UUID> findParent(final UUID interactiveId) {
        return ResultSets.query(session, parentPathwayByInteractiveMaterializer.fetchBy(interactiveId))
                .flatMapIterable(row -> row)
                .map(parentPathwayByInteractiveMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Finds the latest Interactive Config for the Interactive id
     *
     * @param interactiveId the interactive id
     * @return Mono with Interactive Config, empty Mono if no config
     */
    @Trace(async = true)
    public Mono<InteractiveConfig> findLatestConfig(final UUID interactiveId) {
        return ResultSets.query(session, interactiveConfigIdMaterializer.fetchLatestConfig(interactiveId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToInteractiveConfigId)
                .singleOrEmpty()
                .flatMap(interactiveConfigId ->
                                 ResultSets.query(session, interactiveConfigLatestMaterializer.fetchLatestConfig(interactiveConfigId))
                                         .flatMapIterable(row -> row)
                                         .map(this::mapRowToInteractiveConfig)
                                         .singleOrEmpty());
    }

    private InteractiveConfig mapRowToInteractiveConfig(Row row) {
        return new InteractiveConfig()
                .setId(row.getUUID("id"))
                .setInteractiveId(row.getUUID("interactive_id"))
                .setConfig(row.getString("config"));
    }

    private InteractiveConfigId mapRowToInteractiveConfigId(Row row) {
        return new InteractiveConfigId()
                .setConfigId(row.getUUID("config_id"))
                .setInteractiveId(row.getUUID("interactive_id"));
    }

    /**
     * Update the evaluation mode for an interactive
     *
     * @param interactiveId the id of the interactive to update the evaluation mode for
     * @param evaluationMode the evaluation mode value to update
     * @return a mono of void
     */
    public Mono<Void> updateEvaluationMode(final UUID interactiveId, final EvaluationMode evaluationMode) {
        return Mutators.execute(session, Flux.just(
                interactiveMutator.updateEvaluationMode(interactiveId, evaluationMode)
        )).singleOrEmpty();
    }
}
