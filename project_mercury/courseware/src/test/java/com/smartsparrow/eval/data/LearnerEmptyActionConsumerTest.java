package com.smartsparrow.eval.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.action.progress.EmptyActionResult;

class LearnerEmptyActionConsumerTest {

    @InjectMocks
    private LearnerEmptyActionConsumer consumer;

    @Mock
    private Action<? extends ActionContext<?>> action;

    @Mock
    private LearnerEvaluationResponseContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(action.getType()).thenReturn(Action.Type.SEND_FEEDBACK);
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
        EmptyActionResult result = consumer.consume(action, context)
                .block();

        assertNotNull(result);
        assertEquals(action, result.getValue());
        assertEquals(Action.Type.SEND_FEEDBACK, result.getType());
    }
}