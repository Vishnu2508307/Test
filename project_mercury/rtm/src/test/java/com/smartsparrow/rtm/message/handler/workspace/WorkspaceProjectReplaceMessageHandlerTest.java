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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceProjectReplaceMessage;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class WorkspaceProjectReplaceMessageHandlerTest {

    @InjectMocks
    private WorkspaceProjectReplaceMessageHandler handler;

    @Mock
    private ProjectService projectService;

    @Mock
    private WorkspaceProjectReplaceMessage message;

    private static final UUID projectId = UUID.randomUUID();
    private static final String config = "{\"a\":\"value\"}";
    private static final String name = "Bronte";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getConfig()).thenReturn(config);
        when(message.getName()).thenReturn(name);
    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("projectId is required", fault.getMessage());
    }

    @Test
    void validate_noName() {
        when(message.getName()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("name is required", fault.getMessage());
    }

    @Test
    @DisplayName("It should not throw an error when the config is null, config is an optional field")
    void validate_noConfig() {
        when(message.getConfig()).thenReturn(null);

        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(projectService.replaceName(projectId, name))
                .thenReturn(Mono.just(name));

        when(projectService.replaceConfig(projectId, config))
                .thenReturn(Mono.just(config));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.project.replace.ok\"," +
                            "\"response\":{" +
                                "\"projectId\":\"" + projectId + "\"" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_failure() throws WriteResponseException {
        TestPublisher<String> configPublisher = TestPublisher.create();
        configPublisher.error(new RuntimeException("error"));

        when(projectService.replaceName(projectId, name))
                .thenReturn(Mono.just(name));

        when(projectService.replaceConfig(projectId, config))
                .thenReturn(configPublisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.replace.error\",\"code\":422,\"message\":\"error updating the project\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
