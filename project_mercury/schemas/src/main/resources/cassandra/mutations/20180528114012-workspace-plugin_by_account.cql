--
-- workspace.plugin_by_account
-- --------------------
-- Keeps plugins visible for a specific account
--
-- Find all plugins visible for an account
-- > SELECT * FROM workspace.plugin_by_account WHERE account_id=?
--
CREATE TABLE IF NOT EXISTS workspace.plugin_by_account (
    account_id timeuuid,
    plugin_id timeuuid,
    PRIMARY KEY (account_id, plugin_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
