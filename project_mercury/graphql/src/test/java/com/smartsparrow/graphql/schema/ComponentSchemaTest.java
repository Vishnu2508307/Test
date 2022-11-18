package com.smartsparrow.graphql.schema;

import static com.smartsparrow.learner.service.ConfigurationFieldDataStub.buildField;
import static com.smartsparrow.learner.service.DeploymentDataStub.buildDeployment;
import static com.smartsparrow.learner.service.ManualGradingConfigurationDataStub.buildManualGradingConfiguration;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.LearnerManualGradingConfiguration;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortContributorOrHigher;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.type.mutation.LearnerManualGradingReportInput;
import com.smartsparrow.graphql.type.mutation.ManualGradeArg;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.StudentManualGrade;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerComponentService;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.learner.service.StudentScoreService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ComponentSchemaTest {

    @InjectMocks
    private ComponentSchema componentSchema;

    @Mock
    private LearnerComponentService learnerComponentService;
    @Mock
    private LearnerService learnerService;

    @Mock
    private AllowCohortInstructor allowCohortInstructor;

    @Mock
    private ManualGradeService manualGradeService;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private AllowCohortContributorOrHigher allowCohortContributorOrHigher;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private StudentScoreService studentScoreService;

    @Mock
    private Account account;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    private final Deployment deployment = buildDeployment();

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID walkableId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);

        resolutionEnvironment = new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(this.authenticationContextProvider.get())).build(),
                                                          null,
                                                          null,
                                                          null,
                                                          null);

    }

    @Test
    void getComponents_activity() {
        LearnerActivity activity = new LearnerActivity().setDeploymentId(deploymentId).setId(walkableId);
        when(learnerComponentService.findComponents(walkableId, CoursewareElementType.ACTIVITY, deploymentId))
                .thenReturn(Flux.just(new LearnerComponent(), new LearnerComponent()));

        List<LearnerComponent> result = componentSchema.getComponents(activity).join();

        assertNotNull(result);
    }

    @Test
    void getComponents_interactive() {
        LearnerInteractive interactive = new LearnerInteractive().setDeploymentId(deploymentId).setId(walkableId);
        when(learnerComponentService.findComponents(walkableId, CoursewareElementType.INTERACTIVE, deploymentId))
                .thenReturn(Flux.just(new LearnerComponent(), new LearnerComponent()));

        List<LearnerComponent> result = componentSchema.getComponents(interactive).join();

        assertNotNull(result);
    }

    @Test
    void getManualGradingConfigurations_notAnInstructor() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(), deployment.getCohortId())).thenReturn(false);

        PermissionFault pf = assertThrows(PermissionFault.class, () -> componentSchema
                .getManualGradingConfigurations(resolutionEnvironment,deployment)
                .join());

        assertNotNull(pf);
        assertEquals("Unauthorized", pf.getMessage());
    }

    @Test
    void getManualGradingConfigurations() {
        LearnerManualGradingConfiguration one = new LearnerManualGradingConfiguration()
                .setChangeId(deployment.getChangeId());

        LearnerManualGradingConfiguration two = new LearnerManualGradingConfiguration()
                .setChangeId(UUID.randomUUID());

        when(allowCohortInstructor.test(authenticationContextProvider.get(),deployment.getCohortId())).thenReturn(true);

        when(manualGradeService.findManualGradingConfigurations(deployment.getId())).thenReturn(Flux.just(one, two));

        List<LearnerManualGradingConfiguration> found = componentSchema
                .getManualGradingConfigurations(resolutionEnvironment,deployment)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getComponentFields() {
        LearnerManualGradingConfiguration manualGradingconfiguration = buildManualGradingConfiguration(deployment.getId(), deployment.getChangeId());

        when(learnerService.fetchFields(deployment.getId(), deployment.getChangeId(),
                manualGradingconfiguration.getComponentId(), Lists.newArrayList("title")))
                .thenReturn(Flux.just(
                        buildField("title", "hey!")
                ));

        List<ConfigurationField> found = componentSchema
                .getComponentFields(manualGradingconfiguration, Lists.newArrayList("title"))
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());
        assertEquals("title", found.get(0).getFieldName());
        assertEquals("hey!", found.get(0).getFieldValue());

        verify(learnerService).fetchFields(eq(deployment.getId()), eq(deployment.getChangeId()),
                eq(manualGradingconfiguration.getComponentId()), any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getParentWalkableFields() {
        LearnerManualGradingConfiguration manualGradingconfiguration = buildManualGradingConfiguration(deployment.getId(), deployment.getChangeId());

        when(learnerService.fetchFields(deployment.getId(), deployment.getChangeId(),
                manualGradingconfiguration.getParentId(), Lists.newArrayList("title")))
                .thenReturn(Flux.just(
                        buildField("title", "parent hey!")
                ));

        List<ConfigurationField> found = componentSchema
                .getParentWalkableFields(manualGradingconfiguration, Lists.newArrayList("title"))
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());
        assertEquals("title", found.get(0).getFieldName());
        assertEquals("parent hey!", found.get(0).getFieldValue());

        verify(learnerService).fetchFields(eq(deployment.getId()), eq(deployment.getChangeId()),
                eq(manualGradingconfiguration.getParentId()), any(List.class));
    }

    @Test
    void getLatestAttemptStudentManualGradeReport() {
        CohortEnrollment cohortEnrollment = new CohortEnrollment()
                .setAccountId(UUID.randomUUID());

        LearnerManualGradingReportInput input = mock(LearnerManualGradingReportInput.class);
        when(input.getDeploymentId()).thenReturn(deploymentId);
        when(input.getComponentId()).thenReturn(componentId);
        when(input.getParentId()).thenReturn(UUID.randomUUID());
        when(input.getParentType()).thenReturn(CoursewareElementType.INTERACTIVE);

        when(manualGradeService.findLatestAttemptManualGradeReport(any(UUID.class), any(UUID.class), any(UUID.class),
                any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Mono.just(new StudentManualGradeReport()));

        componentSchema.getLatestAttemptStudentManualGradeReport(cohortEnrollment, input).join();

        verify(manualGradeService).findLatestAttemptManualGradeReport(cohortEnrollment.getAccountId(), input.getDeploymentId(),
                input.getComponentId(), input.getParentId(), input.getParentType());
    }

    @Test
    void createStudentManualGrade_nullOperator() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> componentSchema
                .createStudentManualGrade(resolutionEnvironment,deploymentId, componentId, new ManualGradeArg()).join());

        assertNotNull(f);
        assertEquals("operator is required", f.getMessage());
    }

    @Test
    void createStudentManualGrade_nullStudentId() {
        ManualGradeArg manualGradeArg = mock(ManualGradeArg.class);
        when(manualGradeArg.getOperator()).thenReturn(MutationOperator.SET);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> componentSchema
                .createStudentManualGrade(resolutionEnvironment,deploymentId, componentId, manualGradeArg).join());

        assertNotNull(f);
        assertEquals("studentId is required", f.getMessage());
    }

    @Test
    void createStudentManualGrade_nullScore() {
        ManualGradeArg manualGradeArg = mock(ManualGradeArg.class);
        when(manualGradeArg.getOperator()).thenReturn(MutationOperator.SET);
        when(manualGradeArg.getStudentId()).thenReturn(UUID.randomUUID());
        when(manualGradeArg.getScore()).thenReturn(null);


        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> componentSchema
                .createStudentManualGrade(resolutionEnvironment,deploymentId, componentId, manualGradeArg).join());

        assertNotNull(f);
        assertEquals("score is required", f.getMessage());
    }

    @Test
    void createStudentManualGrade_nullAttemptId() {
        ManualGradeArg manualGradeArg = mock(ManualGradeArg.class);
        when(manualGradeArg.getOperator()).thenReturn(MutationOperator.SET);
        when(manualGradeArg.getStudentId()).thenReturn(UUID.randomUUID());
        when(manualGradeArg.getScore()).thenReturn(5.5d);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> componentSchema
                .createStudentManualGrade(resolutionEnvironment,deploymentId, componentId, manualGradeArg).join());

        assertNotNull(f);
        assertEquals("attemptId is required", f.getMessage());
    }

    @Test
    void createStudentManualGrade_deploymentNotFound() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();

        ManualGradeArg manualGradeArg = mock(ManualGradeArg.class);
        when(manualGradeArg.getOperator()).thenReturn(MutationOperator.SET);
        when(manualGradeArg.getStudentId()).thenReturn(studentId);
        when(manualGradeArg.getScore()).thenReturn(5.5d);
        when(manualGradeArg.getAttemptId()).thenReturn(attemptId);

        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new DeploymentNotFoundException(null, deploymentId));

        when(deploymentService.findDeployment(deploymentId)).thenReturn(publisher.mono());

        componentSchema
                .createStudentManualGrade(resolutionEnvironment,deploymentId, componentId, manualGradeArg)
                .handle((studentManualGrade, throwable) -> {
                   assertEquals(NotFoundFault.class, throwable.getClass());
                    assertEquals("deployment not found", throwable.getMessage());
                    return studentManualGrade;
                })
                .join();
    }

    @Test
    void createStudentManualGrade_invalidPermission() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();

        ManualGradeArg manualGradeArg = mock(ManualGradeArg.class);
        when(manualGradeArg.getOperator()).thenReturn(MutationOperator.SET);
        when(manualGradeArg.getStudentId()).thenReturn(studentId);
        when(manualGradeArg.getScore()).thenReturn(5.5d);
        when(manualGradeArg.getAttemptId()).thenReturn(attemptId);

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity()
                .setCohortId(deployment.getCohortId())
                .setId(deployment.getId())));

        when(allowCohortContributorOrHigher.test(authenticationContextProvider.get(),deployment.getCohortId())).thenReturn(false);

        componentSchema
                .createStudentManualGrade(resolutionEnvironment,deploymentId, componentId, manualGradeArg)
                .handle((studentManualGrade, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    assertEquals("Unauthorized", throwable.getMessage());
                    return studentManualGrade;
                })
                .join();
    }

    @Test
    void createStudentManualGrade() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        ManualGradeArg manualGradeArg = mock(ManualGradeArg.class);
        when(manualGradeArg.getOperator()).thenReturn(MutationOperator.SET);
        when(manualGradeArg.getStudentId()).thenReturn(studentId);
        when(manualGradeArg.getScore()).thenReturn(5.5d);
        when(manualGradeArg.getAttemptId()).thenReturn(attemptId);

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity()
                .setCohortId(deployment.getCohortId())
                .setId(deployment.getId())));

        when(allowCohortContributorOrHigher.test(authenticationContextProvider.get(),deployment.getCohortId())).thenReturn(true);
        when(account.getId()).thenReturn(instructorId);

        when(studentScoreService.createStudentManualGrade(deploymentId, componentId, studentId, attemptId, 5.5d, MutationOperator.SET, instructorId))
                .thenReturn(Mono.just(new StudentManualGrade()));

        StudentManualGrade created = componentSchema
                .createStudentManualGrade(resolutionEnvironment,deploymentId, componentId, manualGradeArg)
                .join();

        assertNotNull(created);
    }
}
