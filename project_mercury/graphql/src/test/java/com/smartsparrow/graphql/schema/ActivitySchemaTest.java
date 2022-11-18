package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.CoursewareDataStubs.buildLearnerActivity;
import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.graphql.type.EnrollmentLearnerActivity;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.pubsub.subscriptions.learner.StudentWalkablePrefetchProducer;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ActivitySchemaTest {

    @InjectMocks
    private ActivitySchema activitySchema;
    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private DeploymentService deploymentService;
    @Mock
    private LearnerCoursewareService learnerCoursewareService;
    @Mock
    private AllowCohortInstructor allowCohortInstructor;
    @Mock
    private AllowEnrolledStudent allowEnrolledStudent;
    @Mock
    private StudentScoreService studentScoreService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private ManualGradeService manualGradeService;
    @Mock
    private AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    @Mock
    private ActivityService activityService;
    @Mock
    private StudentWalkablePrefetchProducer studentWalkablePrefetchProducer;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;


    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final LearnerActivity learnerActivity = buildLearnerActivity(activityId, deploymentId, UUID.randomUUID());
    private DeployedActivity deployment;
    private static final UUID cohortIdArgument = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<CoursewareElement> ancestry = Lists.newArrayList(
                new CoursewareElement().setElementId(activityId).setElementType(CoursewareElementType.ACTIVITY),
                new CoursewareElement().setElementId(UUIDs.random()).setElementType(CoursewareElementType.PATHWAY),
                new CoursewareElement().setElementId(UUIDs.random()).setElementType(CoursewareElementType.ACTIVITY)
        );

        deployment = new DeployedActivity()
                .setId(deploymentId)
                .setActivityId(activityId)
                .setCohortId(cohortId);

        mockAuthenticationContextProvider(authenticationContextProvider, accountId);

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);
        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);

        when(learnerCoursewareService.getAncestry(deploymentId, activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(ancestry));

        when(studentWalkablePrefetchProducer.buildStudentWalkablePrefetchConsumable(eq(accountId), any(LearnerActivity.class)))
                .thenReturn(studentWalkablePrefetchProducer);

        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContextProvider.get())).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void getLearnerActivity() {
        when(learnerActivityService.findActivity(activityId, deploymentId)).thenReturn(Mono.just(new LearnerActivity()));

        LearnerActivity result = activitySchema.getLearnerActivity(resolutionEnvironment, deployment, null).join();

        assertNotNull(result);
    }

    @Test
    void getLearnerActivity_notFound() {
        when(learnerActivityService.findActivity(activityId, deploymentId)).thenReturn(Mono.empty());

        LearnerActivity result = activitySchema.getLearnerActivity(resolutionEnvironment, deployment, null).join();

        assertNull(result);
    }

    @Test
    void getActivity_ActivityId() {
        when(learnerActivityService.findActivity(activityId, deploymentId)).thenReturn(Mono.just(new LearnerActivity()));

        LearnerActivity result = activitySchema.getLearnerActivity(resolutionEnvironment, deployment, activityId).join();
        assertNotNull(result);
    }

    @Test
    void getAncestry_deploymentNotFound() {
        TestPublisher<DeployedActivity> deployedActivityTestPublisher = TestPublisher.create();
        deployedActivityTestPublisher.error(new DeploymentNotFoundException(null, deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(deployedActivityTestPublisher.mono());
        PermissionFault e = assertThrows(PermissionFault.class,
                () -> activitySchema.getAncestry(resolutionEnvironment, learnerActivity, null));

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAncestry_notAuthorized() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> activitySchema.getAncestry(resolutionEnvironment, learnerActivity, null));

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAncestry_authorized() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortId)).thenReturn(true);

        List<CoursewareElement> result = activitySchema.getAncestry(resolutionEnvironment, learnerActivity, null).join();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(el -> el.getElementId().equals(learnerActivity.getId())));
    }

    @Test
    void getAncestry_authorized_nullAncestry() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortId)).thenReturn(true);

        when(learnerCoursewareService.getAncestry(deploymentId, activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.empty());

        activitySchema.getAncestry(resolutionEnvironment, learnerActivity, null)
                .handle((coursewareElements, throwable) -> {
                    assertEquals(0, coursewareElements.size());
                    return coursewareElements;
                }).join();
    }

    @Test
    void getAncestry_notAuthorized_withCohortIdArgument() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortIdArgument)).thenReturn(false);
        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortIdArgument)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> activitySchema.getAncestry(resolutionEnvironment, learnerActivity, cohortIdArgument));

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAncestry_instructorAuthorized_withCohortIdArgument() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortIdArgument)).thenReturn(true);

        List<CoursewareElement> result =
                activitySchema.getAncestry(resolutionEnvironment, learnerActivity, cohortIdArgument).join();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(el -> el.getElementId().equals(learnerActivity.getId())));
    }
    @Test
    void getAncestry_studentAuthorized_withCohortIdArgument() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortIdArgument)).thenReturn(true);

        List<CoursewareElement> result =
                activitySchema.getAncestry(resolutionEnvironment, learnerActivity, cohortIdArgument).join();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(el -> el.getElementId().equals(learnerActivity.getId())));
    }

    @Test
    void getLearnerActivityByDeployment_unauthorized() {
        CohortEnrollment enrollment = new CohortEnrollment()
                .setCohortId(cohortId);

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> activitySchema.getLearnerActivityByDeployment(resolutionEnvironment,enrollment, deploymentId, activityId).join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getLearnerActivityByDeployment_authorized() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);

        CohortEnrollment enrollment = new CohortEnrollment()
                .setCohortId(cohortId);

        when(learnerActivityService.findActivity(activityId, deploymentId)).thenReturn(Mono.just(new LearnerActivity()));

        EnrollmentLearnerActivity result = activitySchema.getLearnerActivityByDeployment(resolutionEnvironment,enrollment, deploymentId, activityId).join();

        assertNotNull(result);
        assertEquals(enrollment, result.getEnrollment());
        assertNotNull(result.getLearnerActivity());
    }

    @Test
    void getScore_unauthorized() {
        EnrollmentLearnerActivity enrollmentLearnerActivity = new EnrollmentLearnerActivity()
                .setEnrollment(new CohortEnrollment().setCohortId(cohortId));

        assertThrows(PermissionFault.class, () -> activitySchema.getScore(resolutionEnvironment,enrollmentLearnerActivity).join());
    }

    @Test
    void getScore() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);

        EnrollmentLearnerActivity enrollmentLearnerActivity = new EnrollmentLearnerActivity()
                .setLearnerActivity(learnerActivity)
                .setEnrollment(new CohortEnrollment()
                        .setCohortId(cohortId)
                        .setAccountId(accountId));

        when(studentScoreService.computeScore(deploymentId, accountId, activityId, null))
                .thenReturn(Mono.just(new Score()
                        .setValue(10d)
                        .setReason(ScoreReason.SCORED)));

        Score score = activitySchema.getScore(resolutionEnvironment,enrollmentLearnerActivity).join();

        assertNotNull(score);
        assertEquals(Double.valueOf(10d), score.getValue());
        assertEquals(ScoreReason.SCORED, score.getReason());
    }

    @Test
    void getManualGradeReports_byActivity_notAllowed() {
        EnrollmentLearnerActivity enrollmentLearnerActivity = new EnrollmentLearnerActivity()
                .setEnrollment(new CohortEnrollment()
                        .setCohortId(cohortId));

        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);

        PermissionFault f = assertThrows(PermissionFault.class, () -> activitySchema.getManualGradeReports(resolutionEnvironment,enrollmentLearnerActivity).join());

        assertEquals("Unauthorized", f.getMessage());
    }

    @Test
    void getManualGradeReports_byActivity() {
        EnrollmentLearnerActivity enrollmentLearnerActivity = new EnrollmentLearnerActivity()
                .setEnrollment(new CohortEnrollment()
                        .setCohortId(cohortId)
                        .setAccountId(accountId))
                .setLearnerActivity(new LearnerActivity()
                        .setDeploymentId(deploymentId)
                        .setId(activityId)
                        .setChangeId(changeId));
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);
        when(manualGradeService.findLatestAttemptManualGradeReport(deploymentId, changeId, activityId, accountId))
                .thenReturn(Flux.empty());

        List<StudentManualGradeReport> reports = activitySchema.getManualGradeReports(resolutionEnvironment,enrollmentLearnerActivity).join();

        assertNotNull(reports);

        verify(manualGradeService).findLatestAttemptManualGradeReport(deploymentId, changeId, activityId, accountId);
    }

    @Test
    void getActivity_noPermission() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace()
                .setId(workspaceId);

        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(),workspaceId)).thenReturn(false);

        PermissionFault f = assertThrows(PermissionFault.class, () -> activitySchema.getActivity(resolutionEnvironment,workspace, UUID.randomUUID()).join());

        assertEquals("Higher permission level required", f.getMessage());
    }

    @Test
    void getActivity_notFound() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace()
                .setId(workspaceId);

        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(),workspaceId)).thenReturn(true);

        TestPublisher<Activity> publisher = TestPublisher.create();

        publisher.error(new ActivityNotFoundException(activityId));

        when(activityService.findById(activityId)).thenReturn(publisher.mono());

        activitySchema.getActivity(resolutionEnvironment,workspace, activityId)
                        .handle((activity, throwable) -> {
                            assertEquals(NotFoundFault.class, throwable.getClass());
                            return activity;
                        }).join();
    }

    @Test
    void getActivity() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace()
                .setId(workspaceId);

        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(true);

        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));

        Activity found = activitySchema.getActivity(resolutionEnvironment,workspace, activityId).join();

        assertNotNull(found);
    }

}
