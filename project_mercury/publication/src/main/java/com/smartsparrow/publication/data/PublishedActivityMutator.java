package com.smartsparrow.publication.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class PublishedActivityMutator extends SimpleTableMutator<PublishedActivity> {

    @Override
    public String getUpsertQuery(PublishedActivity mutation) {

        return  "INSERT INTO publication.published_activity (" +
                "  activity_id" +
                ", version" +
                ", description" +
                ", publication_id" +
                ", title" +
                ", output_type"
                + ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PublishedActivity mutation) {
        stmt.setUUID(0, mutation.getActivityId());
        stmt.setString(1, mutation.getVersion());
        stmt.setString(2, mutation.getDescription());
        stmt.setUUID(3, mutation.getPublicationId());
        stmt.setString(4, mutation.getTitle());
        stmt.setString(5, Enums.asString(mutation.getOutputType()));
    }


    public Statement setTitle(UUID activityId, String title,  String version) {
        String SET_NAME = "UPDATE publication.published_activity " +
                "SET title = ? " +
                "WHERE activity_id = ? AND " +
                "      version = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_NAME);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(title, activityId, version);
        stmt.setIdempotent(true);
        return stmt;
    }
}
