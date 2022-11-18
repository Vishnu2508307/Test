package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.asset.data.ExternalSource;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ExternalSourceByAssetMutator extends SimpleTableMutator<ExternalSource> {
    @Override
    public String getUpsertQuery(ExternalSource mutation) {
        return "INSERT INTO learner.external_source_by_asset (" +
                " asset_id" +
                ", url" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ExternalSource mutation) {
        stmt.bind(mutation.getAssetId(), mutation.getUrl());
    }
}
