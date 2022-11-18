--
-- math.asset_summary
-- --------------------
-- Stores math asset information.
--
-- Fetch a math asset by id
-- > SELECT * FROM math.asset_summary WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS math.asset_summary (
    asset_id timeuuid,
    hash text,
    math_ml text,
    alt_text text,
    svg_text text,
    svg_shape text,
    PRIMARY KEY (asset_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
