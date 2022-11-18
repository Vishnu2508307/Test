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
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;

class AllowWorkspaceOwnerOrHigherTest {

    @InjectMocks
    private AllowWorkspaceOwnerOrHigher authorizer;

    @Mock
    private WorkspaceService workspaceService;

    private WorkspaceMessage message;
    private AuthenticationContext authenticationContext;
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(WorkspaceMessage.class);
        authenticationContext = mock(AuthenticationContext.class);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(authenticationContext.getAccount()).thenReturn(account);

    }

    @Test
    void test_workspaceIdNotSupplied() {
        when(message.getWorkspaceId()).thenReturn(null);
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_permissionNotFound() {
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_reviewerPermissionLevel() {
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_contributorPermissionLevel() {
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_ownerPermissionLevel() {
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.OWNER));
        assertTrue(authorizer.test(authenticationContext, message));
    }

}
