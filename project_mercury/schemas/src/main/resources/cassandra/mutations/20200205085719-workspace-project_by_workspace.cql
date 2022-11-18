--
-- workspace.project_by_workspace
-- --------------------------------
-- Stores all the projects by workspace ordered by their time of creation
--
-- Find all the projects for a workspace
-- > SELECT * FROM workspace.project_by_workspace WHERE workspace_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.project_by_workspace (
    workspace_id timeuuid,
    project_id timeuuid,
    name text,
    created_at text,
    PRIMARY KEY (workspace_id, project_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
