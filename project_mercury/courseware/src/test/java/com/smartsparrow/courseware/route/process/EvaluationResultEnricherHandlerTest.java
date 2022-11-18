package com.smartsparrow.courseware.route.process;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.feedback.SendFeedbackAction;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.EvaluationEventMessage;

class EvaluationResultEnricherHandlerTest {

    @InjectMocks
    private EvaluationResultEnricherHandler evaluationResultEnricherHandler;

    @Mock
    private Exchange exchange;

    @Mock
    private EvaluationEventMessage eventMessage;

    private EvaluationResult evaluationResult = new EvaluationResult();

    private List<Action> actions;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(eventMessage);
        when(eventMessage.getEvaluationResult()).thenReturn(evaluationResult);

        Message message = mock(Message.class);

        when(exchange.getIn()).thenReturn(message);
        actions = new ArrayList<>();
        when(message.getBody(List.class)).thenReturn(actions);
        when(exchange.getOut()).thenReturn(message);

        assertNotNull(evaluationResult.getTriggeredActions());
        assertTrue(evaluationResult.getTriggeredActions().isEmpty());
    }

    @Test
    void handle_noProgressionActions() {
        SendFeedbackAction action = new SendFeedbackAction()
                .setType(Action.Type.SEND_FEEDBACK);

        actions.add(action);

        evaluationResultEnricherHandler.handle(exchange);

        assertEquals(actions, evaluationResult.getTriggeredActions());
        assertFalse(evaluationResult.getInteractiveComplete());
    }

    @Test
    void handle_changeProgress_incomplete() {
        ProgressAction repeat = new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setContext(new ProgressActionContext()
                        .setProgressionType(ProgressionType.INTERACTIVE_REPEAT));

        actions.add(repeat);

        evaluationResultEnricherHandler.handle(exchange);

        assertEquals(actions, evaluationResult.getTriggeredActions());
        assertFalse(evaluationResult.getInteractiveComplete());
    }

    @Test
    void handle_changeProgress_complete() {
        ProgressAction completeAndGoTo = new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setContext(new ProgressActionContext()
                        .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO));

        actions.add(completeAndGoTo);

        evaluationResultEnricherHandler.handle(exchange);

        assertEquals(actions, evaluationResult.getTriggeredActions());
        assertTrue(evaluationResult.getInteractiveComplete());
    }

}