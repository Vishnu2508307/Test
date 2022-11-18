--
-- math.asset_id_by_urn
-- --------------------
-- Find an asset id by asset urn.
--
-- Find the asset id by asset urn
-- > SELECT * FROM math.asset_id_by_urn WHERE asset_urn = ?
--
CREATE TABLE IF NOT EXISTS math.asset_id_by_urn (
    asset_urn text,
    asset_id timeuuid,
    PRIMARY KEY (asset_urn)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
