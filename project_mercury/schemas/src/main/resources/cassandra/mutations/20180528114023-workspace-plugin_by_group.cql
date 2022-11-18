--
-- workspace.plugin_by_group
-- --------------------
-- Keeps all plugins visible for a specific group
--
-- Find all plugins visible for a group
-- > SELECT * FROM workspace.plugin_by_group WHERE group_id=?
--
CREATE TABLE IF NOT EXISTS workspace.plugin_by_group (
    group_id timeuuid,
    plugin_id timeuuid,
    PRIMARY KEY (group_id, plugin_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
