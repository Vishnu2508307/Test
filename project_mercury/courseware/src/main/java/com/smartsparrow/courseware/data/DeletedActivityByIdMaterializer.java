package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;

public class DeletedActivityByIdMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    DeletedActivityByIdMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchDeletedActivityById(final UUID activityId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  activity_id"
                + ", account_id"
                + ", deleted_at"
                + " FROM courseware.deleted_activity_by_id"
                + " WHERE activity_id = ?"
                + " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public DeletedActivity fromRow(Row row) {
        return new DeletedActivity()
                .setActivityId(row.getUUID("activity_id"))
                .setAccountId(row.getUUID("account_id"))
                .setDeletedAt(row.getString("deleted_at"));
    }
}
