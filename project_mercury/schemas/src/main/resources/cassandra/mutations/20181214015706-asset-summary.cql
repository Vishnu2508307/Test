--
-- asset.summary
-- -------------
-- Stores summary data for assets
--
-- Select asset summary by asset_id
-- > SELECT * FROM asset.summary WHERE id = '002c52e0-fee9-11e8-8080-808080808080'
--
CREATE TABLE IF NOT EXISTS asset.summary (
    id timeuuid,
    provider text,
    owner_id timeuuid,
    subscription_id timeuuid,
    media_type text,
    hash text,
    visibility text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
