package com.smartsparrow.eval.action.resolver;

import static com.smartsparrow.eval.action.resolver.ActionDataStubs.buildAction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.parser.LiteralContext;

class ActionLiteralResolverTest {

    @InjectMocks
    private ActionLiteralResolver actionLiteralResolver;

    @Mock
    private ActionContext context;

    private Action resolvable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.getValue()).thenReturn("a value");

        resolvable = buildAction(context, new LiteralContext());
    }

    @Test
    void resolve() {
        Action resolved = actionLiteralResolver.resolve(resolvable, context).block();

        assertNotNull(resolved);
        assertEquals("a value", resolved.getResolvedValue());
    }
}
