--
-- iam_global.group_by_plugin_permission
-- --------------------
-- Tracks groups ids by plugin and permission permissionLevel
--
-- Find all groups for plugin with specific permission permissionLevel
-- > SELECT * FROM iam_global.group_by_plugin_permission WHERE plugin_id=? and permission_level=?
--
CREATE TABLE IF NOT EXISTS iam_global.group_by_plugin_permission (
    plugin_id timeuuid,
    permission_level text,
    group_id timeuuid,
    PRIMARY KEY ((plugin_id, permission_level), group_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
