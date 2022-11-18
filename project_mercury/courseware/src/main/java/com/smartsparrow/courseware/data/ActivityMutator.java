package com.smartsparrow.courseware.data;

import static com.smartsparrow.dse.api.Mutators.bindNonNull;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class ActivityMutator extends SimpleTableMutator<Activity> {

    @Override
    public String getUpsertQuery(Activity mutation) {
        // @formatter:off
        return "INSERT INTO courseware.activity ("
                + "  id"
                + ", plugin_id"
                + ", plugin_version_expr"
                + ", creator_id"
                + ", student_scope_urn"
                + ", evaluation_mode"
                + ") VALUES ( ?, ?, ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Activity mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getPluginId());
        stmt.setString(2, mutation.getPluginVersionExpr());
        stmt.setUUID(3, mutation.getCreatorId());
        stmt.setUUID(4, mutation.getStudentScopeURN());
        bindNonNull(stmt, 5, mutation.getEvaluationMode());
    }

    public Statement updateEvaluationMode(final UUID activityId, final EvaluationMode evaluationMode) {
        final String QUERY = "UPDATE" +
                " courseware.activity" +
                " SET evaluation_mode = ?" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Enums.asString(evaluationMode), activityId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
