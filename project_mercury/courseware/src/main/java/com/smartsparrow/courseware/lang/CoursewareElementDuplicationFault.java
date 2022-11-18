package com.smartsparrow.courseware.lang;

import com.smartsparrow.exception.Fault;

public class CoursewareElementDuplicationFault extends Fault {

    public CoursewareElementDuplicationFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 422;
    }

    @Override
    public String getType() {
        return "UNPROCESSABLE_ENTITY";
    }
}
