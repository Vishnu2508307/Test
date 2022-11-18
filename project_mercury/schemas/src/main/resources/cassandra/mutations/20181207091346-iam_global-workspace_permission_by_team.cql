--
-- iam_global.workspace_permission_by_team
-- --------------------
-- Tracks workspace permissions for teams
--
-- Find a permission level for a team over a workspace
-- > SELECT * FROM iam_global.workspace_permission_by_team WHERE team_id = ? AND workspace_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.workspace_permission_by_team (
    team_id timeuuid,
    workspace_id timeuuid,
    permission_level text,
    PRIMARY KEY (team_id, workspace_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
