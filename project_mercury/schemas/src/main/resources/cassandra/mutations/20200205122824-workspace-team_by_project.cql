--
-- workspace.team_by_project
-- -------------------------
-- Store all the teams that have access to a project
--
-- Find all the teams that have access to a project
-- > SELECT * FROM workspace.team_by_project WHERE project_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.team_by_project (
    project_id timeuuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY (project_id, team_id)
) WITH CLUSTERING ORDER BY (team_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
