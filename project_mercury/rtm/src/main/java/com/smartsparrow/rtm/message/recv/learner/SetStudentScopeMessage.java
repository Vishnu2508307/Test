package com.smartsparrow.rtm.message.recv.learner;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SetStudentScopeMessage extends ReceivedMessage {

    private UUID sourceId;
    private UUID deploymentId;
    private UUID studentScopeURN;
    private String data;
    private UUID timeId;

    public UUID getSourceId() {
        return sourceId;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public String getData() {
        return data;
    }

    public UUID getTimeId() {
        return timeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetStudentScopeMessage that = (SetStudentScopeMessage) o;
        return Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(studentScopeURN, that.studentScopeURN) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, deploymentId, studentScopeURN, data);
    }

    @Override
    public String toString() {
        return "SetStudentScopeMessage{" +
                "sourceId=" + sourceId +
                ", deploymentId=" + deploymentId +
                ", studentScopeURN=" + studentScopeURN +
                ", data='" + data + '\'' +
                "} " + super.toString();
    }
}
