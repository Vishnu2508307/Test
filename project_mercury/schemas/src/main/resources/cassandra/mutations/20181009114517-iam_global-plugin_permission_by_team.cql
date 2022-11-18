--
-- iam_global.plugin_permission_by_team
-- --------------------
-- Keeps plugin permissions for a team
--
-- Find permission level for a team over a plugin
-- > SELECT * FROM iam_global.plugin_permission_by_team WHERE team_id=? and plugin_id=?
--
CREATE TABLE IF NOT EXISTS iam_global.plugin_permission_by_team (
    team_id timeuuid,
    plugin_id timeuuid,
    permission_level text,
    PRIMARY KEY (team_id, plugin_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};