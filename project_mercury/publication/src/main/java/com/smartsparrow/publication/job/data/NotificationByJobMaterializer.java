package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;


class NotificationByJobMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public NotificationByJobMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID jobId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  job_id" +
                ", notification_id" +
                " FROM publication.notification_by_job" +
                " WHERE job_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(jobId);
        return stmt;
    }

    public NotificationByJob fromRow(Row row) {
        return new NotificationByJob()
                .setJobId(row.getUUID("job_id"))
                .setNotificationId(row.getUUID("notification_id"));
    }
}
