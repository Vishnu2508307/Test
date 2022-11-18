package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LatestStudentScopeEntryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LatestStudentScopeEntryMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByScope(UUID scopeId) {
        // @formatter:off
        final String SELECT = "SELECT "
                + "  source_id"
                + ", data"
                + " FROM learner.latest_student_scope_entry_by_source"
                + " WHERE scope_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(scopeId);
        return stmt;
    }

    public StudentScopeData fromRow(Row row) {
        return new StudentScopeData()
                .setSourceId(row.getUUID("source_id"))
                .setData(row.getString("data"));
    }
}
