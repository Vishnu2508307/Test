package com.smartsparrow.rtm.message.recv.workspace;

import java.util.UUID;

import com.google.common.base.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProjectChangeLogListMessage extends ProjectGenericMessage {

    private UUID projectId;
    private Integer limit;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public Integer getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectChangeLogListMessage that = (ProjectChangeLogListMessage) o;
        return Objects.equal(projectId, that.projectId) &&
                Objects.equal(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectId, limit);
    }

    @Override
    public String toString() {
        return "ProjectChangeLogListMessage{" +
                "projectId=" + projectId +
                ", limit=" + limit +
                "} " + super.toString();
    }
}
