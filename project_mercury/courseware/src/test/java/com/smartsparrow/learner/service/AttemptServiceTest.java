package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.AttemptGateway;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AttemptServiceTest {

    @InjectMocks
    private AttemptService attemptService;

    @Mock
    private AttemptGateway attemptGateway;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();

    private static Attempt attempt = new Attempt()
            .setId(UUID.randomUUID())
            .setDeploymentId(deploymentId)
            .setCoursewareElementId(interactiveId)
            .setStudentId(studentId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(attemptGateway.persist(any(Attempt.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void findLatestAttempt_noAttempt() {
        when(attemptGateway.findLatest(eq(deploymentId), any(UUID.class), eq(studentId))).thenReturn(Mono.empty());

        assertThrows(AttemptNotFoundFault.class, () ->
                attemptService.findLatestAttempt(deploymentId, interactiveId, studentId).block());
    }

    @Test
    void findLatestAttempt() {
        when(attemptGateway.findLatest(eq(deploymentId), any(UUID.class), eq(studentId))).thenReturn(Mono.just(attempt));

        Attempt result = attemptService.findLatestAttempt(deploymentId, interactiveId, studentId).block();

        assertEquals(attempt, result);
    }

    @Test
    void newAttempt() {
        Attempt result = attemptService.newAttempt(deploymentId, studentId, CoursewareElementType.INTERACTIVE,
                interactiveId, pathwayId, 2).block();

        ArgumentCaptor<Attempt> attemptCaptor = ArgumentCaptor.forClass(Attempt.class);
        verify(attemptGateway).persist(attemptCaptor.capture());

        assertNotNull(result);
        assertEquals(attemptCaptor.getValue(), result);

        assertNotNull(result.getId());
        assertEquals(pathwayId, result.getParentId());
        assertEquals(interactiveId, result.getCoursewareElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, result.getCoursewareElementType());
        assertEquals(deploymentId, result.getDeploymentId());
        assertEquals(studentId, result.getStudentId());
        assertEquals(2, (int) result.getValue());
    }

    @Test
    void newAttempt_noValue() {
        Attempt result = attemptService.newAttempt(deploymentId, studentId, CoursewareElementType.INTERACTIVE,
                interactiveId, pathwayId).block();

        assertNotNull(result);
        assertEquals(1, (int) result.getValue());
    }


}
