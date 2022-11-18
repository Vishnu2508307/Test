package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ActivityChangeMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ActivityChangeMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchLatestChange(UUID activityId) {
        final String SELECT = "SELECT " +
                "activity_id, " +
                "change_id " +
                "FROM courseware.activity_change " +
                "WHERE activity_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    /**
     * Convert a data row into an activity change object
     * @param row the row to get the data from
     */
    public ActivityChange fromRow(Row row) {
        return new ActivityChange()
                .setActivityId(row.getUUID("activity_id"))
                .setChangeId(row.getUUID("change_id"));
    }
}
