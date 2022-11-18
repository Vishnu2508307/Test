package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ActivityConfigMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ActivityConfigMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchLatestConfig(final UUID activityId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", activity_id"
                + ", config"
                + " FROM courseware.activity_config"
                + " WHERE activity_id = ?"
                + " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public Statement fetchLatestConfigId(final UUID activityId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + " FROM courseware.activity_config"
                + " WHERE activity_id = ?"
                + " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("id");
    }
}
