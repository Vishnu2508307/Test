-- FIXME this table is deprecated and it will be dropped soon
-- learner.metadata_by_asset
-- --------------------
-- Stores metadata associated to an asset
--
-- fetch all metadata rows for a given asset
-- > SELECT * FROM learner.metadata_by_asset WHERE asset_id = '1234-...-456789'
--
CREATE TABLE IF NOT EXISTS learner.metadata_by_asset (
    asset_id timeuuid,
    key text,
    value text,
    PRIMARY KEY (asset_id, key, value)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
