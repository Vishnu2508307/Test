package com.smartsparrow.export.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AmbrosiaReducerErrorByExportMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AmbrosiaReducerErrorByExportMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findErrors(final UUID exportId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  export_id" +
                ", cause" +
                ", error_message" +
                " FROM export.ambrosia_reducer_error_by_export" +
                " WHERE export_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(exportId);
        return stmt;
    }

    public AmbrosiaReducerErrorLog fromRow(Row row) {
        return new AmbrosiaReducerErrorLog()
                .setExportId(row.getUUID("export_id"))
                .setCause(row.getString("cause"))
                .setErrorMessage(row.getString("error_message"));
    }
}
