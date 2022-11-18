package com.smartsparrow.courseware.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.data.AnnotationGateway;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.WalkableChild;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AnnotationMoveServiceTest {

    @InjectMocks
    private AnnotationMoveService annotationMoveService;
    @Mock
    private ActivityService activityService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private AnnotationService annotationService;
    @Mock
    private AnnotationGateway annotationGateway;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final UUID newRootElementId = UUID.randomUUID();
    private static final UUID oldRootElementId = UUID.randomUUID();

    private final CoursewareAnnotation annotation = new CoursewareAnnotation()
            .setId(annotationId)
            .setElementId(interactiveId)
            .setRootElementId(activityId)
            .setMotivation(Motivation.classifying);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        WalkableChild walkableChild = new WalkableChild().setElementId(interactiveId).setElementType(INTERACTIVE);
        when(activityService.findChildPathwayIds(any())).thenReturn(Mono.just(Collections.singletonList(pathwayId)));
        when(pathwayService.getOrderedWalkableChildren(any())).thenReturn(Mono.just(Arrays.asList(walkableChild)));
        when(annotationService.moveAnnotations(any(), any(), any())).thenReturn(Flux.empty());
    }


    @Test
    void moveAnnotations() {
        annotationMoveService.moveAnnotations(activityId, oldRootElementId, newRootElementId).singleOrEmpty().block();
        verify(annotationService).moveAnnotations(any(), any(), any());
    }

    @Test
    void moveAnnotationsNewRootElementIdNull() {

        Throwable e = assertThrows(IllegalArgumentException.class,
                                   () -> annotationMoveService.moveAnnotations(activityId, oldRootElementId, null));
        assertEquals(e.getMessage(), "missing new root element id");
    }

    @Test
    void moveAnnotationsOldRootElementIdNull() {

        Throwable e = assertThrows(IllegalArgumentException.class,
                                   () -> annotationMoveService.moveAnnotations(activityId, null, newRootElementId));
        assertEquals(e.getMessage(), "missing old root element id");
    }

    @Test
    void moveAnnotationsActivityElementIdNull() {

        Throwable e = assertThrows(IllegalArgumentException.class,
                                   () -> annotationMoveService.moveAnnotations(null, oldRootElementId, newRootElementId));
        assertEquals(e.getMessage(), "missing activity id");
    }
}
