package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ExportAmbrosiaSnippetMutator extends SimpleTableMutator<ExportAmbrosiaSnippet> {

    @Override
    public Statement upsert(ExportAmbrosiaSnippet mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO export.ambrosia_snippet_by_export (" +
                "  export_id" +
                ", notification_id" +
                ", element_id" +
                ", element_type" +
                ", account_id" +
                ", ambrosia_snippet"
                + ") VALUES (?, ?, ?, ?, ?, ?)"
                + " USING TTL 172800";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, ExportAmbrosiaSnippet mutation) {
        stmt.setUUID(0, mutation.getExportId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setUUID(2, mutation.getElementId());
        stmt.setString(3, Enums.asString(mutation.getElementType()));
        stmt.setUUID(4, mutation.getAccountId());
        stmt.setString(5, mutation.getAmbrosiaSnippet());
    }
}
