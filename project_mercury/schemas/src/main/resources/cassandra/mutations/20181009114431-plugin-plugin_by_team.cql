--
-- plugin.plugin_by_team
-- --------------------
-- Tracks plugins which are shared with a team (visible for a team)
--
-- Find all plugins visible for a team
-- > SELECT * FROM plugin.plugin_by_team WHERE team_id=?
--
CREATE TABLE IF NOT EXISTS plugin.plugin_by_team (
    team_id timeuuid,
    plugin_id timeuuid,
    PRIMARY KEY (team_id, plugin_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};