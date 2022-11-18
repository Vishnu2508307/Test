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

import com.smartsparrow.iam.data.permission.workspace.AccountWorkspacePermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.workspace.GrantWorkspacePermissionMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;

class AllowGrantEqualOrHigherAccountWorkspacePermissionLevelTest {

    @InjectMocks
    private AllowGrantEqualOrHigherWorkspacePermissionLevel authorizer;

    @Mock
    private WorkspaceService workspaceService;

    private GrantWorkspacePermissionMessage message;
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID requestingAccountId = UUID.randomUUID();
    private static final UUID accountIdOne = UUID.randomUUID();
    private static final UUID accountIdTwo = UUID.randomUUID();
    private static final UUID teamIdOne = UUID.randomUUID();
    private static final UUID teamIdTwo = UUID.randomUUID();
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(GrantWorkspacePermissionMessage.class);
        authenticationContext = mock(AuthenticationContext.class);

        when(message.getWorkspaceId()).thenReturn(workspaceId);

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
        when(workspaceService.findHighestPermissionLevel(requestingAccountId, workspaceId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied accountIds")
    void test_accountIdsPermissionNotFound() {
        when(workspaceService.findHighestPermissionLevel(requestingAccountId, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(workspaceService.fetchPermission(accountIdOne, workspaceId)).thenReturn(Mono.empty());
        when(workspaceService.fetchPermission(accountIdTwo, workspaceId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 of the supplied accountIds has higher permission")
    void test_invalidAccountPermissionFound() {
        when(workspaceService.findHighestPermissionLevel(requestingAccountId, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(workspaceService.fetchPermission(accountIdOne, workspaceId))
                .thenReturn(Mono.just(new AccountWorkspacePermission()
                        .setPermissionLevel(PermissionLevel.OWNER)
                        .setAccountId(accountIdOne)
                        .setWorkspaceId(workspaceId)));
        when(workspaceService.fetchPermission(accountIdTwo, workspaceId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied accountIds have either lower or no permission at all")
    void test_validAccountPermissionFound() {
        when(workspaceService.findHighestPermissionLevel(requestingAccountId, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(workspaceService.fetchPermission(accountIdOne, workspaceId))
                .thenReturn(Mono.just(new AccountWorkspacePermission()
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)
                        .setAccountId(accountIdOne)
                        .setWorkspaceId(workspaceId)));
        when(workspaceService.fetchPermission(accountIdTwo, workspaceId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied teamIds")
    @SuppressWarnings("Duplicates")
    void test_teamIdsPermissionNotFound() {
        when(workspaceService.findHighestPermissionLevel(requestingAccountId, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(workspaceService.fetchTeamPermission(teamIdOne, workspaceId)).thenReturn(Mono.empty());
        when(workspaceService.fetchTeamPermission(teamIdTwo, workspaceId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 of the supplied teamIds has higher permission")
    void test_invalidTeamPermissionFound() {
        when(workspaceService.findHighestPermissionLevel(requestingAccountId, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(workspaceService.fetchTeamPermission(teamIdOne, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        when(workspaceService.fetchTeamPermission(teamIdTwo, workspaceId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied teamIds have either lower or no permission at all")
    void test_validTeamPermissionFound() {
        when(workspaceService.findHighestPermissionLevel(requestingAccountId, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(workspaceService.fetchTeamPermission(teamIdOne, workspaceId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        when(workspaceService.fetchTeamPermission(teamIdTwo, workspaceId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }
}
