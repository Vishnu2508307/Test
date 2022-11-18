--
-- workspace.workspace_by_project
-- ------------------------------
-- Allows to find a workspace a project belongs to
--
-- Find the workspace by project
-- > SELECT * FROM workspace.workspace_by_project WHERE project_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.workspace_by_project (
    project_id timeuuid,
    workspace_id timeuuid,
    PRIMARY KEY (project_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
