package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.data.permission.workspace.TeamWorkspacePermissionMaterializer;
import com.smartsparrow.iam.data.permission.workspace.TeamWorkspacePermissionMutator;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class WorkspaceAccessGateway {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceAccessGateway.class);

    private final Session session;

    // account accessibility
    private final WorkspaceByAccountMaterializer workspaceByAccountMaterializer;
    private final WorkspaceByAccountMutator workspaceByAccountMutator;
    private final WorkspaceAccountCollaboratorMaterializer workspaceAccountCollaboratorMaterializer;
    private final WorkspaceAccountCollaboratorMutator workspaceAccountCollaboratorMutator;
    // team accessibility
    private final WorkspaceByTeamMaterializer workspaceByTeamMaterializer;
    private final WorkspaceByTeamMutator workspaceByTeamMutator;
    private final WorkspaceTeamCollaboratorMaterializer workspaceTeamCollaboratorMaterializer;
    private final WorkspaceTeamCollaboratorMutator workspaceTeamCollaboratorMutator;
    private final TeamWorkspacePermissionMaterializer teamWorkspacePermissionMaterializer;
    private final TeamWorkspacePermissionMutator teamWorkspacePermissionMutator;

    @Inject
    public WorkspaceAccessGateway(Session session,
                                  WorkspaceByAccountMaterializer workspaceByAccountMaterializer,
                                  WorkspaceByAccountMutator workspaceByAccountMutator,
                                  WorkspaceAccountCollaboratorMaterializer workspaceAccountCollaboratorMaterializer,
                                  WorkspaceAccountCollaboratorMutator workspaceAccountCollaboratorMutator,
                                  WorkspaceByTeamMaterializer workspaceByTeamMaterializer,
                                  WorkspaceByTeamMutator workspaceByTeamMutator,
                                  WorkspaceTeamCollaboratorMaterializer workspaceTeamCollaboratorMaterializer,
                                  WorkspaceTeamCollaboratorMutator workspaceTeamCollaboratorMutator,
                                  TeamWorkspacePermissionMaterializer teamWorkspacePermissionMaterializer,
                                  TeamWorkspacePermissionMutator teamWorkspacePermissionMutator) {
        this.session = session;
        this.workspaceByAccountMaterializer = workspaceByAccountMaterializer;
        this.workspaceByAccountMutator = workspaceByAccountMutator;
        this.workspaceAccountCollaboratorMaterializer = workspaceAccountCollaboratorMaterializer;
        this.workspaceAccountCollaboratorMutator = workspaceAccountCollaboratorMutator;
        this.workspaceByTeamMaterializer = workspaceByTeamMaterializer;
        this.workspaceByTeamMutator = workspaceByTeamMutator;
        this.workspaceTeamCollaboratorMaterializer = workspaceTeamCollaboratorMaterializer;
        this.workspaceTeamCollaboratorMutator = workspaceTeamCollaboratorMutator;
        this.teamWorkspacePermissionMaterializer = teamWorkspacePermissionMaterializer;
        this.teamWorkspacePermissionMutator = teamWorkspacePermissionMutator;
    }

    /**
     * Save the workspace account mapping to workspace.workspace_by_account
     */
    public Flux<Void> persist(final WorkspaceAccount workspaceAccount) {
        return Mutators.execute(session, Flux.just(
                workspaceByAccountMutator.upsert(workspaceAccount)
        ));
    }

    /**
     * Save the workspace account mapping to workspace.account_by_workspace
     */
    public Flux<Void> persist(final WorkspaceAccountCollaborator workspaceAccountCollaborator) {
        return Mutators.execute(session, Flux.just(
                workspaceAccountCollaboratorMutator.upsert(workspaceAccountCollaborator)
        ));
    }

    /**
     * Find account ids for a given workspace id
     */
    public Flux<WorkspaceAccountCollaborator> findAccountsByWorkspace(final UUID workspaceId) {
        return ResultSets.query(session,
                workspaceAccountCollaboratorMaterializer.fetchAccountsForWorkspace(workspaceId))
                .flatMapIterable(row -> row)
                .map(workspaceAccountCollaboratorMaterializer::fromRow);
    }

    /**
     * Delete the workspace account mapping from workspace.workspace_by_account
     *
     * @param workspaceAccount the workspace and account ids which mapping should be deleted
     */
    public Flux<Void> delete(WorkspaceAccount workspaceAccount) {
        Flux<? extends Statement> stmt = Mutators.delete(workspaceByAccountMutator, workspaceAccount);
        return Mutators.execute(session, stmt);
    }

    /**
     * Delete the workspace account mapping from workspace.account_by_workspace
     *
     * @param workspaceAccountCollaborator the workspace and account ids which mapping should be deleted
     */
    public Flux<Void> delete(WorkspaceAccountCollaborator workspaceAccountCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(workspaceAccountCollaboratorMutator, workspaceAccountCollaborator);
        return Mutators.execute(session, stmt);
    }

    /**
     * Save the workspace team collaborator
     */
    public Flux<Void> persist(WorkspaceTeamCollaborator workspaceTeamCollaborator) {
        return Mutators.execute(session, Flux.just(
                workspaceTeamCollaboratorMutator.upsert(workspaceTeamCollaborator)
        ));
    }

    /**
     * Delete the workspace team collaborator
     */
    public Flux<Void> delete(WorkspaceTeamCollaborator workspaceTeamCollaborator) {
        return Mutators.execute(session, Flux.just(
                workspaceTeamCollaboratorMutator.delete(workspaceTeamCollaborator)
        ));
    }

    /**
     * Save a workspace by team relationship
     */
    public Flux<Void> persist(WorkspaceByTeam workspaceByTeam) {
        return Mutators.execute(session, Flux.just(
                workspaceByTeamMutator.upsert(workspaceByTeam)
        ));
    }

    /**
     * Delete a workspace by team relationship
     */
    public Flux<Void> delete(WorkspaceByTeam workspaceByTeam) {
        return Mutators.execute(session, Flux.just(
                workspaceByTeamMutator.delete(workspaceByTeam)
        ));
    }

    /**
     * Find team collaborators for a workspace
     *
     * @param workspaceId the workspace to search the collaborators for
     * @return a flux of workspace team collaborators
     */
    public Flux<WorkspaceTeamCollaborator> findTeamsByWorkspace(final UUID workspaceId) {
        return ResultSets.query(session,
                workspaceTeamCollaboratorMaterializer.fetchByWorkspace(workspaceId))
                .flatMapIterable(row->row)
                .map(workspaceTeamCollaboratorMaterializer::fromRow);
    }

    /**
     * Find the permission level of a team over a workspace
     *
     * @param teamId the team id to search the permission level for
     * @param workspaceId the workspace id
     */
    public Mono<PermissionLevel> findTeamPermission(UUID teamId, UUID workspaceId) {
        return ResultSets.query(session, teamWorkspacePermissionMaterializer.fetchPermissionLevel(teamId, workspaceId))
                .flatMapIterable(row->row)
                .map(teamWorkspacePermissionMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find workspace ids for a given account id
     */
    @Trace(async = true)
    public Flux<UUID> findWorkspacesByAccount(final UUID accountId) {
        return ResultSets.query(session,
                workspaceByAccountMaterializer.fetchWorkspacesForAccount(accountId))
                .flatMapIterable(row -> row)
                .map(workspaceByAccountMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find workspace ids for a given team id
     */
    @Trace(async = true)
    public Flux<UUID> findWorkspacesByTeam(final UUID teamId) {
        return ResultSets.query(session,
                workspaceByTeamMaterializer.fetchByTeam(teamId))
                .flatMapIterable(row -> row)
                .map(workspaceByTeamMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
