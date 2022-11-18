--
-- asset.asset_by_hash
-- -------------------
-- Used to lookup an asset by its hash
--
-- Finds asset by hash
-- > SELECT * FROM asset.asset_by_hash WHERE hash = 'g44%êfl6dshjk4..'
--
CREATE TABLE IF NOT EXISTS asset.asset_by_hash (
    hash text,
    asset_id timeuuid,
    provider text,
    subscription_id timeuuid,
    owner_id timeuuid,
    PRIMARY KEY (hash, asset_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
