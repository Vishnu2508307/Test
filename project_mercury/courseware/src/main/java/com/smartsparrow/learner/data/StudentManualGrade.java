package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.eval.mutation.MutationOperator;

public class StudentManualGrade {

    private UUID id;
    private Double score;
    private MutationOperator operator;
    private UUID instructorId;
    private String createdAt;

    public UUID getId() {
        return id;
    }

    public StudentManualGrade setId(UUID id) {
        this.id = id;
        return this;
    }

    public Double getScore() {
        return score;
    }

    public StudentManualGrade setScore(Double score) {
        this.score = score;
        return this;
    }

    public MutationOperator getOperator() {
        return operator;
    }

    public StudentManualGrade setOperator(MutationOperator operator) {
        this.operator = operator;
        return this;
    }

    public UUID getInstructorId() {
        return instructorId;
    }

    public StudentManualGrade setInstructorId(UUID instructorId) {
        this.instructorId = instructorId;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public StudentManualGrade setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentManualGrade that = (StudentManualGrade) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(score, that.score) &&
                operator == that.operator &&
                Objects.equals(instructorId, that.instructorId) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, score, operator, instructorId, createdAt);
    }

    @Override
    public String toString() {
        return "StudentManualGrade{" +
                "id=" + id +
                ", score=" + score +
                ", operator=" + operator +
                ", instructorId=" + instructorId +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
