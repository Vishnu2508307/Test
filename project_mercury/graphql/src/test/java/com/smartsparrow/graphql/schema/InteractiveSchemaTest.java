package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.LearnerInteractiveService;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.learner.service.StudentScoreService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class InteractiveSchemaTest {

    @InjectMocks
    private InteractiveSchema interactiveSchema;
    @Mock
    private LearnerInteractiveService learnerInteractiveService;
    @Mock
    private DeploymentService deploymentService;
    @Mock
    private LearnerCoursewareService learnerCoursewareService;
    @Mock
    private AllowCohortInstructor allowCohortInstructor;
    @Mock
    private AllowEnrolledStudent allowEnrolledStudent;
    @Mock
    private ManualGradeService manualGradeService;
    @Mock
    private StudentScoreService studentScoreService;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private DeployedActivity deployment;
    private Account account;
    @Mock
    private LearnerInteractive learnerInteractive;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deployment = new DeployedActivity().setId(deploymentId).setCohortId(cohortId);
        account = new Account().setId(UUID.randomUUID());
        when(authenticationContext.getAccount()).thenReturn(account);
        List<CoursewareElement> ancestry = Lists.newArrayList(
                new CoursewareElement().setElementId(interactiveId).setElementType(CoursewareElementType.INTERACTIVE),
                new CoursewareElement().setElementId(UUID.randomUUID()).setElementType(CoursewareElementType.PATHWAY),
                new CoursewareElement().setElementId(UUID.randomUUID()).setElementType(CoursewareElementType.ACTIVITY)
        );

        when(learnerInteractive.getId()).thenReturn(interactiveId);
        when(learnerInteractive.getDeploymentId()).thenReturn(deploymentId);
        when(learnerInteractive.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(false);
        when(allowEnrolledStudent.test(authenticationContext, cohortId)).thenReturn(false);

        when(learnerCoursewareService.getAncestry(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE))
                .thenReturn(Mono.just(ancestry));
        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContext)).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void getInteractive_InteractiveIdNotSupplied() {
        when(learnerInteractiveService.findInteractive(interactiveId, deploymentId)).thenReturn(Mono.just(new LearnerInteractive()));

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> interactiveSchema
                .getInteractive(deployment, null).join());

        assertNotNull(e);
        assertEquals("interactiveId is required", e.getMessage());
    }


    @Test
    void getInteractive_DeploymentNotSupplied() {
        when(learnerInteractiveService.findInteractive(interactiveId, deploymentId)).thenReturn(Mono.just(new LearnerInteractive()));

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> interactiveSchema
                .getInteractive(null, null).join());

        assertNotNull(e);
        assertEquals("deployment context is required", e.getMessage());
    }

    @Test
    void getInteractive_valid() {
        LearnerInteractive learnerInteractive = new LearnerInteractive()
                .setId(interactiveId)
                .setDeploymentId(deploymentId);
        when(learnerInteractiveService.findInteractive(interactiveId, deploymentId)).thenReturn(Mono.just(learnerInteractive));

        LearnerInteractive result = interactiveSchema.getInteractive(deployment, interactiveId).join();

        assertNotNull(result);
        assertEquals(learnerInteractive, result);
    }

    @Test
    void getAncestry_deploymentNotFound() {
        TestPublisher<DeployedActivity> deployedActivityTestPublisher = TestPublisher.create();
        deployedActivityTestPublisher.error(new DeploymentNotFoundException(null, deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(deployedActivityTestPublisher.mono());

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> interactiveSchema.getAncestry(resolutionEnvironment, learnerInteractive, null));

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAncestry_notAuthorized() {
        when(allowEnrolledStudent.test(authenticationContext, cohortId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> interactiveSchema.getAncestry(resolutionEnvironment, learnerInteractive, null));

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAncestry_authorized() {
        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(true);

        List<CoursewareElement> result = interactiveSchema.getAncestry(resolutionEnvironment, learnerInteractive, null).join();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(el -> el.getElementId().equals(learnerInteractive.getId())));
    }

    @Test
    void getAncestry_authorized_nullAncestry() {
        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(true);

        when(learnerCoursewareService.getAncestry(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE))
                .thenReturn(Mono.empty());

        assertEquals(0, interactiveSchema.getAncestry(resolutionEnvironment, learnerInteractive, null).join().size());
    }

    @Test
    void getManualGradeReports() {
        UUID studentId = UUID.randomUUID();

        CohortEnrollment cohortEnrollment = new CohortEnrollment()
                .setCohortId(cohortId)
                .setAccountId(studentId);

        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(true);
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity()
                .setChangeId(changeId)));
        when(manualGradeService.findLatestAttemptManualGradeReport(deploymentId, changeId, interactiveId, studentId))
                .thenReturn(Flux.empty());

        List<StudentManualGradeReport> reports = interactiveSchema
                .getManualGradeReports(resolutionEnvironment, cohortEnrollment, deploymentId, interactiveId)
                .join();

        assertNotNull(reports);

        verify(manualGradeService).findLatestAttemptManualGradeReport(deploymentId, changeId, interactiveId, studentId);
    }

    @Test
    void score_notAllowed() {
        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(false);

        CohortEnrollment enrollment = new CohortEnrollment()
                .setCohortId(cohortId);

        PermissionFault f = assertThrows(PermissionFault.class, () -> interactiveSchema
                .getScore(resolutionEnvironment, enrollment, deploymentId, interactiveId).join());

        assertEquals("Unauthorized", f.getMessage());
    }

    @Test
    void score() {
        UUID studentId = UUID.randomUUID();
        CohortEnrollment enrollment = new CohortEnrollment()
                .setCohortId(cohortId)
                .setAccountId(studentId);

        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(true);
        when(studentScoreService.computeScore(deploymentId, studentId, interactiveId, null))
                .thenReturn(Mono.just(new Score()
                        .setReason(ScoreReason.INSTRUCTOR_SCORED)
                        .setValue(10d)));

        Score score = interactiveSchema.getScore(resolutionEnvironment, enrollment, deploymentId, interactiveId).join();

        assertNotNull(score);
        assertEquals(Double.valueOf(10), score.getValue());
        assertEquals(ScoreReason.INSTRUCTOR_SCORED, score.getReason());
    }
}
