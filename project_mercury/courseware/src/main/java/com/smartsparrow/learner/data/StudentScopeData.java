package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

public class StudentScopeData {

    private UUID sourceId;
    private String data;

    public UUID getSourceId() {
        return sourceId;
    }

    public StudentScopeData setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getData() {
        return data;
    }

    public StudentScopeData setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeData that = (StudentScopeData) o;
        return Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, data);
    }

    @Override
    public String toString() {
        return "StudentScopeData{" +
                "sourceId=" + sourceId +
                ", data='" + data + '\'' +
                '}';
    }
}
