--
-- workspace.workspace_by_team
-- --------------------
-- Tracks workspaces that have been share with a team
--
-- Find all workspaces a team has access to
-- > SELECT * FROM workspace.workspace_by_team WHERE team_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.workspace_by_team (
    team_id timeuuid,
    workspace_id timeuuid,
    PRIMARY KEY (team_id, workspace_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
