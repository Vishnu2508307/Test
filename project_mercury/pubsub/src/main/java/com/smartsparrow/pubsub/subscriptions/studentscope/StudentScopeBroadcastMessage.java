package com.smartsparrow.pubsub.subscriptions.studentscope;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class StudentScopeBroadcastMessage implements BroadcastMessage {

    private UUID deploymentId;
    private UUID studentId;
    private UUID studentScopeUrn;
    private Object studentScopeEntry;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public StudentScopeBroadcastMessage setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public StudentScopeBroadcastMessage setStudentId(final UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getStudentScopeUrn() {
        return studentScopeUrn;
    }

    public StudentScopeBroadcastMessage setStudentScopeUrn(final UUID studentScopeUrn) {
        this.studentScopeUrn = studentScopeUrn;
        return this;
    }

    public Object getStudentScopeEntry() {
        return studentScopeEntry;
    }

    public StudentScopeBroadcastMessage setStudentScopeEntry(final Object studentScopeEntry) {
        this.studentScopeEntry = studentScopeEntry;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeBroadcastMessage that = (StudentScopeBroadcastMessage) o;
        return Objects.equals(deploymentId, that.deploymentId) && Objects.equals(studentId,
                                                                                 that.studentId) && Objects.equals(
                studentScopeUrn,
                that.studentScopeUrn) && Objects.equals(studentScopeEntry, that.studentScopeEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, studentId, studentScopeUrn, studentScopeEntry);
    }

    @Override
    public String toString() {
        return "StudentScopeBroadcastMessage{" +
                "deploymentId=" + deploymentId +
                ", studentId=" + studentId +
                ", studentScopeUrn=" + studentScopeUrn +
                ", studentScopeEntry=" + studentScopeEntry +
                '}';
    }
}
