--
-- learner.asset_summary
-- --------------------
-- Stores summary data for assets
--
-- Select asset summary by asset_id
-- > SELECT * FROM learner.asset_summary WHERE asset_id = '002c52e0-fee9-11e8-8080-808080808080'
--
CREATE TABLE IF NOT EXISTS learner.asset_summary (
    asset_id timeuuid,
    provider text,
    owner_id timeuuid,
    subscription_id timeuuid,
    media_type text,
    hash text,
    visibility text,
    PRIMARY KEY (asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
