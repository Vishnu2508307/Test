package com.smartsparrow.rtm.message.handler.workspace;

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

import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ListWorkspaceMessageHandlerTest {

    @InjectMocks
    private ListWorkspaceMessageHandler handler;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    private EmptyReceivedMessage message;
    private static final String messageId = "message id";
    private static final UUID accountId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(EmptyReceivedMessage.class);
        Account account = mock(Account.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);

        when(message.getId()).thenReturn(messageId);
        when(account.getId()).thenReturn(accountId);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void handle() throws WriteResponseException {
        UUID workspaceIdOne = UUID.randomUUID();
        UUID workspaceIdTwo = UUID.randomUUID();

        Workspace workspaceOne = buildWorkspace(workspaceIdOne, "one workspace", "king");
        Workspace workspaceTwo = buildWorkspace(workspaceIdTwo, "another workspace", "kong");

        when(workspaceService.fetchWorkspaces(accountId)).thenReturn(Flux.just(workspaceOne, workspaceTwo));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.list.ok\"," +
                            "\"response\":{" +
                                "\"workspaces\":[" +
                                    "{" +
                                        "\"id\":\""+workspaceIdOne+"\"," +
                                        "\"name\":\"one workspace\"," +
                                        "\"description\":\"king\"" +
                                    "},{" +
                                        "\"id\":\""+workspaceIdTwo+"\"," +
                                        "\"name\":\"another workspace\"," +
                                        "\"description\":\"kong\"" +
                                    "}" +
                                "]" +
                            "},\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handle_fail() throws WriteResponseException {
        TestPublisher<Workspace> error = TestPublisher.create();

        when(workspaceService.fetchWorkspaces(accountId)).thenReturn(error.flux());
        error.error(new RuntimeException());

        handler.handle(session, message);

        verify(workspaceService, atLeastOnce()).fetchWorkspaces(accountId);

        String expected = "{\"type\":\"workspace.list.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error while listing workspaces\"," +
                            "\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    private Workspace buildWorkspace(UUID workspaceId, String name, String description) {
        return new Workspace()
                .setId(workspaceId)
                .setName(name)
                .setDescription(description);
    }

}
