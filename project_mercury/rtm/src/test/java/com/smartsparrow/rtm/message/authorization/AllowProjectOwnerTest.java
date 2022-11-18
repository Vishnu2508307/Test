package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.IamTestUtils;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.workspace.ProjectMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;

import reactor.core.publisher.Mono;

class AllowProjectOwnerTest {

    @InjectMocks
    private AllowProjectOwner authorizer;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private ProjectMessage message;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    private AuthenticationContext authenticationContext = IamTestUtils.mockAuthenticationContext(accountId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getProjectId()).thenReturn(projectId);
    }

    @Test
    void test_reviewer() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_contributor() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_owner() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_noPermission() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }
}
