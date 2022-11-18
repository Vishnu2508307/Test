package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class PublicationSummaryMutator extends SimpleTableMutator<PublicationSummary> {

    @Override
    public String getUpsertQuery(PublicationSummary mutation) {

        return  "INSERT INTO publication.summary (" +
                "  id" +
                ", title" +
                ", description" +
                ", config" +
                ", output_type"
                + ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PublicationSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setString(1, mutation.getTitle());
        stmt.setString(2, mutation.getDescription());
        stmt.setString(3, mutation.getConfig());
        stmt.setString(4, Enums.asString(mutation.getOutputType()));
    }
}
