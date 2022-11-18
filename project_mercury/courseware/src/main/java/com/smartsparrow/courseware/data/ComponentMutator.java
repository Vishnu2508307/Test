package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ComponentMutator extends SimpleTableMutator<Component> {

    @Override
    public String getUpsertQuery(Component mutation) {
        // @formatter:off
        return "INSERT INTO courseware.component ("
                + "  id"
                + ", plugin_id"
                + ", plugin_version_expr"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Component mutation) {
        stmt.bind(mutation.getId(), mutation.getPluginId(), mutation.getPluginVersionExpr());
    }
}
