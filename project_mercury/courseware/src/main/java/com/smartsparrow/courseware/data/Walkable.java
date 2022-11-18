package com.smartsparrow.courseware.data;

import java.io.Serializable;
import java.util.UUID;

import com.smartsparrow.eval.data.Evaluable;
import com.smartsparrow.learner.data.Element;

/**
 * This interface dictates objects which can be used within a Pathway.
 */
public interface Walkable extends Element, Evaluable,  Serializable {

    UUID getStudentScopeURN();

    /**
     * Deprecated, use {@link Element#getElementType()} instead.
     * Keeping for now for front end backwards compatibility.
     */
    @Deprecated
    default CoursewareElementType getCoursewareElementType() {
        return getElementType();
    }

}
