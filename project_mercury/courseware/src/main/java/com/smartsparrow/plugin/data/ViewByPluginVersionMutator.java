package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ViewByPluginVersionMutator extends SimpleTableMutator<ManifestView> {

    @Override
    public String getUpsertQuery(ManifestView mutation) {
        return "INSERT INTO plugin.view_by_plugin_version ("
                + " plugin_id"
                + ", version"
                + ", context"
                + ", entry_point_path"
                + ", entry_point_data"
                + ", content_type"
                + ", public_dir"
                + ", editor_mode) VALUES (?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ManifestView mutation) {
        stmt.bind(mutation.getPluginId(),
                mutation.getVersion(),
                mutation.getContext(),
                mutation.getEntryPointPath(),
                mutation.getEntryPointData(),
                mutation.getContentType(),
                mutation.getPublicDir(),
                mutation.getEditorMode());
    }
}
