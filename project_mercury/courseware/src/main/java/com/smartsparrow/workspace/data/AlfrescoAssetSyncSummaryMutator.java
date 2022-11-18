package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class AlfrescoAssetSyncSummaryMutator extends SimpleTableMutator<AlfrescoAssetSyncSummary> {

    @Override
    public Statement upsert(AlfrescoAssetSyncSummary mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO asset.alfresco_sync_summary (" +
                " reference_id" +
                ", course_id" +
                ", sync_type" +
                ", status" +
                ", completed_at"
                + ") VALUES (?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, AlfrescoAssetSyncSummary mutation) {
        stmt.setUUID(0, mutation.getReferenceId());
        stmt.setUUID(1, mutation.getCourseId());
        stmt.setString(2, Enums.asString(mutation.getSyncType()));
        stmt.setString(3, Enums.asString(mutation.getStatus()));
        Mutators.bindNonNull(stmt, 4, mutation.getCompletedAt());
    }

}
