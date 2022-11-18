--
-- iam_global.plugin_permission_by_account
-- --------------------
-- Keeps plugin permissions by account
--
-- Find permission permissionLevel for an account and a plugin
-- > SELECT * FROM iam_global.plugin_permission_by_account WHERE account_id=? and plugin_id=?
--
CREATE TABLE IF NOT EXISTS iam_global.plugin_permission_by_account (
    account_id timeuuid,
    plugin_id timeuuid,
    permission_level text,
    PRIMARY KEY (account_id, plugin_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
