package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.CoursewareDataStubs.buildLearnerActivity;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerCoursewareElement;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.LearnerInteractiveService;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.util.UUIDs;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

class ElementSchemaTest {

    @InjectMocks
    private ElementSchema elementSchema;
    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private LearnerCoursewareService learnerCoursewareService;
    @Mock
    private AllowCohortInstructor allowCohortInstructor;
    @Mock
    private AllowEnrolledStudent allowEnrolledStudent;
    @Mock
    private LearnerService learnerService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private LearnerInteractiveService learnerInteractiveService;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final LearnerActivity learnerActivity = buildLearnerActivity(elementId, deploymentId, UUID.randomUUID());
    private DeployedActivity deployment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<CoursewareElement> ancestry = Lists.newArrayList(
                new CoursewareElement().setElementId(activityId).setElementType(ACTIVITY),
                new CoursewareElement().setElementId(UUIDs.random()).setElementType(CoursewareElementType.PATHWAY),
                new CoursewareElement().setElementId(UUIDs.random()).setElementType(ACTIVITY)
        );

        deployment = new DeployedActivity()
                .setId(deploymentId)
                .setActivityId(activityId)
                .setCohortId(cohortId);

        mockAuthenticationContextProvider(authenticationContextProvider, accountId);

        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);
        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);

        when(learnerCoursewareService.getAncestry(deploymentId, elementId, ACTIVITY))
                .thenReturn(Mono.just(ancestry));

        when(learnerService.findElementByDeployment(elementId, deploymentId)).thenReturn(
                Mono.just(new LearnerCoursewareElement().setElementType(INTERACTIVE)));

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
    void getLearnerElementActivity() {
        when(learnerService.findElementByDeployment(elementId, deploymentId)).thenReturn(
                Mono.just(new LearnerCoursewareElement().setElementType(ACTIVITY)));
        when(learnerActivityService.findActivity(elementId, deploymentId)).thenReturn(Mono.just(new LearnerActivity()));

        LearnerWalkable result = elementSchema.getElement(deployment, elementId).join();

        assertNotNull(result);
    }

    @Test
    void getLearnerElementInteractive() {
        when(learnerService.findElementByDeployment(elementId, deploymentId)).thenReturn(
                Mono.just(new LearnerCoursewareElement().setElementType(INTERACTIVE)));
        when(learnerInteractiveService.findInteractive(elementId, deploymentId)).thenReturn(Mono.just(new LearnerInteractive()));

        LearnerWalkable result = elementSchema.getElement(deployment, elementId).join();

        assertNotNull(result);
    }

    @Test
    void getLearnerElement_notFound() {
        when(learnerService.findElementByDeployment(elementId, deploymentId)).thenReturn(Mono.empty());

        LearnerWalkable result = elementSchema.getElement(deployment, elementId).join();

        assertNull(result);
    }

    @Test
    void getAncestry_authorized() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortId)).thenReturn(true);
        when(learnerActivityService.findActivity(activityId, deploymentId)).thenReturn(Mono.just(learnerActivity));
        when(learnerActivityService.findActivity(any(UUID.class), any(UUID.class))).thenReturn(Mono.just(new LearnerActivity().setId(UUIDs.random())));

        List<LearnerActivity> result = elementSchema.getAncestry(resolutionEnvironment, learnerActivity, cohortId).join();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(el -> el.getId().equals(learnerActivity.getId())));
    }

    @Test
    void getAncestry_authorized_nullAncestry() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);

        when(learnerCoursewareService.getAncestry(deploymentId, elementId, ACTIVITY))
                .thenReturn(Mono.empty());

        elementSchema.getAncestry(resolutionEnvironment, learnerActivity, cohortId)
                        .handle((coursewareElements, throwable) -> {
                            assertEquals(0, coursewareElements.size());
                            return coursewareElements;
                        }).join();
    }
}
