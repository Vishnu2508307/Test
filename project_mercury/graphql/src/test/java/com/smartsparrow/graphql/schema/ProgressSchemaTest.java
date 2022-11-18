package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.attemptId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.deploymentId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.elementId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.studentId;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

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
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GeneralProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AcquireAttemptService;
import com.smartsparrow.learner.service.ProgressService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

class ProgressSchemaTest {

    @InjectMocks
    private ProgressSchema progressSchema;

    @Mock
    private ProgressService progressService;

    @Mock
    private AcquireAttemptService acquireAttemptService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    ResolutionEnvironment resolutionEnvironment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockAuthenticationContextProvider(authenticationContextProvider, studentId);
        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContextProvider.get())).build(),
                null,
                null,
                null,
                null);
    }

    @Test
    void getProgress_noProgressForLatestAttempt() {
        when(acquireAttemptService.acquireLatestAttempt(deploymentId, elementId, CoursewareElementType.ACTIVITY, studentId))
                .thenReturn(Mono.just(new Attempt().setId(attemptId)));
        when(progressService.findLatest(deploymentId, elementId, studentId))
                .thenReturn(Mono.just(new GeneralProgress().setAttemptId(UUID.randomUUID())));

        LearnerWalkable walkable = new LearnerActivity().setId(elementId).setDeploymentId(deploymentId);

        Progress progress = progressSchema.getProgress(resolutionEnvironment, walkable).join();

        assertNotNull(progress);
        assertNull(progress.getId());
        assertEquals(Float.valueOf(0f), progress.getCompletion().getValue());
        assertEquals(Float.valueOf(0f), progress.getCompletion().getConfidence());
    }

    @Test
    void getProgress_progressExists() {
        Progress progress = new GeneralProgress().setAttemptId(attemptId).setId(UUID.randomUUID())
                .setCompletion(new Completion().setValue(0.5f).setConfidence(1f));

        when(acquireAttemptService.acquireLatestAttempt(deploymentId, elementId, CoursewareElementType.ACTIVITY, studentId))
                .thenReturn(Mono.just(new Attempt().setId(attemptId)));
        when(progressService.findLatest(deploymentId, elementId, studentId)).thenReturn(Mono.just(progress));

        LearnerWalkable walkable = new LearnerActivity().setId(elementId).setDeploymentId(deploymentId);

        Progress result = progressSchema.getProgress(resolutionEnvironment, walkable).join();

        assertNotNull(result);
        assertEquals(progress.getId(), result.getId());
        assertEquals(Float.valueOf(0.5f), progress.getCompletion().getValue());
        assertEquals(Float.valueOf(1f), progress.getCompletion().getConfidence());
    }
}
