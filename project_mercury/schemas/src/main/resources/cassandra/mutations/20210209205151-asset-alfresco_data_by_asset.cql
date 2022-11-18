--
-- asset.alfresco_data_by_asset
-- --------------------
-- Write a short description about the intention of this table.
--
-- Write a description of the query that this table supports
-- > SELECT * FROM asset.alfresco_data_by_asset WHERE ...
--
CREATE TABLE IF NOT EXISTS asset.alfresco_data_by_asset (
    asset_id timeuuid,
    alfresco_id uuid,
    name text,
    version text,
    last_modified_date bigint,
    last_sync_date bigint,
    PRIMARY KEY (asset_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
