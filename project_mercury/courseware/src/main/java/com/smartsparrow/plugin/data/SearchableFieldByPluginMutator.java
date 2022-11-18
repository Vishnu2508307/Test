package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;

import com.smartsparrow.dse.api.SimpleTableMutator;

public class SearchableFieldByPluginMutator extends SimpleTableMutator<PluginSearchableField> {

    @Override
    public Statement upsert(PluginSearchableField mutation) {
        // @formatter:off
        String QUERY = "INSERT INTO plugin.searchable_field_by_plugin_version (" +
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
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginSearchableField mutation) {
        stmt.setUUID(0, mutation.getPluginId());
        stmt.setString(1, mutation.getVersion());
        stmt.setUUID(2, mutation.getId());
        stmt.setString(3, mutation.getName());
        stmt.setString(4, mutation.getContentType());
        stmt.setSet(5, mutation.getSummary());
        stmt.setSet(6, mutation.getBody());
        stmt.setSet(7, mutation.getSource());
        stmt.setSet(8, mutation.getPreview());
        stmt.setSet(9, mutation.getTag());
    }
}
