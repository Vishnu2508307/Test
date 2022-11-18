--
-- asset.audio_source_by_asset
-- ---------------------------
-- Store audio source by asset
--
-- Find an audio source by asset and name
-- > SELECT * FROM asset.audio_source_by_asset WHERE asset_id = ? and name = ?
--
CREATE TABLE IF NOT EXISTS asset.audio_source_by_asset (
    asset_id uuid,
    name text,
    url text,
    PRIMARY KEY (asset_id, name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
