package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.payload.ProjectPayload;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetProjectMessageHandlerTest {

    @InjectMocks
    private GetProjectMessageHandler handler;

    @Mock
    private ProjectService projectService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ProjectGenericMessage message;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private final UUID projectId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(message.getProjectId()).thenReturn(projectId);
    }

    @Test
    void validate_missingProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("projectId is required", fault.getMessage());
    }

    @Test
    void validate_hasProjectId() throws RTMValidationException {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_projectNotFound() throws WriteResponseException {
        when(projectService.findPayloadById(message.getProjectId(), accountId))
                .thenReturn(Mono.empty());

        handler.handle(session, message);

        final String expected = "{" +
                "\"type\":\"workspace.project.get.error\"," +
                "\"code\":404," +
                "\"message\":\"project with id " + projectId + " not found\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_unexpectedError() throws WriteResponseException {
        TestPublisher<ProjectPayload> projectPublisher = TestPublisher.create();
        projectPublisher.error(new RuntimeException("not expected"));

        when(projectService.findPayloadById(message.getProjectId(), accountId))
                .thenReturn(projectPublisher.mono());

        handler.handle(session, message);

        final String expected = "{" +
                "\"type\":\"workspace.project.get.error\"," +
                "\"code\":422," +
                "\"message\":\"error fetching the project\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(projectService.findPayloadById(message.getProjectId(), accountId))
                .thenReturn(Mono.just(new ProjectPayload()
                .setId(projectId)
                .setPermissionLevel(PermissionLevel.REVIEWER)));

        handler.handle(session, message);

        final String expected = "{" +
                "\"type\":\"workspace.project.get.ok\"," +
                "\"response\":{" +
                    "\"project\":{" +
                        "\"id\":\"" + projectId + "\"," +
                        "\"permissionLevel\":\"REVIEWER\"" +
                    "}" +
                "}}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}