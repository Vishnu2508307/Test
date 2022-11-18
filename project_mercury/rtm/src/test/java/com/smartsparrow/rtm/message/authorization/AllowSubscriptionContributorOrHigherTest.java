package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionMessage;

import reactor.core.publisher.Mono;

class AllowSubscriptionContributorOrHigherTest {

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private AllowSubscriptionContributorOrHigher allowSubscriptionContributorOrHigher;
    private AuthenticationContext authenticationContext;
    private Account targetAccount;
    private UUID targetSubscription = UUID.randomUUID();
    private UUID accountId = UUID.randomUUID();
    private UUID targetAccountId = UUID.randomUUID();
    private SubscriptionMessage accountMessageNoAccountId;
    private SubscriptionMessage accountMessageNoSubscriptionId;
    private SubscriptionMessage invalidAccountMessage;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        allowSubscriptionContributorOrHigher = new AllowSubscriptionContributorOrHigher(subscriptionPermissionService);

        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        targetAccount = mock(Account.class);

        accountMessageNoAccountId = mock(SubscriptionMessage.class);
        accountMessageNoSubscriptionId = mock(SubscriptionMessage.class);
        invalidAccountMessage = mock(SubscriptionMessage.class);

        when(invalidAccountMessage.getSubscriptionId()).thenReturn(null);

        when(accountMessageNoAccountId.getSubscriptionId()).thenReturn(targetSubscription);

        when(accountMessageNoSubscriptionId.getSubscriptionId()).thenReturn(null);

        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        when(targetAccount.getId()).thenReturn(targetAccountId);
        when(targetAccount.getSubscriptionId()).thenReturn(targetSubscription);
    }

    @Test
    void test_noAccountFromContext() {
        when(authenticationContext.getAccount()).thenReturn(null);
        assertFalse(allowSubscriptionContributorOrHigher.test(authenticationContext, accountMessageNoAccountId));
        verify(subscriptionPermissionService, never()).findHighestPermissionLevel(accountId, targetSubscription);
    }

    @Test
    void test_noSubscription_unauthorized() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, targetSubscription))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertFalse(allowSubscriptionContributorOrHigher.test(authenticationContext, accountMessageNoSubscriptionId));
        verify(subscriptionPermissionService, never()).findHighestPermissionLevel(accountId, targetSubscription);
    }

    @Test
    void test_noSubscription_targetAccountNotFound() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, targetSubscription))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(allowSubscriptionContributorOrHigher.test(authenticationContext, accountMessageNoSubscriptionId));
        verify(subscriptionPermissionService, never()).findHighestPermissionLevel(accountId, targetSubscription);
    }

    @Test
    void test_noSubscriptionNoAccount() {
        assertFalse(allowSubscriptionContributorOrHigher.test(authenticationContext, invalidAccountMessage));
        verify(subscriptionPermissionService, never()).findHighestPermissionLevel(accountId, targetSubscription);
    }

    @Test
    void test_noAccount_unauthorized() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, targetSubscription))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertFalse(allowSubscriptionContributorOrHigher.test(authenticationContext, accountMessageNoAccountId));
        verify(subscriptionPermissionService, atLeastOnce()).findHighestPermissionLevel(accountId, targetSubscription);

    }

    @Test
    void test_noAccount_authorized() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, targetSubscription))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        assertTrue(allowSubscriptionContributorOrHigher.test(authenticationContext, accountMessageNoAccountId));
        verify(subscriptionPermissionService, atLeastOnce()).findHighestPermissionLevel(accountId, targetSubscription);
    }
}
