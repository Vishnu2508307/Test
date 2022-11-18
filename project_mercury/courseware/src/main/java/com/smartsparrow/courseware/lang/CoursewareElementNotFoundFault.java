package com.smartsparrow.courseware.lang;

import com.smartsparrow.exception.Fault;

public class CoursewareElementNotFoundFault extends Fault {
    /**
     * Construct a new object with the specified message.
     *
     * @param message the exception message
     */
    public CoursewareElementNotFoundFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 404;
    }

    @Override
    public String getType() {
        return "NOT_FOUND";
    }
}
