package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class PublishedActivityByPublicationMutator extends SimpleTableMutator<PublishedActivity> {

    @Override
    public String getUpsertQuery(PublishedActivity mutation) {

        return  "INSERT INTO publication.activity_by_publication (" +
                "  publication_id" +
                ", activity_id" +
                ", version" +
                ", title" +
                ", description" +
                ", output_type" +
                ", status"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PublishedActivity mutation) {
        stmt.setUUID(0, mutation.getPublicationId());
        stmt.setUUID(1, mutation.getActivityId());
        stmt.setString(2, mutation.getVersion());
        stmt.setString(3, mutation.getTitle());
        stmt.setString(4, mutation.getDescription());
        stmt.setString(5, Enums.asString(mutation.getOutputType()));
        stmt.setString(6, Enums.asString(mutation.getStatus()));
    }
}
