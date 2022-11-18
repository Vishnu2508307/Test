package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class VideoSubtitleByAssetMutator extends SimpleTableMutator<VideoSubtitle> {

    @Override
    public String getUpsertQuery(VideoSubtitle mutation) {
        return "INSERT INTO asset.video_subtitle_by_asset (" +
                "asset_id, " +
                "lang, " +
                "url) " +
                "VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, VideoSubtitle mutation) {
        stmt.bind(
                mutation.getAssetId(),
                mutation.getLang(),
                mutation.getUrl()
        );
    }
}
