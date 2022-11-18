--
-- asset.video_source_by_asset
-- ---------------------------
--
-- Stores various resolution variations of a video asset, like "720p", "1080p". Necessary for variable resolution
-- streaming
--
-- fetch all resolution variations of a video type asset
-- > SELECT * FROM asset.video_source_by_asset WHERE asset_id = '1234-...-456789'
--
CREATE TABLE IF NOT EXISTS asset.video_source_by_asset (
    asset_id UUID,
    name text,
    url text,
    resolution text,
    PRIMARY KEY (asset_id, name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
