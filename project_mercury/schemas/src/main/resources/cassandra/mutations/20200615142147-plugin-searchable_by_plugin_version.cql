--
-- plugin.searchable_by_plugin_version
-- --------------------
-- This table is designed to record the searchable field by plugin.
--
-- Find the searchable field by plugin id.
-- > SELECT * FROM plugin.searchable_by_plugin_version WHERE ...
--
CREATE TABLE IF NOT EXISTS plugin.searchable_by_plugin_version
(
    plugin_id timeuuid,
    version text,
    type text,
    name text,
    searchable set<text>,
    primary key (plugin_id, version)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
