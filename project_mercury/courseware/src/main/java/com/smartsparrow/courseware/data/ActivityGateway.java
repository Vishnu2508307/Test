package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Statement;
import com.smartsparrow.workspace.data.DeletedWorkspace;
import com.smartsparrow.workspace.data.DeletedWorkspaceByIdMutator;
import com.smartsparrow.workspace.data.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ActivityGateway {

    private final Session session;
    private static final Logger log = LoggerFactory.getLogger(ActivityGateway.class);

    //
    private final ActivityMutator activityMutator;
    private final ActivityByPluginMutator activityByPluginMutator;
    private final ActivityConfigMutator activityConfigMutator;
    private final ParentPathwayByActivityMutator parentPathwayByActivityMutator;
    private final LinkPathwayByActivityMutator linkPathwayByActivityMutator;
    private final ActivityChangeMutator activityChangeMutator;
    private final WalkableByStudentScopeMutator walkableByStudentScopeMutator;
    private final DeletedActivityByIdMutator deletedActivityByIdMutator;
    private final ElementMutator elementMutator;

    //
    private final ActivityMaterializer activityMaterializer;
    private final ActivityConfigMaterializer activityConfigMaterializer;
    private final ParentPathwayByActivityMaterializer parentPathwayByActivityMaterializer;
    private final ChildPathwayByActivityMaterializer childPathwayByActivityMaterializer;
    private final LinkPathwayByActivityMaterializer linkPathwayByActivityMaterializer;
    private final ActivityChangeMaterializer activityChangeMaterializer;
    private final ActivityByPluginMaterializer activityByPluginMaterializer;
    private final DeletedActivityByIdMaterializer deletedActivityByIdMaterializer;

    @Inject
    public ActivityGateway(Session session,
                           ActivityMutator activityMutator,
                           ActivityByPluginMutator activityByPluginMutator,
                           ActivityConfigMutator activityConfigMutator,
                           ParentPathwayByActivityMutator parentPathwayByActivityMutator,
                           LinkPathwayByActivityMutator linkPathwayByActivityMutator,
                           ActivityChangeMutator activityChangeMutator,
                           WalkableByStudentScopeMutator walkableByStudentScopeMutator,
                           DeletedActivityByIdMutator deletedActivityByIdMutator,
                           ElementMutator elementMutator,
                           ActivityMaterializer activityMaterializer,
                           ActivityConfigMaterializer activityConfigMaterializer,
                           ParentPathwayByActivityMaterializer parentPathwayByActivityMaterializer,
                           ChildPathwayByActivityMaterializer childPathwayByActivityMaterializer,
                           LinkPathwayByActivityMaterializer linkPathwayByActivityMaterializer,
                           ActivityChangeMaterializer activityChangeMaterializer,
                           ActivityByPluginMaterializer activityByPluginMaterializer,
                           DeletedActivityByIdMaterializer deletedActivityByIdMaterializer) {
        this.session = session;
        this.activityMutator = activityMutator;
        this.activityByPluginMutator = activityByPluginMutator;
        this.activityConfigMutator = activityConfigMutator;
        this.parentPathwayByActivityMutator = parentPathwayByActivityMutator;
        this.linkPathwayByActivityMutator = linkPathwayByActivityMutator;
        this.activityChangeMutator = activityChangeMutator;
        this.walkableByStudentScopeMutator = walkableByStudentScopeMutator;
        this.deletedActivityByIdMutator = deletedActivityByIdMutator;
        this.activityMaterializer = activityMaterializer;
        this.activityConfigMaterializer = activityConfigMaterializer;
        this.parentPathwayByActivityMaterializer = parentPathwayByActivityMaterializer;
        this.childPathwayByActivityMaterializer = childPathwayByActivityMaterializer;
        this.linkPathwayByActivityMaterializer = linkPathwayByActivityMaterializer;
        this.activityChangeMaterializer = activityChangeMaterializer;
        this.activityByPluginMaterializer = activityByPluginMaterializer;
        this.deletedActivityByIdMaterializer = deletedActivityByIdMaterializer;
        this.elementMutator = elementMutator;
    }

    /**
     * Persists an activity
     * @param activity activity object to be persisted
     */
    @Trace(async = true)
    public Flux<Void> persist(final Activity activity) {

        CoursewareElement activityElement = new CoursewareElement()
                .setElementId(activity.getId())
                .setElementType(CoursewareElementType.ACTIVITY);

        return Mutators.execute(session, Flux.just(
                    activityMutator.upsert(activity), //
                    activityByPluginMutator.upsert(activity),
                    walkableByStudentScopeMutator.upsert(activity.getStudentScopeURN(), activityElement),
                    elementMutator.upsert(activityElement)
                ))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the pathway as a parent for an existing activity
     * @param activityId the child activity
     * @param parentPathwayId the parent pathway
     */
    @Trace(async = true)
    public Flux<Void> persistParent(final UUID activityId, final UUID parentPathwayId) {
        return Mutators.execute(session, Flux.just(
                parentPathwayByActivityMutator.insert(activityId, parentPathwayId)
        )).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a parent relationship for an activity. <b>This method does not remove</b> the activity from the activity table
     * or a pathway from the pathway table. As a result activity do not have a parent pathway and can not be fetched.
     *
     * @param activityId the child activity to delete
     */
    @Trace(async = true)
    public Flux<Void> removeParent(final UUID activityId) {
        return Mutators.execute(session, Flux.just(
                parentPathwayByActivityMutator.deleteBy(activityId)
        ).doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Delete the existing link between an external activity and an existing pathway.
     * Todo: Please be informed that also the activity should be saved in list of pathway children
     *
     * @param activityId the id of the activity to unlink
     * @param pathwayId the pathway to unlink the activity from
     */
    public Flux<Void> unlink(final UUID activityId, final UUID pathwayId) {
        return Mutators.execute(session,
                Flux.just(linkPathwayByActivityMutator.delete(activityId, pathwayId)));
    }

    /**
     * Persists the relationship between an existing external activity and an existing pathway. The relationship is
     * persisted only downwards. The upward relationship is stored in a linked table which is not looked-up when
     * traversing the tree to the top.
     * Todo: Please be informed that also the activity should be deleted from the list of pathway children
     *
     * @param activityId the id of the activity to link
     * @param pathwayId the pathway to link the activity to
     */
    public Flux<Void> link(final UUID activityId, final UUID pathwayId) {
        return Mutators.execute(session,
                Flux.just(linkPathwayByActivityMutator.insert(activityId, pathwayId)));
    }

    /**
     * Find the parent pathway for a given activity.
     *
     * @param activityId the activity to find the parent for
     * @return the parent pathway UUID or an empty stream if the activity has no parent.
     */
    @Trace(async = true)
    public Mono<UUID> findParentPathwayId(final UUID activityId) {
        return ResultSets.query(session, parentPathwayByActivityMaterializer.fetchBy(activityId))
                .flatMapIterable(row->row)
                .map(parentPathwayByActivityMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the children pathway ids for an activity.
     *
     * @param activityId the activity to find the children pathways for
     * @return either a Mono List of UUIDs or an empty stream if none are found
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildPathwayIds(final UUID activityId) {
        return ResultSets.query(session, childPathwayByActivityMaterializer.findByActivity(activityId))
                .flatMapIterable(one-> one)
                .map(childPathwayByActivityMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the ids of all the pathway that have been linked to a given existing activity.
     *
     * @param activityId the activity to lookup the linked pathways for
     */
    public Flux<UUID> findLinkedPathway(final UUID activityId) {
        return ResultSets.query(session, linkPathwayByActivityMaterializer.fetchAll(activityId))
                .flatMapIterable(row->row)
                .map(linkPathwayByActivityMaterializer::fromRow);
    }

    /**
     * Persists activity configuration
     * Schema validation on the configuration is NOT done at this stage
     *
     * @param configuration activity object to be persisted
     */
    @Trace(async = true)
    public Flux<Void> persist(final ActivityConfig configuration) {
        return Mutators.execute(session, Flux.just(activityConfigMutator.upsert(configuration)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches stored activity for supplied id
     *
     * @param activityId the activity's id
     * @return a {@link Mono} that emits the activity or a empty Mono if not found
     */
    @Trace(async = true)
    public Mono<Activity> findById(final UUID activityId) {
        return ResultSets.query(session, activityMaterializer.fetchById(activityId))
                .flatMapIterable(row -> row)
                .map(activityMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching activity %s", activityId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches all activity Ids for plugin id
     *
     * @param pluginId the plugin id to find activity ids for
     */
    public Flux<UUID> findActivityIdsByPluginId(final UUID pluginId) {
        return ResultSets.query(session, activityByPluginMaterializer.fetchAllBy(pluginId))
                .flatMapIterable(row -> row)
                .map(activityByPluginMaterializer::fromRow);
    }

    /**
     * Fetches all activity Ids for plugin id and version
     *
     * @param pluginId the plugin id to find activity ids for
     * @param pluginVersion the plugin version to find activity ids for
     */
    @Trace(async = true)
    public Flux<UUID> findActivityIdsByPluginIdAndVersion(final UUID pluginId, final String pluginVersion) {
        return ResultSets.query(session, activityByPluginMaterializer.fetchAllBy(pluginId))
                .flatMapIterable(row -> row)
                .map(activityByPluginMaterializer::fromRowToActivity)
                .filter(activity -> pluginVersion.equalsIgnoreCase(activity.getPluginVersionExpr()))
                .map(Activity::getId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches latest stored activity configuration for supplied id
     *
     * @param activityId the activity's id
     * @return a {@link Mono} with the activity or a empty Mono if not found
     */
    @Trace(async = true)
    public Mono<ActivityConfig> findLatestConfig(final UUID activityId) {
        return ResultSets.query(session, activityConfigMaterializer.fetchLatestConfig(activityId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToActivityConfig)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching latest config for activity %s", activityId), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .singleOrEmpty();
    }

    /**
     * Maps a row to an activity configuration
     * @param row to be converted
     * @return materialized activity config object built from the row
     */
    private ActivityConfig mapRowToActivityConfig(Row row) {
        return new ActivityConfig() //
                .setId(row.getUUID("id")) //
                .setActivityId(row.getUUID("activity_id")) //
                .setConfig(row.getString("config"));
    }

    /**
     * Persist an activity change
     *
     * @param activityChange the object to persist
     * @return a flux of void
     */
    public Flux<Void> persistChange(final ActivityChange activityChange) {
        return Mutators.execute(session, Flux.just(
                activityChangeMutator.upsert(activityChange)
        ));
    }

    /**
     * Fetch the latest activity change for a given activity id
     *
     * @param activityId the activity id to search the latest change for
     * @return a mono of activity change object
     */
    public Mono<ActivityChange> findLatestActivityChange(final UUID activityId) {
        return ResultSets.query(session, activityChangeMaterializer.fetchLatestChange(activityId))
                .flatMapIterable(row->row)
                .map(activityChangeMaterializer::fromRow)
                .singleOrEmpty();
    }


    /**
     * Fetches latest stored activity configuration id for supplied id
     *
     * @param activityId the activity's id
     * @return a {@link Mono} with the activity or a empty Mono if not found
     */
    @Trace(async = true)
    public Mono<UUID> findLatestConfigId(final UUID activityId) {
        return ResultSets.query(session, activityConfigMaterializer.fetchLatestConfigId(activityId))
                .flatMapIterable(row -> row)
                .map(activityConfigMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching latest config id for activity %s", activityId), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .singleOrEmpty();
    }

    /**
     * Persist the activity to the courseware.deleted_activity_by_id on deletion of activity
     *
     * @param deletedActivity the activity id which should be deleted
     */
    public Flux<Void> persist(DeletedActivity deletedActivity) {
        Flux<? extends Statement> stmt = Flux.just(
                deletedActivityByIdMutator.upsert(deletedActivity));
        return Mutators.execute(session, stmt);
    }

    /**
     * Fetches the deleted activity info for a given activity id
     */
    @Trace(async = true)
    public Mono<DeletedActivity> findDeletedActivityById(final UUID activityId) {
        return ResultSets.query(session, deletedActivityByIdMaterializer.fetchDeletedActivityById(activityId))
                .flatMapIterable(row -> row)
                .map(deletedActivityByIdMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update the evaluation mode for an activity
     *
     * @param activityId the id of the activity to update the evaluation mode for
     * @param evaluationMode the evaluation mode value to update
     * @return a mono of void
     */
    public Mono<Void> updateEvaluationMode(final UUID activityId, final EvaluationMode evaluationMode) {
        return Mutators.execute(session, Flux.just(
                activityMutator.updateEvaluationMode(activityId, evaluationMode)
        )).singleOrEmpty();
    }
}
