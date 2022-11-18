--
-- courseware.component_by_plugin
-- --------------------
-- Track plugin usage by component
--
-- Find all the components using a particular plugin
-- > SELECT * FROM courseware.component_by_plugin WHERE plugin_id = ?;
--
CREATE TABLE IF NOT EXISTS courseware.component_by_plugin (
    plugin_id timeuuid,
    component_id timeuuid,
    plugin_version_exp text,
    PRIMARY KEY(plugin_id, component_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
