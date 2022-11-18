package com.smartsparrow.learner.event;

import java.util.List;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.learner.data.EvaluationResult;

public class EvaluationEventMessageDataStub {

    public static EvaluationEventMessage evaluationEventMessage(EvaluationResult evaluationResult,
            List<CoursewareElement> ancestryList, UUID clientId, UUID studentId) {

        return new EvaluationEventMessage()
                .setEvaluationResult(evaluationResult)
                .setAncestryList(ancestryList)
                .setStudentId(clientId)
                .setStudentId(studentId);
    }

}
