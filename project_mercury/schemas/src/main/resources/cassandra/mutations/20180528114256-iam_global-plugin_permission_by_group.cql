--
-- iam_global.plugin_permission_by_group
-- --------------------
-- Keeps plugin permissions by group
--
-- Find permission permissionLevel for a group and a plugin
-- > SELECT * FROM iam_global.plugin_permission_by_group WHERE group_id=? and plugin_id=?
--
CREATE TABLE IF NOT EXISTS iam_global.plugin_permission_by_group (
    group_id timeuuid,
    plugin_id timeuuid,
    permission_level text,
    PRIMARY KEY (group_id, plugin_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
