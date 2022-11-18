package com.smartsparrow.rtm.message.handler.workspace;

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
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.RevokeProjectPermissionMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class RevokeProjectPermissionMessageHandlerTest {

    @InjectMocks
    private RevokeProjectPermissionMessageHandler handler;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private AccountService accountService;

    @Mock
    private TeamService teamService;

    @Mock
    private RevokeProjectPermissionMessage message;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getAccountId()).thenReturn(null);
        when(message.getTeamId()).thenReturn(null);

        when(accountService.findById(accountId)).thenReturn(Flux.just(new Account()));
        when(teamService.findTeam(teamId)).thenReturn(Mono.just(new TeamSummary()));
    }

    @Test
    void validate_noAccount_noTeam() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("either accountId or teamId is required", fault.getMessage());
    }

    @Test
    void validate_account_and_team() {
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getTeamId()).thenReturn(teamId);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("either accountId or teamId is required", e.getMessage());
    }

    @Test
    void validate_noProjectId() {
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("projectId is required", fault.getMessage());
    }

    @Test
    void validate_accountNotFound() {
        when(message.getAccountId()).thenReturn(accountId);
        when(accountService.findById(accountId)).thenReturn(Flux.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("account " + accountId + " not found", e.getErrorMessage());

    }

    @Test
    void validate_teamNotFound() {
        when(message.getTeamId()).thenReturn(teamId);
        when(teamService.findTeam(teamId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("team " + teamId + " not found", e.getErrorMessage());
    }

    @Test
    void handle_account() throws WriteResponseException {
        when(message.getAccountId()).thenReturn(accountId);
        when(projectPermissionService.deleteAccountPermission(accountId, projectId))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.permission.revoke.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handle_team() throws WriteResponseException {
        when(message.getTeamId()).thenReturn(teamId);
        when(projectPermissionService.deleteTeamPermission(teamId, projectId))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.project.permission.revoke.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
