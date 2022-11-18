--
-- plugin.manifest_by_plugin_version
-- --------------------
-- Holds plugin publishing data
--
-- find all manifest by plugin id
-- > SELECT * FROM plugin.manifest_by_plugin_version WHERE plugin_id = ?
--
-- find manifest by version
-- > SELECT * FROM plugin.manifest_by_plugin_version WHERE plugin_id = ? AND version = ?
--
CREATE TABLE IF NOT EXISTS plugin.manifest_by_plugin_version (
    plugin_id timeuuid,
    version text,
    description text,
    screenshots set<text>,
    thumbnail text,
    publisher_id timeuuid,
    configuration_schema text,
    zip_hash text,
PRIMARY KEY ((plugin_id), version)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
