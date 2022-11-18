package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;


class PublicationMetadataMutator extends SimpleTableMutator<PublicationMetadataByPublishedActivity> {

    @Override
    public String getUpsertQuery(PublicationMetadataByPublishedActivity mutation) {

        return  "INSERT INTO publication.metadata_by_publication (" +
                "  publication_id" +
                ", author" +
                ", etext_version" +
                ", book_id" +
                ", created_by" +
                ", created_at" +
                ", updated_by" +
                ", updated_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PublicationMetadataByPublishedActivity mutation) {
        stmt.setUUID(0, mutation.getPublicationId());
        Mutators.bindNonNull(stmt, 1, mutation.getAuthor());
        stmt.setString(2, mutation.getEtextVersion());
        stmt.setString(3, mutation.getBookId());
        stmt.setUUID(4, mutation.getCreatedBy());
        stmt.setUUID(5, mutation.getCreatedAt());
        stmt.setUUID(6, mutation.getUpdatedBy());
        stmt.setUUID(7, mutation.getUpdatedAt());
    }
}