--
-- learner.asset_metadata
-- ----------------------
-- Stores metadata associated to an asset
--
-- fetch all the metadata key and values for an asset
-- > SELECT * FROM learner.asset_metadata WHERE asset_id = '1234-...-456789'
--
CREATE TABLE IF NOT EXISTS learner.asset_metadata (
    asset_id timeuuid,
    key text,
    value text,
    PRIMARY KEY (asset_id, key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
