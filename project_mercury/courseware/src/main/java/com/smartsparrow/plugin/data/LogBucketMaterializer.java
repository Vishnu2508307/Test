package com.smartsparrow.plugin.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LogBucketMaterializer implements TableMaterializer {

    private static final String BY_DAY = "SELECT " +
            "day" +
            ", time" +
            ", table_name" +
            ", bucket_id" +
            "FROM plugin.log_bucket_by_day_time " +
            "WHERE day = ?";
    private final PreparedStatementCache stmtCache;

    @Inject
    public LogBucketMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByDay(LocalDate day) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_DAY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(day);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByDayTime(LocalDate day, long time) {
        final String BY_DAY_TIME = BY_DAY + " AND time = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_DAY_TIME);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(day, time);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LogBucket fromRow(Row row) {
        return new LogBucket()
                .setDay(row.getDate("day"))
                .setTime(row.getLong("time"))
                .setTableName(row.getString("table_name"))
                .setBucketId(row.getUUID("theme_id"));
    }
}
