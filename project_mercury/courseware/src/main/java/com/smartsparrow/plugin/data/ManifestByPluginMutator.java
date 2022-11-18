package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ManifestByPluginMutator extends SimpleTableMutator<PluginManifest> {

    @Override
    public String getUpsertQuery(PluginManifest mutation) {
        return "INSERT INTO plugin.manifest_by_plugin_version (" +
                "plugin_id, " +
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
                "default_height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginManifest mutation) {
        stmt.bind(mutation.getPluginId(),
                  mutation.getVersion(),
                  mutation.getDescription(),
                  mutation.getScreenshots(),
                  mutation.getThumbnail(),
                  mutation.getPublisherId(),
                  mutation.getConfigurationSchema(),
                  mutation.getZipHash(),
                  mutation.getName(),
                  mutation.getWebsiteUrl(),
                  mutation.getSupportUrl(),
                  mutation.getWhatsNew(),
                  mutation.getTags(),
                  mutation.getType().name(),
                  mutation.getOutputSchema(),
                  mutation.getGuide(),
                  mutation.getDefaultHeight());
    }
}
