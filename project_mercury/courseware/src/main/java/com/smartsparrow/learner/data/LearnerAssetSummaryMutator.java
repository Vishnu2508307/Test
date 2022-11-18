package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.math.data.AssetSummary;

class LearnerAssetSummaryMutator extends SimpleTableMutator<AssetSummary> {

    @Override
    public String getUpsertQuery(AssetSummary mutation) {
        return "INSERT INTO learner.math_asset_summary ("
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
        return "DELETE FROM learner.math_asset_summary" +
                " WHERE asset_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final AssetSummary mutation) {
        stmt.bind(mutation.getId());
    }
}
