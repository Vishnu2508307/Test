package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LogBucketMutator extends SimpleTableMutator<LogBucket> {

    @Override
    public String getUpsertQuery(LogBucket mutation) {
        return "INSERT INTO plugin.log_bucket_by_day_time ("
                + "day"
                + ", time"
                + ", table_name"
                + ", bucket_id"
                + ") VALUES ( ?, ?, ?, ? )";
    }

    @Override
    public String getDeleteQuery(LogBucket mutation) {
        return "DELETE FROM plugin.log_bucket_by_day_time " +
                "WHERE day = ? AND " +
                "time = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, LogBucket mutation) {
        stmt.bind(mutation.getDay(), mutation.getTime());
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LogBucket mutation) {
        stmt.bind(mutation.getDay(),
                  mutation.getTime(),
                  mutation.getTableName(),
                  mutation.getBucketId());
    }
}
