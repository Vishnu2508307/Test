--
-- learner.math_asset_id_by_urn
-- -----------------------
-- Tracks the relationship between an assetUrn and assetIds
--
-- Find the latest math asset for a given assetUrn
-- > SELECT * FROM learner.math_asset_id_by_urn WHERE asset_urn = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.math_asset_id_by_urn(
    asset_urn text,
    asset_id  timeuuid,
    PRIMARY KEY (asset_urn, asset_id)
) WITH CLUSTERING ORDER BY (asset_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
