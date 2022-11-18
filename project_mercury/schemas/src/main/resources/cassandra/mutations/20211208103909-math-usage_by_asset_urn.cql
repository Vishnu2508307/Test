--
-- math.usage_by_asset_urn
-- --------------------
-- Find all usages of a math asset by urn.
--
-- Find all usages of a math asset by urn
-- > SELECT * FROM math.usage_by_asset_urn WHERE asset_urn = ?
--
CREATE TABLE IF NOT EXISTS math.usage_by_asset_urn (
    asset_urn text,
    asset_id timeuuid,
    element_ids text,
    PRIMARY KEY (asset_urn)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
