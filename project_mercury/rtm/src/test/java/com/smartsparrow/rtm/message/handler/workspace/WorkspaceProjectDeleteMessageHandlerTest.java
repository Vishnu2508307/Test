package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
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
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class WorkspaceProjectDeleteMessageHandlerTest {

    @InjectMocks
    private WorkspaceProjectDeleteMessageHandler handler;

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectGenericMessage message;

    private static final UUID projectId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getProjectId()).thenReturn(projectId);
    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("projectId is required", f.getMessage());
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(projectService.deleteProject(projectId)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.delete.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> deletePublisher = TestPublisher.create();
        deletePublisher.error(new RuntimeException("sorry mate"));

        when(projectService.deleteProject(projectId)).thenReturn(deletePublisher.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.delete.error\",\"code\":422,\"message\":\"error deleting the project\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
