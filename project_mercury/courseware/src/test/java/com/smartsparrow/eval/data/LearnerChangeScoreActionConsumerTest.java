package com.smartsparrow.eval.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.learner.data.StudentScoreEntry;
import com.smartsparrow.learner.service.StudentScoreService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerChangeScoreActionConsumerTest {

    @InjectMocks
    private LearnerChangeScoreActionConsumer consumer;

    @Mock
    private StudentScoreService studentScoreService;

    @Mock
    private ChangeScoreAction action;

    @Mock
    private LearnerEvaluationResponseContext context;

    private static final List<CoursewareElement> ancestry = Lists.newArrayList(
            CoursewareElement.from(UUIDs.timeBased(), CoursewareElementType.INTERACTIVE),
            CoursewareElement.from(UUIDs.timeBased(), CoursewareElementType.ACTIVITY)
    );
    private static final Double value = 2.0;
    private static final StudentScoreEntry entry = new StudentScoreEntry()
            .setValue(value);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(action.getType()).thenReturn(Action.Type.CHANGE_SCORE);
        when(action.getResolvedValue()).thenReturn(value);
        when(studentScoreService.create(action, context))
                .thenReturn(Mono.just(entry));

        when(studentScoreService.rollUpScoreEntries(any(StudentScoreEntry.class), any(List.class)))
                .thenReturn(Flux.just(entry));

        when(context.getAncestry()).thenReturn(ancestry);
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
        final EmptyActionResult result = consumer.consume(action, context)
                .block();

        assertNotNull(result);
        assertEquals(action, result.getValue());
        assertEquals(Action.Type.CHANGE_SCORE, result.getType());

        verify(studentScoreService).create(action, context);
        verify(studentScoreService)
                .rollUpScoreEntries(entry, ancestry.subList(1, ancestry.size()));
    }

    @Test
    void consume_scoreEntryEmpty() {
        when(studentScoreService.create(action, context))
                .thenReturn(Mono.empty());
        final EmptyActionResult result = consumer.consume(action, context)
                .block();

        assertNotNull(result);
    }
    @Test
    void consume_scoreEntriesEmpty() {
        when(studentScoreService.rollUpScoreEntries(any(StudentScoreEntry.class), any(List.class)))
                .thenReturn(Flux.empty());
        final EmptyActionResult result = consumer.consume(action, context)
                .block();

        assertNotNull(result);
    }

}