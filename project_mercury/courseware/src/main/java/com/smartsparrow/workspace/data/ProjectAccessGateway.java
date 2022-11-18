package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class ProjectAccessGateway {

    private static final Logger log = LoggerFactory.getLogger(ProjectAccessGateway.class);

    private final Session session;

    // account accessibility
    private final ProjectByAccountMaterializer projectByAccountMaterializer;
    private final ProjectByAccountMutator projectByAccountMutator;
    private final ProjectAccountCollaboratorMaterializer projectAccountCollaboratorMaterializer;
    private final ProjectAccountCollaboratorMutator projectAccountCollaboratorMutator;
    // team accessibility
    private final ProjectByTeamMaterializer projectByTeamMaterializer;
    private final ProjectByTeamMutator projectByTeamMutator;
    private final ProjectTeamCollaboratorMaterializer projectTeamCollaboratorMaterializer;
    private final ProjectTeamCollaboratorMutator projectTeamCollaboratorMutator;

    @Inject
    public ProjectAccessGateway(final Session session,
                                final ProjectByAccountMaterializer projectByAccountMaterializer,
                                final ProjectByAccountMutator projectByAccountMutator,
                                final ProjectAccountCollaboratorMaterializer projectAccountCollaboratorMaterializer,
                                final ProjectAccountCollaboratorMutator projectAccountCollaboratorMutator,
                                final ProjectByTeamMaterializer projectByTeamMaterializer,
                                final ProjectByTeamMutator projectByTeamMutator,
                                final ProjectTeamCollaboratorMaterializer projectTeamCollaboratorMaterializer,
                                final ProjectTeamCollaboratorMutator projectTeamCollaboratorMutator) {
        this.session = session;
        this.projectByAccountMaterializer = projectByAccountMaterializer;
        this.projectByAccountMutator = projectByAccountMutator;
        this.projectAccountCollaboratorMaterializer = projectAccountCollaboratorMaterializer;
        this.projectAccountCollaboratorMutator = projectAccountCollaboratorMutator;
        this.projectByTeamMaterializer = projectByTeamMaterializer;
        this.projectByTeamMutator = projectByTeamMutator;
        this.projectTeamCollaboratorMaterializer = projectTeamCollaboratorMaterializer;
        this.projectTeamCollaboratorMutator = projectTeamCollaboratorMutator;
    }

    /**
     * Save account access to a project
     * <ul>
     *     <li>Save an account into a list of accounts who have direct access to a project</li>
     *     <li>Save a project into a list of visible projects for an account</li>
     * </ul>
     *
     * @param collaborator project-account info including permission level
     * @return a flux of void
     */
    public Flux<Void> persist(final ProjectAccountCollaborator collaborator, final UUID workspaceId) {
        return Mutators.execute(session, Flux.just(
                projectAccountCollaboratorMutator.upsert(collaborator),
                projectByAccountMutator.upsert(new ProjectAccount()
                        .setAccountId(collaborator.getAccountId())
                        .setWorkspaceId(workspaceId)
                        .setProjectId(collaborator.getProjectId()))
        )).doOnError(throwable -> {
            log.error(String.format("Error persisting project account collaborator [%s]", collaborator.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Revoke account access to a project
     *
     * @param projectAccount the project account info
     * @return a flux of void
     */
    public Flux<Void> delete(final ProjectAccount projectAccount) {
        return Mutators.execute(session, Flux.just(
                projectAccountCollaboratorMutator.delete(new ProjectAccountCollaborator()
                        .setAccountId(projectAccount.getAccountId())
                        .setProjectId(projectAccount.getProjectId())),
                projectByAccountMutator.delete(projectAccount)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting project account access [%s]", projectAccount.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Save team access to a project
     * <ul>
     *     <li>Save a team into a list of teams who have direct access to a project</li>
     *     <li>Save a project into a list of visible projects for a team</li>
     * </ul>
     *
     * @param collaborator project-team info including permission level
     * @return a flux of void
     */
    public Flux<Void> persist(final ProjectTeamCollaborator collaborator, final UUID workspaceId) {
        return Mutators.execute(session, Flux.just(
                projectTeamCollaboratorMutator.upsert(collaborator),
                projectByTeamMutator.upsert(new ProjectTeam()
                        .setTeamId(collaborator.getTeamId())
                        .setWorkspaceId(workspaceId)
                        .setProjectId(collaborator.getProjectId()))
        )).doOnError(throwable -> {
            log.error(String.format("Error persisting project team collaborator [%s]", collaborator.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Revoke team access to a project
     *
     * @param projectTeam the project team info
     * @return a flux of void
     */
    public Flux<Void> delete(final ProjectTeam projectTeam) {
        return Mutators.execute(session, Flux.just(
                projectTeamCollaboratorMutator.delete(new ProjectTeamCollaborator()
                        .setTeamId(projectTeam.getTeamId())
                        .setProjectId(projectTeam.getProjectId())),
                projectByTeamMutator.delete(projectTeam)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting project team access [%s]", projectTeam.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetches a list of accounts with direct access to a project
     *
     * @param projectId the project to find the accounts for
     * @return a flux of project account collaborators
     */
    public Flux<ProjectAccountCollaborator> fetchAccountCollaborators(final UUID projectId) {
        return ResultSets.query(session, projectAccountCollaboratorMaterializer.fetchAccountsForProject(projectId))
                .flatMapIterable(row -> row)
                .map(projectAccountCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching accounts for project %s", projectId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches a list of teams with direct access to a project
     *
     * @param projectId the project to find the teams for
     * @return a flux of project team collaborators
     */
    public Flux<ProjectTeamCollaborator> fetchTeamCollaborators(final UUID projectId) {
        return ResultSets.query(session, projectTeamCollaboratorMaterializer.fetchTeamsForProject(projectId))
                .flatMapIterable(row -> row)
                .map(projectTeamCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching teams for project %s", projectId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches all the projects the account has access to
     *
     * @param accountId the account id to find the projects for
     * @param workspaceId only returns the projects belonging to this workspace
     * @return a flux of project ids
     */
    @Trace(async = true)
    public Flux<UUID> fetchProjectsForAccount(final UUID accountId, final UUID workspaceId) {
        return ResultSets.query(session, projectByAccountMaterializer.fetchProjectsForAccount(accountId, workspaceId))
                .flatMapIterable(row -> row)
                .map(projectByAccountMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching projects for account %s and workspace %s",
                            accountId, workspaceId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches all the projects the team has access to
     *
     * @param teamId the team id to find the projects for
     * @return a flux of project ids
     */
    @Trace(async = true)
    public  Flux<UUID> fetchProjectsForTeam(final UUID teamId, final UUID workspaceId) {
        return ResultSets.query(session, projectByTeamMaterializer.fetchProjectsForTeam(teamId, workspaceId))
                .flatMapIterable(row -> row)
                .map(projectByTeamMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching projects for team %s and workspace %s",
                            teamId, workspaceId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
