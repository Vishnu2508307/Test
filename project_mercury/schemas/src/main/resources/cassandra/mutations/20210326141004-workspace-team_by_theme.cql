--
-- workspace.team_by_theme
-- --------------------
-- Tracks teams that have permissions over a theme
--
-- Find all teams that have permission over a theme
-- > SELECT * FROM workspace.team_by_theme WHERE theme_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.team_by_theme (
    theme_id timeuuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY (theme_id, team_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
