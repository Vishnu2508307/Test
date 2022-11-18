package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.workspace.ListProjectCollaboratorMessageHandler.WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.ListProjectCollaboratorMessage;
import com.smartsparrow.workspace.data.ProjectAccountCollaborator;
import com.smartsparrow.workspace.data.ProjectTeamCollaborator;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class ListProjectCollaboratorMessageHandlerTest {

    @InjectMocks
    private ListProjectCollaboratorMessageHandler handler;
    @Mock
    private ProjectService projectService;
    @Mock
    private AccountService accountService;
    @Mock
    private TeamService teamService;
    @Mock
    private ListProjectCollaboratorMessage message;

    private Session session;
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID accountId1 = UUID.randomUUID();
    private static final UUID accountId2 = UUID.randomUUID();
    private static final UUID teamId1 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();
        when(message.getProjectId()).thenReturn(projectId);
        when(message.getLimit()).thenReturn(1);

        ProjectAccountCollaborator coll1 = new ProjectAccountCollaborator().setAccountId(accountId1).setPermissionLevel(PermissionLevel.REVIEWER);
        ProjectAccountCollaborator coll2 = new ProjectAccountCollaborator().setAccountId(accountId2).setPermissionLevel(PermissionLevel.CONTRIBUTOR);

        ProjectTeamCollaborator coll3 = new ProjectTeamCollaborator().setTeamId(teamId1).setPermissionLevel(PermissionLevel.CONTRIBUTOR);

        when(projectService.fetchAccountCollaborators(eq(projectId))).thenReturn(Flux.just(coll1, coll2));
        when(projectService.fetchTeamCollaborators(projectId)).thenReturn(Flux.just(coll3));
        when(accountService.getCollaboratorPayload(any(), any()))
                .thenAnswer((Answer<Mono<CollaboratorPayload>>) invocation -> Mono.just(AccountCollaboratorPayload.from(
                        new AccountPayload().setAccountId((UUID) invocation.getArguments()[0]),
                        (PermissionLevel) invocation.getArguments()[1])));
        when(teamService.getTeamCollaboratorPayload(any(), any()))
                .thenAnswer((Answer<Mono<CollaboratorPayload>>) invocation -> Mono.just(TeamCollaboratorPayload.from(
                        new TeamSummary().setId((UUID) invocation.getArguments()[0]),
                        (PermissionLevel) invocation.getArguments()[1])));
    }

    @Test
    void validate_missingProjectId() {
        when(message.getProjectId()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_ERROR, ex.getType());
        assertEquals("missing projectId", ex.getErrorMessage());
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void validate_missingLimit() throws RTMValidationException {
        when(message.getLimit()).thenReturn(null);

        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"workspace.project.collaborator.summary.ok\"," +
                "\"response\":{" +
                "\"total\":3," +
                "\"collaborators\":{" +
                "\"teams\":[{" +
                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                "\"team\":{" +
                "\"id\":\"" + teamId1 + "\"" +
                "}" +
                "}]" +
                "}" +
                "}}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_zeroLimit() throws IOException {
        when(message.getLimit()).thenReturn(0);

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.collaborator.summary.ok\",\"response\":{\"total\":3,\"collaborators\":{}}}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_missingLimit() throws IOException {
        when(message.getLimit()).thenReturn(null);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"workspace.project.collaborator.summary.ok\"," +
                "\"response\":{" +
                "\"total\":3," +
                "\"collaborators\":{" +
                "\"teams\":[{" +
                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                "\"team\":{" +
                "\"id\":\"" + teamId1 + "\"" +
                "}" +
                "}]," +
                "\"accounts\":[{" +
                "\"permissionLevel\":\"REVIEWER\"," +
                "\"account\":{" +
                "\"accountId\":\"" + accountId1 + "\"" +
                "}" +
                "},{" +
                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                "\"account\":{" +
                "\"accountId\":\"" + accountId2 + "\"" +
                "}" +
                "}]" +
                "}" +
                "}}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_noCollaborators() throws IOException {
        when(projectService.fetchAccountCollaborators(eq(projectId))).thenReturn(Flux.empty());
        when(projectService.fetchTeamCollaborators(projectId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.collaborator.summary.ok\",\"response\":{\"total\":0,\"collaborators\":{}}}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ProjectAccountCollaborator> error = TestPublisher.create();
        when(projectService.fetchAccountCollaborators(eq(projectId))).thenReturn(error.flux());
        error.error(new RuntimeException("some exception"));

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_ERROR + "\",\"code\":422," +
                "\"message\":\"error while listing collaborators for project\"}");
    }

    @Test
    void handle_noTeamCollaborators() throws WriteResponseException {
        when(projectService.fetchTeamCollaborators(projectId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"workspace.project.collaborator.summary.ok\"," +
                "\"response\":{" +
                "\"total\":2," +
                "\"collaborators\":{" +
                "\"accounts\":[{" +
                "\"permissionLevel\":\"REVIEWER\"," +
                "\"account\":{" +
                "\"accountId\":\"" + accountId1 + "\"" +
                "}" +
                "}]" +
                "}" +
                "}}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_noAccountCollaborators() throws WriteResponseException {
        when(projectService.fetchAccountCollaborators(eq(projectId))).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"workspace.project.collaborator.summary.ok\"," +
                "\"response\":{" +
                "\"total\":1," +
                "\"collaborators\":{" +
                "\"teams\":[{" +
                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                "\"team\":{" +
                "\"id\":\"" + teamId1 + "\"" +
                "}" +
                "}]" +
                "}" +
                "}}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}