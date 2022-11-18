--
-- courseware.interactive_by_plugin
-- --------------------
-- Track which plugins are used on an Interactive
--
-- Find all the Interactives which use a particular plugin
-- > SELECT * FROM courseware.interactive_by_plugin WHERE plugin_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.interactive_by_plugin (
    plugin_id timeuuid,
    interactive_id timeuuid,
    plugin_version_expr text,
    PRIMARY KEY(plugin_id, interactive_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
