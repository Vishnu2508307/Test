package com.smartsparrow.learner.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class StudentScopeEntry implements Serializable {

    private static final long serialVersionUID = -148695619160507308L;
    private UUID id;
    private UUID scopeId;
    private UUID sourceId;
    private String data;

    public UUID getScopeId() {
        return scopeId;
    }

    public StudentScopeEntry setScopeId(UUID scopeId) {
        this.scopeId = scopeId;
        return this;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public StudentScopeEntry setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public StudentScopeEntry setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getData() {
        return data;
    }

    public StudentScopeEntry setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeEntry that = (StudentScopeEntry) o;
        return Objects.equals(scopeId, that.scopeId) &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(id, that.id) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopeId, sourceId, id, data);
    }

    @Override
    public String toString() {
        return "StudentScopeEntry{" +
                "scopeId=" + scopeId +
                ", sourceId=" + sourceId +
                ", id=" + id +
                ", data='" + data + '\'' +
                '}';
    }
}
