package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.AttemptStubs.deploymentId;
import static com.smartsparrow.learner.service.AttemptStubs.interactiveId;
import static com.smartsparrow.learner.service.AttemptStubs.mockAttemptService;
import static com.smartsparrow.learner.service.AttemptStubs.pathwayAttemptId;
import static com.smartsparrow.learner.service.AttemptStubs.studentId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.lang.ProgressNotFoundFault;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class FreePathwayAttemptResolverTest {

    @InjectMocks
    private FreePathwayAttemptResolver freePathwayAttemptResolver;

    @Mock
    private AttemptService attemptService;

    @Mock
    private ProgressService progressService;

    @Mock
    private Progress progress;

    private Completion completion;

    private static Attempt parentPathwayAttempt = new Attempt()
            .setId(pathwayAttemptId);

    private static Attempt interactiveAttempt = new Attempt()
            .setId(UUID.randomUUID())
            .setParentId(pathwayAttemptId)
            .setDeploymentId(deploymentId)
            .setStudentId(studentId)
            .setCoursewareElementId(interactiveId)
            .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
            .setValue(2);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        mockAttemptService(attemptService);

        completion = new Completion();

        when(progress.getCompletion()).thenReturn(completion);

    }

    @Test
    @DisplayName("Should return the same attempt if current attempt has no progress")
    void resolveInteractiveAttempt_noEvaluationResult() {
        TestPublisher<Progress> publisher = TestPublisher.create();
        publisher.error(new ProgressNotFoundFault("not found"));
        when(progressService.findLatest(deploymentId, interactiveId, studentId)).thenReturn(publisher.mono());

        Attempt result = freePathwayAttemptResolver
                .resolveInteractiveAttempt(deploymentId, interactiveId, studentId, parentPathwayAttempt, interactiveAttempt).block();

        assertEquals(interactiveAttempt, result);
    }

    @Test
    @DisplayName("Should increase attempt if latest progress is not completed")
    void resolveInteractiveAttempt_notComplete() {
        completion.setValue(0.7f);
        when(progress.getAttemptId()).thenReturn(interactiveAttempt.getId());
        when(progress.getCompletion()).thenReturn(completion);
        when(progressService.findLatest(deploymentId, interactiveId, studentId)).thenReturn(Mono.just(progress));

        Attempt result = freePathwayAttemptResolver
                .resolveInteractiveAttempt(deploymentId, interactiveId, studentId, parentPathwayAttempt, interactiveAttempt).block();

        assertNotNull(result);
        verify(attemptService)
                .newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.INTERACTIVE), eq(interactiveId), eq(parentPathwayAttempt.getId()), eq(3));
        assertEquals(3, (int) result.getValue());
    }

    @Test
    @DisplayName("Should return the same attempt if interactive is completed")
    void resolveInteractiveAttempt_complete() {
        completion.setValue(1f);
        when(progress.getAttemptId()).thenReturn(interactiveAttempt.getId());
        when(progress.getCompletion()).thenReturn(completion);
        when(progressService.findLatest(deploymentId, interactiveId, studentId)).thenReturn(Mono.just(progress));

        Attempt result = freePathwayAttemptResolver
                .resolveInteractiveAttempt(deploymentId, interactiveId, studentId, parentPathwayAttempt, interactiveAttempt).block();

        assertEquals(interactiveAttempt, result);
    }


}
