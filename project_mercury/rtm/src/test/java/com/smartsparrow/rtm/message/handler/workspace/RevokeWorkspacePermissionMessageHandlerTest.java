package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertAll;
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

import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.RevokeWorkspacePermissionMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class RevokeWorkspacePermissionMessageHandlerTest {

    @InjectMocks
    private RevokeWorkspacePermissionMessageHandler handler;

    @Mock
    private WorkspaceService workspaceService;

    private RevokeWorkspacePermissionMessage message;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final String messageId = "message id";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(RevokeWorkspacePermissionMessage.class);

        when(message.getAccountId()).thenReturn(accountId);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getId()).thenReturn(messageId);
        when(workspaceService.deletePermissions(accountId, workspaceId)).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void validate_accountIdNotSupplied() {
        when(message.getAccountId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(messageId, e.getReplyTo());
            assertEquals("workspace.permission.revoke.error", e.getType());
            assertEquals("either accountId or teamId is required", e.getErrorMessage());
        });
    }

    @Test
    void validate_workspaceIdNotSupplied() {
        when(message.getWorkspaceId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(messageId, e.getReplyTo());
            assertEquals("workspace.permission.revoke.error", e.getType());
            assertEquals("workspaceId is required", e.getErrorMessage());
        });
    }

    @Test
    void handle() throws WriteResponseException {
        handler.handle(session, message);

        verify(workspaceService, atLeastOnce()).deletePermissions(message.getAccountId(), message.getWorkspaceId());

        String expected = "{\"type\":\"workspace.permission.revoke.ok\",\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handle_fails() throws WriteResponseException {
        TestPublisher<Void> error = TestPublisher.create();

        when(workspaceService.deletePermissions(accountId, workspaceId)).thenReturn(error.flux());
        error.error(new RuntimeException());

        handler.handle(session, message);

        verify(workspaceService, atLeastOnce()).deletePermissions(message.getAccountId(), message.getWorkspaceId());

        String expected = "{\"type\":\"workspace.permission.revoke.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error while revoking permission\"," +
                            "\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }
}
