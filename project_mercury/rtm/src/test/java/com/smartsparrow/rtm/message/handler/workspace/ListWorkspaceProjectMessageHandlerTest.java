package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.payload.ProjectPayload;
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
import com.smartsparrow.rtm.message.recv.courseware.activity.WorkspaceGenericMessage;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ListWorkspaceProjectMessageHandlerTest {

    @InjectMocks
    private ListWorkspaceProjectMessageHandler handler;

    @Mock
    private ProjectService projectService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private WorkspaceGenericMessage message;

    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getWorkspaceId()).thenReturn(workspaceId);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));
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
        final UUID projectId = UUID.randomUUID();
        when(projectService.findAccountProjects(accountId, workspaceId))
                .thenReturn(Flux.just(new ProjectPayload()
                .setConfig("config")
                .setCreatedAt("createdAt")
                .setId(projectId)
                .setName("TRO")
                .setWorkspaceId(workspaceId)
                .setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.project.list.ok\"," +
                            "\"response\":{" +
                                "\"projects\":[{" +
                                    "\"id\":\"" + projectId + "\"," +
                                    "\"name\":\"TRO\"," +
                                    "\"config\":\"config\"," +
                                    "\"workspaceId\":\"" + workspaceId + "\"," +
                                    "\"createdAt\":\"createdAt\"," +
                                    "\"permissionLevel\":\"CONTRIBUTOR\""+
                                "}]" +
                            "}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_failure() throws WriteResponseException {
        TestPublisher<ProjectPayload> projects = TestPublisher.create();
        projects.error(new RuntimeException("@#$%*!!"));

        when(projectService.findAccountProjects(accountId, workspaceId)).thenReturn(projects.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.list.error\",\"code\":422,\"message\":\"error listing projects\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
