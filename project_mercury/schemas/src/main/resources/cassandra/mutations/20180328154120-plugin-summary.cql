--
-- plugin.summary
-- --------------------
-- Holds the unique indentifier for a plugin along with other basic info. The main purpose for this table is to
-- generate the unique indentifier.
--
-- find a plugin summary by id
-- > SELECT * FROM plugin.summary WHERE id = ?
--
CREATE TABLE IF NOT EXISTS plugin.summary (
    id timeuuid,
    subscription_id timeuuid,
    name text,
    creator_id timeuuid,
    type text,
    latest_version text,
PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
