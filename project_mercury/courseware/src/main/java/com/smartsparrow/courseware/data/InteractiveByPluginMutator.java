package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class InteractiveByPluginMutator extends SimpleTableMutator<Interactive> {

    @Override
    public String getUpsertQuery(Interactive mutation) {
        // @formatter:off
        return "INSERT INTO courseware.interactive_by_plugin ("
                + "  plugin_id"
                + ", interactive_id"
                + ", plugin_version_expr"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Interactive mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getId(), mutation.getPluginVersionExpr());
    }
}

