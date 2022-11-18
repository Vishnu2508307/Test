package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.asset.data.VideoSubtitle;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class VideoSubtitleByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public VideoSubtitleByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchBy(UUID assetId) {
        final String SELECT = "SELECT " +
                "asset_id, " +
                "lang, " +
                "url " +
                "FROM learner.video_subtitle_by_asset " +
                "WHERE asset_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an video subtitle object
     */
    public VideoSubtitle fromRow(Row row) {
        return new VideoSubtitle()
                .setAssetId(row.getUUID("asset_id"))
                .setLang(row.getString("lang"))
                .setUrl(row.getString("url"));
    }


}
