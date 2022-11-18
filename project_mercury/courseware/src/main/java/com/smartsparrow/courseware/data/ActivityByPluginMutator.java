package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ActivityByPluginMutator extends SimpleTableMutator<Activity> {

    @Override
    public String getUpsertQuery(Activity mutation) {
        // @formatter:off
        return "INSERT INTO courseware.activity_by_plugin ("
                + "  plugin_id"
                + ", activity_id"
                + ", plugin_version_expr"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Activity mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getId(), mutation.getPluginVersionExpr());
    }

}
