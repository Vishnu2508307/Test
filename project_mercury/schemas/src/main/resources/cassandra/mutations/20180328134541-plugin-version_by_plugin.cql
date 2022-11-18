--
-- plugin.version_by_plugin
-- --------------------
-- Holds all the published versions for a plugin.
--
-- Find all versions by plugin_id
-- > SELECT * FROM plugin.version_by_plugin WHERE plugin_id = ?
--
-- Find all the releases for a major version
-- > SELECT * FROM plugin.version_by_plugin WHERE plugin_id = ? AND major = ?
--
-- Find all the patches for a minor version
-- > SELECT * FROM plugin.version_by_plugin WHERE plugin_id = ? AND major = ? AND minor = ?
--
CREATE TABLE IF NOT EXISTS plugin.version_by_plugin (
    plugin_id timeuuid,
    major int,
    minor int,
    patch int,
    release_date bigint,
    pre_release text,
    build text,
PRIMARY KEY((plugin_id), major, minor, patch, release_date)
) WITH CLUSTERING ORDER BY (major DESC, minor DESC, patch DESC, release_date DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};