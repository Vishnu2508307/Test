package com.smartsparrow.learner.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class StudentManualGradeReport {

    private UUID deploymentId;
    private UUID studentId;
    private UUID componentId;
    private UUID attemptId;
    private ScoreReason state;
    private UUID parentId;
    private CoursewareElementType parentType;
    private List<StudentManualGrade> grades = new ArrayList<>();

    @GraphQLIgnore
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public StudentManualGradeReport setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public StudentManualGradeReport setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public StudentManualGradeReport setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public StudentManualGradeReport setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public ScoreReason getState() {
        return state;
    }

    public StudentManualGradeReport setState(ScoreReason state) {
        this.state = state;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public StudentManualGradeReport setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public StudentManualGradeReport setParentType(CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    public List<StudentManualGrade> getGrades() {
        return grades;
    }

    public StudentManualGradeReport setGrades(List<StudentManualGrade> grades) {
        this.grades = grades;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentManualGradeReport that = (StudentManualGradeReport) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(componentId, that.componentId) &&
                Objects.equals(attemptId, that.attemptId) &&
                state == that.state &&
                Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType &&
                Objects.equals(grades, that.grades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, studentId, componentId, attemptId, state, parentId, parentType, grades);
    }

    @Override
    public String toString() {
        return "StudentManualGradeReport{" +
                "deploymentId=" + deploymentId +
                ", studentId=" + studentId +
                ", componentId=" + componentId +
                ", attemptId=" + attemptId +
                ", state=" + state +
                ", parentId=" + parentId +
                ", parentType=" + parentType +
                ", grades=" + grades +
                '}';
    }
}
