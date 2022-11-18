--
-- plugin.filter_by_plugin_version
-- --------------------
-- This table is designed to record the plugin filters.
--
-- Find all plugin filters by plugin id and version
-- > SELECT * FROM plugin.filter_by_plugin_version WHERE plugin_id= ? and version= ?
--
CREATE TABLE IF NOT EXISTS plugin.filter_by_plugin_version (
    plugin_id timeuuid,
    version text,
    filter_type text,
    filter_values set<text>,
    primary key (plugin_id, version, filter_type)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
