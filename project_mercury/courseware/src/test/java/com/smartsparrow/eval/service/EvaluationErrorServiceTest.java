package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.data.EvaluationError;
import com.smartsparrow.eval.data.EvaluationErrorGateway;

import reactor.core.publisher.Flux;

class EvaluationErrorServiceTest {

    @InjectMocks
    private EvaluationErrorService evaluationErrorService;

    @Mock
    private EvaluationErrorGateway evaluationErrorGateway;

    @Mock
    private Throwable throwable;

    private static final String errorMessage = "@#$%!";
    private static final UUID evaluationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        StackTraceElement one = new StackTraceElement(EvaluationErrorServiceTest.class.getCanonicalName(),
                "createGeneric", "", 43);
        StackTraceElement two = new StackTraceElement(EvaluationErrorServiceTest.class.getCanonicalName(),
                "createGeneric", "", 45);

        when(evaluationErrorGateway.persist(any(EvaluationError.class))).thenReturn(Flux.just(new Void[]{}));
        when(throwable.getStackTrace()).thenReturn(new StackTraceElement[] { one, two });
        when(throwable.getMessage()).thenReturn(errorMessage);

    }

    @Test
    void createGeneric() {
        String expectedStacktrace = "com.smartsparrow.eval.service.EvaluationErrorServiceTest.createGeneric(:43)\n" +
                                    "com.smartsparrow.eval.service.EvaluationErrorServiceTest.createGeneric(:45)";

        EvaluationError evaluationError = evaluationErrorService.createGeneric(throwable, evaluationId)
                .block();

        assertNotNull(evaluationError);
        assertEquals(evaluationId, evaluationError.getEvaluationId());
        assertNotNull(evaluationError.getId());
        assertNotNull(evaluationError.getOccurredAt());
        assertEquals(EvaluationError.Type.GENERIC, evaluationError.getType());
        assertEquals(errorMessage, evaluationError.getError());
        assertEquals(expectedStacktrace, evaluationError.getStacktrace());
    }

    @Test
    void create() {
        String expectedStacktrace = "com.smartsparrow.eval.service.EvaluationErrorServiceTest.createGeneric(:43)\n" +
                                    "com.smartsparrow.eval.service.EvaluationErrorServiceTest.createGeneric(:45)";

        EvaluationError evaluationError = evaluationErrorService.create(throwable, evaluationId, EvaluationError.Type.GENERIC)
                .block();

        assertNotNull(evaluationError);
        assertEquals(evaluationId, evaluationError.getEvaluationId());
        assertNotNull(evaluationError.getId());
        assertNotNull(evaluationError.getOccurredAt());
        assertEquals(EvaluationError.Type.GENERIC, evaluationError.getType());
        assertEquals(errorMessage, evaluationError.getError());
        assertEquals(expectedStacktrace, evaluationError.getStacktrace());
    }

}
