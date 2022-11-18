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

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.rtm.message.recv.plugin.PluginMessage;

import reactor.core.publisher.Mono;

class AllowPluginOwnerTest {

    @Mock
    private PluginPermissionService pluginPermissionService;

    @InjectMocks
    private AllowPluginOwner allowPluginOwner;

    private PluginMessage pluginMessage;
    private AuthenticationContext authenticationContext;
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        pluginMessage = mock(PluginMessage.class);
        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);

        when(account.getId()).thenReturn(accountId);
        when(pluginMessage.getPluginId()).thenReturn(pluginId);
        when(authenticationContext.getAccount()).thenReturn(account);

    }

    @Test
    void test_noPermissionLevelFound() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.empty());
        assertFalse(allowPluginOwner.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_permissionLevelReviewer() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertFalse(allowPluginOwner.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_permissionLevelContributor() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        assertFalse(allowPluginOwner.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_permissionLevelOwner() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        assertTrue(allowPluginOwner.test(authenticationContext, pluginMessage));
    }

}
