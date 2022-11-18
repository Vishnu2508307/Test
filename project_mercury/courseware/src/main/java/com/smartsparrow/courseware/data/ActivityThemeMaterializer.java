package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ActivityThemeMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ActivityThemeMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchLatestByActivity(final UUID activityId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  activity_id"
                + ", id"
                + ", config"
                + " FROM courseware.activity_theme"
                + " WHERE activity_id = ?"
                + " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }


    public ActivityTheme fromRow(Row row) {
        return new ActivityTheme()
                .setId(row.getUUID("id"))
                .setActivityId(row.getUUID("activity_id"))
                .setConfig(row.getString("config"));
    }
}
