package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.Walkable;
import com.smartsparrow.eval.data.EvaluationRequest;
import com.smartsparrow.eval.data.TestEvaluationRequest;
import com.smartsparrow.eval.data.TestEvaluationResponse;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.learner.service.EvaluationSubmitService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class WalkableServiceTest {

    @InjectMocks
    private WalkableService walkableService;

    @Mock
    private ActivityService activityService;

    @Mock
    private InteractiveService interactiveService;

    @Mock
    private EvaluationSubmitService evaluationSubmitService;

    private static final UUID elementId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateEvaluationMode_nullElementId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> walkableService.updateEvaluationMode(null, null, null));

        assertEquals("elementId is required", f.getMessage());
    }

    @Test
    void updateEvaluationMode_nullElementType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> walkableService.updateEvaluationMode(elementId, null, null));

        assertEquals("elementType is required", f.getMessage());
    }

    @Test
    void updateEvaluationMode_nullEvaluationMode() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> walkableService.updateEvaluationMode(elementId, CoursewareElementType.ACTIVITY, null));

        assertEquals("evaluationMode is required", f.getMessage());
    }

    @Test
    void updateEvaluationMode_unsupportedType() {
        Arrays.stream(CoursewareElementType.values())
                .filter(elementType -> !CoursewareElementType.isAWalkable(elementType))
                .forEach(elementType -> {
                    UnsupportedOperationFault f = assertThrows(UnsupportedOperationFault.class,
                            () -> walkableService.updateEvaluationMode(elementId, elementType, EvaluationMode.COMBINED));
                    assertEquals(String.format("EvaluationMode unsupported for element type %s", elementType), f.getMessage());
                });
    }

    @Test
    void updateEvaluationMode_activity() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(activityService.updateEvaluationMode(elementId, EvaluationMode.DEFAULT))
                .thenReturn(publisher.mono());

        walkableService.updateEvaluationMode(elementId, CoursewareElementType.ACTIVITY, EvaluationMode.DEFAULT)
                .block();

        verify(activityService).updateEvaluationMode(elementId, EvaluationMode.DEFAULT);
        verify(interactiveService, never()).updateEvaluationMode(any(UUID.class), any(EvaluationMode.class));
    }

    @Test
    void updateEvaluationMode_interactive() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(interactiveService.updateEvaluationMode(elementId, EvaluationMode.DEFAULT))
                .thenReturn(publisher.mono());

        walkableService.updateEvaluationMode(elementId, CoursewareElementType.INTERACTIVE, EvaluationMode.DEFAULT)
                .block();

        verify(interactiveService).updateEvaluationMode(elementId, EvaluationMode.DEFAULT);
        verify(activityService, never()).updateEvaluationMode(any(UUID.class), any(EvaluationMode.class));
    }

    @Test
    void fetchEvaluationMode_nullElementId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> walkableService.fetchEvaluationMode(null, null));

        assertEquals("elementId is required", f.getMessage());
    }

    @Test
    void fetchEvaluationMode_nullElementType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> walkableService.fetchEvaluationMode(elementId, null));

        assertEquals("elementType is required", f.getMessage());
    }

    @Test
    void fetchEvaluationMode_unsupportedType() {
        Arrays.stream(CoursewareElementType.values())
                .filter(elementType -> !CoursewareElementType.isAWalkable(elementType))
                .forEach(elementType -> {
                    UnsupportedOperationFault f = assertThrows(UnsupportedOperationFault.class,
                            () -> walkableService.fetchEvaluationMode(elementId, elementType));
                    assertEquals(String.format("EvaluationMode unsupported for element type %s", elementType), f.getMessage());
                });
    }

    @Test
    void fetchEvaluationMode_activity() {
        when(activityService.findById(elementId))
                .thenReturn(Mono.just(new Activity()
                        .setEvaluationMode(EvaluationMode.COMBINED)));

        final EvaluationMode result = walkableService.fetchEvaluationMode(elementId, CoursewareElementType.ACTIVITY)
                .block();

        assertEquals(EvaluationMode.COMBINED, result);

        verify(activityService).findById(elementId);
        verify(interactiveService, never()).findById(any(UUID.class));
    }


    @Test
    void fetchEvaluationMode_interactive() {
        when(interactiveService.findById(elementId))
                .thenReturn(Mono.just(new Interactive()
                        .setEvaluationMode(EvaluationMode.COMBINED)));

        final EvaluationMode result = walkableService.fetchEvaluationMode(elementId, CoursewareElementType.INTERACTIVE)
                .block();

        assertEquals(EvaluationMode.COMBINED, result);

        verify(interactiveService).findById(elementId);
        verify(activityService, never()).findById(any(UUID.class));
    }


    @Test
    void findWalkable_nullWalkableId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> walkableService.findWalkable(null, null));

        assertEquals("walkableId is required", f.getMessage());
    }

    @Test
    void findWalkable_nullWalkableType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> walkableService.findWalkable(elementId, null));

        assertEquals("walkableType is required", f.getMessage());
    }

    @Test
    void findWalkable_invalidWalkableType() {
        Arrays.stream(CoursewareElementType.values())
                .filter(elementType -> !CoursewareElementType.isAWalkable(elementType))
                .forEach(elementType -> {
                    UnsupportedOperationFault f = assertThrows(UnsupportedOperationFault.class,
                            () -> walkableService.findWalkable(elementId, elementType));
                    assertEquals(String.format("walkableType %s not supported", elementType), f.getMessage());
                });
    }

    @Test
    void findWalkable_activity() {
        when(activityService.findById(elementId)).thenReturn(Mono.just(new Activity()));

        final Walkable walkable = walkableService.findWalkable(elementId, CoursewareElementType.ACTIVITY)
                .block();

        assertNotNull(walkable);

        verify(activityService).findById(elementId);
        verify(interactiveService, never()).findById(elementId);
    }

    @Test
    void findWalkable_interactive() {
        when(interactiveService.findById(elementId)).thenReturn(Mono.just(new Interactive()));

        final Walkable walkable = walkableService.findWalkable(elementId, CoursewareElementType.INTERACTIVE)
                .block();

        assertNotNull(walkable);

        verify(interactiveService).findById(elementId);
        verify(activityService, never()).findById(elementId);
    }

    @Test
    void evaluate_interactive() {
        final Interactive interactive = new Interactive()
                .setId(elementId);
        final String testData = "testData";

        ArgumentCaptor<TestEvaluationRequest> requestArgumentCaptor = ArgumentCaptor.forClass(TestEvaluationRequest.class);

        when(interactiveService.findById(elementId)).thenReturn(Mono.just(interactive));
        when(evaluationSubmitService.submit(any(TestEvaluationRequest.class), eq(TestEvaluationResponse.class)))
                .thenReturn(Mono.just(new TestEvaluationResponse()));

        final TestEvaluationResponse result = walkableService.evaluate(elementId, CoursewareElementType.INTERACTIVE, testData)
                .block();

        assertNotNull(result);

        verify(evaluationSubmitService).submit(requestArgumentCaptor.capture(), eq(TestEvaluationResponse.class));

        final TestEvaluationRequest request = requestArgumentCaptor.getValue();

        assertNotNull(request);
        assertEquals(testData, request.getData());
        assertEquals(interactive, request.getWalkable());
        assertNotNull(request.getScenarioLifecycle());
        assertEquals(EvaluationRequest.Type.TEST, request.getType());
    }
}