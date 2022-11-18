package com.smartsparrow.rtm.message.authorization;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.message.recv.learner.annotation.CreatorMessage;

import reactor.core.publisher.Mono;

class AllowListDeploymentAnnotationAuthorizerTest {

    @InjectMocks
    private AllowListDeploymentAnnotationAuthorizer authorizer;

    @Mock
    private DeploymentService deploymentService;
    @Mock
    private CohortPermissionService cohortPermissionService;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();

    @Mock
    private DeployedActivity deployment;
    @Mock
    private CreatorMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getCreatorAccountId()).thenReturn(accountId);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(deployment.getCohortId()).thenReturn(cohortId);
    }

    @Test
    void test_success_forCreatorAccount() {
        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_success_forCohortInstructor() {
        when(message.getCreatorAccountId()).thenReturn(null);
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));
        when(authenticationContext.getAccount().getRoles()).thenReturn(new HashSet<>(Arrays.asList(AccountRole.DEVELOPER)));

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_null() {
        when(message.getCreatorAccountId()).thenReturn(null);
        when(message.getDeploymentId()).thenReturn(null);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }
}

