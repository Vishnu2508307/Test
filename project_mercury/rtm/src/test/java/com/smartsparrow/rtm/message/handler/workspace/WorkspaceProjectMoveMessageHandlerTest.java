package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.rtm.message.handler.workspace.WorkspaceProjectMoveMessageHandler.WORKSPACE_PROJECT_MOVE_ERROR;
import static com.smartsparrow.rtm.message.handler.workspace.WorkspaceProjectMoveMessageHandler.WORKSPACE_PROJECT_MOVE_OK;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceProjectMoveMessage;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class WorkspaceProjectMoveMessageHandlerTest {

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    @InjectMocks
    private WorkspaceProjectMoveMessageHandler handler;
    @Mock
    private ProjectService projectService;
    @Mock
    private WorkspaceProjectMoveMessage message;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("projectId is required", f.getMessage());
    }

    @Test
    void validate_noWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("workspaceId is required", f.getMessage());
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(projectService.moveProject(projectId, workspaceId, accountId)).thenReturn(Mono.just(new Project()));

        handler.handle(session, message);

        String expected = "{\"type\":\"" + WORKSPACE_PROJECT_MOVE_OK + "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Project> movePublisher = TestPublisher.create();
        movePublisher.error(new RuntimeException("some exception"));

        when(projectService.moveProject(projectId, workspaceId, accountId)).thenReturn(movePublisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"" + WORKSPACE_PROJECT_MOVE_ERROR + "\",\"code\":422,\"message\":\"error moving the project\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
