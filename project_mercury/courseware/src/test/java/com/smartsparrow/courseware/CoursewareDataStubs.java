package com.smartsparrow.courseware;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerWalkable;

public class CoursewareDataStubs {

    public static final UUID ELEMENT_ID = UUID.randomUUID();

    public static LearnerWalkable mockLearnerWalkable(final CoursewareElementType elementType) {
        LearnerWalkable walkable = mock(LearnerWalkable.class);
        when(walkable.getId()).thenReturn(ELEMENT_ID);
        when(walkable.getElementType()).thenReturn(elementType);
        return walkable;
    }

    public static CoursewareElement mockCoursewareElement(final UUID elementId, final CoursewareElementType type) {
        CoursewareElement element = mock(CoursewareElement.class);
        when(element.getElementId()).thenReturn(elementId);
        when(element.getElementType()).thenReturn(type);
        return element;
    }

    public static ConfigurationField buildConfigurationField(String fieldName, String fieldValue) {
        return new ConfigurationField()
                .setFieldName(fieldName)
                .setFieldValue(fieldValue);
    }

    public static LearnerActivity buildLearnerActivity(UUID activityId, UUID deploymentId, UUID changeId) {
        return new LearnerActivity()
                .setChangeId(changeId)
                .setDeploymentId(deploymentId)
                .setId(activityId);
    }
}
