package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.publication.job.enums.JobStatus;
import com.smartsparrow.publication.job.enums.JobType;

import javax.inject.Inject;
import java.util.UUID;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

class JobSummaryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public JobSummaryMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID jobId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  id" +
                ", job_type" +
                ", status" +
                ", status_desc" +
                " FROM publication.job_summary" +
                " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(jobId);
        return stmt;
    }

    public JobSummary fromRow(Row row) {
        return new JobSummary()
                .setId(row.getUUID("id"))
                .setJobType(getNullableEnum(row, "job_type", JobType.class))
                .setStatus(getNullableEnum(row, "status", JobStatus.class))
                .setStatusDesc(row.getString("status_desc"));
    }
}
