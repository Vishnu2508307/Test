package com.smartsparrow.rtm.message.handler.team;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.payload.TeamPayload;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.ListMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ListTeamMessageHandlerTest {

    @InjectMocks
    private ListTeamMessageHandler handler;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private TeamService teamService;

    private AuthenticationContext authenticationContext;
    private ListMessage message;
    private static final UUID accountId = UUID.randomUUID();
    private static final String messageId = "messageId";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        authenticationContext = RTMWebSocketTestUtils.mockAuthenticationContext(accountId);
        message = mock(ListMessage.class);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(message.getId()).thenReturn(messageId);
        when(message.getCollaboratorLimit()).thenReturn(null);
    }

    @Test
    void validate_collaboratorsLimitIsZero() {
        when(message.getCollaboratorLimit()).thenReturn(-1);
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
            assertEquals(messageId, e.getReplyTo());
            assertEquals("iam.team.list.error", e.getType());
            assertEquals("collaboratorsLimit should be a positive integer", e.getErrorMessage());
        });
    }

    @Test
    void handle_accountNotInContext() throws WriteResponseException {
        when(authenticationContext.getAccount()).thenReturn(null);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.team.list.error\"," +
                "\"code\":401," +
                "\"message\":\"Error Listing teams\"," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {
        UUID teamId = UUID.randomUUID();
        when(message.getCollaboratorLimit()).thenReturn(2);

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(teamService.getTeamPayload(teamId, message.getCollaboratorLimit()))
                .thenReturn(Mono.just(new TeamPayload()
                        .setCount(1)
                        .setName("wow team")));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.team.list.ok\"," +
                "\"response\":{" +
                    "\"teams\":[{\"" +
                        "name\":\"wow team\"," +
                        "\"count\":1" +
                    "}]},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handle_teamsNotFound() throws WriteResponseException {
        TestPublisher<TeamAccount> teamAccounts = TestPublisher.create();
        Flux<TeamAccount> error = teamAccounts.error(new RuntimeException("blowing up")).flux();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(error);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.team.list.error\"," +
                "\"code\":422," +
                "\"message\":\"could not fetch teams\"," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_teamPayloadNotReturned() throws WriteResponseException {
        TestPublisher<TeamPayload> testPublisher = TestPublisher.create();

        Mono<TeamPayload> error = testPublisher.error(new RuntimeException("blowing up")).mono();

        UUID teamId = UUID.randomUUID();
        when(message.getCollaboratorLimit()).thenReturn(2);

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(teamService.getTeamPayload(teamId, message.getCollaboratorLimit()))
                .thenReturn(error);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.team.list.error\"," +
                "\"code\":422," +
                "\"message\":\"could not fetch teams\"," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_teamPayloadEmptyStream() throws WriteResponseException {
        UUID teamId = UUID.randomUUID();
        when(message.getCollaboratorLimit()).thenReturn(2);

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(teamService.getTeamPayload(teamId, message.getCollaboratorLimit()))
                .thenReturn(Mono.empty());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.team.list.ok\"," +
                "\"response\":{" +
                    "\"teams\":[]" +
                "}," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_teamsEmptyStream() throws WriteResponseException {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.team.list.ok\"," +
                "\"response\":{" +
                    "\"teams\":[]" +
                "}," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
