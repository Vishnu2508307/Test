package com.smartsparrow.rtm.message.authorization;

import com.google.common.collect.ImmutableSet;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.message.recv.learner.LearnerAnnotationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AllowDeleteLearnerAnnotationAuthorizerTest {

    @InjectMocks
    private AllowDeleteLearnerAnnotationAuthorizer authorizer;
    @Mock
    private AnnotationService annotationService;
    @Mock
    private DeploymentService deploymentService;
    @Mock
    private CohortPermissionService cohortPermissionService;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID creatorId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private Set<AccountRole> roles = ImmutableSet.of(AccountRole.ADMIN);
    private Set<AccountRole> studentRole = ImmutableSet.of(AccountRole.STUDENT);

    @Mock
    private LearnerAnnotationMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(new LearnerAnnotation()
                .setDeploymentId(deploymentId)
                .setCreatorAccountId(creatorId)));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity()
                .setCohortId(cohortId)));
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId)).thenReturn(Mono.just(PermissionLevel.OWNER));
    }

    @Test
    void test_success_forCreator() {
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(creatorId));
        boolean result = authorizer.test(authenticationContext, message);
        assertTrue(result);
    }

    @Test
    void test_success_forCohortInstructor() {
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId)
                .setRoles(roles));
        boolean result = authorizer.test(authenticationContext, message);
        assertTrue(result);
    }


    @Test
    void test_fail_forNotInstructorOrOwner() {
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId)
                .setRoles(studentRole));
        boolean result = authorizer.test(authenticationContext, message);
        assertFalse(result);
    }
}
