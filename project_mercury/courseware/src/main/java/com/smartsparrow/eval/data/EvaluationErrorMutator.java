package com.smartsparrow.eval.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class EvaluationErrorMutator extends SimpleTableMutator<EvaluationError> {

    @Override
    public String getUpsertQuery(final EvaluationError mutation) {
        return "INSERT INTO learner.evaluation_error_by_evaluation_id (" +
                "evaluation_id" +
                ", type" +
                ", id" +
                ", occurred_at" +
                ", error" +
                ", stacktrace" +
                ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final EvaluationError mutation) {
        stmt.bind(
                mutation.getEvaluationId(),
                Enums.asString(mutation.getType()),
                mutation.getId(),
                mutation.getOccurredAt(),
                mutation.getError(),
                mutation.getStacktrace()
        );
    }
}
