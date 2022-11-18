package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AmbrosiaReducerErrorByExportMutator extends SimpleTableMutator<AmbrosiaReducerErrorLog> {

    @Override
    public String getUpsertQuery(AmbrosiaReducerErrorLog mutation) {
        return "INSERT INTO export.ambrosia_reducer_error_by_export (" +
                "  export_id" +
                ", cause" +
                ", error_message" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AmbrosiaReducerErrorLog mutation) {
        stmt.setUUID(0, mutation.getExportId());
        stmt.setString(1, mutation.getCause());
        stmt.setString(2, mutation.getErrorMessage());
    }
}
