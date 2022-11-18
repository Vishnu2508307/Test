package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ArtifactContentMutator extends SimpleTableMutator<ArtifactContent> {

    @Override
    public String getUpsertQuery(ArtifactContent mutation) {

        return "INSERT INTO publication.artifact_content (" +
                "  id" +
                ", artifact_id"
                + ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ArtifactContent mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getArtifactId());
    }
}

