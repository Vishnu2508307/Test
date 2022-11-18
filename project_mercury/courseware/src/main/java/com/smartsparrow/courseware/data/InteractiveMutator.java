package com.smartsparrow.courseware.data;

import static com.smartsparrow.dse.api.Mutators.bindNonNull;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class InteractiveMutator extends SimpleTableMutator<Interactive> {

    @Override
    public String getUpsertQuery(Interactive mutation) {
        // @formatter:off
        return "INSERT INTO courseware.interactive ("
                + "  id"
                + ", plugin_id"
                + ", plugin_version_expr"
                + ", student_scope_urn"
                + ", evaluation_mode"
                + ") VALUES ( ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Interactive mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getPluginId(),
                mutation.getPluginVersionExpr(),
                mutation.getStudentScopeURN()
        );

        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getPluginId());
        stmt.setString(2, mutation.getPluginVersionExpr());
        stmt.setUUID(3, mutation.getStudentScopeURN());
        bindNonNull(stmt, 4, mutation.getEvaluationMode());
    }

    public Statement updateEvaluationMode(final UUID interactiveId, final EvaluationMode evaluationMode) {
        final String QUERY = "UPDATE" +
                " courseware.interactive" +
                " SET evaluation_mode = ?" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Enums.asString(evaluationMode), interactiveId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
