package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.courseware.service.CoursewareElementDataStub.build;
import static com.smartsparrow.learner.event.EvaluationEventMessageDataStub.evaluationEventMessage;
import static com.smartsparrow.learner.service.ChangeScoreActionDataStub.buildChangeScoreAction;
import static com.smartsparrow.learner.service.EvaluationDataStub.buildEvaluationResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.StudentScoreEntry;
import com.smartsparrow.learner.event.EvaluationEventMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ChangeScoreEventHandlerTest {

    @InjectMocks
    private ChangeScoreEventHandler changeScoreEventHandler;

    @Mock
    private StudentScoreService studentScoreService;

    private Exchange exchange;
    private EvaluationEventMessage eventMessage;
    private static final UUID clientId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Message message = mock(Message.class);
        exchange = mock(Exchange.class);

        when(exchange.getIn()).thenReturn(message);

        EvaluationResult evaluationResult = buildEvaluationResult(true);
        ChangeScoreAction action = buildChangeScoreAction(10d, evaluationResult.getCoursewareElementId(), CoursewareElementType.INTERACTIVE, MutationOperator.ADD);

        when(message.getBody(ChangeScoreAction.class)).thenReturn(action);

        List<CoursewareElement> ancestry = Lists.newArrayList(
                new CoursewareElement()
                .setElementId(evaluationResult.getCoursewareElementId())
                .setElementType(CoursewareElementType.INTERACTIVE),
                build(CoursewareElementType.PATHWAY),
                build(CoursewareElementType.ACTIVITY)
        );

        // ensure sublist works as expected
        List<CoursewareElement> subList = ancestry.subList(1, ancestry.size());
        assertEquals(2, subList.size());
        assertFalse(subList.contains(ancestry.get(0)));

        eventMessage = evaluationEventMessage(evaluationResult, ancestry, clientId, studentId);

        when(exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(eventMessage);

        when(studentScoreService.create(action, eventMessage)).thenReturn(Mono.just(new StudentScoreEntry()));
        when(studentScoreService.createAncestorEntry(any(StudentScoreEntry.class), any(CoursewareElement.class)))
                .thenReturn(Mono.just(new StudentScoreEntry()));
    }

    @Test
    void handle_emptyAncestry() {
        eventMessage.setAncestryList(new ArrayList<>());

        IllegalStateFault e = assertThrows(IllegalStateFault.class, () -> changeScoreEventHandler.handle(exchange));

        assertNotNull(e);
        assertEquals("error processing CHANGE_SCORE action. Ancestry cannot be empty", e.getMessage());
    }

    @Test
    void handle() {
        when(studentScoreService.rollUpScoreEntries(any(StudentScoreEntry.class), any(List.class)))
                .thenReturn(Flux.empty());

        changeScoreEventHandler.handle(exchange);

        verify(studentScoreService).create(any(ChangeScoreAction.class), any(EvaluationEventMessage.class));
        verify(studentScoreService).rollUpScoreEntries(any(StudentScoreEntry.class), any(List.class));
    }

    @Test
    @DisplayName("It should not throw an ancestry empty error when processing multiple actions")
    void handle_testAncestryEmptyError() {
        when(studentScoreService.rollUpScoreEntries(any(StudentScoreEntry.class), any(List.class)))
                .thenReturn(Flux.empty());

        for (int i = 0; i < 5; i++) {
            changeScoreEventHandler.handle(exchange);
        }

        verify(studentScoreService, times(5)).create(any(ChangeScoreAction.class), any(EvaluationEventMessage.class));
        verify(studentScoreService, times(5)).rollUpScoreEntries(any(StudentScoreEntry.class), any(List.class));
    }

    @Test
    void foo() {

    }

}