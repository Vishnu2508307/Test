package com.smartsparrow.sso.data.ltiv11;

import java.util.Objects;
import java.util.UUID;

public class LTIv11ConsumerConfiguration {

    private UUID id;
    private UUID workspaceId;
    private String comment;

    public UUID getId() {
        return id;
    }

    public LTIv11ConsumerConfiguration setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public LTIv11ConsumerConfiguration setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public LTIv11ConsumerConfiguration setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIv11ConsumerConfiguration that = (LTIv11ConsumerConfiguration) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workspaceId, comment);
    }

    @Override
    public String toString() {
        return "LTIv11ConsumerConfiguration{" +
                "id=" + id +
                ", workspaceId=" + workspaceId +
                ", comment='" + comment + '\'' +
                '}';
    }
}
