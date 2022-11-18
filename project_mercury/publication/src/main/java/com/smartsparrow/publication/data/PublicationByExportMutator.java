package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class PublicationByExportMutator extends SimpleTableMutator<PublicationByExport> {

    @Override
    public String getUpsertQuery(PublicationByExport mutation) {

        return "INSERT INTO publication.publication_by_export (" +
                "  publication_id" +
                ", export_id"
                + ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PublicationByExport mutation) {
        stmt.setUUID(0, mutation.getPublicationId());
        stmt.setUUID(1, mutation.getExportId());
    }
}
