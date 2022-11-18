--
-- iam_global.account_by_plugin_permission
-- --------------------
-- Tracks accounts ids by plugin and permission permissionLevel
--
-- Find all account for plugin with specific permission permissionLevel
-- > SELECT * FROM iam_global.account_by_plugin_permission WHERE plugin_id=? and permission_level=?
--
CREATE TABLE IF NOT EXISTS iam_global.account_by_plugin_permission (
    plugin_id timeuuid,
    permission_level text,
    account_id timeuuid,
    PRIMARY KEY ((plugin_id, permission_level), account_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
