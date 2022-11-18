package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionMessage;

import reactor.core.publisher.Mono;

class AllowSubscriptionReviewerOrHigherTest {

    @InjectMocks
    private AllowSubscriptionReviewerOrHigher authorizer;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private AuthenticationContext authenticationContext;
    private UUID subscriptionId = UUID.randomUUID();
    private UUID accountId = UUID.randomUUID();
    private SubscriptionMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        message = mock(SubscriptionMessage.class);

        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        when(message.getSubscriptionId()).thenReturn(subscriptionId);
    }

    @Test
    void test_noAccountFromContext() {
        when(authenticationContext.getAccount()).thenReturn(null);
        assertFalse(authorizer.test(authenticationContext, message));
        verify(subscriptionPermissionService, never()).findHighestPermissionLevel(accountId, subscriptionId);
    }

    @Test
    void test_reviewer() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertTrue(authorizer.test(authenticationContext, message));
        verify(subscriptionPermissionService).findHighestPermissionLevel(accountId, subscriptionId);
    }

    @Test
    void test_contributor() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        assertTrue(authorizer.test(authenticationContext, message));
        verify(subscriptionPermissionService).findHighestPermissionLevel(accountId, subscriptionId);
    }

    @Test
    void test_owner() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        assertTrue(authorizer.test(authenticationContext, message));
        verify(subscriptionPermissionService).findHighestPermissionLevel(accountId, subscriptionId);
    }

    @Test
    void test_permissionNotFound() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, message));
        verify(subscriptionPermissionService).findHighestPermissionLevel(accountId, subscriptionId);
    }


}
