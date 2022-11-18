package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class PublicationByActivityMutator extends SimpleTableMutator<PublicationByActivity> {

    @Override
    public String getUpsertQuery(PublicationByActivity mutation) {

        return "INSERT INTO publication.publication_by_activity (" +
                "  publication_id" +
                ", activity_id"
                + ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PublicationByActivity mutation) {
        stmt.setUUID(0, mutation.getPublicationId());
        stmt.setUUID(1, mutation.getActivityId());
    }
}
