package com.smartsparrow.eval.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.action.scope.ChangeScopeAction;
import com.smartsparrow.eval.action.scope.ChangeScopeActionContext;
import com.smartsparrow.eval.mutation.MutationOperation;
import com.smartsparrow.eval.mutation.MutationOperationService;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeProducer;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Mono;

@SuppressWarnings({"unchecked", "rawtypes"})
class LearnerChangeScopeActionConsumerTest {

    @InjectMocks
    private LearnerChangeScopeActionConsumer consumer;

    @Mock
    private StudentScopeService studentScopeService;

    @Mock
    private MutationOperationService mutationOperationService;

    @Mock
    private StudentScopeProducer studentScopeProducer;

    @Mock
    private ChangeScopeAction action;
    @Mock
    private ChangeScopeActionContext context;
    @Mock
    private MutationOperation mutationOperation;
    @Mock
    private StudentScopeEntry entry;
    @Mock
    private LearnerWalkable walkable;
    private LearnerEvaluationResponseContext responseContext;
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID sourceId = UUIDs.timeBased();
    private static final UUID studentScopeURN = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID scopeId = UUIDs.timeBased();
    private static final String data = "{\"foo\":\"bar\", \"hiddenScore\": 2}";
    private static final String clientId = "clientId";
    private static final Deployment deployment = new Deployment()
            .setId(deploymentId);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        entry = new StudentScopeEntry()
                .setData(data)
                .setScopeId(scopeId)
                .setSourceId(sourceId);

        when(action.getContext()).thenReturn(context);
        when(action.getResolvedValue()).thenReturn("resolved");
        when(action.getType()).thenReturn(Action.Type.CHANGE_SCOPE);
        when(context.getSourceId()).thenReturn(sourceId);
        when(context.getStudentScopeURN()).thenReturn(studentScopeURN);
        when(context.getContext()).thenReturn(Lists.newArrayList("foo"));
        when(context.getOperator()).thenReturn(MutationOperator.SET);
        when(context.getDataType()).thenReturn(DataType.STRING);

        when(walkable.getStudentScopeURN()).thenReturn(studentScopeURN);

        when(mutationOperationService.getMutationOperation(DataType.STRING, MutationOperator.SET, false))
                .thenReturn(mutationOperation);
        when(mutationOperation.apply(any(), any())).thenReturn("mutated");

        when(studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)).thenReturn(Mono.just(scopeId));
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.just(entry));

        when(studentScopeService.setStudentScope(eq(deployment), eq(studentId), eq(studentScopeURN), eq(sourceId), any(String.class)))
                .thenReturn(Mono.just(entry));

        when(studentScopeProducer.buildStudentScopeConsumable(any(UUID.class),
                                                              any(UUID.class),
                                                              any(UUID.class),
                                                              any(StudentScopeEntry.class)))
                .thenReturn(studentScopeProducer);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(new LearnerEvaluationResponse()
                        .setEvaluationRequest(new LearnerEvaluationRequest()
                                .setProducingClientId(clientId)
                                .setDeployment(deployment)
                                .setStudentId(studentId)
                                .setLearnerWalkable(walkable)));

    }

    @Test
    void getActionConsumerOptions_notAsync() {
        ActionConsumerOptions options = consumer.getActionConsumerOptions()
                .block();
        assertNotNull(options);
        assertFalse(options.isAsync());
    }

    @Test
    void consume() {
        final EmptyActionResult result = consumer.consume(action, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(action, result.getValue());
        assertEquals(Action.Type.CHANGE_SCOPE, result.getType());

        verify(studentScopeService).setStudentScope(deployment, studentId, studentScopeURN, sourceId, "{\"foo\":\"mutated\",\"hiddenScore\":2}");
        verify(studentScopeProducer).buildStudentScopeConsumable(studentId, deploymentId, studentScopeURN,entry);
        verify(studentScopeService).findScopeId(deploymentId, studentId, studentScopeURN);
        verify(studentScopeService).fetchScopeEntry(scopeId, sourceId);
    }

    @Test
    void consume_scopeIdEmpty() {
        when(studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)).thenReturn(Mono.empty());
        final EmptyActionResult result = consumer.consume(action, responseContext)
                .block();

        assertNull(result);
    }
    @Test
    void consume_scopeEntryEmpty() {
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.empty());
        final EmptyActionResult result = consumer.consume(action, responseContext)
                .block();

        assertNull(result);
    }
    @Test
    void consume_setScopeEmpty() {
        when(studentScopeService.setStudentScope(eq(deployment), eq(studentId), eq(studentScopeURN), eq(sourceId), any(String.class)))
                .thenReturn(Mono.empty());

        final EmptyActionResult result = consumer.consume(action, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(action, result.getValue());
        assertEquals(Action.Type.CHANGE_SCOPE, result.getType());
    }
}
