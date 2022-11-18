--
-- iam.project_permission_by_team
-- ------------------------------
-- Store all the permission level for a team over projects
--
-- Find the permission level of a team over a project
-- > SELECT * FROM iam_global.project_permission_by_team WHERE team_id = ? AND project_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.project_permission_by_team (
    team_id timeuuid,
    project_id timeuuid,
    permission_level text,
    PRIMARY KEY (team_id, project_id)
) WITH CLUSTERING ORDER BY (project_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
