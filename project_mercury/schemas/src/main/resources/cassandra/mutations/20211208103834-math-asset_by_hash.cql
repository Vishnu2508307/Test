--
-- math.asset_by_hash
-- --------------------
-- Find an asset id by the content hash.
--
-- Find an asset id by the content hash
-- > SELECT * FROM math.asset_by_hash WHERE hash = ?
--
CREATE TABLE IF NOT EXISTS math.asset_by_hash (
    hash text,
    asset_id timeuuid,
    owner_id timeuuid,
    PRIMARY KEY (hash)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
