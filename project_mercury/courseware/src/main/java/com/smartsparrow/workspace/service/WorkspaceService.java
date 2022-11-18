package com.smartsparrow.workspace.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.data.permission.workspace.AccountWorkspacePermission;
import com.smartsparrow.iam.data.permission.workspace.TeamWorkspacePermission;
import com.smartsparrow.iam.data.permission.workspace.WorkspacePermissionGateway;
import com.smartsparrow.iam.service.HighestPermissionLevel;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.DeletedWorkspace;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.data.WorkspaceAccessGateway;
import com.smartsparrow.workspace.data.WorkspaceAccount;
import com.smartsparrow.workspace.data.WorkspaceAccountCollaborator;
import com.smartsparrow.workspace.data.WorkspaceByTeam;
import com.smartsparrow.workspace.data.WorkspaceGateway;
import com.smartsparrow.workspace.data.WorkspaceTeamCollaborator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class WorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceService.class);

    private final WorkspaceGateway workspaceGateway;
    private final WorkspacePermissionGateway workspacePermissionGateway;
    private final WorkspaceAccessGateway workspaceAccessGateway;
    private final TeamService teamService;

    @Inject
    public WorkspaceService(WorkspaceGateway workspaceGateway,
                            WorkspacePermissionGateway workspacePermissionGateway,
                            WorkspaceAccessGateway workspaceAccessGateway,
                            TeamService teamService) {
        this.workspaceGateway = workspaceGateway;
        this.workspacePermissionGateway = workspacePermissionGateway;
        this.workspaceAccessGateway = workspaceAccessGateway;
        this.teamService = teamService;
    }

    /**
     * Create a new workspace and saves creator as an OWNER of the workspace
     *
     * @param subscriptionId the subscription id which this workspace belongs to
     * @param creatorId the creator account id
     * @param name the workspace name
     * @param description the workspace description, optional
     * @return a mono with newly created workspace
     */
    public Mono<Workspace> createWorkspace(final UUID subscriptionId,
            final UUID creatorId,
            final String name,
            final String description) {
        //
        checkArgument(subscriptionId != null, "subscriptionId is required");
        checkArgument(creatorId != null, "creatorId is required");
        checkArgument(name != null, "name is required");

        // FIXME: assert that the owner is allowed to create a workspace in this subscription.

        Workspace workspace = new Workspace()
                .setId(UUIDs.timeBased())
                .setSubscriptionId(subscriptionId)
                .setName(name)
                .setDescription(description);

        return workspaceGateway.persist(workspace)
                .thenEmpty(savePermissions(creatorId, workspace.getId(), PermissionLevel.OWNER))
                .then(Mono.just(workspace));
    }

    /**
     * Save permissions for workspace
     *
     * @param accountId       account id to be granted with permission
     * @param workspaceId     workspace id
     * @param permissionLevel permission level
     */
    public Flux<Void> savePermissions(UUID accountId, UUID workspaceId, PermissionLevel permissionLevel) {
        checkNotNull(accountId, "accountId is required");
        checkNotNull(workspaceId, "workspaceId is required");
        checkNotNull(permissionLevel, "permissionLevel is required");

        return Flux.merge(
                workspacePermissionGateway.persist(new AccountWorkspacePermission()
                        .setAccountId(accountId)
                        .setWorkspaceId(workspaceId)
                        .setPermissionLevel(permissionLevel)),
                workspaceAccessGateway.persist(new WorkspaceAccount()
                        .setAccountId(accountId)
                        .setWorkspaceId(workspaceId)),
                workspaceAccessGateway.persist(new WorkspaceAccountCollaborator()
                        .setAccountId(accountId)
                        .setWorkspaceId(workspaceId)
                        .setPermissionLevel(permissionLevel)));
    }

    /**
     * Delete the account permissions over a workspace entity.
     *
     * @param accountId   the account to delete the permission for
     * @param workspaceId the workspace the permission relates to
     */
    public Flux<Void> deletePermissions(UUID accountId, UUID workspaceId) {
        checkNotNull(accountId, "accountId is required");
        checkNotNull(workspaceId, "workspaceId is required");

        return Flux.merge(
                workspacePermissionGateway.delete(new AccountWorkspacePermission()
                        .setAccountId(accountId)
                        .setWorkspaceId(workspaceId)),
                workspaceAccessGateway.delete(new WorkspaceAccount()
                        .setAccountId(accountId)
                        .setWorkspaceId(workspaceId)),
                workspaceAccessGateway.delete(new WorkspaceAccountCollaborator()
                        .setAccountId(accountId)
                        .setWorkspaceId(workspaceId))
        );
    }

    /**
     * Save team permissions for workspace
     *
     * @param teamId the team id to save the permission for
     * @param workspaceId the workspace id the team will have permission over
     * @param permissionLevel the permission level
     */
    public Flux<Void> saveTeamPermission(UUID teamId, UUID workspaceId, PermissionLevel permissionLevel) {
        checkNotNull(teamId, "teamId is required");
        checkNotNull(workspaceId, "workspaceId is required");
        checkNotNull(permissionLevel, "permissionLevel is required");

        return Flux.merge(
                workspacePermissionGateway.persist(new TeamWorkspacePermission()
                        .setTeamId(teamId)
                        .setWorkspaceId(workspaceId)
                        .setPermissionLevel(permissionLevel)),
                workspaceAccessGateway.persist(new WorkspaceByTeam()
                        .setTeamId(teamId)
                        .setWorkspaceId(workspaceId)),
                workspaceAccessGateway.persist(new WorkspaceTeamCollaborator()
                        .setTeamId(teamId)
                        .setWorkspaceId(workspaceId)
                        .setPermissionLevel(permissionLevel))
        );
    }

    /**
     * Delete team permission for a workspace
     *
     * @param teamId the team id to delete the permission for
     * @param workspaceId the workspace id related to the permission
     * @return a flux of void
     */
    public Flux<Void> deleteTeamPermission(UUID teamId, UUID workspaceId) {
        checkNotNull(teamId, "teamId is required");
        checkNotNull(workspaceId, "workspaceId is required");

        return Flux.merge(
                workspacePermissionGateway.delete(new TeamWorkspacePermission()
                        .setTeamId(teamId)
                        .setWorkspaceId(workspaceId)),
                workspaceAccessGateway.delete(new WorkspaceByTeam()
                        .setTeamId(teamId)
                        .setWorkspaceId(workspaceId)),
                workspaceAccessGateway.delete(new WorkspaceTeamCollaborator()
                        .setTeamId(teamId)
                        .setWorkspaceId(workspaceId))
        );
    }

    /**
     * Update workspace info
     *
     * @param workspaceId workspace id to update
     * @param name        updated workspace name
     * @param description updated workspace description
     * @return mono with updated workspace
     */
    public Mono<Workspace> updateWorkspace(UUID workspaceId, String name, String description) {
        checkArgument(workspaceId != null, "workspaceId is required");
        checkArgument(name != null, "name is required");

        return workspaceGateway
                .findById(workspaceId)
                .map(workspace -> {
                        workspace.setName(name)
                                .setDescription(description);
                         workspaceGateway.persist(workspace).block();
                         return workspace;
                });
    }

    /**
     * Delete workspace
     *
     * @param workspaceId workspace id to delete
     * @param name        deleted workspace name
     * @param accountId   account id of user that deleted workspace
     * @param subscriptionId   id of subscription workspace belongs to
     * @return a flux of void
     */
    public Flux<Void> deleteWorkspace(UUID workspaceId, String name, UUID accountId, UUID subscriptionId) {
        affirmArgument(workspaceId != null, "workspaceId is required");
        affirmArgument(name != null, "name is required");
        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(subscriptionId != null, "subscriptionId is required");

        return Flux.merge(
                workspaceGateway.delete(new Workspace()
                                        .setId(workspaceId)
                                        .setSubscriptionId(subscriptionId)),
                workspaceAccessGateway.findAccountsByWorkspace(workspaceId)
                    .flatMap(account -> workspaceAccessGateway.delete(new WorkspaceAccount()
                                                                   .setAccountId(account.getAccountId())
                                                                   .setWorkspaceId(workspaceId))),
                workspaceAccessGateway.findTeamsByWorkspace(workspaceId)
                        .flatMap(team -> Flux.merge(workspaceAccessGateway.delete(new WorkspaceByTeam()
                                                                   .setTeamId(team.getTeamId())
                                                                   .setWorkspaceId(workspaceId)),
                                                    workspaceAccessGateway.delete(team))
                                 ),
                workspaceGateway.persist(new DeletedWorkspace()
                                         .setWorkspaceId(workspaceId)
                                         .setName(name)
                                         .setAccountId(accountId)
                                         .setDeletedAt(DateFormat.asRFC1123(UUIDs.timeBased())))
        );
    }

    /**
     * Fetches a list of workspaces available for an account
     *
     * @param accountId the account id
     */
    @Trace(async = true)
    public Flux<Workspace> fetchWorkspaces(UUID accountId) {
        checkArgument(accountId != null, "accountId is required");
        return workspaceAccessGateway.findWorkspacesByAccount(accountId)
                .mergeWith(teamService.findTeamsForAccount(accountId)
                        .flatMap(teamId -> workspaceAccessGateway.findWorkspacesByTeam(teamId.getTeamId())))
                .distinct()
                .flatMap(this::fetchById)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches a workspace info by id
     *
     * @param workspaceId the workspace id
     */
    @Trace(async = true)
    public Mono<Workspace> fetchById(UUID workspaceId) {
        checkArgument(workspaceId != null, "workspaceId is required");
        return workspaceGateway.findById(workspaceId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches a list of account collaborators for workspace
     *
     * @param workspaceId the workspace id
     */
    public Flux<WorkspaceAccountCollaborator> fetchAccountCollaborators(UUID workspaceId) {
        checkArgument(workspaceId != null, "workspaceId is required");
        return workspaceAccessGateway.findAccountsByWorkspace(workspaceId);
    }

    /**
     * Fetch account permission over a workspace
     *
     * @param accountId   the account id to fetch the permission for
     * @param workspaceId the workspace entity the permission relates to
     * @return a {@link Mono} of {@link AccountWorkspacePermission}
     */
    public Mono<AccountWorkspacePermission> fetchPermission(UUID accountId, UUID workspaceId) {
        return workspacePermissionGateway.findPermission(accountId, workspaceId);
    }

    public Mono<PermissionLevel> fetchTeamPermission(UUID teamId, UUID workspaceId) {
        return workspaceAccessGateway.findTeamPermission(teamId, workspaceId);
    }

    /**
     * Finds all the permission the account has over the workspace. The methods finds both account and team specific
     * permissions over the workspace, then the highest permission level is returned.
     *
     * @param accountId the account id to search the permissions for
     * @param workspaceId  the workspace id the account should have permission over
     * @return a mono of permission level
     */
    public Mono<PermissionLevel> findHighestPermissionLevel(UUID accountId, UUID workspaceId) {
        return teamService.findTeamsForAccount(accountId)
                .map(teamAccount -> fetchTeamPermission(teamAccount.getTeamId(), workspaceId))
                .flatMap(one -> one)
                .mergeWith(fetchPermission(accountId, workspaceId)
                        .map(AccountWorkspacePermission::getPermissionLevel))
                .reduce(new HighestPermissionLevel());
    }

    /**
     * Fetches a list of team collaborators for a workspace
     *
     * @param workspaceId the workspace id to find the team collaborators for
     * @return a flux of workspace team collaborator
     */
    public Flux<WorkspaceTeamCollaborator> fetchTeamCollaborators(UUID workspaceId) {
        return workspaceAccessGateway.findTeamsByWorkspace(workspaceId);
    }
}
