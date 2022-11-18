package com.smartsparrow.eval.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.eval.lang.UnableToResolveException;
import com.smartsparrow.eval.parser.Operand;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.EvaluationTestContext;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Mono;

class OperandScopeResolverTest {

    @InjectMocks
    private OperandScopeResolver operandScopeResolver;

    @Mock
    private StudentScopeService studentScopeService;

    private Operand operand;
    private EvaluationLearnerContext evaluationLearnerContext;
    private ScopeContext scopeContext;
    private StudentScopeEntry studentScopeEntry;
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final UUID sourceId = UUID.randomUUID();
    private static final UUID scopeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        scopeContext = mock(ScopeContext.class);
        studentScopeEntry = mock(StudentScopeEntry.class);
        operand = new Operand()
                .setResolver(scopeContext);

        evaluationLearnerContext = new EvaluationLearnerContext()
                .setDeploymentId(deploymentId)
                .setStudentId(studentId);

        when(scopeContext.getStudentScopeURN()).thenReturn(studentScopeURN);
        when(scopeContext.getSourceId()).thenReturn(sourceId);

        when(studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN))
                .thenReturn(Mono.just(scopeId));
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.just(studentScopeEntry));
    }

    @Test
    void resolve_scopeIdNotFound() {
        when(studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)).thenReturn(Mono.empty());
        UnableToResolveException e = assertThrows(UnableToResolveException.class,
                () -> operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block());

        assertTrue(e.getMessage().contains("scope id not found"));
    }

    @Test
    void resolve_studentScopeEntryNotFound() {
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.empty());
        UnableToResolveException e = assertThrows(UnableToResolveException.class,
                () -> operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block());

        assertTrue(e.getMessage().contains("student scope entry not found"));
    }

    @Test
    void resolve_invalidData() {
        when(studentScopeEntry.getData()).thenReturn("{\"invalid\"json}");
        assertThrows(JSONException.class, () -> operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block());
    }

    @Test
    void resolve_invalidJsonPath() {
        when(scopeContext.getContext()).thenReturn(Lists.newArrayList());
        when(studentScopeEntry.getData()).thenReturn("{\"valid\":\"json\"}");
        UnableToResolveException e = assertThrows(UnableToResolveException.class,
                () -> operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block());

        assertEquals("context is not defined", e.getMessage());
    }

    @Test
    void resolve_success() {
        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("valid"));
        when(studentScopeEntry.getData()).thenReturn("{\"valid\":\"json\"}");

        Operand resolved = operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block();

        assertNotNull(resolved);
        assertEquals("json", resolved.getResolvedValue());
    }

    @Test
    void resolve_successNestedValue() {
        String json = "{\"selection\": {\"foo\":\"bar\"}}";

        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("selection", "foo"));
        when(studentScopeEntry.getData()).thenReturn(json);

        Operand resolved = operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block();

        assertNotNull(resolved);
        assertEquals("bar", resolved.getResolvedValue());
    }

    @Test
    void resolve_successNestedList() {
        String json = "{\"selection\":[\"foo\",\"bar\"]}";

        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("selection", "0"));
        when(studentScopeEntry.getData()).thenReturn(json);

        Operand resolved = operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block();

        assertNotNull(resolved);
        assertEquals("foo", resolved.getResolvedValue());
    }

    @Test
    void resolve_array() {
        String json = "{\"selection\":[\"foo\",\"bar\"]}";

        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("selection"));
        when(studentScopeEntry.getData()).thenReturn(json);

        Operand resolved = operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block();

        assertNotNull(resolved);
        assertFalse(resolved.getResolvedValue() instanceof JSONArray);
        assertTrue(resolved.getResolvedValue() instanceof List);
    }

    @Test
    void resolve_object() {
        String json = "{\"selection\": {\"foo\":\"bar\"}}";

        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("selection"));
        when(studentScopeEntry.getData()).thenReturn(json);

        Operand resolved = operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block();

        assertNotNull(resolved);
        assertFalse(resolved.getResolvedValue() instanceof JSONObject);
    }


    @Test
    void resolve_fieldValueIsNull() {
        String json = "{\"selection\":null}";

        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("selection"));
        when(studentScopeEntry.getData()).thenReturn(json);

        Operand resolved = operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block();

        assertNotNull(resolved);
        assertNull(resolved.getResolvedValue());
    }

    @Test
    void resolve_fieldDoesNotExistInScope() {
        String json = "{\"selection\":null}";

        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("anotherField"));
        when(studentScopeEntry.getData()).thenReturn(json);

        assertThrows(UnableToResolveException.class, () -> operandScopeResolver.resolve(operand, DataType.STRING, evaluationLearnerContext).block());
    }

    @Test
    void resolve_testContext_scopeEntryNotFound() {
        EvaluationTestContext evaluationTestContext = new EvaluationTestContext("{\"foo\":\"bar\"}");

        UnableToResolveException e = assertThrows(UnableToResolveException.class,
                () -> operandScopeResolver.resolve(operand, DataType.STRING, evaluationTestContext)
                        .block());

        assertNotNull(e);
        assertEquals("could not find test scopeEntry " + sourceId + " in {\"foo\":\"bar\"}", e.getMessage());
    }

    @Test
    void resolve_testContext_scopeDataNotFound() {
        EvaluationTestContext evaluationTestContext = new EvaluationTestContext("{\"" + sourceId + "\":{\"foo\":\"bar\"}}");
        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("selection"));

        UnableToResolveException e = assertThrows(UnableToResolveException.class,
                () -> operandScopeResolver.resolve(operand, DataType.STRING, evaluationTestContext)
                        .block());

        assertNotNull(e);
        assertEquals("could not find path [selection]", e.getMessage());

    }

    @Test
    void resolve_testContext() {
        final String data = "{\"" + sourceId + "\":{\"selection\":\"lol\"}}";
        EvaluationTestContext evaluationTestContext = new EvaluationTestContext(data);
        when(scopeContext.getContext()).thenReturn(Lists.newArrayList("selection"));

        Operand resolved = operandScopeResolver.resolve(operand, DataType.STRING, evaluationTestContext)
                .block();

        assertNotNull(resolved);
        assertEquals("lol", resolved.getResolvedValue());
    }

}
