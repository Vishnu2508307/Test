package com.smartsparrow.eval.data;

import java.util.Objects;
import java.util.UUID;

public class EvaluationError {

    public enum Type {
        GENERIC,
    }

    private UUID evaluationId;
    private Type type;
    private UUID id;
    private String occurredAt;
    private String error;
    private String stacktrace;

    public UUID getEvaluationId() {
        return evaluationId;
    }

    public EvaluationError setEvaluationId(final UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    public Type getType() {
        return type;
    }

    public EvaluationError setType(final Type type) {
        this.type = type;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public EvaluationError setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public EvaluationError setOccurredAt(final String occurredAt) {
        this.occurredAt = occurredAt;
        return this;
    }

    public String getError() {
        return error;
    }

    public EvaluationError setError(final String error) {
        this.error = error;
        return this;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public EvaluationError setStacktrace(final String stacktrace) {
        this.stacktrace = stacktrace;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationError that = (EvaluationError) o;
        return Objects.equals(evaluationId, that.evaluationId) &&
                type == that.type &&
                Objects.equals(id, that.id) &&
                Objects.equals(occurredAt, that.occurredAt) &&
                Objects.equals(error, that.error) &&
                Objects.equals(stacktrace, that.stacktrace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationId, type, id, occurredAt, error, stacktrace);
    }

    @Override
    public String toString() {
        return "EvaluationError{" +
                "evaluationId=" + evaluationId +
                ", type=" + type +
                ", id=" + id +
                ", timestamp='" + occurredAt + '\'' +
                ", error='" + error + '\'' +
                ", stacktrace='" + stacktrace + '\'' +
                '}';
    }
}
