--
-- asset.asset_by_subscription
-- --------------------
-- Stores assets retrievable by visibility and subscription
--
-- Fetch assets by visibility and subscription id
-- > SELECT * FROM asset.asset_by_subscription WHERE visibility = ? and subscription_id = ?
--
CREATE TABLE IF NOT EXISTS asset.asset_by_subscription (
    visibility text,
    subscription_id timeuuid,
    media_type text,
    owner_id timeuuid,
    asset_id timeuuid,
    PRIMARY KEY (visibility, subscription_id, media_type, owner_id, asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
