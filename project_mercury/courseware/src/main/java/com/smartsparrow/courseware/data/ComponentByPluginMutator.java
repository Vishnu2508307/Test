package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ComponentByPluginMutator extends SimpleTableMutator<Component> {

    @Override
    public String getUpsertQuery(Component mutation) {
        // @formatter:off
        return "INSERT INTO courseware.component_by_plugin ("
                + "  plugin_id"
                + ", component_id"
                + ", plugin_version_exp"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Component mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getId(), mutation.getPluginVersionExpr());
    }
}
