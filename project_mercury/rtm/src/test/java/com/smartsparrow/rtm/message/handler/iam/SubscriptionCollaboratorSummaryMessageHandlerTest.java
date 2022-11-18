package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.smartsparrow.iam.data.SubscriptionAccountCollaborator;
import com.smartsparrow.iam.data.SubscriptionTeamCollaborator;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionCollaboratorSummayMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class SubscriptionCollaboratorSummaryMessageHandlerTest {

    @InjectMocks
    private SubscriptionCollaboratorSummaryMessageHandler handler;

    @Mock
    private TeamService teamService;

    @Mock
    private AccountService accountService;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private static final UUID subscriptionId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String messageId = "lol";
    private static final Integer limit = 1;
    private SubscriptionCollaboratorSummayMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(SubscriptionCollaboratorSummayMessage.class);

        when(message.getSubscriptionId()).thenReturn(subscriptionId);
        when(message.getLimit()).thenReturn(limit);
        when(message.getId()).thenReturn(messageId);
    }

    @Test
    void validate_noSubscriptionId() {
        when(message.getSubscriptionId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals("subscriptionId is required", e.getErrorMessage());
        assertEquals("iam.subscription.collaborator.summary.error", e.getType());
    }

    @Test
    void validate_invalidLimit() {
        when(message.getLimit()).thenReturn(-1);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals("limit `-1` should be >= 0", e.getErrorMessage());
        assertEquals("iam.subscription.collaborator.summary.error", e.getType());
    }

    @Test
    void handle_noTeams_noAccounts() throws WriteResponseException {
        when(subscriptionPermissionService.findTeamCollaborators(subscriptionId)).thenReturn(Flux.empty());
        when(subscriptionPermissionService.findAccountCollaborators(subscriptionId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"iam.subscription.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":0,\"collaborators\":{}" +
                            "},\"replyTo\":\"lol\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_noTeams() throws WriteResponseException {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();

        when(subscriptionPermissionService.findTeamCollaborators(subscriptionId)).thenReturn(Flux.empty());
        when(subscriptionPermissionService.findAccountCollaborators(subscriptionId)).thenReturn(Flux.just(
                new SubscriptionAccountCollaborator()
                        .setAccountId(accountIdOne)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR),
                new SubscriptionAccountCollaborator()
                        .setAccountId(accountIdTwo)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)
        ));

        when(accountService.getCollaboratorPayload(eq(accountIdOne), any(PermissionLevel.class)))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));
        when(accountService.getCollaboratorPayload(eq(accountIdTwo), any(PermissionLevel.class)))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"iam.subscription.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":2," +
                                "\"collaborators\":{" +
                                    "\"accounts\":[{}]" +
                                "}" +
                            "},\"replyTo\":\"lol\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_noAccounts() throws WriteResponseException {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(subscriptionPermissionService.findTeamCollaborators(subscriptionId)).thenReturn(Flux.just(
                new SubscriptionTeamCollaborator()
                        .setTeamId(teamIdOne)
                        .setPermissionLevel(PermissionLevel.REVIEWER),
                new SubscriptionTeamCollaborator()
                        .setTeamId(teamIdTwo)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)
        ));
        when(subscriptionPermissionService.findAccountCollaborators(subscriptionId)).thenReturn(Flux.empty());

        when(teamService.getTeamCollaboratorPayload(eq(teamIdOne), any(PermissionLevel.class)))
                .thenReturn(Mono.just(new TeamCollaboratorPayload()));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.subscription.collaborator.summary.ok\"," +
                    "\"response\":{" +
                    "\"total\":2," +
                    "\"collaborators\":{" +
                        "\"teams\":[{}]" +
                    "}" +
                "},\"replyTo\":\"lol\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_withTeams_withAccounts() throws WriteResponseException {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();

        when(message.getLimit()).thenReturn(4);

        when(subscriptionPermissionService.findTeamCollaborators(subscriptionId)).thenReturn(Flux.just(
                new SubscriptionTeamCollaborator()
                        .setTeamId(teamIdOne)
                        .setPermissionLevel(PermissionLevel.REVIEWER),
                new SubscriptionTeamCollaborator()
                        .setTeamId(teamIdTwo)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)
        ));

        when(subscriptionPermissionService.findAccountCollaborators(subscriptionId)).thenReturn(Flux.just(
                new SubscriptionAccountCollaborator()
                        .setAccountId(accountIdOne)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR),
                new SubscriptionAccountCollaborator()
                        .setAccountId(accountIdTwo)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)
        ));

        when(teamService.getTeamCollaboratorPayload(any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(Mono.just(new TeamCollaboratorPayload()));

        when(accountService.getCollaboratorPayload(any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"iam.subscription.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":4," +
                                "\"collaborators\":{" +
                                    "\"teams\":[{},{}]," +
                                    "\"accounts\":[{},{}]" +
                                "}" +
                            "},\"replyTo\":\"lol\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<AccountCollaboratorPayload> payloadPublisher = TestPublisher.create();
        payloadPublisher.error(new RuntimeException("nooooo"));

        UUID accountIdOne = UUID.randomUUID();

        when(subscriptionPermissionService.findTeamCollaborators(subscriptionId)).thenReturn(Flux.empty());
        when(subscriptionPermissionService.findAccountCollaborators(subscriptionId)).thenReturn(Flux.just(
                new SubscriptionAccountCollaborator()
                        .setAccountId(accountIdOne)
                        .setPermissionLevel(PermissionLevel.OWNER)
        ));

        when(accountService.getCollaboratorPayload(accountIdOne, PermissionLevel.OWNER))
                .thenReturn(payloadPublisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"iam.subscription.collaborator.summary.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error while listing collaborators\"," +
                            "\"replyTo\":\"lol\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

}
