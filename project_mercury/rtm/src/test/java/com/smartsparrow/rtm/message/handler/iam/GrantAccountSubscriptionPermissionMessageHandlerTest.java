package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertAll;
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

import com.google.common.collect.Lists;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.Subscription;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.iam.service.SubscriptionService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.iam.GrantSubscriptionPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GrantAccountSubscriptionPermissionMessageHandlerTest {

    @InjectMocks
    private GrantSubscriptionPermissionMessageHandler handler;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    @Mock
    private AccountService accountService;

    @Mock
    private TeamService teamService;

    @Mock
    private SubscriptionService subscriptionService;

    private GrantSubscriptionPermissionMessage message;
    private static final String messageId = "me.id";
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(GrantSubscriptionPermissionMessage.class);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(message.getAccountIds()).thenReturn(null);
        when(message.getTeamIds()).thenReturn(null);
        when(message.getSubscriptionId()).thenReturn(subscriptionId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);
        when(message.getId()).thenReturn(messageId);
        when(accountService.findById(accountId)).thenReturn(Flux.just(account));
        when(subscriptionService.find(subscriptionId)).thenReturn(Flux.just(new Subscription()));
    }

    @Test
    void validate_accountsAndTeamSupplied() {
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(UUID.randomUUID()));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(UUID.randomUUID()));

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("too many arguments supplied. Either accountIds or teamIds is required",
                    e.getErrorMessage());
            assertEquals("iam.subscription.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_accountsAndTeamNotSupplied() {
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("either accountIds or teamIds is required", e.getErrorMessage());
            assertEquals("iam.subscription.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_teamNotFound() {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(teamService.findTeam(teamIdOne)).thenReturn(Mono.just(new TeamSummary()));
        when(teamService.findTeam(teamIdTwo)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(String.format("team %s not found", teamIdTwo), e.getErrorMessage());
            assertEquals("iam.subscription.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_accountNotFound() {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(accountService.findById(accountIdOne)).thenReturn(Flux.just(new Account()));
        when(accountService.findById(accountIdTwo)).thenReturn(Flux.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(String.format("account %s not found", accountIdTwo), e.getErrorMessage());
            assertEquals("iam.subscription.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_subscriptionIdNotSupplied() {
        when(message.getSubscriptionId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("subscriptionId is required", e.getErrorMessage());
            assertEquals("iam.subscription.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_permissionLevelNotSupplied() {
        when(message.getPermissionLevel()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("permissionLevel is required", e.getErrorMessage());
            assertEquals("iam.subscription.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_subscriptionNotFound() {
        when(subscriptionService.find(subscriptionId)).thenReturn(Flux.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(String.format("subscription %s not found", subscriptionId), e.getErrorMessage());
            assertEquals("iam.subscription.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void handle_accountIds() throws WriteResponseException {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));

        when(subscriptionPermissionService.saveAccountPermission(any(UUID.class), any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verify(subscriptionPermissionService).saveAccountPermission(eq(accountIdOne), any(UUID.class), any(PermissionLevel.class));
        verify(subscriptionPermissionService).saveAccountPermission(eq(accountIdTwo), any(UUID.class), any(PermissionLevel.class));

        String expected = "{" +
                "\"type\":\"iam.subscription.permission.grant.ok\"," +
                "\"response\":{" +
                    "\"permissionLevel\":\"REVIEWER\"," +
                    "\"accountIds\":[\"" + accountIdOne + "\",\"" + accountIdTwo + "\"]," +
                    "\"subscriptionId\":\"" + subscriptionId + "\"" +
                "},\"replyTo\":\"me.id\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_teamIds() throws WriteResponseException {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(subscriptionPermissionService.saveTeamPermission(any(UUID.class), any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verify(subscriptionPermissionService).saveTeamPermission(eq(teamIdOne), any(UUID.class), any(PermissionLevel.class));
        verify(subscriptionPermissionService).saveTeamPermission(eq(teamIdTwo), any(UUID.class), any(PermissionLevel.class));

        String expected = "{" +
                            "\"type\":\"iam.subscription.permission.grant.ok\"," +
                            "\"response\":{" +
                                "\"permissionLevel\":\"REVIEWER\"," +
                                "\"subscriptionId\":\"" + subscriptionId + "\"," +
                                "\"teamIds\":[\"" + teamIdOne + "\",\"" + teamIdTwo + "\"]" +
                            "},\"replyTo\":\"me.id\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_fails() throws WriteResponseException {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException(":face_palm:"));

        when(subscriptionPermissionService.saveTeamPermission(any(UUID.class), any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(publisher.flux());

        handler.handle(session, message);

        verify(subscriptionPermissionService).saveTeamPermission(eq(teamIdOne), any(UUID.class), any(PermissionLevel.class));
        verify(subscriptionPermissionService).saveTeamPermission(eq(teamIdTwo), any(UUID.class), any(PermissionLevel.class));

        String expected = "{" +
                            "\"type\":\"iam.subscription.permission.grant.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error granting permission\"," +
                            "\"replyTo\":\"me.id\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
