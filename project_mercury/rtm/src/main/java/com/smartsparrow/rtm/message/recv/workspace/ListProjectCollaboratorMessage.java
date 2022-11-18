package com.smartsparrow.rtm.message.recv.workspace;

import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListProjectCollaboratorMessage extends ReceivedMessage implements ProjectMessage {

    private UUID projectId;
    private Integer limit;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public ListProjectCollaboratorMessage setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListProjectCollaboratorMessage setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListProjectCollaboratorMessage that = (ListProjectCollaboratorMessage) o;
        return Objects.equal(projectId, that.projectId) &&
                Objects.equal(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectId, limit);
    }

    @Override
    public String toString() {
        return "ListProjectCollaboratorMessage{" +
                "projectId=" + projectId +
                ", limit=" + limit +
                "} " + super.toString();
    }
}
