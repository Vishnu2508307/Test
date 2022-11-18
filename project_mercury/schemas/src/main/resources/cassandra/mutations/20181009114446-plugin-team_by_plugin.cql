--
-- plugin.team_by_plugin
-- --------------------
-- Tracks teams that have permissions over a plugin
--
-- Find all teams that have permissions for a plugin
-- > SELECT * FROM plugin.team_by_plugin WHERE plugin_id = ?
--
CREATE TABLE IF NOT EXISTS plugin.team_by_plugin (
    plugin_id timeuuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY (plugin_id, team_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};