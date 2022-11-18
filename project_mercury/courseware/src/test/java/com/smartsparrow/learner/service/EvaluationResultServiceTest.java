package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.EvaluationResultGateway;
import com.smartsparrow.learner.data.EvaluationScopeData;
import com.smartsparrow.learner.lang.EvaluationResultNotFoundFault;
import com.smartsparrow.learner.payload.StudentScopePayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class EvaluationResultServiceTest {

    @InjectMocks
    private EvaluationResultService evaluationResultService;

    @Mock
    private EvaluationResultGateway evaluationResultGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void fetch_evaluationNotFound() {
        UUID evaluationId = UUID.randomUUID();

        TestPublisher<Evaluation> publisher = TestPublisher.create();
        publisher.error(new NoSuchElementException());

        when(evaluationResultGateway.find(evaluationId)).thenReturn(publisher.mono());

        NotFoundFault e = assertThrows(NotFoundFault.class, () -> evaluationResultService.fetch(evaluationId).block());

        assertNotNull(e);
        assertEquals("evaluation with id `" + evaluationId + "` not found", e.getMessage());
    }

    @Test
    void findByAttempt_notFound() {
        UUID attemptId = UUID.randomUUID();

        TestPublisher<EvaluationResult> publisher = TestPublisher.create();
        publisher.error(new NoSuchElementException());

        when(evaluationResultGateway.findByAttempt(attemptId)).thenReturn(publisher.mono());

        EvaluationResultNotFoundFault e = assertThrows(EvaluationResultNotFoundFault.class,
                () -> evaluationResultService.findByAttempt(attemptId).block());

        assertNotNull(e);
        assertTrue(e.getMessage().contains(attemptId.toString()));
    }

    @Test
    void persist() {
        Evaluation toPersist = new Evaluation();

        when(evaluationResultGateway.persist(any(Evaluation.class))).thenReturn(Flux.just(new Void[]{}));

        Evaluation persisted = evaluationResultService.persist(toPersist)
                .block();

        assertNotNull(persisted);
        assertEquals(toPersist, persisted);
    }

    @Test
    void fetchHistoricScope() {
        UUID evaluationId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID studentScopeURN = UUID.randomUUID();
        Map<UUID, String> dataMap = new HashMap<>();
        dataMap.put(sourceId, "some data here");
        EvaluationScopeData evaluationScopeData = new EvaluationScopeData()
                .setStudentScopeURN(studentScopeURN)
                .setStudentScopeDataMap(dataMap);

        when(evaluationResultGateway.fetchHistoricScope(evaluationId)).thenReturn(Mono.just(evaluationScopeData));

        List<StudentScopePayload> scopes = evaluationResultService.fetchHistoricScope(evaluationId)
                .block();

        assertNotNull(scopes);
        assertEquals(1, scopes.size());
        assertEquals(sourceId, scopes.get(0).getSourceId());
        assertEquals("some data here", scopes.get(0).getData());
        assertEquals(studentScopeURN, scopes.get(0).getScopeURN());
    }

}