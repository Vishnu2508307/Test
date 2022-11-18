--
-- plugin.searchable_by_plugin_version
-- --------------------
-- This table is designed to record the searchable field by plugin.
-- Fields name and type included to make it easier/faster logging/debugging csg messages
--
-- Find the searchable field by plugin id.
-- > SELECT * FROM plugin.searchable_by_plugin_version WHERE ...
--
CREATE TABLE IF NOT EXISTS plugin.searchable_field_by_plugin_version
(
    plugin_id timeuuid,
    version text,
    id timeuuid,
    name text,
    content_type text,
    summary set<text>,
    body set<text>,
    source set<text>,
    preview set<text>,
    tag set<text>,
    primary key (plugin_id, version, id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
