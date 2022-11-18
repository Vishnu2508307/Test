--
-- asset.alfresco_data_by_asset
-- -----------------------------
-- Store Alfresco specific asset data
--
-- Find Alfresco asset by asset id
-- > SELECT * FROM asset.alfresco_data_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS asset.alfresco_data_by_asset (
    asset_id timeuuid,
    alfresco_id timeuuid,
    name text,
    version text,
    last_modified_date bigint,
    last_sync_date bigint,
    PRIMARY KEY (asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
