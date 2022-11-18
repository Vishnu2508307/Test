package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class FilterByPluginVersionMutator extends SimpleTableMutator<PluginFilter> {

    @Override
    public String getUpsertQuery(PluginFilter mutation) {
        // @formatter:off
        return "INSERT INTO plugin.filter_by_plugin_version (" +
                "  plugin_id" +
                ", version" +
                ", filter_type" +
                ", filter_values" +
                ") VALUES (?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginFilter mutation) {
        stmt.setUUID(0, mutation.getPluginId());
        stmt.setString(1, mutation.getVersion());
        stmt.setString(2, Enums.asString(mutation.getFilterType()));
        stmt.setSet(3, mutation.getFilterValues());
    }
}
