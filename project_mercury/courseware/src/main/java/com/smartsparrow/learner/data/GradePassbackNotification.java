package com.smartsparrow.learner.data;

import com.smartsparrow.courseware.data.CoursewareElementType;

import java.util.Objects;
import java.util.UUID;

public class GradePassbackNotification {

    public enum Status {
        SENDING,
        SUCCESS,
        FAILURE
    }

    private UUID notificationId;
    private UUID deploymentId;
    private UUID changeId;
    private UUID studentId;
    private UUID coursewareElementId;
    private CoursewareElementType coursewareElementType;
    private double resultScore;
    private Status status;
    private UUID completedAt;

    public UUID getNotificationId() {
        return notificationId;
    }

    public GradePassbackNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public GradePassbackNotification setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public GradePassbackNotification setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public GradePassbackNotification setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public GradePassbackNotification setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public GradePassbackNotification setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    public double getResultScore() {
        return resultScore;
    }

    public GradePassbackNotification setResultScore(double resultScore) {
        this.resultScore = resultScore;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public GradePassbackNotification setStatus(Status status) {
        this.status = status;
        return this;
    }

    public UUID getCompletedAt() {
        return completedAt;
    }

    public GradePassbackNotification setCompletedAt(UUID completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradePassbackNotification that = (GradePassbackNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                Objects.equals(coursewareElementType, that.coursewareElementType) &&
                Objects.equals(resultScore, that.resultScore) &&
                status == that.status &&
                Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, deploymentId, changeId, studentId, coursewareElementId, coursewareElementType,
                resultScore, status, completedAt);
    }

    @Override
    public String toString() {
        return "GradePassbackNotification{" +
                "notificationId=" + notificationId +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", studentId=" + studentId +
                ", coursewareElementId=" + coursewareElementId +
                ", coursewareElementType=" + coursewareElementType +
                ", resultScore=" + resultScore +
                ", status=" + status +
                ", completedAt=" + completedAt +
                '}';
    }
}
