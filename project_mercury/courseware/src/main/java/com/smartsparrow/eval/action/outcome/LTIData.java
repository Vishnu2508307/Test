package com.smartsparrow.eval.action.outcome;

import java.util.Objects;

public class LTIData {

    private String userId;
    private Integer assignmentId;
    private String courseId;
    private Integer attemptLimit;
    private String customGradingMethod;
    private String discipline;
    private String role;

    public String getUserId() {
        return userId;
    }

    public LTIData setUserId(final String userId) {
        this.userId = userId;
        return this;
    }

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public LTIData setAssignmentId(final Integer assignmentId) {
        this.assignmentId = assignmentId;
        return this;
    }

    public String getCourseId() {
        return courseId;
    }

    public LTIData setCourseId(final String courseId) {
        this.courseId = courseId;
        return this;
    }

    public Integer getAttemptLimit() {
        return attemptLimit;
    }

    public LTIData setAttemptLimit(final Integer attemptLimit) {
        this.attemptLimit = attemptLimit;
        return this;
    }

    public String getCustomGradingMethod() {
        return customGradingMethod;
    }

    public LTIData setCustomGradingMethod(final String customGradingMethod) {
        this.customGradingMethod = customGradingMethod;
        return this;
    }

    public String getDiscipline() {
        return discipline;
    }

    public LTIData setDiscipline(final String discipline) {
        this.discipline = discipline;
        return this;
    }

    public String getRole() {
        return role;
    }

    public LTIData setRole(final String role) {
        this.role = role;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIData ltiData = (LTIData) o;
        return Objects.equals(userId, ltiData.userId)
                && Objects.equals(assignmentId, ltiData.assignmentId)
                && Objects.equals(courseId, ltiData.courseId)
                && Objects.equals(attemptLimit, ltiData.attemptLimit)
                && Objects.equals(customGradingMethod, ltiData.customGradingMethod)
                && Objects.equals(discipline, ltiData.discipline)
                && Objects.equals(role, ltiData.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, assignmentId, courseId, attemptLimit, customGradingMethod, discipline, role);
    }

    @Override
    public String toString() {
        return "LTIData{" +
                "userId='" + userId + '\'' +
                ", assignmentId=" + assignmentId +
                ", courseId='" + courseId + '\'' +
                ", attemptLimit='" + attemptLimit + '\'' +
                ", customGradingMethod='" + customGradingMethod + '\'' +
                ", discipline='" + discipline + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
