package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.message.recv.team.TeamMessage;

import reactor.core.publisher.Mono;

class AllowTeamContributorOrHigherTest {

    @InjectMocks
    private AllowTeamContributorOrHigher authorizer;

    @Mock
    private TeamService teamService;

    private static final UUID teamId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private AuthenticationContext authenticationContext;
    private TeamPermission teamPermission;
    private TeamMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(TeamMessage.class);
        authenticationContext = mock(AuthenticationContext.class);
        teamPermission = mock(TeamPermission.class);
        Account account = mock(Account.class);

        when(account.getId()).thenReturn(accountId);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(message.getTeamId()).thenReturn(teamId);
        when(teamService.fetchPermission(accountId, teamId)).thenReturn(Mono.just(teamPermission));
    }

    @Test
    void test_permissionNotFound() {
        when(teamService.fetchPermission(accountId, teamId)).thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_foundReviewer() {
        when(teamPermission.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_foundContributor() {
        when(teamPermission.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_foundOwner() {
        when(teamPermission.getPermissionLevel()).thenReturn(PermissionLevel.OWNER);
        assertTrue(authorizer.test(authenticationContext, message));
    }
}
