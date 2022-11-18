--
-- plugin.view_by_plugin_version
-- --------------------
-- This table stores view entry_points data for each plugin version.
--
-- Find a specific entry point given version and context
-- > SELECT * FROM plugin.view_by_plugin_version WHERE plugin_id = ? AND version = ? AND context = ?
--
CREATE TABLE IF NOT EXISTS plugin.view_by_plugin_version (
    plugin_id timeuuid,
    version text,
    context text,
    entry_point_path text,
    entry_point_data text,
    content_type text,
    public_dir text,
    editor_mode text,
PRIMARY KEY (plugin_id, version, context)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
