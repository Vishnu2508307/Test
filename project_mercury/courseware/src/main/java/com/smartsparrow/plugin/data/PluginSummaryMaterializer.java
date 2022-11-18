package com.smartsparrow.plugin.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;
import static com.smartsparrow.dse.api.ResultSets.getNullableLong;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class PluginSummaryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PluginSummaryMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String SELECT = "SELECT id, " +
            "subscription_id, " +
            "name, " +
            "creator_id," +
            "type, " +
            "latest_version, " +
            "description," +
            "thumbnail," +
            "tags," +
            "toTimestamp(id) as created_ts, " +
            "latest_version_release_date, " +
            "deleted_id, " +
            "latest_guide, " +
            "publish_mode," +
            "default_height FROM plugin.summary ";

    public Statement fetchById(UUID id) {
        final String BY_ID = SELECT + "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchAll() {
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Maps a row to a plugin summary
     *
     * @param row the row to convert
     * @return {@link PluginSummary}
     */
    public PluginSummary fromRow(Row row) {
        return new PluginSummary()
                .setId(row.getUUID("id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setName(row.getString("name"))
                .setCreatorId(row.getUUID("creator_id"))
                .setType(getNullableEnum(row, "type", PluginType.class))
                .setLatestVersion(row.getString("latest_version"))
                .setDescription(row.getString("description"))
                .setLatestVersionReleaseDate(getNullableLong(row, "latest_version_release_date"))
                .setDeletedId(row.getUUID("deleted_id"))
                .setThumbnail(row.getString("thumbnail"))
                .setTags(row.getList("tags", String.class))
                .setLatestGuide(row.getString("latest_guide"))
                .setPublishMode(getNullableEnum(row, "publish_mode", PublishMode.class))
                .setDefaultHeight(row.getString("default_height"));
    }
}
