package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;

class PublicationMetadataByPublishedActivityMutator extends SimpleTableMutator<PublicationMetadataByPublishedActivity> {

    @Override
    public String getUpsertQuery(PublicationMetadataByPublishedActivity mutation) {

        return  "INSERT INTO publication.metadata_by_published_activity (" +
                "  activity_id" +
                ", version" +
                ", publication_id" +
                ", author" +
                ", etext_version" +
                ", book_id" +
                ", created_by" +
                ", created_at" +
                ", updated_by" +
                ", updated_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PublicationMetadataByPublishedActivity mutation) {
        stmt.setUUID(0, mutation.getActivityId());
        stmt.setString(1, mutation.getVersion());
        stmt.setUUID(2, mutation.getPublicationId());
        Mutators.bindNonNull(stmt, 3, mutation.getAuthor());
        stmt.setString(4, mutation.getEtextVersion());
        stmt.setString(5, mutation.getBookId());
        stmt.setUUID(6, mutation.getCreatedBy());
        stmt.setUUID(7, mutation.getCreatedAt());
        stmt.setUUID(8, mutation.getUpdatedBy());
        stmt.setUUID(9, mutation.getUpdatedAt());
    }
}