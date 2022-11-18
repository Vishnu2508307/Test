package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;

public class AllowWorkspaceReviewerOrHigherTest {

    @InjectMocks
    private AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;
    @Mock
    private WorkspaceService workspaceService;

    private UUID workspaceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_WorkspaceIdNotProvided() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> allowWorkspaceReviewerOrHigher.test(authenticationContext,null));
        assertEquals("workspaceId is required", e.getMessage());
    }

    @Test
    void test_RoleNotAvailable() {
        when(authenticationContext.getAccount()).thenReturn(null);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> allowWorkspaceReviewerOrHigher.test(authenticationContext,workspaceId));
        assertEquals("account is required", e.getMessage());
    }


    @Test
    void test_instructor_noPermission() {
        when(workspaceService.findHighestPermissionLevel(account.getId(), workspaceId)).thenReturn(Mono.empty());

        assertFalse(allowWorkspaceReviewerOrHigher.test(authenticationContext,workspaceId));
    }

    @ParameterizedTest
    @EnumSource(PermissionLevel.class)
    void test_instructor_withPermission(PermissionLevel level) {

        when(workspaceService.findHighestPermissionLevel(account.getId(), workspaceId))
                .thenReturn(Mono.just(level));

        assertTrue(allowWorkspaceReviewerOrHigher.test(authenticationContext,workspaceId));
    }
}
