package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.attemptId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.deploymentId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.elementId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.studentId;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.AcquireAttemptService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

class AttemptSchemaTest {

    @InjectMocks
    private AttemptSchema attemptSchema;

    @Mock
    private AcquireAttemptService acquireAttemptService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        mockAuthenticationContextProvider(this.authenticationContextProvider,studentId);

        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(this.authenticationContextProvider.get())).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void getAttempt() {
        when(acquireAttemptService.acquireLatestAttempt(deploymentId, elementId,
                CoursewareElementType.ACTIVITY, studentId)).thenReturn(Mono.just(new Attempt().setId(attemptId)));

        LearnerWalkable walkable = new LearnerActivity().setId(elementId).setDeploymentId(deploymentId);

        Attempt attempt = attemptSchema.getAttempt(resolutionEnvironment,walkable).join();

        assertNotNull(attempt);
        assertEquals(attemptId, attempt.getId());
    }
}
