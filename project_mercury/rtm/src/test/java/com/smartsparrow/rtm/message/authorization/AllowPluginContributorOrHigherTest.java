package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.plugin.AccountPluginPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.rtm.message.recv.plugin.PluginMessage;
import com.smartsparrow.rtm.message.recv.plugin.PluginPermissionMessage;

import reactor.core.publisher.Mono;

class AllowPluginContributorOrHigherTest {

    @Mock
    private PluginPermissionService pluginPermissionService;

    private AllowPluginContributorOrHigher allowPluginContributorOrHigher;
    private AuthenticationContext authenticationContext;
    private Account account;
    private PluginMessage pluginMessage;
    private AccountPluginPermission authorized;
    private AccountPluginPermission unauthorized;
    private UUID accountId = UUID.randomUUID();
    private UUID pluginId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        allowPluginContributorOrHigher = new AllowPluginContributorOrHigher(pluginPermissionService);

        authenticationContext = mock(AuthenticationContext.class);
        account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(authenticationContext.getAccount()).thenReturn(account);

        pluginMessage = mock(PluginPermissionMessage.class);
        when(pluginMessage.getPluginId()).thenReturn(pluginId);

        authorized = mock(AccountPluginPermission.class);
        when(authorized.getPermissionLevel()).thenReturn(PermissionLevel.OWNER);
        unauthorized = mock(AccountPluginPermission.class);
        when(unauthorized.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);
    }

    @Test
    void test_ownerAuthorized() {
        when(pluginPermissionService.findHighestPermissionLevel(account.getId(), pluginMessage.getPluginId()))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(allowPluginContributorOrHigher.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_contributorAuthorized() {
        when(pluginPermissionService.findHighestPermissionLevel(account.getId(), pluginMessage.getPluginId()))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(allowPluginContributorOrHigher.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_reviewerUnauthorized() {
        when(pluginPermissionService.findHighestPermissionLevel(account.getId(), pluginMessage.getPluginId()))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(allowPluginContributorOrHigher.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_unauthorizedPermissionNotFound() {
        when(pluginPermissionService.findHighestPermissionLevel(account.getId(), pluginMessage.getPluginId()))
                .thenReturn(Mono.empty());

        assertFalse(allowPluginContributorOrHigher.test(authenticationContext, pluginMessage));
    }

}
