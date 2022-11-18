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

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.message.recv.AnnotationMessage;

import reactor.core.publisher.Mono;

class AllowUpdateDeploymentAnnotationAuthorizerTest {

    @InjectMocks
    private AllowUpdateDeploymentAnnotationAuthorizer authorizer;

    @Mock
    private DeploymentService deploymentService;
    @Mock
    private AnnotationService annotationService;
    @Mock
    private CohortPermissionService cohortPermissionService;


    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();

    @Mock
    private LearnerAnnotation annotation;
    @Mock
    private DeployedActivity deployment;
    @Mock
    private AnnotationMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(annotation.getCreatorAccountId()).thenReturn(accountId);
        when(annotation.getDeploymentId()).thenReturn(deploymentId);
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
        when(annotation.getCreatorAccountId()).thenReturn(null);
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId)).thenReturn(Mono.just(
                PermissionLevel.REVIEWER));
        when(authenticationContext.getAccount().getRoles()).thenReturn(new HashSet<>(Arrays.asList(AccountRole.DEVELOPER)));


        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_null() {
        when(annotation.getCreatorAccountId()).thenReturn(null);
        when(annotation.getDeploymentId()).thenReturn(null);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }
}
