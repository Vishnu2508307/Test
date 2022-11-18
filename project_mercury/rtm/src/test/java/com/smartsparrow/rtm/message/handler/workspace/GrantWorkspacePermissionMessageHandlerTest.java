package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.GrantWorkspacePermissionMessage;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GrantWorkspacePermissionMessageHandlerTest {

    @InjectMocks
    private GrantWorkspacePermissionMessageHandler handler;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private AccountService accountService;

    @Mock
    private TeamService teamService;

    private GrantWorkspacePermissionMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String messageId = "message id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(GrantWorkspacePermissionMessage.class);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(message.getAccountIds()).thenReturn(null);
        when(message.getTeamIds()).thenReturn(null);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);
        when(message.getId()).thenReturn(messageId);
        when(accountService.findById(accountId)).thenReturn(Flux.just(account));
        when(workspaceService.fetchById(workspaceId)).thenReturn(Mono.just(new Workspace()));
    }

    @Test
    void validate_accountsAndTeamSupplied() {
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(UUID.randomUUID()));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(UUID.randomUUID()));

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("too many arguments supplied. Either accountIds or teamIds is required",
                    e.getErrorMessage());
            assertEquals("workspace.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_accountsAndTeamNotSupplied() {
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("either accountIds or teamIds is required", e.getErrorMessage());
            assertEquals("workspace.permission.grant.error", e.getType());
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
            assertEquals("workspace.permission.grant.error", e.getType());
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
            assertEquals("workspace.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_workspaceIdNotSupplied() {
        when(message.getWorkspaceId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("workspaceId is required", e.getErrorMessage());
            assertEquals("workspace.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_permissionLevelNotSupplied() {
        when(message.getPermissionLevel()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("permissionLevel is required", e.getErrorMessage());
            assertEquals("workspace.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void validate_workspaceNotFound() {
        when(workspaceService.fetchById(workspaceId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(String.format("workspace %s not found", workspaceId), e.getErrorMessage());
            assertEquals("workspace.permission.grant.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
        });
    }

    @Test
    void handle_success_teams() throws WriteResponseException {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(workspaceService.saveTeamPermission(any(UUID.class), any(UUID.class), eq(PermissionLevel.REVIEWER)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verify(workspaceService, times(2))
                .saveTeamPermission(any(UUID.class), eq(workspaceId), eq(PermissionLevel.REVIEWER));

        String expected = "{" +
                            "\"type\":\"workspace.permission.grant.ok\"," +
                            "\"response\":{" +
                                "\"permissionLevel\":\"REVIEWER\"," +
                                "\"teamIds\":[\"" + teamIdOne + "\",\"" + teamIdTwo + "\"]," +
                                "\"workspaceId\":\"" + workspaceId + "\"" +
                            "},\"replyTo\":\"message id\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_accounts() throws WriteResponseException {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));

        when(workspaceService.savePermissions(any(UUID.class), any(UUID.class), eq(PermissionLevel.REVIEWER)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verify(workspaceService, times(2))
                .savePermissions(any(UUID.class), eq(workspaceId), eq(PermissionLevel.REVIEWER));

        String expected = "{" +
                "\"type\":\"workspace.permission.grant.ok\"," +
                "\"response\":{" +
                "\"permissionLevel\":\"REVIEWER\"," +
                "\"accountIds\":[\"" + accountIdOne + "\",\"" + accountIdTwo + "\"]," +
                "\"workspaceId\":\"" + workspaceId + "\"" +
                "},\"replyTo\":\"message id\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle_fail() throws WriteResponseException {
        TestPublisher<Void> permissionPublisher = TestPublisher.create();
        permissionPublisher.error(new RuntimeException(":face_palm:"));
        UUID accountIdOne = UUID.randomUUID();

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne));
        when(workspaceService.savePermissions(accountIdOne, workspaceId, message.getPermissionLevel()))
                .thenReturn(permissionPublisher.flux());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.permission.grant.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error granting permission\"," +
                            "\"replyTo\":\"message id\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
