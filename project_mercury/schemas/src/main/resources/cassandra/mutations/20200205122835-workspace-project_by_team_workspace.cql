--
-- workspace.project_by_team
-- -------------------------
-- Store all the project a team has access to
--
-- Find all the projects that a team has access to within a workspace
-- > SELECT * FROM workspace.project_by_team WHERE team_id = ? AND workspace_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.project_by_team_workspace (
    team_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    PRIMARY KEY ((team_id, workspace_id), project_id)
) WITH CLUSTERING ORDER BY (project_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
