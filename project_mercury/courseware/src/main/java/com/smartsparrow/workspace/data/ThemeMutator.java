package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ThemeMutator extends SimpleTableMutator<Theme> {

    @Override
    public String getUpsertQuery(Theme mutation) {
        // @formatter:off
        return "INSERT INTO workspace.theme (" +
                "  id" +
                ", name" +
                ") VALUES (?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Theme mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setString(1, mutation.getName());
    }

    @Override
    public String getDeleteQuery(Theme mutation) {
        return "DELETE FROM workspace.theme " +
                "WHERE id = ? ";
    }

    @Override
    public void bindDelete(BoundStatement stmt, Theme mutation) {
        stmt.bind(mutation.getId());
    }
}
