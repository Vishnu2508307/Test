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
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.recv.iam.AccountMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AllowAccountSubscriptionContributorOrHigherTest {

    @InjectMocks
    private AllowAccountSubscriptionContributorOrHigher authorizer;

    @Mock
    private AccountService accountService;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private AccountMessage message;
    private AuthenticationContext authenticationContext;
    private static final UUID requesterAccountId = UUID.randomUUID();
    private static final UUID targetAccountId = UUID.randomUUID();
    private static final UUID targetSubscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(AccountMessage.class);
        authenticationContext = mock(AuthenticationContext.class);
        Account requester = mock(Account.class);
        Account target = mock(Account.class);

        when(target.getSubscriptionId()).thenReturn(targetSubscriptionId);
        when(requester.getId()).thenReturn(requesterAccountId);

        when(message.getAccountId()).thenReturn(targetAccountId);
        when(authenticationContext.getAccount()).thenReturn(requester);
        when(accountService.findById(targetAccountId)).thenReturn(Flux.just(target));

    }

    @Test
    void test_targetAccountNotFound() {
        when(subscriptionPermissionService.findHighestPermissionLevel(requesterAccountId, targetSubscriptionId))
                .thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_permissionContributor() {
        when(subscriptionPermissionService.findHighestPermissionLevel(requesterAccountId, targetSubscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_permissionReviewer() {
        when(subscriptionPermissionService.findHighestPermissionLevel(requesterAccountId, targetSubscriptionId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_permissionOwner() {
        when(subscriptionPermissionService.findHighestPermissionLevel(requesterAccountId, targetSubscriptionId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        assertTrue(authorizer.test(authenticationContext, message));
    }

}
