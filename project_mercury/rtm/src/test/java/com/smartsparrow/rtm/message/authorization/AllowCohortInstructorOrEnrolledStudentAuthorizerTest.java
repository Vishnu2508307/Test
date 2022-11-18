package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.message.recv.learner.annotation.CreateLearnerAnnotationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class AllowCohortInstructorOrEnrolledStudentAuthorizerTest {

    @InjectMocks
    private AllowCohortInstructorOrEnrolledStudentAuthorizer authorizer;
    @Mock
    private DeploymentService deploymentService;
    @Mock
    private CohortEnrollmentService cohortEnrollmentService;
    @Mock
    private CohortPermissionService cohortPermissionService;
    private AuthenticationContext authenticationContext;


    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();

    @Mock
    private CreateLearnerAnnotationMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(deploymentService.findDeployment(message.getDeploymentId())).thenReturn(Mono.just(new DeployedActivity()
                .setCohortId(cohortId)));
    }

    @Test
    void test_success_ForInstructor(){
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId))
                .thenReturn(Mono.just(new CohortEnrollment()));
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));

        boolean result = authorizer.test(authenticationContext, message);
        assertTrue(result);
    }

    @Test
    void test_success_forEnrolledStudent() {
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId))
                .thenReturn(Mono.just(new CohortEnrollment()
        .setAccountId(accountId)));
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId)).thenReturn(Mono.just(PermissionLevel.OWNER));
        boolean result = authorizer.test(authenticationContext, message);
        assertTrue(result);
    }

    @Test
    void test_failure_NodeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);
        boolean result = authorizer.test(authenticationContext, message);
        assertFalse(result);
    }

    @Test
    void test_failure_CohortIdNull() {
        when(deploymentService.findDeployment(message.getDeploymentId())).
                thenReturn(Mono.just(new DeployedActivity()));
        boolean result = authorizer.test(authenticationContext, message);
        assertFalse(result);
    }
}
