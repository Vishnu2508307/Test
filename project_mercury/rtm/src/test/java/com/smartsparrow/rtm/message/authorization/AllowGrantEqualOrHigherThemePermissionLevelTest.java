package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.service.ThemePermissionService;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByAccount;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.courseware.theme.GrantThemePermissionMessage;

import reactor.core.publisher.Mono;

class AllowGrantEqualOrHigherThemePermissionLevelTest {

    @InjectMocks
    private AllowGrantEqualOrHigherThemePermissionLevel authorizer;

    @Mock
    private ThemePermissionService themePermissionService;
    @Mock
    private GrantThemePermissionMessage message;

    private static final UUID themeId = UUID.randomUUID();
    private static final UUID requestingAccountId = UUID.randomUUID();
    private static final UUID accountIdOne = UUID.randomUUID();
    private static final UUID accountIdTwo = UUID.randomUUID();
    private static final UUID teamIdOne = UUID.randomUUID();
    private static final UUID teamIdTwo = UUID.randomUUID();
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        authenticationContext = mock(AuthenticationContext.class);

        when(message.getThemeId()).thenReturn(themeId);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(requestingAccountId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        when(message.getAccountIds()).thenReturn(null);
        when(message.getTeamIds()).thenReturn(null);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    @DisplayName("Not Permitted: permission not found for requesting account")
    void test_requestingPermissionNotFound() {
        when(themePermissionService.findHighestPermissionLevel(requestingAccountId, themeId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied accountIds")
    void test_accountIdsPermissionNotFound() {
        when(themePermissionService.findHighestPermissionLevel(requestingAccountId, themeId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(themePermissionService.fetchThemePermissionByAccount(accountIdOne, themeId)).thenReturn(Mono.empty());
        when(themePermissionService.fetchThemePermissionByAccount(accountIdTwo, themeId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 of the supplied accountIds has higher permission")
    void test_invalidAccountPermissionFound() {
        when(themePermissionService.findHighestPermissionLevel(requestingAccountId, themeId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(themePermissionService.fetchThemePermissionByAccount(accountIdOne, themeId))
                .thenReturn(Mono.just(new ThemePermissionByAccount()
                                              .setPermissionLevel(PermissionLevel.OWNER)
                                              .setAccountId(accountIdOne)
                                              .setThemeId(themeId)));

        when(themePermissionService.fetchThemePermissionByAccount(accountIdTwo, themeId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied accountIds have either lower or no permission at all")
    void test_validAccountPermissionFound() {
        when(themePermissionService.findHighestPermissionLevel(requestingAccountId, themeId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(themePermissionService.fetchThemePermissionByAccount(accountIdOne, themeId))
                .thenReturn(Mono.just(new ThemePermissionByAccount()
                                              .setPermissionLevel(PermissionLevel.CONTRIBUTOR)
                                              .setAccountId(accountIdOne)
                                              .setThemeId(themeId)));
        when(themePermissionService.fetchThemePermissionByAccount(accountIdTwo, themeId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied teamIds")
    @SuppressWarnings("Duplicates")
    void test_teamIdsPermissionNotFound() {
        when(themePermissionService.findHighestPermissionLevel(requestingAccountId, themeId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(themePermissionService.fetchThemePermissionByTeam(teamIdOne, themeId)).thenReturn(Mono.empty());
        when(themePermissionService.fetchThemePermissionByTeam(teamIdTwo, themeId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 of the supplied teamIds has higher permission")
    void test_invalidTeamPermissionFound() {
        when(themePermissionService.findHighestPermissionLevel(requestingAccountId, themeId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(themePermissionService.fetchThemePermissionByTeam(teamIdOne, themeId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        when(themePermissionService.fetchThemePermissionByTeam(teamIdTwo, themeId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied teamIds have either lower or no permission at all")
    void test_validTeamPermissionFound() {
        when(themePermissionService.findHighestPermissionLevel(requestingAccountId, themeId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(themePermissionService.fetchThemePermissionByTeam(teamIdOne, themeId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        when(themePermissionService.fetchThemePermissionByTeam(teamIdTwo, themeId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }
}
