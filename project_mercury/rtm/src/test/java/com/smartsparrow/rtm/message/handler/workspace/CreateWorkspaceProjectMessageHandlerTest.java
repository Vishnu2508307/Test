package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.workspace.CreateWorkspaceProjectMessage;
import com.smartsparrow.rtm.subscription.workspace.ProjectCreatedRTMProducer;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateWorkspaceProjectMessageHandlerTest {

    private CreateWorkspaceProjectMessageHandler handler;

    @Mock
    private ProjectService projectService;

    @Mock
    private CreateWorkspaceProjectMessage message;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ProjectCreatedRTMProducer projectCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;


    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final String name = "Stars";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn(name);

        handler = new CreateWorkspaceProjectMessageHandler(
                projectService,
                authenticationContextProvider,
                rtmClientContextProvider,
                projectCreatedRTMProducer
        );
    }

    @Test
    void validate_noName() {
        when(message.getName()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("name is required", fault.getMessage());
    }

    @Test
    void validate_noWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("workspaceId is required", fault.getMessage());
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException {
        final UUID projectId = UUIDs.timeBased();
        when(projectService.createProject(name, null, workspaceId, accountId))
                .thenReturn(Mono.just(new Project()
                        .setId(projectId)));

        when(projectCreatedRTMProducer.buildProjectCreatedRTMConsumable(rtmClientContext, workspaceId, projectId))
                .thenReturn(projectCreatedRTMProducer);

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.create.ok\",\"response\":{\"project\":{\"id\":\"" + projectId + "\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(projectCreatedRTMProducer).buildProjectCreatedRTMConsumable(rtmClientContext, workspaceId, projectId);
        verify(projectCreatedRTMProducer).produce();

    }

    @Test
    void handle_failure() throws WriteResponseException {

        TestPublisher<Project> projectTestPublisher = TestPublisher.create();
        projectTestPublisher.error(new RuntimeException("kaboom!"));

        when(projectService.createProject(name, null, workspaceId, accountId))
                .thenReturn(projectTestPublisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.create.error\",\"code\":422,\"message\":\"error creating the project\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(projectCreatedRTMProducer, never()).produce();
    }

}
