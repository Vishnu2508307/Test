--
-- asset.icon_source_by_asset
-- ---------------------------
-- Stores various size variations of an icon image asset, like "original", "thumbnail", "small", etc...
--
-- fetch all size variations of an icon image type asset
-- > SELECT * FROM asset.icon_source_by_asset WHERE asset_id = '1234-...-456789'
--
CREATE TABLE IF NOT EXISTS asset.icon_source_by_asset (
    asset_id UUID,
    name text,
    url text,
    width double,
    height double,
    PRIMARY KEY (asset_id, name)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
