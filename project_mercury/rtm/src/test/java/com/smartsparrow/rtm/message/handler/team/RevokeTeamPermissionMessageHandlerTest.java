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

import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.team.RevokeTeamPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class RevokeTeamPermissionMessageHandlerTest {

    @InjectMocks
    private RevokeTeamPermissionMessageHandler handler;

    @Mock
    private TeamService teamService;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final String messageId = "messageId";
    private RevokeTeamPermissionMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(RevokeTeamPermissionMessage.class);

        when(message.getAccountIds()).thenReturn(Arrays.asList(accountId));
        when(message.getTeamId()).thenReturn(teamId);
        when(message.getId()).thenReturn(messageId);

        when(teamService.fetchPermission(accountId, teamId)).thenReturn(Mono.just(new TeamPermission()));
    }

    @Test
    void validate_teamIdNotSupplied() {
        when(message.getTeamId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError("teamId is required", e);
    }

    @Test
    void validate_accountIdNotSupplied() {
        when(message.getAccountIds()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError("accountIds is required", e);
    }

    @Test
    void validate_permissionNotFound() {
        when(teamService.fetchPermission(accountId, teamId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        verifyValidationError("permission not found for account " + accountId, e);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(teamService.deletePermission(accountId, teamId)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verify(teamService, atLeastOnce()).deletePermission(accountId, teamId);

        String expected = "{\"type\":\"iam.team.permission.revoke.ok\",\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> deletePermissionPublisher = TestPublisher.create();
        Flux<Void> error = deletePermissionPublisher.error(new RuntimeException()).flux();
        when(teamService.deletePermission(accountId, teamId)).thenReturn(error);

        handler.handle(session, message);

        verify(teamService, atLeastOnce()).deletePermission(accountId, teamId);

        String expected = "{" +
                            "\"type\":\"iam.team.permission.revoke.error\"," +
                            "\"code\":422," +
                            "\"message\":\"failed to revoke permission\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    private void verifyValidationError(String message, RTMValidationException e) {
        assertAll(()->{
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
            assertEquals(message, e.getErrorMessage());
            assertEquals(messageId, e.getReplyTo());
            assertEquals("iam.team.permission.revoke.error", e.getType());
        });
    }
}
