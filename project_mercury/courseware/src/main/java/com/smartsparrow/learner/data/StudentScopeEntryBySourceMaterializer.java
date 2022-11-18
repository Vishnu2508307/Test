package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class StudentScopeEntryBySourceMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public StudentScopeEntryBySourceMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatest(UUID scopeId, UUID sourceId) {
        final String SELECT = "SELECT " +
                "id, " +
                "source_id, " +
                "scope_id, " +
                "data " +
                "FROM learner.student_scope_entry_by_source " +
                "WHERE scope_id = ? " +
                "AND source_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(scopeId, sourceId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public StudentScopeEntry fromRow(Row row) {
        return new StudentScopeEntry()
                .setScopeId(row.getUUID("scope_id"))
                .setSourceId(row.getUUID("source_id"))
                .setId(row.getUUID("id"))
                .setData(row.getString("data"));
    }

}
