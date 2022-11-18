--
-- plugin.deleted
-- --------------------
-- This is a table to list all deleted plugins
--
-- Find all deleted plugins
-- > SELECT * FROM plugin.deleted
--
CREATE TABLE IF NOT EXISTS plugin.deleted (
    id timeuuid,
    plugin_id timeuuid,
    account_id timeuuid,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
