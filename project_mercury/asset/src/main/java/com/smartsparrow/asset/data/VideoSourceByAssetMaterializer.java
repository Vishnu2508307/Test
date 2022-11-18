package com.smartsparrow.asset.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class VideoSourceByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public VideoSourceByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String SELECT_BY_ASSET_ID = "SELECT " +
            "asset_id, " +
            "name, " +
            "url, " +
            "resolution " +
            "FROM asset.video_source_by_asset " +
            "WHERE asset_id = ?";

    @SuppressWarnings("Duplicates")
    public Statement fetchAllBy(UUID assetId) {
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ASSET_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchBy(UUID assetId, VideoSourceName videoSourceName) {
        String SELECT_BY_NAME = SELECT_BY_ASSET_ID + " AND name = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_NAME);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId, videoSourceName.name());
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an video source object
     */
    public VideoSource fromRow(Row row) {
        return new VideoSource()
                .setAssetId(row.getUUID("asset_id"))
                .setName(Enums.of(VideoSourceName.class, row.getString("name")))
                .setUrl(row.getString("url"))
                .setResolution(row.getString("resolution"));
    }
}
