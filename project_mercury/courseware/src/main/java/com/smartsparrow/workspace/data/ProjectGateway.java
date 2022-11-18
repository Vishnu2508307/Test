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
import reactor.core.publisher.Mono;

@Singleton
public class ProjectGateway {

    private static final Logger log = LoggerFactory.getLogger(ProjectGateway.class);

    private final Session session;
    private final ProjectByWorkspaceMaterializer projectByWorkspaceMaterializer;
    private final ProjectByWorkspaceMutator projectByWorkspaceMutator;
    private final WorkspaceByProjectMaterializer workspaceByProjectMaterializer;
    private final WorkspaceByProjectMutator workspaceByProjectMutator;
    private final ProjectMaterializer projectMaterializer;
    private final ProjectMutator projectMutator;
    private final ActivityByProjectMaterializer activityByProjectMaterializer;
    private final ActivityByProjectMutator activityByProjectMutator;
    private final ProjectByActivityMaterializer projectByActivityMaterializer;
    private final ProjectByActivityMutator projectByActivityMutator;

    @Inject
    public ProjectGateway(final Session session,
                          final ProjectByWorkspaceMaterializer projectByWorkspaceMaterializer,
                          final ProjectByWorkspaceMutator projectByWorkspaceMutator,
                          final WorkspaceByProjectMaterializer workspaceByProjectMaterializer,
                          final WorkspaceByProjectMutator workspaceByProjectMutator,
                          final ProjectMaterializer projectMaterializer,
                          final ProjectMutator projectMutator,
                          final ActivityByProjectMaterializer activityByProjectMaterializer,
                          final ActivityByProjectMutator activityByProjectMutator,
                          final ProjectByActivityMaterializer projectByActivityMaterializer,
                          final ProjectByActivityMutator projectByActivityMutator) {
        this.session = session;
        this.projectByWorkspaceMaterializer = projectByWorkspaceMaterializer;
        this.projectByWorkspaceMutator = projectByWorkspaceMutator;
        this.workspaceByProjectMaterializer = workspaceByProjectMaterializer;
        this.workspaceByProjectMutator = workspaceByProjectMutator;
        this.projectMaterializer = projectMaterializer;
        this.projectMutator = projectMutator;
        this.activityByProjectMaterializer = activityByProjectMaterializer;
        this.activityByProjectMutator = activityByProjectMutator;
        this.projectByActivityMaterializer = projectByActivityMaterializer;
        this.projectByActivityMutator = projectByActivityMutator;
    }

    /**
     * Persist a project to all the relevant tables in the database
     *
     * @param project the project to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final Project project) {
        final ProjectByWorkspace projectByWorkspace = new ProjectByWorkspace()
                .setName(project.getName())
                .setProjectId(project.getId())
                .setWorkspaceId(project.getWorkspaceId())
                .setCreatedAt(project.getCreatedAt());

        return Mutators.execute(session, Flux.just(
                projectMutator.upsert(project),
                projectByWorkspaceMutator.upsert(projectByWorkspace),
                workspaceByProjectMutator.upsert(projectByWorkspace)
        )).doOnError(throwable -> {
            log.error(String.format("Error persisting the project [%s]", project.toString()), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Update the config for a project
     *
     * @param projectId the project id to update the config for
     * @param config the new value of the config
     * @return a flux of void
     */
    public Flux<Void> updateProjectConfig(final UUID projectId, final String config) {
        return Mutators.execute(session, Flux.just(
                projectMutator.updateConfig(projectId, config)
        )).doOnError(throwable -> {
            log.error(String.format("Error updating the config for project [%s]", projectId), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Update the name for a project
     *
     * @param projectId the project id to update the config for
     * @param name the new name to save
     * @return a flux of void
     */
    public Flux<Void> updateProjectName(final UUID projectId, final String name) {
        return Mutators.execute(session, Flux.just(
                projectMutator.updateName(projectId, name)
        )).doOnError(throwable -> {
            log.error(String.format("Error updating the config for project [%s]", projectId), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Delete a project from the database. Delete the record from each project related table
     *
     * @param project the project to delete
     * @return a flux of void
     */
    public Flux<Void> delete(final Project project) {
        final ProjectByWorkspace projectByWorkspace = new ProjectByWorkspace()
                .setProjectId(project.getId())
                .setWorkspaceId(project.getWorkspaceId());

        return Mutators.execute(session, Flux.just(
                projectMutator.delete(project),
                projectByWorkspaceMutator.delete(projectByWorkspace),
                workspaceByProjectMutator.delete(projectByWorkspace)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting project [%s]", project.getId()), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Persist the relationship between a project and an activity
     *
     * @param projectActivity the relationship to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final ProjectActivity projectActivity) {
        return Mutators.execute(session, Flux.just(
                activityByProjectMutator.upsert(projectActivity),
                projectByActivityMutator.upsert(projectActivity)
        )).doOnError(throwable -> {
            log.error(String.format("Error persisting project activity [%s]", projectActivity.toString()), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Delete the relationship between a project and an activity
     *
     * @param projectActivity the relationship to delete
     * @return a flux of void
     */
    public Flux<Void> delete(final ProjectActivity projectActivity) {
        return Mutators.execute(session, Flux.just(
                activityByProjectMutator.delete(projectActivity),
                projectByActivityMutator.delete(projectActivity)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting project activity [%s]", projectActivity.toString()), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the project id an activity belongs to
     *
     * @param activityId the activity id to find the project for
     * @return a project activity obj which includes the activity id and the project id, or an empty stream
     * when not found
     */
    @Trace(async = true)
    public Mono<ProjectActivity> findProjectId(final UUID activityId) {
        return ResultSets.query(session, projectByActivityMaterializer.findProjectId(activityId))
                .flatMapIterable(row -> row)
                .map(projectByActivityMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the activity ids that belong to a specific project id
     *
     * @param projectId the project to find all the activity ids for
     * @return a flux of activity project
     */
    @Trace(async = true)
    public Flux<ProjectActivity> findActivities(final UUID projectId) {
        return ResultSets.query(session, activityByProjectMaterializer.findAllActivities(projectId))
                .flatMapIterable(row -> row)
                .map(activityByProjectMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the workspace a project belongs to
     *
     * @param projectId the project id to find the workspace for
     * @return a mono of workspace project
     */
    @Trace(async = true)
    public Mono<WorkspaceProject> findWorkspaceForProject(final UUID projectId) {
        return ResultSets.query(session, workspaceByProjectMaterializer.findWorkspaceId(projectId))
                .flatMapIterable(row -> row)
                .map(workspaceByProjectMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a project by id
     *
     * @param projectId the id of the project to find
     * @return a mono of project
     */
    @Trace(async = true)
    public Mono<Project> findById(final UUID projectId) {
        return ResultSets.query(session, projectMaterializer.findProject(projectId))
                .flatMapIterable(row -> row)
                .map(projectMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find projects by workspace id
     *
     * @param workspaceId the id of the workspace to find
     * @return a flux of ProjectByWorkspace
     */
    @Trace(async = true)
    public Flux<ProjectByWorkspace> findProjects(final UUID workspaceId) {
        return ResultSets.query(session, projectByWorkspaceMaterializer.findAll(workspaceId))
                .flatMapIterable(row -> row)
                .map(projectByWorkspaceMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
