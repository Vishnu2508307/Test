package com.smartsparrow.workspace.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.data.permission.workspace.AccountProjectPermission;
import com.smartsparrow.iam.data.permission.workspace.ProjectPermissionGateway;
import com.smartsparrow.iam.data.permission.workspace.TeamProjectPermission;
import com.smartsparrow.iam.service.HighestPermissionLevel;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ProjectAccessGateway;
import com.smartsparrow.workspace.data.ProjectAccount;
import com.smartsparrow.workspace.data.ProjectAccountCollaborator;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.ProjectTeam;
import com.smartsparrow.workspace.data.ProjectTeamCollaborator;
import com.smartsparrow.workspace.lang.ProjectPermissionPeristenceException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ProjectPermissionService {

    private static final Logger log = LoggerFactory.getLogger(ProjectPermissionService.class);

    private final ProjectAccessGateway projectAccessGateway;
    private final ProjectPermissionGateway projectPermissionGateway;
    private final TeamService teamService;
    private final ProjectGateway projectGateway;

    @Inject
    public ProjectPermissionService(final ProjectAccessGateway projectAccessGateway,
                                    final ProjectPermissionGateway projectPermissionGateway,
                                    final TeamService teamService,
                                    final ProjectGateway projectGateway) {
        this.projectAccessGateway = projectAccessGateway;
        this.projectPermissionGateway = projectPermissionGateway;
        this.teamService = teamService;
        this.projectGateway = projectGateway;
    }

    /**
     * Save the permission for an account over a project. Finds the workspace id for the project then proceeds
     * persisting the account permission
     *
     * @param accountId the account to save the permission for
     * @param projectId the project the permission refers to
     * @param permissionLevel the level of the permission
     * @return a flux of void
     */
    public Flux<Void> saveAccountPermission(final UUID accountId, final UUID projectId, final PermissionLevel permissionLevel) {
        return projectGateway.findWorkspaceForProject(projectId)
                // throw the exception if project not found
                .single()
                // turn it into a flux
                .flux()
                .flatMap(workspaceProject -> Flux.merge(
                        projectAccessGateway.persist(new ProjectAccountCollaborator()
                                .setAccountId(accountId)
                                .setProjectId(projectId)
                                .setPermissionLevel(permissionLevel), workspaceProject.getWorkspaceId()),
                        projectPermissionGateway.persist(new AccountProjectPermission()
                                .setAccountId(accountId)
                                .setProjectId(projectId)
                                .setPermissionLevel(permissionLevel))
                )).doOnError(t -> {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Error saving permission: %s", t.getMessage()), t);
                    }
                    throw new ProjectPermissionPeristenceException("Error granting permission", t);
                });
    }

    /**
     * Revoke the permission for an account over a project. Find the workspace id for the project then proceeds
     * deleting the permission
     *
     * @param accountId the account to revoke the permission for
     * @param projectId the project the permission refers to
     * @return a flux of void
     */
    public Flux<Void> deleteAccountPermission(final UUID accountId, final UUID projectId) {
        return projectGateway.findWorkspaceForProject(projectId)
                // throw the exception if project not found
                .single()
                // turn it into a flux
                .flux()
                .flatMap(workspaceProject -> Flux.merge(
                        projectAccessGateway.delete(new ProjectAccount()
                                .setAccountId(accountId)
                                .setWorkspaceId(workspaceProject.getWorkspaceId())
                                .setProjectId(projectId)),
                        projectPermissionGateway.delete(new AccountProjectPermission()
                                .setAccountId(accountId)
                                .setProjectId(projectId))
                )).doOnError(t -> {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Error deleting permission: %s", t.getMessage()), t);
                    }
                    throw new ProjectPermissionPeristenceException("Error revoking permission", t);
                });
    }

    /**
     * Save the permission for a team over a project. First finds the workspace for the project then proceeds
     * persisting the permission for the team
     *
     * @param teamId the team to save the permission for
     * @param projectId the project the permission refers to
     * @param level the level of the permission
     * @return a flux of void
     */
    public Flux<Void> saveTeamPermission(final UUID teamId, final UUID projectId, final PermissionLevel level) {
        return projectGateway.findWorkspaceForProject(projectId)
                // throw the exception if project not found
                .single()
                // turn it into a flux
                .flux()
                .flatMap(workspaceProject -> Flux.merge(
                        projectAccessGateway.persist(new ProjectTeamCollaborator()
                                .setTeamId(teamId)
                                .setProjectId(projectId)
                                .setPermissionLevel(level), workspaceProject.getWorkspaceId()),
                        projectPermissionGateway.persist(new TeamProjectPermission()
                                .setTeamId(teamId)
                                .setProjectId(projectId)
                                .setPermissionLevel(level))
                )).doOnError(t -> {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Error saving permission: %s", t.getMessage()), t);
                    }
                });
    }

    /**
     * Revoke the permission for a team over a project. First finds the workspace for the project then proceeds
     * deleting the team permission
     *
     * @param teamId the team to revoke the permission for
     * @param projectId the project the permission refers to
     * @return a flux of void
     */
    public Flux<Void> deleteTeamPermission(final UUID teamId, final UUID projectId) {
        return projectGateway.findWorkspaceForProject(projectId)
                // throw the exception if project not found
                .single()
                // turn it into a flux
                .flux()
                .flatMap(workspaceProject -> Flux.merge(
                        projectAccessGateway.delete(new ProjectTeam()
                                .setTeamId(teamId)
                                .setWorkspaceId(workspaceProject.getWorkspaceId())
                                .setProjectId(projectId)),
                        projectPermissionGateway.delete(new TeamProjectPermission()
                                .setTeamId(teamId)
                                .setProjectId(projectId))))
                .doOnError(t -> {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Error deleting permission: %s", t.getMessage()), t);
                    }
                    throw new ProjectPermissionPeristenceException("Error revoking permission", t);
                });
    }

    /**
     * Find the permission level of an account over a project
     *
     * @param accountId the account to find the permission level for
     * @param projectId the project the permission refers to
     * @return a mono of permission level
     */
    @Trace(async = true)
    public Mono<PermissionLevel> fetchAccountPermission(final UUID accountId, final UUID projectId) {
        return projectPermissionGateway.findAccountPermission(accountId, projectId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the permission level of a team over a project
     *
     * @param teamId the team to find the permission level for
     * @param projectId the project the permission refers to
     * @return a mono of permission level
     */
    @Trace(async = true)
    public Mono<PermissionLevel> fetchTeamPermission(final UUID teamId, final UUID projectId) {
        return projectPermissionGateway.findTeamPermission(teamId, projectId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the permission the account has over the project. The methods finds both account and team specific
     * permissions over the project, then the highest level is returned
     *
     * @param accountId the account id to search the highest permission for
     * @param projectId the project id the account should have permission over
     * @return a mono of permission level
     */
    @Trace(async = true)
    public Mono<PermissionLevel> findHighestPermissionLevel(final UUID accountId, final UUID projectId) {
        return teamService.findTeamsForAccount(accountId)
                .onErrorResume(ex -> {
                    if (log.isDebugEnabled()) {
                        log.debug("could not fetch teams for account {} {}", accountId, ex.getMessage());
                    }
                    return Mono.empty();
                })
                .map(teamAccount -> fetchTeamPermission(teamAccount.getTeamId(), projectId))
                .flatMap(one -> one)
                .mergeWith(fetchAccountPermission(accountId, projectId))
                .reduce(new HighestPermissionLevel())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
