package com.smartsparrow.courseware.service;

import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementDescriptionGateway;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class CoursewareElementDescriptionServiceTest {

    private static final UUID elementId = UUID.randomUUID();
    private final CoursewareElementType elementType = CoursewareElementType.INTERACTIVE;
    private static final String description = "";
    @InjectMocks
    CoursewareElementDescriptionService coursewareElementDescriptionService;
    @Mock
    CoursewareElementDescriptionGateway coursewareElementDescriptionGateway;
    CoursewareElementDescription coursewareElementDescription;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        coursewareElementDescription = new CoursewareElementDescription()
                .setElementId(elementId)
                .setElementType(elementType)
                .setValue(description);

        when(coursewareElementDescriptionGateway.persist(coursewareElementDescription)).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void createCoursewareElementDescription_noElementId() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> coursewareElementDescriptionService.createCoursewareElementDescription(
                        null,
                        elementType,
                        description
                ));

        assertEquals("elementId is required", fault.getMessage());
    }

    @Test
    void createCoursewareElementDescription_noOnElement() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> coursewareElementDescriptionService.createCoursewareElementDescription(
                        elementId,
                        null,
                        description
                ));

        assertEquals("elementType is required", fault.getMessage());
    }

    @Test
    void createCoursewareElementDescription_nullDescription() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> coursewareElementDescriptionService.createCoursewareElementDescription(
                        elementId,
                        elementType,
                        null
                ));

        assertEquals("description is required", fault.getMessage());
    }

    @Test
    void createCoursewareElementDescription() {
        CoursewareElementDescription created = coursewareElementDescriptionService.createCoursewareElementDescription(
                elementId,
                elementType,
                description
        ).block();

        assertNotNull(created);
        assertEquals(elementId, created.getElementId());
        assertEquals(elementType, created.getElementType());
        assertEquals(description, created.getValue());
    }

    @Test
    void fetchCoursewareDescriptionByElement_noElementId() {
        IllegalArgumentFault ex =
                assertThrows(IllegalArgumentFault.class, () -> coursewareElementDescriptionService
                        .fetchCoursewareDescriptionByElement(null));

        assertEquals("elementId is required", ex.getMessage());
    }
}
