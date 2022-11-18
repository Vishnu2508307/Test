package com.smartsparrow.eval.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class EvaluationErrorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public EvaluationErrorMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findBy(final UUID evaluationId) {
        final String SELECT = "SELECT" +
                " evaluation_id" +
                ", type" +
                ", id" +
                ", occurred_at" +
                ", error" +
                ", stacktrace" +
                " FROM learner.evaluation_error_by_evaluation_id" +
                " WHERE evaluation_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(evaluationId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public EvaluationError fromRow(Row row) {
        return new EvaluationError()
                .setId(row.getUUID("id"))
                .setEvaluationId(row.getUUID("evaluation_id"))
                .setOccurredAt(row.getString("occurred_at"))
                .setError(row.getString("error"))
                .setStacktrace(row.getString("stacktrace"))
                .setType(Enums.of(EvaluationError.Type.class, row.getString("type")));
    }
}
