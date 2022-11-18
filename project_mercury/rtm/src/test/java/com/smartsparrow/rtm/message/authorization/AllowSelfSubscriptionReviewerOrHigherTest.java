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
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.ReceivedMessage;

import reactor.core.publisher.Mono;

class AllowSelfSubscriptionReviewerOrHigherTest {

    @InjectMocks
    private AllowSelfSubscriptionReviewerOrHigher allowSelfSubscriptionReviewerOrHigher;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private AuthenticationContext authenticationContext;
    private ReceivedMessage receivedMessage;
    private UUID accountId = UUID.randomUUID();
    private UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        receivedMessage = mock(ReceivedMessage.class);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(account.getSubscriptionId()).thenReturn(subscriptionId);
    }

    @Test
    void test_ownerPermission() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(allowSelfSubscriptionReviewerOrHigher.test(authenticationContext, receivedMessage));
    }

    @Test
    void test_contributorPermission() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(allowSelfSubscriptionReviewerOrHigher.test(authenticationContext, receivedMessage));
    }

    @Test
    void test_reviewerPermission() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(allowSelfSubscriptionReviewerOrHigher.test(authenticationContext, receivedMessage));
    }

    @Test
    void test_permissionNotFound() {
        when(subscriptionPermissionService.findHighestPermissionLevel(accountId, subscriptionId))
                .thenReturn(Mono.empty());

        assertFalse(allowSelfSubscriptionReviewerOrHigher.test(authenticationContext, receivedMessage));
    }
}