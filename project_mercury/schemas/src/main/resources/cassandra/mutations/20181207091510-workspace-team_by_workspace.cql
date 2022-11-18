--
-- workspace.team_by_workspace
-- --------------------
-- Tracks teams that have permissions over a workspace
--
-- Find all teams that have permission over a workspace
-- > SELECT * FROM workspace.team_by_workspace WHERE workspace_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.team_by_workspace (
    workspace_id timeuuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY (workspace_id, team_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
