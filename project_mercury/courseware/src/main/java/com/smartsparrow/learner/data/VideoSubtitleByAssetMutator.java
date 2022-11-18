package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.asset.data.VideoSubtitle;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class VideoSubtitleByAssetMutator extends SimpleTableMutator<VideoSubtitle> {

    @Override
    public String getUpsertQuery(VideoSubtitle mutation) {
        return "INSERT INTO learner.video_subtitle_by_asset (" +
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
