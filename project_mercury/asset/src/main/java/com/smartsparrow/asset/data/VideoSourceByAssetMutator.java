package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class VideoSourceByAssetMutator extends SimpleTableMutator<VideoSource> {

    @Override
    public String getUpsertQuery(VideoSource mutation) {
        return "INSERT INTO asset.video_source_by_asset (" +
                "asset_id, " +
                "name, " +
                "url, " +
                "resolution) " +
                "VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, VideoSource mutation) {
        stmt.bind(
                mutation.getAssetId(),
                mutation.getName().name(),
                mutation.getUrl(),
                mutation.getResolution()
        );
    }
}
