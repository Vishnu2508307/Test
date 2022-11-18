package com.smartsparrow.plugin.data;


import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class SearchableFieldByPluginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public SearchableFieldByPluginMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findSearchableFieldByPlugin(final UUID pluginId, String version) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "plugin_id" +
                ", version" +
                ", id" +
                ", name" +
                ", content_type" +
                ", summary" +
                ", body" +
                ", source" +
                ", preview" +
                ", tag" +
                " FROM plugin.searchable_field_by_plugin_version " +
                " WHERE plugin_id = ? and version = ? ";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(pluginId, version);
        return stmt;
    }

    public PluginSearchableField fromRow(Row row) {
        return new PluginSearchableField()
                .setPluginId(row.getUUID("plugin_id"))
                .setVersion(row.getString("version"))
                .setId(row.getUUID("id"))
                .setName(row.getString("name"))
                .setContentType(row.getString("content_type"))
                .setSummary(row.getSet("summary", String.class))
                .setBody(row.getSet("body", String.class))
                .setSource(row.getSet("source", String.class))
                .setPreview(row.getSet("preview", String.class))
                .setTag(row.getSet("tag", String.class));
    }
}
