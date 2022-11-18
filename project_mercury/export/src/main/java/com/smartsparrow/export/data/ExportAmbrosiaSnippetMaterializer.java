package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

public class ExportAmbrosiaSnippetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ExportAmbrosiaSnippetMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID exportId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  export_id" +
                ", notification_id" +
                ", element_id" +
                ", element_type" +
                ", account_id" +
                ", ambrosia_snippet" +
                " FROM export.ambrosia_snippet_by_export" +
                " WHERE export_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(exportId);
        return stmt;
    }

    public Statement findByNotificationId(final UUID exportId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  export_id" +
                ", notification_id" +
                ", element_id" +
                ", element_type" +
                ", account_id" +
                ", ambrosia_snippet" +
                " FROM export.ambrosia_snippet_by_export" +
                " WHERE notification_id = ? ALLOW FILTERING";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(exportId);
        return stmt;
    }

    public ExportAmbrosiaSnippet fromRow(Row row) {
        return new ExportAmbrosiaSnippet()
                .setExportId(row.getUUID("export_id"))
                .setNotificationId(row.getUUID("notification_id"))
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setAccountId(row.getUUID("account_id"))
                .setAmbrosiaSnippet(row.getString("ambrosia_snippet"));
    }
}
