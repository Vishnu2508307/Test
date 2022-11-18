package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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

class BKTPathwayAttemptResolverTest {

    @InjectMocks
    private BKTPathwayAttemptResolver bktPathwayAttemptResolver;

    @Mock
    private AttemptService attemptService;

    @Mock
    private ProgressService progressService;

    @Mock
    private Progress latestProgress;

    @Mock
    private Attempt parentPathwayAttempt;

    @Mock
    private Attempt interactiveAttempt;

    @Mock
    private Completion completion;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID parentPathwayAttemptId = UUID.randomUUID();
    private static final UUID interactiveAttemptId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(progressService.findLatest(deploymentId, interactiveId, studentId)).thenReturn(Mono.just(latestProgress));
        when(latestProgress.getCompletion()).thenReturn(completion);
        when(completion.isCompleted()).thenReturn(true);

        when(parentPathwayAttempt.getId()).thenReturn(parentPathwayAttemptId);
        when(interactiveAttempt.getId()).thenReturn(interactiveAttemptId);
        when(interactiveAttempt.getValue()).thenReturn(1);
    }

    @Test
    void resolveAttempt_latestProgressSameAttempt_notCompleted() {
        when(completion.isCompleted()).thenReturn(false);
        when(latestProgress.getAttemptId()).thenReturn(interactiveAttemptId);

        when(attemptService.newAttempt(deploymentId, studentId, CoursewareElementType.INTERACTIVE, interactiveId, parentPathwayAttemptId, 2))
                .thenReturn(Mono.just(new Attempt()
                        .setValue(2)));

        Attempt resolvedAttempt = bktPathwayAttemptResolver.resolveInteractiveAttempt(deploymentId, interactiveId, studentId,
                parentPathwayAttempt, interactiveAttempt)
                .block();

        assertNotNull(resolvedAttempt);
        assertEquals(Integer.valueOf(2), resolvedAttempt.getValue());

        verify(attemptService).newAttempt(deploymentId, studentId, CoursewareElementType.INTERACTIVE, interactiveId, parentPathwayAttemptId, 2);
    }

    @Test
    void resolveAttempt_latestProgressSameAttempt_completed() {
        when(latestProgress.getAttemptId()).thenReturn(interactiveAttemptId);

        Attempt resolvedAttempt = bktPathwayAttemptResolver.resolveInteractiveAttempt(deploymentId, interactiveId, studentId,
                parentPathwayAttempt, interactiveAttempt)
                .block();

        assertNotNull(resolvedAttempt);
        assertEquals(Integer.valueOf(1), resolvedAttempt.getValue());

        verify(attemptService, never()).newAttempt(any(UUID.class), any(UUID.class), any(CoursewareElementType.class), any(UUID.class), any(UUID.class),any(Integer.class));
    }

    @Test
    void resolveAttempt_latestProgressDifferentAttempt() {
        when(latestProgress.getAttemptId()).thenReturn(UUID.randomUUID());

        Attempt resolvedAttempt = bktPathwayAttemptResolver.resolveInteractiveAttempt(deploymentId, interactiveId, studentId,
                parentPathwayAttempt, interactiveAttempt)
                .block();

        assertNotNull(resolvedAttempt);
        assertEquals(Integer.valueOf(1), resolvedAttempt.getValue());

        verify(attemptService, never()).newAttempt(any(UUID.class), any(UUID.class), any(CoursewareElementType.class), any(UUID.class), any(UUID.class),any(Integer.class));
    }

    @Test
    void resolveAttempt_progressNotFound() {
        TestPublisher<Progress> publisher = TestPublisher.create();
        publisher.error(new ProgressNotFoundFault("not found"));

        when(progressService.findLatest(deploymentId, interactiveId, studentId)).thenReturn(publisher.mono());

        Attempt resolvedAttempt = bktPathwayAttemptResolver.resolveInteractiveAttempt(deploymentId, interactiveId, studentId,
                parentPathwayAttempt, interactiveAttempt)
                .block();

        assertNotNull(resolvedAttempt);
        assertEquals(Integer.valueOf(1), resolvedAttempt.getValue());

    }

}
