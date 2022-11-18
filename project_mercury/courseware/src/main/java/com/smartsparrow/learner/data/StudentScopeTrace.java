package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class StudentScopeTrace {

    private UUID deploymentId;
    private UUID scopeId;
    private UUID studentId;
    private UUID studentScopeUrn;
    private UUID rootId;
    private UUID elementId;
    private CoursewareElementType elementType;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public StudentScopeTrace setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getScopeId() {
        return scopeId;
    }

    public StudentScopeTrace setScopeId(UUID scopeId) {
        this.scopeId = scopeId;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public StudentScopeTrace setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getStudentScopeUrn() {
        return studentScopeUrn;
    }

    public StudentScopeTrace setStudentScopeUrn(UUID studentScopeUrn) {
        this.studentScopeUrn = studentScopeUrn;
        return this;
    }

    public UUID getRootId() {
        return rootId;
    }

    public StudentScopeTrace setRootId(UUID rootId) {
        this.rootId = rootId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public StudentScopeTrace setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public StudentScopeTrace setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeTrace that = (StudentScopeTrace) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(scopeId, that.scopeId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(studentScopeUrn, that.studentScopeUrn) &&
                Objects.equals(rootId, that.rootId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, scopeId, studentId, studentScopeUrn, rootId, elementId, elementType);
    }

    @Override
    public String toString() {
        return "StudentScopeTrace{" +
                "deploymentId=" + deploymentId +
                ", scopeId=" + scopeId +
                ", studentId=" + studentId +
                ", studentScopeUrn=" + studentScopeUrn +
                ", rootId=" + rootId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
