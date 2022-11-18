package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class VersionByPluginMutator extends SimpleTableMutator<PluginVersion> {

    @Override
    public String getUpsertQuery(PluginVersion mutation) {
        return "INSERT INTO plugin.version_by_plugin (" +
                "plugin_id, " +
                "major, " +
                "minor, " +
                "patch, " +
                "release_date, " +
                "pre_release, " +
                "build, " +
                "unpublished) VALUES (?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginVersion mutation) {
        stmt.bind(mutation.getPluginId(),
                mutation.getMajor(),
                mutation.getMinor(),
                mutation.getPatch(),
                mutation.getReleaseDate(),
                mutation.getPreRelease(),
                mutation.getBuild(),
                mutation.getUnpublished());
    }

    public Statement unPublishPluginVersion(PluginVersion mutation) {
        final String UN_PUBLISH_QUERY = "UPDATE plugin.version_by_plugin " +
                "SET unpublished = true " +
                "WHERE plugin_id = ? " +
                "AND major = ? " +
                "AND minor = ? " +
                "AND patch = ? " +
                "AND release_date = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UN_PUBLISH_QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(mutation.getPluginId(), mutation.getMajor(), mutation.getMinor(), mutation.getPatch(), mutation.getReleaseDate());
        stmt.setIdempotent(true);
        return stmt;
    }

    @Override
    public String getDeleteQuery(PluginVersion mutation) {
        return "DELETE FROM plugin.version_by_plugin " +
                "WHERE plugin_id = ? " +
                "AND major = ? " +
                "AND minor = ? " +
                "AND patch = ? " +
                "AND release_date = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, PluginVersion mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getMajor(), mutation.getMinor(), mutation.getPatch(), mutation.getReleaseDate());
    }
}
