package com.smartsparrow.math.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.ImmutableSet;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.AccountRole;

class AssetSummaryMutator extends SimpleTableMutator<AssetSummary> {

    @Override
    public String getUpsertQuery(AssetSummary mutation) {
        return "INSERT INTO math.asset_summary ("
                + "  asset_id"
                + ", alt_text"
                + ", hash"
                + ", math_ml"
                + ", svg_shape"
                + ", svg_text"
                + ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setString(1, mutation.getAltText());
        stmt.setString(2, mutation.getHash());
        stmt.setString(3, mutation.getMathML());
        stmt.setString(4, mutation.getSvgShape());
        stmt.setString(5, mutation.getSvgText());
    }

    @Override
    public String getDeleteQuery(final AssetSummary mutation) {
        return "DELETE FROM math.asset_summary" +
                " WHERE asset_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final AssetSummary mutation) {
        stmt.bind(mutation.getId());
    }

    public Statement setSvgShape(UUID id, String svgShape) {
        // @formatter:off
        final String QUERY = "UPDATE math.asset_summary "
                + "  SET svg_shape = ?"
                + " WHERE asset_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(svgShape, id);
        stmt.setIdempotent(true);
        return stmt;
    }
}
