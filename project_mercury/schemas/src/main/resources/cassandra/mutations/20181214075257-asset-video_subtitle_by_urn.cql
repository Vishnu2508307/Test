--
-- asset.video_subtitle_by_asset
-- -----------------------------
-- Stores urls to subtitle files for different languages
--
-- Fetch all available subtitles for the asset
-- > SELECT * FROM asset.video_subtitle_by_asset WHERE asset_id = '1234-...-4567'
--
CREATE TABLE IF NOT EXISTS asset.video_subtitle_by_asset (
    asset_id timeuuid,
    lang text,
    url text,
    PRIMARY KEY (asset_id, lang)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
