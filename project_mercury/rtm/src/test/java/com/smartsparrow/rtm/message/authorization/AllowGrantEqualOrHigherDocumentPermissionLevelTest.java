package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.competency.GrantDocumentPermissionMessage;

import reactor.core.publisher.Mono;

public class AllowGrantEqualOrHigherDocumentPermissionLevelTest {

    @InjectMocks
    AllowGrantEqualOrHigherDocumentPermissionLevel authorizer;

    AuthenticationContext authenticationContext;

    @Mock
    DocumentPermissionService documentPermissionService;

    @Mock
    GrantDocumentPermissionMessage message;

    UUID documentId = UUID.randomUUID();
    private static final UUID requestingAccountId = UUID.randomUUID();
    private static final UUID accountIdOne = UUID.randomUUID();
    private static final UUID accountIdTwo = UUID.randomUUID();
    private static final UUID teamIdOne = UUID.randomUUID();
    private static final UUID teamIdTwo = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Account requestingAccount = mock(Account.class);
        authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getAccount()).thenReturn(requestingAccount);
        when(requestingAccount.getId()).thenReturn(requestingAccountId);
        message = mock(GrantDocumentPermissionMessage.class);
        when(message.getDocumentId()).thenReturn(documentId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        when(message.getAccountIds()).thenReturn(null);
        when(message.getTeamIds()).thenReturn(null);
    }

    @Test
    void test_requestingPermissionNotFound() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId)).thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_accountIdsPermissionNotFound() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(documentPermissionService.fetchAccountPermission(accountIdOne, documentId)).thenReturn(Mono.empty());
        when(documentPermissionService.fetchAccountPermission(accountIdTwo, documentId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_invalidAccountPermissionFound() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(documentPermissionService.fetchAccountPermission(accountIdOne, documentId)).thenReturn(Mono.empty());
        when(documentPermissionService.fetchAccountPermission(accountIdTwo, documentId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_samePermission() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(documentPermissionService.fetchAccountPermission(accountIdOne, documentId)).thenReturn(Mono.empty());
        when(documentPermissionService.fetchAccountPermission(accountIdTwo, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_validAccountPermissionFound() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(documentPermissionService.fetchAccountPermission(accountIdOne, documentId)).thenReturn(Mono.empty());
        when(documentPermissionService.fetchAccountPermission(accountIdTwo, documentId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @SuppressWarnings("Duplicates")
    void test_teamIdsPermissionNotFound() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(documentPermissionService.fetchTeamPermission(teamIdOne, documentId)).thenReturn(Mono.empty());
        when(documentPermissionService.fetchTeamPermission(teamIdTwo, documentId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_invalidTeamPermissionFound() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(documentPermissionService.fetchTeamPermission(teamIdOne, documentId)).thenReturn(Mono.empty());
        when(documentPermissionService.fetchTeamPermission(teamIdTwo, documentId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_validTeamPermissionFound() {
        when(documentPermissionService.findHighestPermissionLevel(requestingAccountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(documentPermissionService.fetchTeamPermission(teamIdOne, documentId)).thenReturn(Mono.empty());
        when(documentPermissionService.fetchTeamPermission(teamIdTwo, documentId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

}
