package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.scope.ChangeScopeAction;
import com.smartsparrow.eval.action.scope.ChangeScopeActionContext;
import com.smartsparrow.eval.mutation.MutationOperation;
import com.smartsparrow.eval.mutation.MutationOperationService;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.eval.mutation.operations.SumMutationOperation;
import com.smartsparrow.eval.parser.LiteralContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeProducer;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Mono;

class ChangeScopeEventHandlerTest {

    @InjectMocks
    private ChangeScopeEventHandler changeScopeEventHandler;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private StudentScopeService studentScopeService;

    @Mock
    private MutationOperationService mutationOperationService;

    @Mock
    private MutationOperation mutationOperation;

    @Mock
    private StudentScopeProducer studentScopeProducer;

    @Mock
    private StudentScopeEntry entry;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID sourceId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID scopeId = UUID.randomUUID();
    private static final String data = "{\"foo\":\"bar\", \"hiddenScore\": 2.0}";

    private Exchange exchange;
    private EvaluationEventMessage eventMessage;
    private ChangeScopeAction action;
    private ChangeScopeActionContext context;
    private DeployedActivity deployment;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        exchange = mock(Exchange.class);
        eventMessage = mock(EvaluationEventMessage.class);
        action = mock(ChangeScopeAction.class);
        context = mock(ChangeScopeActionContext.class);
        deployment = mock(DeployedActivity.class);
        Message message = mock(Message.class);

        when(message.getBody(ChangeScopeAction.class)).thenReturn(action);
        when(exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(eventMessage);
        when(eventMessage.getStudentId()).thenReturn(studentId);
        when(eventMessage.getDeploymentId()).thenReturn(deploymentId);
        when(exchange.getIn()).thenReturn(message);
        when(action.getContext()).thenReturn(context);
        when(action.getResolvedValue()).thenReturn("resolved");
        when(context.getSourceId()).thenReturn(sourceId);
        when(context.getStudentScopeURN()).thenReturn(studentScopeURN);
        when(message.getBody(ChangeScopeAction.class)).thenReturn(action);
        when(context.getContext()).thenReturn(Lists.newArrayList("foo"));
        when(context.getOperator()).thenReturn(MutationOperator.SET);
        when(context.getDataType()).thenReturn(DataType.STRING);
        when(entry.getData()).thenReturn(data);

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(mutationOperationService.getMutationOperation(DataType.STRING, MutationOperator.SET, false))
                .thenReturn(mutationOperation);
        when(studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)).thenReturn(Mono.just(scopeId));
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.just(entry));
        when(mutationOperation.apply(any(), any())).thenReturn("mutated");
        when(studentScopeService.setStudentScope(eq(deployment), eq(studentId), eq(studentScopeURN), eq(sourceId), any(String.class)))
                .thenReturn(Mono.just(new StudentScopeEntry()));
        when(studentScopeProducer.buildStudentScopeConsumable(any(UUID.class),
                                                              any(UUID.class),
                                                              any(UUID.class),
                                                              any(StudentScopeEntry.class)))
                .thenReturn(studentScopeProducer);

    }

    @Test
    void handle_success() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        changeScopeEventHandler.handle(exchange);

        verify(studentScopeService).setStudentScope(eq(deployment), eq(studentId), eq(studentScopeURN), eq(sourceId), captor.capture());

        String value = captor.getValue();

        assertNotNull(value);
        assertTrue(value.contains("mutated"));
    }

    @Test
    void handle_scopeEntryNotFound() {
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.empty());

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> changeScopeEventHandler.handle(exchange));

        assertNotNull(e);
        assertEquals("scopeEntry not found", e.getMessage());
    }

    @Test
    void mutationOperation_shouldHandleMultipleNumberTypes() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        ChangeScopeActionContext changeScopeActionContext = new ChangeScopeActionContext()
                .setContext(Lists.newArrayList("hiddenScore"))
                .setStudentScopeURN(studentScopeURN)
                .setDataType(DataType.NUMBER)
                .setOperator(MutationOperator.ADD)
                .setSourceId(sourceId)
                .setValue(1)
                .setSchemaProperty(new HashMap<>());

        ChangeScopeAction action = new ChangeScopeAction()
                .setContext(changeScopeActionContext)
                .setResolvedValue(1)
                .setResolver(new LiteralContext()
                        .setType(Resolver.Type.LITERAL))
                .setType(Action.Type.CHANGE_SCOPE);

        Message message = mock(Message.class);
        when(message.getBody(ChangeScopeAction.class)).thenReturn(action);
        when(exchange.getIn()).thenReturn(message);
        when(mutationOperationService.getMutationOperation(DataType.NUMBER, MutationOperator.ADD, false))
                .thenReturn(new SumMutationOperation());

        changeScopeEventHandler.handle(exchange);

        verify(studentScopeService).setStudentScope(any(Deployment.class), any(UUID.class), any(UUID.class), any(UUID.class), captor.capture());

        String mutatedData = captor.getValue();

        assertTrue(mutatedData.contains("3"));
        assertFalse(mutatedData.contains("3.0"));
    }

}
