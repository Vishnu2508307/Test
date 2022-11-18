package com.smartsparrow.eval.action.resolver;

import static com.smartsparrow.eval.action.resolver.ActionDataStubs.DATA;
import static com.smartsparrow.eval.action.resolver.ActionDataStubs.buildAction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.lang.UnableToResolveException;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Mono;

class ActionScopeResolverTest {

    @InjectMocks
    private ActionScopeResolver actionScopeResolver;

    @Mock
    private StudentScopeService studentScopeService;

    @Mock
    private ActionContext context;

    @Mock
    private ScopeContext scopeContext;

    @Mock
    private LearnerEvaluationResponseContext responseContext;

    @Mock
    private LearnerEvaluationResponse response;

    @Mock
    private LearnerEvaluationRequest request;

    private Action resolvable;
    private StudentScopeEntry scopeEntry;
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID sourceId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final UUID scopeId = UUID.randomUUID();
    private static final List<String> contextPath = Lists.newArrayList("context", "data", "type");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(responseContext.getResponse()).thenReturn(response);
        when(response.getEvaluationRequest()).thenReturn(request);
        when(request.getStudentId()).thenReturn(studentId);
        when(request.getDeployment()).thenReturn(new Deployment()
                .setId(deploymentId));

        when(scopeContext.getContext()).thenReturn(contextPath);
        when(scopeContext.getSourceId()).thenReturn(sourceId);
        when(scopeContext.getStudentScopeURN()).thenReturn(studentScopeURN);

        resolvable = buildAction(context, scopeContext);
        scopeEntry = new StudentScopeEntry()
                .setData(DATA);

        when(studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)).thenReturn(Mono.just(scopeId));
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.just(scopeEntry));
    }

    @Test
    @DisplayName("It should resolve the resolvable")
    void resolve() {
        Action resolved = actionScopeResolver.resolve(resolvable, DataType.STRING, responseContext).block();

        assertNotNull(resolved);
        assertEquals("list", resolved.getResolvedValue());
    }

    @Test
    @DisplayName("It should throw UnableToResolveException when the scopeId is not found")
    void resolve_scopeIdNotFound() {
        when(studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)).thenReturn(Mono.empty());

        UnableToResolveException e = assertThrows(UnableToResolveException.class,
                () -> actionScopeResolver.resolve(resolvable, DataType.STRING, responseContext).block());

        assertNotNull(e);
        assertEquals("scope id not found", e.getMessage());
    }

    @Test
    @DisplayName("It should throw UnableToResolveException when the scopeEntry is not found")
    void resolve_scopeEntryNotFound() {
        when(studentScopeService.fetchScopeEntry(scopeId, sourceId)).thenReturn(Mono.empty());

        UnableToResolveException e = assertThrows(UnableToResolveException.class,
                () -> actionScopeResolver.resolve(resolvable, DataType.STRING, responseContext).block());

        assertNotNull(e);
        assertTrue(e.getMessage().contains("student scope entry not found for"));
    }

    @Test
    @DisplayName("It should throw an exception when the path to query is not found")
    void resolve_pathNotFound() {
        List<String> contextPath = Lists.newArrayList("context", "data", "foo");
        when(scopeContext.getContext()).thenReturn(contextPath);

        assertThrows(UnableToResolveException.class,
                () -> actionScopeResolver.resolve(resolvable, DataType.STRING, responseContext).block());
    }

    @Test
    @DisplayName("It should throw an exception when the path to query is illegal")
    void resolve_illegalPath() {
        List<String> contextPath = Lists.newArrayList("context", "data", "type", "foo");
        when(scopeContext.getContext()).thenReturn(contextPath);

        assertThrows(UnableToResolveException.class,
                () -> actionScopeResolver.resolve(resolvable, DataType.STRING, responseContext).block());
    }

    @Test
    @DisplayName("It should throw an exception when the path to query is illegal")
    void resolve_emptyPath() {
        List<String> contextPath = Lists.newArrayList();
        when(scopeContext.getContext()).thenReturn(contextPath);

        assertThrows(UnableToResolveException.class,
                () -> actionScopeResolver.resolve(resolvable, DataType.STRING, responseContext).block());
    }
}