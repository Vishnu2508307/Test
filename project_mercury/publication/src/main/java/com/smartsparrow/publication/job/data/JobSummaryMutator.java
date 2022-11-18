package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class JobSummaryMutator extends SimpleTableMutator<JobSummary> {

    @Override
    public String getUpsertQuery(JobSummary mutation) {

        return "INSERT INTO publication.job_summary (" +
                "  id" +
                ", job_type" +
                ", status" +
                ", status_desc"
                + ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, JobSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setString(1, Enums.asString(mutation.getJobType()));
        stmt.setString(2, Enums.asString(mutation.getStatus()));
        stmt.setString(3, mutation.getStatusDesc());
    }
}
