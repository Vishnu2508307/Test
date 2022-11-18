package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ImageSourceByAssetMutator extends SimpleTableMutator<ImageSource> {

    @Override
    public String getUpsertQuery(ImageSource mutation) {
        return "INSERT INTO asset.image_source_by_asset (" +
                "asset_id, " +
                "name, " +
                "url, " +
                "width, " +
                "height) " +
                "VALUES (?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ImageSource mutation) {
        stmt.bind(
                mutation.getAssetId(),
                mutation.getName().name(),
                mutation.getUrl(),
                mutation.getWidth(),
                mutation.getHeight()
        );
    }
}
