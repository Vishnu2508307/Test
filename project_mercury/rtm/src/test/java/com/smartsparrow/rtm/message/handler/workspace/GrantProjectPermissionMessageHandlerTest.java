package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.GrantProjectPermissionMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;

import reactor.core.publisher.Flux;

class GrantProjectPermissionMessageHandlerTest {

    @InjectMocks
    private GrantProjectPermissionMessageHandler handler;

    @Mock
    private TeamService teamService;

    @Mock
    private AccountService accountService;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private GrantProjectPermissionMessage message;

    private static final UUID projectId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
    }

    @Test
    void validate_noPermissionLevel() {
        when(message.getPermissionLevel()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("permissionLevel is required", fault.getMessage());
    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("projectId is required", fault.getMessage());
    }

    @Test
    void validate_noTeams_noAccounts() {
        when(message.getTeamIds()).thenReturn(null);
        when(message.getAccountIds()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("either accountIds or teamIds is required", e.getErrorMessage());
    }

    @Test
    void validate_accounts_and_teams() {
        when(message.getTeamIds()).thenReturn(new ArrayList<>());
        when(message.getAccountIds()).thenReturn(new ArrayList<>());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("too many arguments supplied. Either accountIds or teamIds is required", e.getErrorMessage());
    }

    @Test
    void handle_teams() throws WriteResponseException {
        final UUID teamId = UUID.randomUUID();

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamId));
        when(projectPermissionService.saveTeamPermission(any(UUID.class), eq(projectId), eq(PermissionLevel.CONTRIBUTOR)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.project.permission.grant.ok\"," +
                            "\"response\":{" +
                                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                "\"projectId\":\"" + projectId + "\"," +
                                "\"teamIds\":[\"" + teamId + "\"]" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_accounts() throws WriteResponseException {

        final UUID accountId = UUID.randomUUID();

        when(message.getTeamIds()).thenReturn(null);
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountId));
        when(projectPermissionService.saveAccountPermission(any(UUID.class), eq(projectId), eq(PermissionLevel.CONTRIBUTOR)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.project.permission.grant.ok\"," +
                            "\"response\":{" +
                                "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                "\"accountIds\":[\"" + accountId + "\"]," +
                                "\"projectId\":\"" + projectId + "\"}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
