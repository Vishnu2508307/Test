--
-- asset.asset_by_media_type
-- -----------------------
-- Stores assets retrievable by visibility and media_type.
--
-- Fetch assets by visibility and media_type
-- > SELECT * FROM asset.asset_by_media_type WHERE visibility = ? AND media_type = ?
--

CREATE TABLE IF NOT EXISTS asset.asset_by_media_type (
    visibility text,
    media_type text,
    asset_id timeuuid,
    PRIMARY KEY (visibility, media_type, asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
