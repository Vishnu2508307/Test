--
-- workspace.account_by_plugin
-- --------------------
-- Keeps accounts who have access to plugin
--
-- Find all accounts for plugin
-- > SELECT * FROM workspace.account_by_plugin WHERE plugin_id=?
--
CREATE TABLE IF NOT EXISTS workspace.account_by_plugin (
    plugin_id timeuuid,
    account_id timeuuid,
    permission_level text,
    PRIMARY KEY (plugin_id, account_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
