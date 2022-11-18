package com.smartsparrow.rtm.message.handler.team;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.team.TeamPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GrantTeamPermissionMessageHandlerTest {

    @InjectMocks
    private GrantTeamPermissionMessageHandler handler;

    @Mock
    private TeamService teamService;

    @Mock
    private AccountService accountService;

    private TeamPermissionMessage message;
    private TeamPermissionMessage message2;
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID accountId2 = UUID.randomUUID();
    private static final String messageId = "messageId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(TeamPermissionMessage.class);

        when(message.getTeamId()).thenReturn(teamId);
        when(message.getAccountIds()).thenReturn(Arrays.asList(accountId));
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(message.getId()).thenReturn(messageId);

        message2 = mock(TeamPermissionMessage.class);

        when(message2.getTeamId()).thenReturn(teamId);
        when(message2.getAccountIds()).thenReturn(Arrays.asList(accountId,accountId2));
        when(message2.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(message2.getId()).thenReturn(messageId);

        when(teamService.findTeam(teamId)).thenReturn(Mono.just(new TeamSummary()));

        when(accountService.findById(accountId)).thenReturn(Flux.just(new Account()));


    }

    @Test
    void validate_accountIdsNotSupplied() {
        when(message.getAccountIds()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError("accountIds is required", e);

    }

    @Test
    void validate_teamIdNotSupplied() {
        when(message.getTeamId()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError("teamId is required", e);
    }

    @Test
    void validate_permissionLevelNotSupplied() {
        when(message.getPermissionLevel()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError("permissionLevel is required", e);
    }

    @Test
    void validate_teamNotFound() {
        when(teamService.findTeam(teamId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError(String.format("team %s not found", teamId), e);
    }

    @Test
    void validate_accountNorFound() {
        when(accountService.findById(accountId)).thenReturn(Flux.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError(String.format("account %s not found", accountId), e);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(teamService.savePermission(accountId, teamId, PermissionLevel.CONTRIBUTOR)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verify(teamService, atLeastOnce()).savePermission(accountId, teamId, PermissionLevel.CONTRIBUTOR);

        String expected = "{" +
                            "\"type\":\"iam.team.permission.grant.ok\"," +
                            "\"response\":{" +
                                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                "\"accountIds\":[\""+accountId+"\"]," +
                                "\"teamId\":\""+teamId+"\"" +
                            "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_MultipleAccounts() throws WriteResponseException {
        when(teamService.savePermission(accountId, teamId, PermissionLevel.CONTRIBUTOR)).thenReturn(Flux.just(new Void[]{}));
        when(teamService.savePermission(accountId2, teamId, PermissionLevel.CONTRIBUTOR)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message2);

        verify(teamService, atLeastOnce()).savePermission(accountId, teamId, PermissionLevel.CONTRIBUTOR);

        String expected = "{" +
                "\"type\":\"iam.team.permission.grant.ok\"," +
                "\"response\":{" +
                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                "\"accountIds\":[\""+accountId+"\",\""+accountId2+"\"]," +
                "\"teamId\":\""+teamId+"\"" +
                "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> savePermissionPublisher = TestPublisher.create();
        Flux<Void> error = savePermissionPublisher.error(new RuntimeException()).flux();
        when(teamService.savePermission(accountId, teamId, PermissionLevel.CONTRIBUTOR)).thenReturn(error);

        handler.handle(session, message);

        verify(teamService, atLeastOnce()).savePermission(accountId, teamId, PermissionLevel.CONTRIBUTOR);

        String expected = "{" +
                            "\"type\":\"iam.team.permission.grant.error\"," +
                            "\"code\":422," +
                            "\"message\":\"failed to grant permission\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    private void verifyValidationError(String message, RTMValidationException e) {
        assertAll(()->{
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
            assertEquals(message, e.getErrorMessage());
            assertEquals(messageId, e.getReplyTo());
            assertEquals("iam.team.permission.grant.error", e.getType());
        });
    }

}
