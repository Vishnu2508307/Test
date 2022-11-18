package com.smartsparrow.iam.data.permission.workspace;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class WorkspacePermissionGateway {

    private final Session session;

    private final WorkspacePermissionByAccountMutator workspacePermissionByAccountMutator;
    private final WorkspacePermissionByAccountMaterializer workspacePermissionByAccountMaterializer;
    private final TeamWorkspacePermissionMutator teamWorkspacePermissionMutator;
    private final TeamWorkspacePermissionMaterializer teamWorkspacePermissionMaterializer;

    @Inject
    public WorkspacePermissionGateway(Session session,
                                      WorkspacePermissionByAccountMutator workspacePermissionByAccountMutator,
                                      WorkspacePermissionByAccountMaterializer workspacePermissionByAccountMaterializer,
                                      TeamWorkspacePermissionMutator teamWorkspacePermissionMutator,
                                      TeamWorkspacePermissionMaterializer teamWorkspacePermissionMaterializer) {
        this.session = session;
        this.workspacePermissionByAccountMutator = workspacePermissionByAccountMutator;
        this.workspacePermissionByAccountMaterializer = workspacePermissionByAccountMaterializer;
        this.teamWorkspacePermissionMutator = teamWorkspacePermissionMutator;
        this.teamWorkspacePermissionMaterializer = teamWorkspacePermissionMaterializer;
    }

    /**
     * Save an account workspace permission
     *
     * @param accountWorkspacePermission the permission to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(AccountWorkspacePermission accountWorkspacePermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(workspacePermissionByAccountMutator, accountWorkspacePermission);
        return Mutators.execute(session, stmt);
    }

    /**
     * Delete a workspace permission.
     *
     * @param accountWorkspacePermission the workspace permission which should be deleted
     */
    public Flux<Void> delete(AccountWorkspacePermission accountWorkspacePermission) {
        Flux<? extends Statement> stmt = Mutators.delete(workspacePermissionByAccountMutator, accountWorkspacePermission);
        return Mutators.execute(session, stmt);
    }

    /**
     * Find the permission level for an account over a given workspace
     *
     * @param accountId   the account to fetch the permission for
     * @param workspaceId the workspace entity the permission refers to
     * @return a {@link Mono} of {@link AccountWorkspacePermission}
     */
    public Mono<AccountWorkspacePermission> findPermission(UUID accountId, UUID workspaceId) {
        return ResultSets.query(session, workspacePermissionByAccountMaterializer.fetchPermission(accountId, workspaceId))
                .flatMapIterable(row -> row)
                .map(this::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the permission level for a team over a workspace
     *
     * @param teamId the team to fetch the permission for
     * @param workspaceId the workspace the permission refers to
     * @return a mono of permission level
     */
    public Mono<PermissionLevel> findTeamPermission(UUID teamId, UUID workspaceId) {
        return ResultSets.query(session, teamWorkspacePermissionMaterializer.fetchPermissionLevel(teamId, workspaceId))
                .flatMapIterable(row->row)
                .map(teamWorkspacePermissionMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Save a team worksapce permission
     *
     * @param teamWorkspacePermission the permission to save
     * @return a flux of void
     */
    public Flux<Void> persist(TeamWorkspacePermission teamWorkspacePermission) {
        return Mutators.execute(session, Flux.just(
                teamWorkspacePermissionMutator.upsert(teamWorkspacePermission)
        ));
    }

    /**
     * Delete a team workspace permission
     *
     * @param teamWorkspacePermission the permission to delete
     * @return a flux of void
     */
    public Flux<Void> delete(TeamWorkspacePermission teamWorkspacePermission) {
        return Mutators.execute(session, Flux.just(
                teamWorkspacePermissionMutator.delete(teamWorkspacePermission)
        ));    }

    /**
     * Convert a cassandra row to a workspace permission
     *
     * @param row the {@link Row} to convert
     * @return a {@link AccountWorkspacePermission} object
     */
    private AccountWorkspacePermission fromRow(Row row) {
        return new AccountWorkspacePermission()
                .setAccountId(row.getUUID("account_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
