package com.smartsparrow.pubsub.subscriptions.studentprogress;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class StudentProgressBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = 6742519178389493358L;
    private UUID studentId;
    private UUID coursewareElementId;
    private UUID deploymentId;
    private Object progress;


    public UUID getStudentId() {
        return studentId;
    }

    public StudentProgressBroadcastMessage setStudentId(final UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public StudentProgressBroadcastMessage setCoursewareElementId(final UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public StudentProgressBroadcastMessage setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public Object getProgress() {
        return progress;
    }

    public StudentProgressBroadcastMessage setProgress(final Object progress) {
        this.progress = progress;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentProgressBroadcastMessage that = (StudentProgressBroadcastMessage) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(progress, that.progress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, coursewareElementId, deploymentId, progress);
    }

    @Override
    public String toString() {
        return "StudentProgressBroadcastMessage{" +
                "studentId=" + studentId +
                ", coursewareElementId=" + coursewareElementId +
                ", deploymentId=" + deploymentId +
                ", progress=" + progress +
                '}';
    }
}
