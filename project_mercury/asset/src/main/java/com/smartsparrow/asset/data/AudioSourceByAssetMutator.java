package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class AudioSourceByAssetMutator extends SimpleTableMutator<AudioSource> {

    @Override
    public String getUpsertQuery(AudioSource mutation) {
        return "INSERT INTO asset.audio_source_by_asset (" +
                " asset_id" +
                ", name" +
                ", url" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AudioSource mutation) {
        stmt.bind(
                mutation.getAssetId(),
                Enums.asString(mutation.getName()),
                mutation.getUrl()
        );
    }
}
