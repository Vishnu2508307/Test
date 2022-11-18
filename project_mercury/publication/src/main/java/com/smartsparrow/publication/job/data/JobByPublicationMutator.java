package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class JobByPublicationMutator extends SimpleTableMutator<JobByPublication> {

    @Override
    public String getUpsertQuery(JobByPublication mutation) {

        return "INSERT INTO publication.job_by_publication (" +
                "  publication_id" +
                ", job_id"
                + ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, JobByPublication mutation) {
        stmt.setUUID(0, mutation.getPublicationId());
        stmt.setUUID(1, mutation.getJobId());
    }
}
