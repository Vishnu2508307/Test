--
-- workspace.group_by_plugin
-- --------------------
-- Keeps groups who have access to plugin
--
-- Find all groups by plugin
-- > SELECT * FROM workspace.group_by_plugin WHERE plugin_id=?
--
CREATE TABLE IF NOT EXISTS workspace.group_by_plugin (
    plugin_id timeuuid,
    group_id timeuuid,
    permission_level text,
    PRIMARY KEY (plugin_id, group_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
