package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class ArtifactMutator extends SimpleTableMutator<Artifact> {

    @Override
    public String getUpsertQuery(Artifact mutation) {

        return "INSERT INTO publication.artifact (" +
                "  id" +
                ", job_id" +
                ", artifact_type"
                + ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Artifact mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getJobId());
        stmt.setString(2, Enums.asString(mutation.getArtifactType()));
    }
}
