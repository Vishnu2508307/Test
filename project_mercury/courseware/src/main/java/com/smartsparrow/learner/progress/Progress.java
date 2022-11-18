package com.smartsparrow.learner.progress;

import java.io.Serializable;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

import io.leangen.graphql.annotations.GraphQLIgnore;

/**
 * Minimum fields required for Progress tracking.
 */
public interface Progress extends Serializable {

    /**
     * The ID of the progress
     *
     * @return a time based UUID for the creation of the Progress
     */
    UUID getId();

    /**
     * The ID of the deployment
     *
     * @return a time based UUID linking to the underlying deployment
     */
    @GraphQLIgnore
    UUID getDeploymentId();

    /**
     * The ID of the deployment's change
     *
     * @return a time based UUID of the change
     */
    @GraphQLIgnore
    UUID getChangeId();

    /**
     * The ID of the element in which Progress tracking is occuring on, e.g. the Activity or Interactive ID.
     *
     * @return a time based UUID on which to track the Progress
     */
    @GraphQLIgnore
    UUID getCoursewareElementId();

    /**
     * The type of the courseware element
     *
     * @return the type of the courseware element
     */
    @GraphQLIgnore
    CoursewareElementType getCoursewareElementType();

    /**
     * The ID of the student
     *
     * @return a time based UUID of the Student.
     */
    @GraphQLIgnore
    UUID getStudentId();

    /**
     * The ID associated with the attempt
     *
     * @return a time based UUID suuplied by the underlying attempt data
     */
    UUID getAttemptId();

    /**
     * The ID associated with the evaluation
     *
     * @return a time based UUID supplied by the underlying evaluation operation
     */
    UUID getEvaluationId();

    /**
     * The completion data
     *
     * @return an object representing the Progress completion
     */
    Completion getCompletion();

}
