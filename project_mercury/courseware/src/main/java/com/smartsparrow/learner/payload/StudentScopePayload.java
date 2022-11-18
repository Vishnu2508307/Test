package com.smartsparrow.learner.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.learner.data.StudentScopeEntry;

public class StudentScopePayload {

    private UUID sourceId;
    private UUID scopeURN;
    private String data;

    public static StudentScopePayload from(@Nonnull StudentScopeEntry entry, UUID scopeURN) {
        StudentScopePayload payload = new StudentScopePayload();
        payload.sourceId = entry.getSourceId();
        payload.data = entry.getData();
        payload.scopeURN = scopeURN;
        return payload;
    }
    public static StudentScopePayload from(@Nonnull UUID sourceId, @Nonnull UUID scopeURN, @Nonnull String data) {
        StudentScopePayload payload = new StudentScopePayload();
        payload.sourceId = sourceId;
        payload.data = data;
        payload.scopeURN = scopeURN;
        return payload;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public UUID getScopeURN() {
        return scopeURN;
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopePayload that = (StudentScopePayload) o;
        return Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(scopeURN, that.scopeURN) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, scopeURN, data);
    }

    @Override
    public String toString() {
        return "StudentScopePayload{" +
                "sourceId=" + sourceId +
                ", scopeURN=" + scopeURN +
                ", data='" + data + '\'' +
                '}';
    }
}
