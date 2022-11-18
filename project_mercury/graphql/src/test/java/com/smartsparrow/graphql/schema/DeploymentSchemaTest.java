package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.EvaluationResultService;
import com.smartsparrow.learner.service.LearnerCoursewareService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class DeploymentSchemaTest {

    @InjectMocks
    private DeploymentSchema deploymentSchema;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private EvaluationResultService evaluationResultService;

    @Mock
    private LearnerCoursewareService learnerCoursewareService;

    @Mock
    private AllowCohortInstructor allowCohortInstructor;

    @Mock
    private AllowEnrolledStudent allowEnrolledStudent;

    @Mock
    private ResolutionEnvironment resolutionEnvironment;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID evaluationId = UUID.randomUUID();
    private CohortSummary cohort;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cohort = new CohortSummary().setId(cohortId);
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
    void getCohortDeployment_noDeploymentId() {
        when(deploymentService.findDeployments(cohortId)).thenReturn(Flux.just(new DeployedActivity(), new DeployedActivity()));

        List<DeployedActivity> result = deploymentSchema.getCohortDeployment(cohort, null).join();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getCohortDeployment_deploymentIdNotFound() {
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.empty());

        deploymentSchema.getCohortDeployment(cohort, deploymentId)
                .handle((deployedActivities, throwable) -> {
                    assertEquals(IllegalArgumentFault.class, throwable.getClass());
                    return deployedActivities;
                })
                .join();
    }

    @Test
    void getCohortDeployment_wrongCohortId() {
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity().setCohortId(UUID.randomUUID())));
        when(deploymentService.findDeploymentIds(cohortId)).thenReturn(Flux.just(UUID.randomUUID()));

        deploymentSchema.getCohortDeployment(cohort, deploymentId)
                .handle((deployedActivities, throwable) -> {
                    assertEquals(IllegalArgumentFault.class, throwable.getClass());
                    return deployedActivities;
                })
                .join();
    }

    @Test
    void getCohortDeployment_success() {
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity().setCohortId(cohortId)));

        List<DeployedActivity> result = deploymentSchema.getCohortDeployment(cohort, deploymentId).join();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCohortDeployment_success_onDemandCohort() {
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity().setId(deploymentId).setCohortId(UUID.randomUUID())));
        when(deploymentService.findDeploymentIds(cohortId)).thenReturn(Flux.just(UUID.randomUUID(), deploymentId, UUID.randomUUID()));

        List<DeployedActivity> result = deploymentSchema.getCohortDeployment(cohort, deploymentId).join();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cohortId, result.get(0).getCohortId());
    }

    @Test
    void getEvaluation_nullId() {
        assertThrows(IllegalArgumentFault.class, () -> deploymentSchema
                .getEvaluation(null, null).join());
    }

    @Test
    void getEvaluation() {
        when(evaluationResultService.fetch(evaluationId)).thenReturn(Mono.just(new Evaluation()));

        DeployedActivity deployment = mock(DeployedActivity.class);
        Evaluation evaluation = deploymentSchema.getEvaluation(deployment, evaluationId).join();

        assertNotNull(evaluation);
        verify(evaluationResultService).fetch(evaluationId);
    }

    @Test
    void getLearnerAncestry() {
        UUID elementId = UUID.randomUUID();
        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(false);
        when(allowEnrolledStudent.test(authenticationContext, cohortId)).thenReturn(true);

        when(learnerCoursewareService.findCoursewareElementAncestry(elementId, deploymentId))
                .thenReturn(Mono.just(new CoursewareElementAncestry()));

        CoursewareElementAncestry ancestry = deploymentSchema.getLearnerAncestry(resolutionEnvironment, new DeployedActivity()
                .setCohortId(cohortId)
                .setId(deploymentId), elementId)
                .join();

        assertNotNull(ancestry);

        verify(learnerCoursewareService).findCoursewareElementAncestry(elementId, deploymentId);
    }
}
