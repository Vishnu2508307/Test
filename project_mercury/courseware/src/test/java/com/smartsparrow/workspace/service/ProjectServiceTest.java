package com.smartsparrow.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.data.ProjectAccessGateway;
import com.smartsparrow.workspace.data.ProjectAccount;
import com.smartsparrow.workspace.data.ProjectAccountCollaborator;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.WorkspaceProject;
import com.smartsparrow.workspace.data.ProjectByWorkspace;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.payload.ProjectPayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectGateway projectGateway;

    @Mock
    private ProjectAccessGateway projectAccessGateway;

    @Mock
    private ProjectPermissionService projectPermissionService;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID accountId2 = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final String config = "config";
    private static final String name = "Tzen ze re rei";

    @Mock
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createProject_missingName() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> projectService.createProject(null, null, null, null));

        assertEquals("name is missing", f.getMessage());
    }

    @Test
    void createProject_missingWorkspaceId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> projectService.createProject("name", null, null, null));

        assertEquals("workspaceId is missing", f.getMessage());
    }

    @Test
    void createProject() {
        when(projectGateway.persist(any(Project.class))).thenReturn(Flux.just(new Void[]{}));
        when(projectPermissionService.saveAccountPermission(eq(accountId), any(UUID.class), eq(PermissionLevel.OWNER)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

        Project project = projectService.createProject(name, config, workspaceId, accountId)
                .block();

        assertNotNull(project);

        verify(projectGateway).persist(projectCaptor.capture());
        verify(projectPermissionService).saveAccountPermission(accountId, project.getId(), PermissionLevel.OWNER);

        Project captured = projectCaptor.getValue();

        assertEquals(name, project.getName());
        assertEquals(config, project.getConfig());
        assertEquals(workspaceId, project.getWorkspaceId());
        assertNotNull(project.getCreatedAt());
        assertNotNull(project.getId());

        assertEquals(project, captured);
    }

    @Test
    void deleteProject_missingProjectId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> projectService.deleteProject(null));

        assertEquals("projectId is missing", f.getMessage());
    }

    @Test
    void deleteProject() {
        when(projectGateway.delete(any(Project.class))).thenReturn(Flux.just(new Void[]{}));
        when(projectGateway.findWorkspaceForProject(projectId))
                .thenReturn(Mono.just(new WorkspaceProject().setWorkspaceId(workspaceId)));

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

        projectService.deleteProject(projectId)
                .blockFirst();

        verify(projectGateway).delete(projectCaptor.capture());
        verify(projectGateway).findWorkspaceForProject(projectId);

        Project deleted = projectCaptor.getValue();

        assertNotNull(deleted);
        assertEquals(projectId, deleted.getId());
    }

    @Test
    void moveProject() {
        when(projectGateway.delete(any(Project.class))).thenReturn(Flux.just(new Void[]{}));
        when(projectAccessGateway.delete(any(ProjectAccount.class))).thenReturn(Flux.just(new Void[]{}));
        when(projectGateway.persist(any(Project.class))).thenReturn(Flux.just(new Void[]{}));
        when(projectGateway.findById(projectId)).thenReturn(Mono.just(new Project()));
        when(projectPermissionService.saveAccountPermission(eq(accountId), any(UUID.class), eq(PermissionLevel.OWNER)))
                .thenReturn(Flux.just(new Void[]{}));
        when(projectAccessGateway.fetchAccountCollaborators(projectId))
                .thenReturn(Flux.just(new ProjectAccountCollaborator()
                        .setAccountId(accountId)
                        .setPermissionLevel(PermissionLevel.OWNER)
                        .setProjectId(projectId)));
        when(projectAccessGateway.fetchTeamCollaborators(projectId)).thenReturn(Flux.empty());
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        ArgumentCaptor<ProjectAccount> projectAccountCaptor = ArgumentCaptor.forClass(ProjectAccount.class);

        projectService.moveProject(projectId, workspaceId, accountId)
                .block();

        verify(projectGateway).delete(projectCaptor.capture());
        verify(projectAccessGateway).delete(projectAccountCaptor.capture());
        verify(projectGateway).persist(projectCaptor.capture());
        verify(projectPermissionService).saveAccountPermission(accountId, projectId, PermissionLevel.OWNER);

        Project moved = projectCaptor.getValue();

        assertNotNull(moved);
        assertEquals(projectId, moved.getId());
        assertEquals(workspaceId, moved.getWorkspaceId());
    }

    @Test
    void replaceConfig_noConfig() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> projectService.replaceConfig(projectId, null));

        assertEquals("config is missing", f.getMessage());
    }

    @Test
    void replaceConfig_noProjectId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> projectService.replaceConfig(null, null));

        assertEquals("projectId is missing", f.getMessage());
    }

    @Test
    void replaceConfig() {
        when(projectGateway.updateProjectConfig(projectId, config)).thenReturn(Flux.just(new Void[]{}));

        String replaced = projectService.replaceConfig(projectId, config)
                .block();

        assertNotNull(replaced);

        verify(projectGateway).updateProjectConfig(projectId, config);

        assertEquals(config, replaced);
    }

    @Test
    void replaceName_noName() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> projectService.replaceName(projectId, null));

        assertEquals("name is missing", f.getMessage());
    }

    @Test
    void replaceName_noProjectId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> projectService.replaceName(null, name));

        assertEquals("projectId is missing", f.getMessage());
    }

    @Test
    void replaceName() {
        when(projectGateway.updateProjectName(projectId, name))
                .thenReturn(Flux.just(new Void[]{}));

        String replaced = projectService.replaceName(projectId, name)
                .block();

        assertNotNull(replaced);

        verify(projectGateway).updateProjectName(projectId, name);

        assertEquals(name, replaced);
    }

    @Test
    void findById() {
        when(projectGateway.findById(projectId))
                .thenReturn(Mono.just(new Project()));

        Project found = projectService.findById(projectId)
                .block();

        assertNotNull(found);

        verify(projectGateway).findById(projectId);
    }

    @Test
    void findAccountProjects() {
        final UUID teamId = UUID.randomUUID();

        when(projectAccessGateway.fetchProjectsForAccount(accountId, workspaceId))
                .thenReturn(Flux.just(projectId));
        when(teamService.findTeamsForAccount(accountId))
                .thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));

        when(projectAccessGateway.fetchProjectsForTeam(teamId, workspaceId))
                .thenReturn(Flux.just(projectId));

        when(projectGateway.findById(projectId))
                .thenReturn(Mono.just(new Project().setId(projectId)));
        when(projectGateway.findById(projectId)).thenReturn(
                Mono.just(new Project()
                        .setId(projectId)
                        .setName("test project")
                        .setWorkspaceId(workspaceId)
                        .setConfig("config")
                        .setCreatedAt("now")));

        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        List<ProjectPayload> found = projectService.findAccountProjects(accountId, workspaceId)
                .collectList()
                .block();

        assertNotNull(found);

        assertEquals(1, found.size());

        ProjectPayload first = found.get(0);

        assertEquals(projectId, first.getId());
        assertEquals(PermissionLevel.CONTRIBUTOR, first.getPermissionLevel());
    }

    @Test
    void fetchCollaborators() {
        ProjectAccountCollaborator projectAccountCollaborator1 = new ProjectAccountCollaborator().setProjectId(projectId).setAccountId(accountId);
        ProjectAccountCollaborator projectAccountCollaborator2 = new ProjectAccountCollaborator().setProjectId(projectId).setAccountId(accountId2);
        when(projectAccessGateway.fetchAccountCollaborators(projectId)).thenReturn(Flux.just(projectAccountCollaborator1, projectAccountCollaborator2));

        StepVerifier.create(projectService.fetchAccountCollaborators(projectId))
                .expectNext(projectAccountCollaborator1, projectAccountCollaborator2)
                .verifyComplete();
    }

    @Test
    void fetchCollaborators_noCollaborators() {
        when(projectAccessGateway.fetchAccountCollaborators(projectId)).thenReturn(Flux.empty());

        StepVerifier.create(projectService.fetchAccountCollaborators(projectId)).verifyComplete();
    }

    @Test
    void findPayloadById() {
        when(projectGateway.findById(projectId)).thenReturn(
                Mono.just(new Project()
                          .setId(projectId)
                          .setName("test project")
                          .setWorkspaceId(workspaceId)
                          .setConfig("config")
                          .setCreatedAt("now")));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        ProjectPayload found = projectService.findPayloadById(projectId, accountId)
                .block();

        assertNotNull(found);

        verify(projectGateway).findById(projectId);
    }

    @Test
    void find_workspace_activities() {
        when(projectGateway.findProjects(workspaceId)).thenReturn(Flux.just(new ProjectByWorkspace()
                .setWorkspaceId(workspaceId)
                .setProjectId(projectId)));
        when(projectGateway.findActivities(projectId)).thenReturn(Flux.just(new ProjectActivity()
                .setProjectId(projectId)
                .setActivityId(UUID.randomUUID())));

        List<UUID> list = projectService.findWorkspaceActivities(workspaceId)
                .collectList()
                .block();

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void findAlfrescoSite() {
        when(projectGateway.findById(projectId)).thenReturn(
                Mono.just(new Project()
                        .setId(projectId)
                        .setName("test project")
                        .setWorkspaceId(workspaceId)
                        .setConfig("config")
                        .setCreatedAt("now")));

        String config = projectService.findProjectConfig(projectId)
                .block();

        assertNotNull(config);
        assertEquals("config", config);
    }
}
