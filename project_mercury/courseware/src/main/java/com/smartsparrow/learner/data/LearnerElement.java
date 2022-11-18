package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElementType;

import io.leangen.graphql.annotations.GraphQLIgnore;

public interface LearnerElement extends Element {

    @GraphQLIgnore
    UUID getDeploymentId();

    @GraphQLIgnore
    UUID getChangeId();

    CoursewareElementType getElementType();

    /**
     * @return the learner element config or null when the element has no config
     */
    @Nullable
    String getConfig();

}
