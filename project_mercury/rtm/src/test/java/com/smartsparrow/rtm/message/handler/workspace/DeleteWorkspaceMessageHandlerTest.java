package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.workspace.DeleteWorkspaceMessageHandler.WORKSPACE_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.workspace.DeleteWorkspaceMessageHandler.WORKSPACE_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.workspace.DeleteWorkspaceMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class DeleteWorkspaceMessageHandlerTest {

    @InjectMocks
    private DeleteWorkspaceMessageHandler handler;
    @Mock
    private WorkspaceService workspaceService;
    private Session session;
    @Mock
    private DeleteWorkspaceMessage message;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private static final UUID workspaceId = UUID.randomUUID();
    private static final String name = "Workspace 2";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn(name);
        when(message.getSubscriptionId()).thenReturn(subscriptionId);
    }

    @Test
    void validate_noWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_DELETE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing workspaceId", ex.getErrorMessage());
    }

    @Test
    void validate_noName() {
        when(message.getName()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_DELETE_ERROR, ex.getType());
        assertEquals("missing name", ex.getErrorMessage());
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void validate_EmptyName() {
        when(message.getName()).thenReturn("");

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_DELETE_ERROR, ex.getType());
        assertEquals("missing name", ex.getErrorMessage());
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void validate_noSubscriptionId() {
        when(message.getSubscriptionId()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_DELETE_ERROR, ex.getType());
        assertEquals("missing subscriptionId", ex.getErrorMessage());
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void handle() throws IOException {
        when(workspaceService.deleteWorkspace(eq(workspaceId), eq(name), eq(accountId), eq(subscriptionId)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_DELETE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't delete"));
        when(workspaceService.deleteWorkspace(eq(workspaceId), eq(name), eq(accountId), eq(subscriptionId)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + WORKSPACE_DELETE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to delete workspace\"}");
    }
}
