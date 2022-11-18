package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class FilterByPluginVersionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public FilterByPluginVersionMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findFilterByPluginVersion(final UUID pluginId, String version) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  plugin_id" +
                ", version" +
                ", filter_type" +
                ", filter_values" +
                " FROM plugin.filter_by_plugin_version " +
                " WHERE plugin_id = ? and version = ? ";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(pluginId, version);
        return stmt;
    }

    public PluginFilter fromRow(Row row) {
        return new PluginFilter()
                .setPluginId(row.getUUID("plugin_id"))
                .setVersion(row.getString("version"))
                .setFilterType(Enums.of(PluginFilterType.class, row.getString("filter_type")))
                .setFilterValues(row.getSet("filter_values", String.class));

    }
}
