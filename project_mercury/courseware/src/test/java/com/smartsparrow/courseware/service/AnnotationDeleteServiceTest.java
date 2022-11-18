package com.smartsparrow.courseware.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AnnotationDeleteServiceTest {

    @InjectMocks
    private AnnotationDeleteService annotationDeleteService;
    @Mock
    private ActivityService activityService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private AnnotationService annotationService;
    @Mock
    private ComponentService componentService;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        WalkableChild walkableChild = new WalkableChild().setElementId(interactiveId).setElementType(INTERACTIVE);
        when(activityService.findChildPathwayIds(elementId)).thenReturn(Mono.just(Arrays.asList(pathwayId)));
        when(annotationService.deleteAnnotation(any(), any())).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(any())).thenReturn(Flux.just(componentId));
        when(pathwayService.getOrderedWalkableChildren(any())).thenReturn(Mono.just(Arrays.asList(walkableChild)));
    }


    @Test
    void deleteActivityAnnotations() {
        annotationDeleteService.deleteAnnotations(elementId, CoursewareElementType.ACTIVITY, rootElementId).singleOrEmpty().block();
        verify(annotationService, times(3)).deleteAnnotation(any(), any());
    }

    @Test
    void deleteActivityAnnotationElementAsNull() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                                   () -> annotationDeleteService.deleteAnnotations(null, CoursewareElementType.ACTIVITY,  rootElementId));
        assertEquals(e.getMessage(), "missing element id");
    }

    @Test
    void deleteActivityAnnotationElementTypeAsNull() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                                   () -> annotationDeleteService.deleteAnnotations(elementId, null, rootElementId));
        assertEquals(e.getMessage(), "missing element type");
    }

    @Test
    void deleteActivityAnnotationRootElementIdAsNull() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                                   () -> annotationDeleteService.deleteAnnotations(elementId, CoursewareElementType.ACTIVITY, null));
        assertEquals(e.getMessage(), "missing root element id");
    }

}
