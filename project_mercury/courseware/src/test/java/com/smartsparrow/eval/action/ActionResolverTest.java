package com.smartsparrow.eval.action;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.action.resolver.ActionLiteralResolver;
import com.smartsparrow.eval.action.resolver.ActionScopeResolver;
import com.smartsparrow.eval.action.resolver.DeprecatedActionScopeResolver;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.util.DataType;

class ActionResolverTest {

    @InjectMocks
    private ActionResolver actionResolver;

    @Mock
    private ActionLiteralResolver actionLiteralResolver;

    @Mock
    private DeprecatedActionScopeResolver deprecatedActionScopeResolver;

    @Mock
    private ActionScopeResolver actionScopeResolver;

    @Mock
    private Action action;

    @Mock
    private ResolverContext resolverContext;

    @Mock
    private EvaluationEventMessage eventMessage;

    @Mock
    private LearnerEvaluationResponseContext responseContext;

    @Mock
    private ActionContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(action.getContext()).thenReturn(context);
        when(context.getDataType()).thenReturn(DataType.STRING);
        when(action.getResolver()).thenReturn(resolverContext);
    }

    @Test
    void resolve_literal() {
        when(resolverContext.getType()).thenReturn(Resolver.Type.LITERAL);
        actionResolver.resolve(action, eventMessage);
        verify(actionLiteralResolver).resolve(action, context);
    }

    @Test
    void resolve_web() {
        when(resolverContext.getType()).thenReturn(Resolver.Type.WEB);
        assertThrows(UnsupportedOperationFault.class, ()-> actionResolver.resolve(action, eventMessage));
    }

    @Test
    void resolve_scopeDeprecated() {
        when(resolverContext.getType()).thenReturn(Resolver.Type.SCOPE);
        actionResolver.resolve(action, eventMessage);
        verify(deprecatedActionScopeResolver).resolve(action, DataType.STRING, eventMessage);
    }

    @Test
    void resolve_scope() {
        when(resolverContext.getType()).thenReturn(Resolver.Type.SCOPE);
        actionResolver.resolve(action, responseContext);
        verify(actionScopeResolver).resolve(action, DataType.STRING, responseContext);
    }

}
