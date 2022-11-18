package com.smartsparrow.learner.event;

import java.util.List;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.learner.attempt.Attempt;

/**
 * this is an interface to implement by event messages which want to trigger progress recalculation
 */
public interface UpdateProgressMessage {

    UUID getStudentId();
    String getProducingClientId();
    List<CoursewareElement> getAncestryList();
    UUID getAttemptId();
    Attempt getAttempt();
    UUID getChangeId();
    UUID getDeploymentId();
    UUID getEvaluationId();

}
