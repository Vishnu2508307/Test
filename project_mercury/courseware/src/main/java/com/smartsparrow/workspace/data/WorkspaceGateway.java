package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class WorkspaceGateway {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceGateway.class);

    private final Session session;
    private final ActivityByWorkspaceMutator activityByWorkspaceMutator;
    private final ActivityByWorkspaceMaterializer activityByWorkspaceMaterializer;
    private final WorkspaceSummaryMutator workspaceSummaryMutator;
    private final WorkspaceBySubscriptionMutator workspaceBySubscriptionMutator;
    private final WorkspaceByActivityMutator workspaceByActivityMutator;
    private final WorkspaceSummaryMaterializer workspaceSummaryMaterializer;
    private final WorkspaceByActivityMaterializer workspaceByActivityMaterializer;
    private final DeletedWorkspaceByIdMutator deletedWorkspaceByIdMutator;

    @Inject
    public WorkspaceGateway(Session session,
            ActivityByWorkspaceMutator activityByWorkspaceMutator,
            ActivityByWorkspaceMaterializer activityByWorkspaceMaterializer,
            WorkspaceSummaryMutator workspaceSummaryMutator,
            WorkspaceBySubscriptionMutator workspaceBySubscriptionMutator,
            WorkspaceByActivityMutator workspaceByActivityMutator,
            WorkspaceSummaryMaterializer workspaceSummaryMaterializer,
            WorkspaceByActivityMaterializer workspaceByActivityMaterializer,
            DeletedWorkspaceByIdMutator deletedWorkspaceByIdMutator) {
        this.session = session;
        this.activityByWorkspaceMutator = activityByWorkspaceMutator;
        this.activityByWorkspaceMaterializer = activityByWorkspaceMaterializer;
        this.workspaceSummaryMutator = workspaceSummaryMutator;
        this.workspaceBySubscriptionMutator = workspaceBySubscriptionMutator;
        this.workspaceByActivityMutator = workspaceByActivityMutator;
        this.workspaceSummaryMaterializer = workspaceSummaryMaterializer;
        this.workspaceByActivityMaterializer = workspaceByActivityMaterializer;
        this.deletedWorkspaceByIdMutator = deletedWorkspaceByIdMutator;
    }

    /**
     * Fetches activities ids for the given workspace id
     *
     * @param workspaceId workspace id
     */
    public Flux<ActivityByWorkspace> findActivities(final UUID workspaceId) {
        return ResultSets.query(session, activityByWorkspaceMaterializer.fetchActivities(workspaceId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToActivityByWorkspace)
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching activities by workspace %s", workspaceId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persists addition activity to workspace
     *
     * @param activityByWorkspace contains activityId and workspaceId
     */
    public Flux<Void> persist(final ActivityByWorkspace activityByWorkspace) {
        return Mutators.execute(session, Flux.just(activityByWorkspaceMutator.upsert(activityByWorkspace),
                workspaceByActivityMutator.upsert(activityByWorkspace.getActivityId(), activityByWorkspace.getWorkspaceId())));
    }

    /**
     * Save a workspace info - On create of a workspace
     */
    public Mono<Void> persist(final Workspace workspace) {
        return Mutators.execute(session,
                Flux.just(workspaceSummaryMutator.upsert(workspace), workspaceBySubscriptionMutator.upsert(workspace)))
                .singleOrEmpty();
    }

    /**
     * Fetches the workspace info for a given workspace id
     */
    @Trace(async = true)
    public Mono<Workspace> findById(final UUID workspaceId) {
        return ResultSets.query(session, workspaceSummaryMaterializer.findById(workspaceId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToWorkspace)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the workspace activity mapping from workspace.activity_by_workspace
     *
     * @param activityByWorkspace the workspace and activity ids which mapping should be deleted
     */
    public Flux<Void> delete(ActivityByWorkspace activityByWorkspace) {
        Flux<? extends Statement> stmt = Flux.just(
                activityByWorkspaceMutator.delete(activityByWorkspace),
                workspaceByActivityMutator.delete(activityByWorkspace.getActivityId()));
        return Mutators.execute(session, stmt);
    }

    /**
     * Find a workspace id for the given activityId
     */
    public Mono<UUID> findByActivityId(UUID activityId) {
        return ResultSets.query(session,
                workspaceByActivityMaterializer.fetchWorkspaceForActivity(activityId))
                .flatMapIterable(row -> row)
                .map(workspaceByActivityMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the workspace to the workspace.deleted_workspace_by_id on deletion of workspace
     *
     * @param deletedWorkspace the workspace ids which should be deleted
     */
    public Flux<Void> persist(DeletedWorkspace deletedWorkspace) {
        Flux<? extends Statement> stmt = Flux.just(
                deletedWorkspaceByIdMutator.upsert(deletedWorkspace));
        return Mutators.execute(session, stmt);
    }

    /**
     * Delete the workspace from workspace.workspace_by_subscription
     *
     * @param workspace the workspace id which should be deleted
     */
    public Flux<Void> delete(Workspace workspace) {
        Flux<? extends Statement> stmt = Flux.just(
                workspaceBySubscriptionMutator.delete(workspace));
        return Mutators.execute(session, stmt);
    }

    private Workspace mapRowToWorkspace(Row row) {
        return new Workspace()
                .setId(row.getUUID("id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setName(row.getString("name"))
                .setDescription(row.getString("description"));
    }

    private ActivityByWorkspace mapRowToActivityByWorkspace(Row row) {
        return new ActivityByWorkspace()
                .setActivityId(row.getUUID("activity_id"))
                .setWorkspaceId(row.getUUID("workspace_id"));
    }
}
