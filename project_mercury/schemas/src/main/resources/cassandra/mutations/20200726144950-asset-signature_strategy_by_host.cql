--
-- asset.signature_strategy_by_host
-- --------------------------------
-- Store the configured signature strategy for a particular domain path
--
-- Find the configured signature strategy for a domain path
-- > SELECT * FROM asset.signature_strategy_by_host WHERE host = ? AND path = ?
--
CREATE TABLE IF NOT EXISTS asset.signature_strategy_by_host (
    host text,
    path text,
    signature_type text,
    id timeuuid,
    config text,
    PRIMARY KEY (host, path)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
