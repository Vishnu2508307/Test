package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class IconSourceByAssetMutator extends SimpleTableMutator<IconSource> {

    @Override
    public String getUpsertQuery(IconSource mutation) {
        return "INSERT INTO asset.icon_source_by_asset (" +
                "asset_id, " +
                "name, " +
                "url, " +
                "width, " +
                "height) " +
                "VALUES (?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IconSource mutation) {
        stmt.bind(
                mutation.getAssetId(),
                mutation.getName().name(),
                mutation.getUrl(),
                mutation.getWidth(),
                mutation.getHeight()
        );
    }
}
