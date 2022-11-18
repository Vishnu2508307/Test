package com.smartsparrow.plugin.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class PluginSummaryMutator extends SimpleTableMutator<PluginSummary> {

    @Override
    public String getUpsertQuery(PluginSummary mutation) {
        return "INSERT INTO plugin.summary (" +
                "id, " +
                "subscription_id, " +
                "name, " +
                "creator_id," +
                "type, " +
                "latest_version, " +
                "description, " +
                "latest_version_release_date, " +
                "deleted_id, " +
                "thumbnail, " +
                "tags, " +
                "latest_guide, " +
                "publish_mode," +
                "default_height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginSummary mutation) {
        stmt.bind(mutation.getId(),
                  mutation.getSubscriptionId(),
                  mutation.getName(),
                  mutation.getCreatorId(),
                  (mutation.getType() != null ? mutation.getType().name() : null),
                  mutation.getLatestVersion(),
                  mutation.getDescription(),
                  mutation.getLatestVersionReleaseDate(),
                  mutation.getDeletedId(),
                  mutation.getThumbnail(),
                  mutation.getTags(),
                  mutation.getLatestGuide(),
                  mutation.getPublishMode().name(),
                  mutation.getDefaultHeight());
    }

    public Statement updatePluginSummary(final PluginSummary pluginSummary) {
        final String UPDATE = "UPDATE plugin.summary "
                + "SET publish_mode = ?"
                + "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Enums.asString(pluginSummary.getPublishMode()),
                  pluginSummary.getId());
        stmt.setIdempotent(true);
        return stmt;
    }
}
