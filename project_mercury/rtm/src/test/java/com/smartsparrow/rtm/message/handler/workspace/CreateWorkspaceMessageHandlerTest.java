package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.workspace.CreateWorkspaceMessageHandler.WORKSPACE_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.workspace.CreateWorkspaceMessageHandler.WORKSPACE_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.workspace.CreateWorkspaceMessage;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateWorkspaceMessageHandlerTest {

    @InjectMocks
    private CreateWorkspaceMessageHandler handler;
    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    private Session session;
    @Mock
    private CreateWorkspaceMessage message;

    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final String name = "Workspace 1";
    private static final String description = "Description of Workspace 1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        AuthenticationContext context = mockAuthenticationContext(accountId, subscriptionId);
        when(authenticationContextProvider.get()).thenReturn(context);
        session = mockSession();
        when(message.getName()).thenReturn(name);
        when(message.getDescription()).thenReturn(description);
    }

    @Test
    void validate_noName() {
        when(message.getName()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_CREATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing name", ex.getErrorMessage());
    }

    @Test
    void validate_EmptyName() {
        when(message.getName()).thenReturn("");

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_CREATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing name", ex.getErrorMessage());
    }

    @Test
    void validate_noDescription() throws RTMValidationException {
        when(message.getDescription()).thenReturn(null);
        handler.validate(message);

        when(message.getDescription()).thenReturn("");
        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace().setId(workspaceId).setName(name).setDescription(description);
        when(workspaceService.createWorkspace(eq(subscriptionId), eq(accountId), eq(name), eq(description))) //
                .thenReturn(Mono.just(workspace));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_CREATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("workspace"));
                assertEquals(workspaceId.toString(), responseMap.get("id"));
                assertEquals(name, responseMap.get("name"));
                assertEquals(description, responseMap.get("description"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Workspace> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(workspaceService.createWorkspace(eq(subscriptionId), eq(accountId), eq(name), eq(description))) //
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + WORKSPACE_CREATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to create workspace\"}");
    }
}
