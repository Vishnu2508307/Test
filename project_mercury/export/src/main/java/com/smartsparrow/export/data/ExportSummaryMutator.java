package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ExportSummaryMutator extends SimpleTableMutator<ExportSummary> {

    @Override
    public Statement upsert(ExportSummary mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO export.summary (" +
                "  id" +
                ", element_id" +
                ", account_id" +
                ", element_type" +
                ", project_id" +
                ", workspace_id" +
                ", status" +
                ", completed_at" +
                ", ambrosia_url" +
                ", root_element_id" +
                ", export_type" +
                ", metadata"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, ExportSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getElementId());
        stmt.setUUID(2, mutation.getAccountId());
        stmt.setString(3, Enums.asString(mutation.getElementType()));
        stmt.setUUID(4, mutation.getProjectId());
        stmt.setUUID(5, mutation.getWorkspaceId());
        stmt.setString(6, Enums.asString(mutation.getStatus()));
        Mutators.bindNonNull(stmt, 7, mutation.getCompletedAt());
        Mutators.bindNonNull(stmt, 8, mutation.getAmbrosiaUrl());
        stmt.setUUID(9, mutation.getRootElementId());
        stmt.setString(10, Enums.asString(mutation.getExportType()));
        stmt.setString(11, mutation.getMetadata());
    }
}
