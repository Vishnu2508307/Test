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

class AllowPluginReviewerOrHigherTest {

    @Mock
    private PluginPermissionService pluginPermissionService;

    @InjectMocks
    private AllowPluginReviewerOrHigher authorizer;

    private AuthenticationContext authenticationContext;
    private PluginMessage pluginMessage;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        pluginMessage = mock(PluginMessage.class);
        Account account = mock(Account.class);

        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(pluginMessage.getPluginId()).thenReturn(pluginId);
    }

    @Test
    void test_permissionNotFound() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_reviewerPermissionFound() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_contributorPermissionFound() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, pluginMessage));
    }

    @Test
    void test_ownerPermissionFound() {
        when(pluginPermissionService.findHighestPermissionLevel(accountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(authorizer.test(authenticationContext, pluginMessage));
    }

}
