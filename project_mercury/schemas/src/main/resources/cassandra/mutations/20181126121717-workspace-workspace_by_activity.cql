--
-- workspace.workspace_by_activity
-- --------------------
-- Tracks workspaces by activites. Every activity can belong only to one workspace.
--
-- Find a workspace for the given activity
-- > SELECT * FROM workspace.workspace_by_activity WHERE where activity_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.workspace_by_activity (
    activity_id timeuuid,
    workspace_id timeuuid,
    PRIMARY KEY (activity_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
