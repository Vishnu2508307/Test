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

public class ManifestByPluginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String SELECT = "SELECT plugin_id," +
            "version, " +
            "description, " +
            "screenshots, " +
            "thumbnail, " +
            "publisher_id, " +
            "configuration_schema, " +
            "zip_hash, " +
            "name, " +
            "website_url, " +
            "support_url, " +
            "whats_new, " +
            "tags, " +
            "type, " +
            "output_schema, " +
            "guide," +
            "default_height FROM plugin.manifest_by_plugin_version ";

    @Inject
    public ManifestByPluginMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchAllManifestByPlugin(UUID pluginId) {
        final String WHERE = "WHERE plugin_id=?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT + WHERE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchManifestByVersion(UUID pluginId, String version) {
        final String WHERE = "WHERE plugin_id=? AND version=?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT + WHERE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, version);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Maps a row to a plugin manifest
     *
     * @param row the row to convert
     * @return {@link PluginManifest}
     */
    public PluginManifest fromRow(Row row) {
        return new PluginManifest()
                .setPluginId(row.getUUID("plugin_id"))
                .setVersion(row.getString("version"))
                .setDescription(row.getString("description"))
                .setScreenshots(row.getSet("screenshots", String.class))
                .setThumbnail(row.getString("thumbnail"))
                .setPublisherId(row.getUUID("publisher_id"))
                .setConfigurationSchema(row.getString("configuration_schema"))
                .setZipHash(row.getString("zip_hash"))
                .setName(row.getString("name"))
                .setWebsiteUrl(row.getString("website_url"))
                .setSupportUrl(row.getString("support_url"))
                .setWhatsNew(row.getString("whats_new"))
                .setTags(row.getList("tags", String.class))
                .setType(Enums.of(PluginType.class, row.getString("type")))
                .setOutputSchema(row.getString("output_schema"))
                .setGuide(row.getString("guide"))
                .setDefaultHeight(row.getString("default_height"));
    }
}
