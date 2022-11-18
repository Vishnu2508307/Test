package com.smartsparrow.workspace.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.data.ProjectAccessGateway;
import com.smartsparrow.workspace.data.ProjectAccount;
import com.smartsparrow.workspace.data.ProjectAccountCollaborator;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.data.ProjectCollaborator;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.ProjectTeam;
import com.smartsparrow.workspace.data.ProjectTeamCollaborator;
import com.smartsparrow.workspace.data.WorkspaceProject;
import com.smartsparrow.workspace.payload.ProjectPayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ProjectService {

    private final ProjectGateway projectGateway;
    private final ProjectAccessGateway projectAccessGateway;
    private final TeamService teamService;
    private final ProjectPermissionService projectPermissionService;

    @Inject
    public ProjectService(final ProjectGateway projectGateway,
                          final ProjectAccessGateway projectAccessGateway,
                          final TeamService teamService,
                          final ProjectPermissionService projectPermissionService) {
        this.projectGateway = projectGateway;
        this.projectAccessGateway = projectAccessGateway;
        this.teamService = teamService;
        this.projectPermissionService = projectPermissionService;
    }

    /**
     * Create a project for a workspace. Grants OWNER permission to the account creating the project
     *
     * @param name        the project name
     * @param config      the project config
     * @param workspaceId the workspace id the project belongs to
     * @param accountId   the account creating the project
     * @return a mono with the created project
     */
    public Mono<Project> createProject(final String name, final String config, final UUID workspaceId, final UUID accountId) {

        affirmArgument(!Strings.isNullOrEmpty(name), "name is missing");
        affirmArgument(workspaceId != null, "workspaceId is missing");

        final UUID projectId = UUIDs.timeBased();

        final Project project = new Project()
                .setId(projectId)
                .setConfig(config)
                .setName(name)
                .setWorkspaceId(workspaceId)
                .setCreatedAt(DateFormat.asRFC1123(projectId));

        return projectGateway.persist(project)
                .thenMany(projectPermissionService.saveAccountPermission(accountId, projectId, PermissionLevel.OWNER))
                .then(Mono.just(project));
    }

    /**
     * Delete a project from a workspace
     *
     * @param projectId the id of the project to delete
     * @return a flux of void
     */
    public Flux<Void> deleteProject(final UUID projectId) {
        affirmArgument(projectId != null, "projectId is missing");

        return projectGateway.findWorkspaceForProject(projectId)
                // throw the exception if project not found
                .single()
                // turn it into a flux
                .flux()
                .flatMap(workspaceProject -> projectGateway.delete(new Project()
                        .setId(projectId)
                        .setWorkspaceId(workspaceProject.getWorkspaceId())));
    }

    /**
     * Update the project config
     *
     * @param projectId the project id to update the config for
     * @param config    the updated config
     * @return a mono with the updated config
     */
    public Mono<String> replaceConfig(final UUID projectId, final String config) {
        affirmArgument(projectId != null, "projectId is missing");
        affirmArgument(!Strings.isNullOrEmpty(config), "config is missing");

        return projectGateway.updateProjectConfig(projectId, config)
                .then(Mono.just(config));
    }

    /**
     * Update the project name
     *
     * @param projectId the project id to update the name for
     * @param name      the updated name
     * @return a mono with the updated name value
     */
    public Mono<String> replaceName(final UUID projectId, final String name) {
        affirmArgument(projectId != null, "projectId is missing");
        affirmArgument(!Strings.isNullOrEmpty(name), "name is missing");

        return projectGateway.updateProjectName(projectId, name)
                .then(Mono.just(name));
    }

    /**
     * Find a project by id
     *
     * @param projectId the id of the project to find
     * @return a mono of project
     */
    @Trace(async = true)
    public Mono<Project> findById(final UUID projectId) {
        return projectGateway.findById(projectId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a workspace the project belongs to
     *
     * @param projectId the project id to find the workspace for
     * @return a mono of workspace project
     */
    @Trace(async = true)
    public Mono<WorkspaceProject> findWorkspaceIdByProject(final UUID projectId) {
        affirmArgument(projectId != null, "projectId is missing");
        return projectGateway.findWorkspaceForProject(projectId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the projects an account has access to within a workspace
     *
     * @param accountId   the account id to find the projects for
     * @param workspaceId the workspace the projects belong to
     * @return a flux of projectPayload
     */
    @Trace(async = true)
    public Flux<ProjectPayload> findAccountProjects(final UUID accountId, final UUID workspaceId) {
        return projectAccessGateway.fetchProjectsForAccount(accountId, workspaceId)
                .mergeWith(teamService.findTeamsForAccount(accountId)
                        .flatMap(team -> projectAccessGateway.fetchProjectsForTeam(team.getTeamId(), workspaceId)))
                .distinct()
                .flatMap(projectId -> findPayloadById(projectId, accountId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches a list of team collaborators for a project
     *
     * @param projectId the project id
     * @return a flux of ProjectTeamCollaborators {@link ProjectTeamCollaborator}
     */
    public Flux<ProjectTeamCollaborator> fetchTeamCollaborators(final UUID projectId) {
        affirmArgument(projectId != null, "projectId is required");
        return projectAccessGateway.fetchTeamCollaborators(projectId);
    }

    /**
     * Fetches a list of account collaborators for a project
     *
     * @param projectId the project id
     * @return a flux of ProjectAccountCollaborators {@link ProjectAccountCollaborator}
     */
    public Flux<ProjectAccountCollaborator> fetchAccountCollaborators(final UUID projectId) {
        affirmArgument(projectId != null, "projectId is required");
        return projectAccessGateway.fetchAccountCollaborators(projectId);
    }

    /**
     * Move a project from existing workspace to destination workspace
     *
     * @param projectId              the id of the project to move
     * @param destinationWorkspaceId the workspace that project has to be moved
     * @param accountId              the account id to move the project
     * @return a flux of void
     */
    public Mono<Project> moveProject(final UUID projectId, final UUID destinationWorkspaceId, final UUID accountId) {
        affirmArgument(projectId != null, "projectId is required");
        affirmArgument(destinationWorkspaceId != null, "workspaceId is required");

        final Mono<Project> projectMono = findById(projectId);

        return projectMono.flux()
                .flatMap(project -> {
                    final UUID oldWorkspaceId = project.getWorkspaceId();

                    final Project newProject = new Project()
                            .setId(projectId)
                            .setConfig(project.getConfig())
                            .setName(project.getName())
                            .setWorkspaceId(destinationWorkspaceId)
                            .setCreatedAt(DateFormat.asRFC1123(UUIDs.timeBased()));
                    // save the project to the new workspace
                    return projectGateway.delete(project)
                            // delete the old project
                            .thenMany(projectGateway.persist(newProject))
                            // update the permissions
                            .thenMany(updatePermissions(projectId, oldWorkspaceId))
                            // return the project
                            .thenMany(Flux.just(newProject));
                })
                .singleOrEmpty();
    }

    /**
     * Updates the project access permission by deleting the oldWorkspaceId access and saving a new permission
     * // TODO the approach of migrating the project permissions could be improved, doing a fetch on all collaborators
     * // and for each a delete and persist query
     *
     * @param projectId the project to update the access for
     * @param oldWorkspaceId the old workspace to remove this project access from
     * @return a flux with all the collaborators for this project
     */
    private Flux<? extends ProjectCollaborator> updatePermissions(final UUID projectId, final UUID oldWorkspaceId) {
        // find all the permissions
        final Flux<ProjectTeamCollaborator> teams = fetchTeamCollaborators(projectId);
        final Flux<ProjectAccountCollaborator> accounts = fetchAccountCollaborators(projectId);

        final Flux<? extends ProjectCollaborator> collaboratorsFlux = Flux.concat(teams, accounts);

        return collaboratorsFlux.flatMap(projectCollaborator -> {
            if (projectCollaborator instanceof ProjectTeamCollaborator) {
                // remove the team access to this workspace and project
                return projectAccessGateway.delete(new ProjectTeam()
                        .setProjectId(projectId)
                        .setTeamId(((ProjectTeamCollaborator) projectCollaborator).getTeamId())
                        .setWorkspaceId(oldWorkspaceId))
                        // save the new permission
                        .thenMany(projectPermissionService
                                .saveTeamPermission(((ProjectTeamCollaborator) projectCollaborator).getTeamId(),
                                        projectId, projectCollaborator.getPermissionLevel()))
                        .thenMany(Flux.just(projectCollaborator));
            }
            // remove the account access to this workspace and project
            return projectAccessGateway.delete(new ProjectAccount()
                    .setAccountId(((ProjectAccountCollaborator) projectCollaborator).getAccountId())
                    .setProjectId(projectId)
                    .setWorkspaceId(oldWorkspaceId))
                    // save the new permission
                    .thenMany(projectPermissionService
                            .saveAccountPermission(((ProjectAccountCollaborator) projectCollaborator).getAccountId(),
                                    projectId, projectCollaborator.getPermissionLevel()))
                    .thenMany(Flux.just(projectCollaborator));
        });
    }

    /**
     * Find a project by id, including permission level
     *
     * @param projectId the id of the project to find
     * @param accountId the id of the logged in user account
     * @return a mono of project payload
     */
    @Trace(async = true)
    public Mono<ProjectPayload> findPayloadById(final UUID projectId, final UUID accountId) {
        return projectGateway.findById(projectId)
                .flatMap(project -> projectPermissionService.findHighestPermissionLevel(accountId, projectId)
                .flatMap(permissionLevel -> Mono.just(ProjectPayload.from(project, permissionLevel))))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a project by id, including permission level
     *
     * @param workspaceId the id of the project to find
     * @return a mono of project payload
     */
    @Trace(async = true)
    public Flux<UUID> findWorkspaceActivities(final UUID workspaceId) {
        return projectGateway.findProjects(workspaceId)
                .flatMap(project -> projectGateway.findActivities(project.getProjectId())
                        .map(projectActivity -> projectActivity.getActivityId()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the alfresco site id for the project
     *
     * @param projectId the id of the project
     * @return a mono of alfresco site id
     */
    @Trace(async = true)
    public Mono<String> findProjectConfig(final UUID projectId) {
        return projectGateway.findById(projectId)
                .map(project -> project.getConfig())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
