package com.smartsparrow.learner.attempt;

import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class Attempt {

    private UUID id;
    private UUID parentId;

    //
    private UUID deploymentId;
    private UUID coursewareElementId;
    private CoursewareElementType coursewareElementType;
    private UUID studentId;

    //
    private Integer value;

    public Attempt() {
    }

    public UUID getId() {
        return id;
    }

    public Attempt setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public Attempt setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    @GraphQLIgnore
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public Attempt setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @GraphQLIgnore
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public Attempt setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    @GraphQLIgnore
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public Attempt setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @GraphQLIgnore
    public UUID getStudentId() {
        return studentId;
    }

    public Attempt setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public Integer getValue() {
        return value;
    }

    public Attempt setValue(Integer value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Attempt attempt = (Attempt) o;
        return Objects.equal(id, attempt.id) && Objects.equal(parentId, attempt.parentId) && Objects.equal(deploymentId,
                                                                                                           attempt.deploymentId)
                && Objects.equal(coursewareElementId, attempt.coursewareElementId)
                && coursewareElementType == attempt.coursewareElementType && Objects.equal(studentId, attempt.studentId)
                && Objects.equal(value, attempt.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, parentId, deploymentId, coursewareElementId, coursewareElementType, studentId,
                                value);
    }

    @Override
    public String toString() {
        return "Attempt{" + "id=" + id + ", parentId=" + parentId + ", deploymentId=" + deploymentId
                + ", coursewareElementId=" + coursewareElementId + ", coursewareElementType=" + coursewareElementType
                + ", studentId=" + studentId + ", value=" + value + '}';
    }
}
