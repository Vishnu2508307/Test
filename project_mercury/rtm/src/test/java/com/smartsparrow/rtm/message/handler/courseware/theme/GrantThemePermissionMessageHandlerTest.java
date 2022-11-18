package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.message.handler.courseware.theme.GrantThemePermissionMessageHandler.THEME_PERMISSION_GRANT_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.theme.GrantThemePermissionMessage;
import com.smartsparrow.workspace.data.Theme;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GrantThemePermissionMessageHandlerTest {

    @InjectMocks
    private GrantThemePermissionMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private AccountService accountService;

    @Mock
    private TeamService teamService;

    @Mock
    private GrantThemePermissionMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID themeId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String messageId = "message id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(message.getAccountIds()).thenReturn(null);
        when(message.getTeamIds()).thenReturn(null);
        when(message.getThemeId()).thenReturn(themeId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);
        when(message.getId()).thenReturn(messageId);
        when(accountService.findById(accountId)).thenReturn(Flux.just(account));
        when(themeService.fetchThemeById(themeId)).thenReturn(Mono.just(new Theme()));
    }

    @Test
    void validate_accountsAndTeamSupplied() {
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(UUID.randomUUID()));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(UUID.randomUUID()));


        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("too many arguments supplied. Either accountIds or teamIds is required", ex.getMessage());

    }

    @Test
    void validate_accountsAndTeamNotSupplied() {
        when(message.getTeamIds()).thenReturn(null);
        when(message.getAccountIds()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("either accountIds or teamIds is required", ex.getMessage());
    }

    @Test
    void validate_teamNotFound() {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(teamService.findTeam(teamIdOne)).thenReturn(Mono.just(new TeamSummary()));
        when(teamService.findTeam(teamIdTwo)).thenReturn(Mono.empty());

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals(String.format("team %s not found", teamIdTwo), ex.getMessage());
    }

    @Test
    void validate_accountNotFound() {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(accountService.findById(accountIdOne)).thenReturn(Flux.just(new Account()));
        when(accountService.findById(accountIdTwo)).thenReturn(Flux.empty());

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals(String.format("account %s not found", accountIdTwo), ex.getMessage());
    }

    @Test
    void validate_themeIdNotSupplied() {
        when(message.getThemeId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("themeId is required", ex.getMessage());
    }

    @Test
    void validate_permissionLevelNotSupplied() {
        when(message.getPermissionLevel()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("permissionLevel is required", ex.getMessage());
    }

    @Test
    void validate_workspaceNotFound() {
        when(themeService.fetchThemeById(themeId)).thenReturn(Mono.empty());

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals(String.format("theme %s not found", themeId), ex.getMessage());
    }

    @Test
    void handle_success_teams() throws IOException {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();
        ArrayList<UUID> ids = Lists.newArrayList(teamIdOne, teamIdTwo);

        when(message.getTeamIds()).thenReturn(ids);

        when(themeService.saveTeamPermission(any(UUID.class), eq(themeId), eq(PermissionLevel.REVIEWER)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(THEME_PERMISSION_GRANT_OK, response.getType());
                assertEquals(themeId.toString(), response.getResponse().get("themeId"));
                assertEquals(PermissionLevel.REVIEWER.toString(), response.getResponse().get("permissionLevel"));
                assertEquals(ids.size(), ((List) response.getResponse().get("teamIds")).size());
            });
        });
        verify(themeService, times(2))
                .saveTeamPermission(any(UUID.class), eq(themeId), eq(PermissionLevel.REVIEWER));
    }

    @Test
    void handle_success_accounts() throws IOException {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        ArrayList<UUID> ids = Lists.newArrayList(accountIdOne, accountIdTwo);
        when(message.getAccountIds()).thenReturn(ids);

        when(themeService.saveAccountPermissions(any(UUID.class), eq(themeId), eq(PermissionLevel.REVIEWER)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(THEME_PERMISSION_GRANT_OK, response.getType());
                assertEquals(themeId.toString(), response.getResponse().get("themeId"));
                assertEquals(PermissionLevel.REVIEWER.toString(), response.getResponse().get("permissionLevel"));
                assertEquals(ids.size(), ((List) response.getResponse().get("accountIds")).size());
            });
        });
        verify(themeService, times(2))
                .saveAccountPermissions(any(UUID.class), eq(themeId), eq(PermissionLevel.REVIEWER));
    }

    @Test
    void handle_fail() throws IOException {

        TestPublisher<Void> error = TestPublisher.create();
        UUID accountIdOne = UUID.randomUUID();
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne));
        when(themeService.saveAccountPermissions(accountIdOne, themeId, PermissionLevel.REVIEWER))
                .thenReturn(error.flux());
        error.error(new RuntimeException());

        handler.handle(session, message);

        verify(themeService, atLeastOnce()).saveAccountPermissions(accountIdOne, themeId, PermissionLevel.REVIEWER);

        String expected = "{\"type\":\"theme.permission.grant.error\"," +
                "\"code\":422," +
                "\"message\":\"error granting permission over a theme\"," +
                "\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
