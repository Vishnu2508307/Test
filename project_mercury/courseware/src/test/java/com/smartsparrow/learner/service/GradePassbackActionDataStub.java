package com.smartsparrow.learner.service;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.GradePassbackActionContext;
import com.smartsparrow.eval.parser.LiteralContext;

import java.util.UUID;

public class GradePassbackActionDataStub {

    public static GradePassbackAction buildGradePassbackAction(Double value, UUID elementId, CoursewareElementType elementType) {

        GradePassbackActionContext gradePassbackActionContext = new GradePassbackActionContext()
                .setValue(value)
                .setElementId(elementId)
                .setElementType(elementType);

        return new GradePassbackAction()
                .setResolver(new LiteralContext())
                .setContext(gradePassbackActionContext)
                .setResolvedValue(value);
    }

}
