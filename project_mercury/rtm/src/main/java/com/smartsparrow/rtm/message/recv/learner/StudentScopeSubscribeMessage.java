package com.smartsparrow.rtm.message.recv.learner;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class StudentScopeSubscribeMessage extends ReceivedMessage {

    private UUID deploymentId;

    @Deprecated
    private UUID studentScopeURN;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    // TODO to be removed
    @Deprecated
    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeSubscribeMessage that = (StudentScopeSubscribeMessage) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(studentScopeURN, that.studentScopeURN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, studentScopeURN);
    }

    @Override
    public String toString() {
        return "ProgressSubscribeMessage{" +
                "deploymentId=" + deploymentId +
                ", studentScopeURN=" + studentScopeURN +
                "} " + super.toString();
    }
}
