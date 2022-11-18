--
-- iam_global.theme_permission_by_team
-- --------------------
-- Tracks themes permissions for teams
--
-- Find a permission level for a team over a theme
-- > SELECT * FROM iam_global.theme_permission_by_team WHERE team_id = ? AND workspace_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.theme_permission_by_team (
    team_id timeuuid,
    theme_id timeuuid,
    permission_level text,
    PRIMARY KEY (team_id, theme_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
