package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.math.data.AssetSummary;

class LearnerAssetSummaryMaterializer implements TableMaterializer {
    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public LearnerAssetSummaryMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findById(UUID id) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  asset_id"
                + ", alt_text"
                + ", hash"
                + ", math_ml"
                + ", svg_shape"
                + ", svg_text"
                + " FROM learner.math_asset_summary"
                + " WHERE asset_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(id);
        return stmt;
    }

    public AssetSummary fromRow(Row row) {
        return new AssetSummary()
                .setId(row.getUUID("asset_id"))
                .setAltText(row.getString("alt_text"))
                .setHash(row.getString("hash"))
                .setMathML(row.getString("math_ml"))
                .setSvgShape(row.getString("svg_shape"))
                .setSvgText(row.getString("svg_text"));
    }
}
