--
-- asset.external_source_by_asset
-- ------------------------------
-- Store an external source url by asset id
--
-- find an external source url by asset id
-- > SELECT * FROM asset.external_source_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS asset.external_source_by_asset (
    asset_id timeuuid,
    url text,
    PRIMARY KEY (asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
