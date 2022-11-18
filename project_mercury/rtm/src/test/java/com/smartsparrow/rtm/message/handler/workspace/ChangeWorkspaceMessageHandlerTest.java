package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.workspace.ChangeWorkspaceMessageHandler.WORKSPACE_CHANGE_ERROR;
import static com.smartsparrow.rtm.message.handler.workspace.ChangeWorkspaceMessageHandler.WORKSPACE_CHANGE_OK;
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

import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.workspace.ChangeWorkspaceMessage;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ChangeWorkspaceMessageHandlerTest {

    @InjectMocks
    private ChangeWorkspaceMessageHandler handler;
    @Mock
    private WorkspaceService workspaceService;
    private Session session;
    @Mock
    private ChangeWorkspaceMessage message;

    private static final UUID workspaceId = UUID.randomUUID();
    private static final String name = "Workspace 2";
    private static final String description = "Description of Workspace 2";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn(name);
        when(message.getDescription()).thenReturn(description);
    }

    @Test
    void validate_noWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_CHANGE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing workspaceId", ex.getErrorMessage());
    }

    @Test
    void validate_noName() {
        when(message.getName()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_CHANGE_ERROR, ex.getType());
        assertEquals("missing name", ex.getErrorMessage());
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void validate_EmptyName() {
        when(message.getName()).thenReturn("");

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(WORKSPACE_CHANGE_ERROR, ex.getType());
        assertEquals("missing name", ex.getErrorMessage());
        assertEquals(400, ex.getStatusCode());
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
        Workspace workspace = new Workspace().setId(workspaceId).setName(name).setDescription(description);
        when(workspaceService.updateWorkspace(eq(workspaceId), eq(name), eq(description))).thenReturn(Mono.just(workspace));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_CHANGE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("workspace"));
                assertEquals(workspaceId.toString(), responseMap.get("id"));
                assertEquals(description, responseMap.get("description"));
                assertEquals(name, responseMap.get("name"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Workspace> error = TestPublisher.create();
        error.error(new RuntimeException("can't update"));
        when(workspaceService.updateWorkspace(eq(workspaceId), eq(name), eq(description))).thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + WORKSPACE_CHANGE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to update workspace\"}");
    }
}
