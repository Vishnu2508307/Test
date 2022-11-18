package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.eval.mutation.MutationOperator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
public class ManualGradeArg {

    private UUID studentId;
    private Double score;
    private MutationOperator operator;
    private UUID attemptId;

    @GraphQLInputField(name = "studentId", description = "the id of the student to assign the manual grade to")
    public UUID getStudentId() {
        return studentId;
    }

    @GraphQLInputField(name = "score", description = "the manual grade score value")
    public Double getScore() {
        return score;
    }

    @GraphQLInputField(name = "operator", description = "the manual grade score operator")
    public MutationOperator getOperator() {
        return operator;
    }

    @GraphQLInputField(name = "attemptId", description = "the attempt the manual grade refers to")
    public UUID getAttemptId() {
        return attemptId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualGradeArg that = (ManualGradeArg) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(score, that.score) &&
                operator == that.operator &&
                Objects.equals(attemptId, that.attemptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, score, operator, attemptId);
    }

    @Override
    public String toString() {
        return "ManualGradeArg{" +
                "studentId=" + studentId +
                ", score=" + score +
                ", operator=" + operator +
                ", attemptId=" + attemptId +
                '}';
    }
}
