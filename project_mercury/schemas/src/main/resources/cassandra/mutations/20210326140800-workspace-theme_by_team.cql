--
-- workspace.theme_by_team
-- --------------------
-- Tracks themes that have been share with a team
--
-- Find all themes a team has access to
-- > SELECT * FROM workspace.theme_by_team WHERE team_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.theme_by_team (
    team_id timeuuid,
    theme_id timeuuid,
    PRIMARY KEY (team_id, theme_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
